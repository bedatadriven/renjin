/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.codegen;


import com.sun.codemodel.*;
import org.renjin.invoke.annotations.AllowNull;
import org.renjin.invoke.annotations.PreserveAttributeStyle;
import org.renjin.invoke.codegen.scalars.*;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr.*;

/**
 * Builds the Java-language code which applies a
 * static method to one or more vectors
 */
public class RecycleLoopBuilder {

  private class RecycledArgument {
    private JvmMethod.Argument formal;
    private ScalarType scalarType;
    private JExpression sexp;
    private JVar vector;
    private JVar length;
    private JVar currentElementIndex;
    private JVar currentElement;



    public RecycledArgument(JvmMethod.Argument argument, JExpression parameter) {
      this.formal = argument;
      this.scalarType = ScalarTypes.get(formal.getClazz());
      this.sexp = parameter;
      this.vector = parent.decl(codeModel.ref(Vector.class), "vector" + formal.getIndex(),
          cast(codeModel.ref(Vector.class), sexp));
      this.length = parent.decl(codeModel._ref(int.class), "length" + formal.getIndex(),
          vector.invoke("length"));
      this.currentElementIndex = parent.decl(codeModel._ref(int.class), "currentElementIndex" + formal.getIndex(), lit(0));
      this.currentElement = parent.decl(codeModel._ref(scalarType.getElementStorageType()), "s" + formal.getIndex());
    }

    public JType getVectorType() {
      return codeModel.ref(scalarType.getVectorType());
    }

    public JExpression isCurrentElementNA() {
      return scalarType.testNaExpr(codeModel, currentElement);
    }

    public JExpression getCurrentElement() {
      return vector.invoke(scalarType.getAccessorMethod()).arg(currentElementIndex);
    }

    public JExpression getCurrentElementInScalarType() {
      return scalarType.fromElementStorageType(currentElement);
    }
  }

  private JCodeModel codeModel;
  private JBlock parent;

  private PrimitiveModel primitive;
  /**
   * The JVM method to invoke on the individual elements
   */
  private JvmMethod overload;

  private final JExpression contextVar;
  private List<RecycledArgument> recycledArguments = Lists.newArrayList();
  private Map<JvmMethod.Argument, JExpression> argumentMap = Maps.newHashMap();

  private JVar cycleCount;
  private JVar cycleIndex;

  private ScalarType resultType;

  private boolean useArray;
  private JVar builder;
  
