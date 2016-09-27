/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.apache.commons.math.complex.Complex;
import org.renjin.invoke.annotations.AllowNull;
import org.renjin.invoke.annotations.PreserveAttributeStyle;
import org.renjin.invoke.codegen.scalars.ScalarType;
import org.renjin.invoke.codegen.scalars.ScalarTypes;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
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

    public RecycledArgument(JvmMethod.Argument argument, JExpression parameter) {
      this.formal = argument;
      this.scalarType = ScalarTypes.get(formal.getClazz());
      this.sexp = parameter;
      this.vector = parent.decl(codeModel.ref(Vector.class), "vector" + formal.getIndex(),
              cast(codeModel.ref(Vector.class), sexp));
      this.length = parent.decl(codeModel._ref(int.class), "length" + formal.getIndex(),
              vector.invoke("length"));
      this.currentElementIndex = parent.decl(codeModel._ref(int.class), "currentElementIndex" + formal.getIndex(), lit(0));
    }

    public JType getVectorType() {
      return codeModel.ref(scalarType.getVectorType());
    }

    public JExpression isCurrentElementNA() {
      // If we're returning a double/complex vector, we can handle NaNs,
      // otherwise treat them as NA
      if(overload.getReturnType().equals(double.class) || 
          overload.getReturnType().equals(Complex.class)) {
        return vector.invoke("isElementNA").arg(currentElementIndex);
      } else {
        return vector.invoke("isElementNaN").arg(currentElementIndex);
      }
    }

    public JExpression getCurrentElement() {
      return vector.invoke(scalarType.getAccessorMethod()).arg(currentElementIndex);
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
        this.argumentMap.put(argument, recycledArgument.getCurrentElement());
      } else {
        this.argumentMap.put(argument, argumentMap.get(argument));
      }
    }
  }

  public void build() {

    computeResultLength();
    initializeBuilder();
    loop();
    copyAttributes();
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
      deferred.maybeReturn(parent, cycleCount, deferredArgumentList());
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

    // Generate the code to initialize the builder:
    // org.renjin.sexp.DoubleArrayVector.Builder result = new org.renjin.sexp.DoubleArrayVector.Builder(cycles);
    // int resultIndex = 0;

    JClass builderClass = codeModel.ref(resultType.getBuilderClass());
    builder = parent.decl(builderClass, "builder", JExpr._new(builderClass).arg(cycleCount));
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
    if(!overload.isPassNA()) {
      // by default, primitive implementations do not have to deal
      // with missing values, so we need to handle them here
      JConditional ifNA = loopBody._if(isCurrentElementMissing());
      ifNA._then().add(assignNA());
      ifNA._else().add(assignCycleResult());
    } else {
      // if the implementation is marked with @DataParallel(passNA=true), then
      // we pass in the values as-is
      loopBody.add(assignCycleResult());
    }
  }

  private void incrementCounters(JBlock loopBody) {
    for(RecycledArgument arg : recycledArguments) {
      loopBody.assignPlus(arg.currentElementIndex, lit(1));
      loopBody._if(arg.currentElementIndex.eq(arg.length))._then().assign(arg.currentElementIndex, lit(0));
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

  private JStatement assignCycleResult() {
    // Generate the code to assign the result of the operation. 
    // For example:
    //  result.set(i, org.renjin.primitives.Ops.plus(arg0_element, arg1_element));

    return builder.invoke("set").arg(cycleIndex).arg(computeCycleResult());
  }

  private JStatement assignNA() {
    return builder.invoke("setNA").arg(cycleIndex);
  }

  private JInvocation computeCycleResult() {
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
        .staticInvoke(overload.getName());

    for(JvmMethod.Argument arg : overload.getAllArguments()) {
      if(!argumentMap.containsKey(arg)) {
        throw new AssertionError(arg.getName() + " not present in argumentMap");
      }
      invocation.arg(argumentMap.get(arg));
    }
    return invocation;
  }

  private void copyAttributes() {
    if(overload.getPreserveAttributesStyle() != PreserveAttributeStyle.NONE ) {
      // copy attributes from all arguments that match
      // the final length, giving precedence to earlier arguments
      for(RecycledArgument arg : recycledArguments) {
        parent._if(arg.length.eq(cycleCount))._then().add(copyAttributesFrom(arg));
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

  private JExpression symbol(String name) {
    return codeModel.ref(Symbols.class).staticRef(name);
  }

  private JExpression buildResult() {
    return builder.invoke("build");
  }
}