  public RecycleLoopBuilder(JCodeModel codeModel,
                            JBlock parent,
                            JExpression contextVar,
                            PrimitiveModel primitive,
                            JvmMethod overload,
                            Map<JvmMethod.Argument, JExpression> argumentMap) {

    this.codeModel = codeModel;
    this.parent = parent;
    this.contextVar = contextVar;
    this.primitive = primitive;
    this.overload = overload;
    this.resultType = ScalarTypes.get(overload.getReturnType());

    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isRecycle()) {
        RecycledArgument recycledArgument = new RecycledArgument(argument, argumentMap.get(argument));
        recycledArguments.add(recycledArgument);
        this.argumentMap.put(argument, recycledArgument.getCurrentElementInScalarType());
      } else {
        this.argumentMap.put(argument, argumentMap.get(argument));
      }
    }

    useArray = recycledArguments.size() <= 2 && 
        (resultType instanceof DoubleType ||
            resultType instanceof IntegerType ||
            resultType instanceof BooleanType);

  }

  public void build() {
    computeResultLength();
    initializeBuilder();
    loop();
    if(!useArray) {
      copyAttributesUsingBuilder();
    }
    parent._return(buildResult());
  }

  private void computeResultLength() {
    cycleCount = parent.decl(codeModel._ref(int.class), "cycles");

    if(recycledArguments.size() == 1) {

      // NULL is not legal for unary functions, though is accepted
      // for binary functions, unless the parameter is annotated
      // with @AllowNull
      if(recycledArguments.get(0).formal.getAnnotation(AllowNull.class) == null) {
        parent._if(recycledArguments.get(0).sexp.eq(codeModel.ref(Null.class).staticRef("INSTANCE")))
            ._then()
            ._throw(_new(codeModel.ref(ArgumentException.class)).arg(lit("invalid NULL argument to unary function")));
      }

      parent.assign(cycleCount, recycledArguments.get(0).length);

    } else {
      JConditional zeroLength = parent._if(anyZeroLength());
      zeroLength._then().assign(cycleCount, lit(0));
      findLongestArgument(zeroLength._else());
    }

    if(overload.isDeferrable()) {
      DeferredVectorBuilder deferred = new DeferredVectorBuilder(codeModel, contextVar, primitive, overload);
      deferred.buildClass();
      deferred.maybeReturn(parent, cycleCount, deferredArgumentList(), copyAttributesFast());
    }
  }

  private void findLongestArgument(JBlock parent) {
    parent.assign(cycleCount, lit(0));
    for(RecycledArgument arg : recycledArguments) {
      parent._if(arg.length.gt(cycleCount))._then().assign(cycleCount, arg.length);
    }
  }

  private JExpression anyZeroLength() {
    Iterator<RecycledArgument> iterator = recycledArguments.iterator();
    JExpression expr = iterator.next().length.eq(lit(0));
    while(iterator.hasNext()) {
      expr = expr.cor(iterator.next().length.eq(lit(0)));
    }
    return expr;
  }

  private List<JExpression> deferredArgumentList() {

    // make sure all args are recycled
    for(JvmMethod.Argument arg : overload.getAllArguments()) {
      if(!arg.isRecycle()) {
        throw new UnsupportedOperationException("All arguments of a deferred vector must be @Recycle");
      }
    }

    List<JExpression> list = Lists.newArrayList();
    for(RecycledArgument arg : recycledArguments) {
      list.add(arg.vector);
    }
    return list;
  }

  private JExpression emptyResult() {
    return codeModel.ref(resultType.getVectorType()).staticRef("EMPTY");
  }

  private void initializeBuilder() {


    if (useArray) {
      // Generate the code to initialize the result array
      JType arrayElementType = codeModel._ref(resultType.getBuilderArrayElementClass());
      JClass arrayClass = arrayElementType.array();
      builder = parent.decl(arrayElementType.array(), "array", JExpr.newArray(arrayElementType, cycleCount));
    } else {
      // Generate the code to initialize the builder:
      // org.renjin.sexp.DoubleArrayVector.Builder result = new org.renjin.sexp.DoubleArrayVector.Builder(cycles);
      JClass builderClass = codeModel.ref(resultType.getBuilderClass());
      builder = parent.decl(builderClass, "builder", JExpr._new(builderClass).arg(cycleCount));
    }
  }

  private void loop() {
    JForLoop loop = parent._for();
    cycleIndex = loop.init(codeModel.INT, "i", lit(0));
    loop.test(cycleIndex.ne(cycleCount));
    loop.update(cycleIndex.incr());

    calculateResult(loop.body());
    incrementCounters(loop.body());

  }

  private void calculateResult(JBlock loopBody) {
    for (RecycledArgument recycledArgument : recycledArguments) {
      loopBody.assign(recycledArgument.currentElement, recycledArgument.getCurrentElement());
    }
    if(!overload.isPassNA()) {
      // by default, primitive implementations do not have to deal
      // with missing values, so we need to handle them here
      JConditional ifNA = loopBody._if(isCurrentElementMissing());
      assignNA(ifNA._then());
      assignCycleResult(ifNA._else());
    } else {
      // if the implementation is marked with @DataParallel(passNA=true), then
      // we pass in the values as-is
      assignCycleResult(loopBody);
    }
  }

  private void incrementCounters(JBlock loopBody) {
    for(RecycledArgument arg : recycledArguments) {
      loopBody.assignPlus(arg.currentElementIndex, lit(1));
      if(recycledArguments.size() > 1) {
        loopBody._if(arg.currentElementIndex.eq(arg.length))._then().assign(arg.currentElementIndex, lit(0));
      }
    }
  }

  private JExpression isCurrentElementMissing() {
    if(recycledArguments.isEmpty()) {
      throw new IllegalStateException(overload.getName() + " is marked as @DataParallel, but has no parallel arguments");
    }

    JExpression condition = null;
    for(RecycledArgument arg : recycledArguments) {
      if(condition == null) {
        condition = arg.isCurrentElementNA();
      } else {
        condition = condition.cor(arg.isCurrentElementNA());
      }
    }
    return condition;
  }

  private void assignCycleResult(JBlock block) {
    // Generate the code to assign the result of the operation. 
    // For example:
    //  result.set(i, org.renjin.primitives.Ops.plus(arg0_element, arg1_element));

    if(useArray) {
      JAssignmentTarget componentRef = builder.component(cycleIndex);
      block.assign(componentRef, computeCycleResult());
    } else {
      block.add(builder.invoke("set").arg(cycleIndex).arg(computeCycleResult()));
    }
  }

  private void assignNA(JBlock body) {
    if(useArray) {
      body.assign(builder.component(cycleIndex), resultType.naLiteral(codeModel));
    } else {
      body.add(builder.invoke("setNA").arg(cycleIndex));
    }
  }

  private JExpression computeCycleResult() {
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
        .staticInvoke(overload.getName());

    for(JvmMethod.Argument arg : overload.getAllArguments()) {
      if(!argumentMap.containsKey(arg)) {
        throw new AssertionError(arg.getName() + " not present in argumentMap");
      }
      invocation.arg(argumentMap.get(arg));
    }

    if(useArray) {
      return resultType.toBuildArrayElementType(invocation);
    } else {
      return invocation;
    }
  }

  private void copyAttributesUsingBuilder() {

    // Use the builder's attribute methods
    if (overload.getPreserveAttributesStyle() != PreserveAttributeStyle.NONE) {
      // copy attributes from all arguments that match
      // the final length, giving precedence to earlier arguments
      for (RecycledArgument arg : recycledArguments) {
        parent._if(arg.length.eq(cycleCount))._then().add(copyAttributesFrom(arg));
      }
    }
  }


  private JExpression copyAttributesFast() {
    if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.NONE) {
      return codeModel.ref(AttributeMap.class).staticRef("EMPTY");
    } else {
      if(recycledArguments.size() == 1) {
        return copyAttributes(recycledArguments.get(0).vector);
      } else if(recycledArguments.size() == 2) {
        return copyAttributes(recycledArguments.get(0).vector, recycledArguments.get(1).vector);
      } else {
        throw new UnsupportedOperationException("arity = " + recycledArguments.size());
      }
    }
  }

  private JStatement copyAttributesFrom(RecycledArgument arg) {
    switch(overload.getPreserveAttributesStyle()) {
      case ALL:
        return builder.invoke("combineAttributesFrom").arg(arg.vector);
      case STRUCTURAL:
        return builder.invoke("combineStructuralAttributesFrom").arg(arg.vector);
    }
    throw new IllegalArgumentException("preserve attribute style: " + overload.getPreserveAttributesStyle());
  }


  private JExpression copyAttributes(JExpression arg0, JExpression arg1)  {
    String combineMethod;
    switch(overload.getPreserveAttributesStyle()) {
      case ALL:
        combineMethod = "combineAttributes";
        break;
      case STRUCTURAL:
        combineMethod = "combineStructuralAttributes";
        break;
      default:
        throw new UnsupportedOperationException();
    }

    return codeModel.ref(AttributeMap.class).staticInvoke(combineMethod)
        .arg(arg0)
        .arg(arg1);
  }

  private JExpression copyAttributes(JExpression arg) {
    if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.ALL) {
      return arg.invoke("getAttributes");
    } else if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.STRUCTURAL) {
      return arg.invoke("getAttributes").invoke("copyStructural");
    } else {
      throw new UnsupportedOperationException();
    }
  }


  private JExpression symbol(String name) {
    return codeModel.ref(Symbols.class).staticRef(name);
  }


  private JExpression buildResult() {
    if(useArray) {
      return JExpr._new(codeModel.ref(resultType.getArrayVectorClass()))
          .arg(builder)
          .arg(copyAttributesFast());
    } else {
      return builder.invoke("build");
    }
  }
}
