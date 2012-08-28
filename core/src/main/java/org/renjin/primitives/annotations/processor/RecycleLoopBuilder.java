package org.renjin.primitives.annotations.processor;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import org.renjin.primitives.annotations.PreserveAttributeStyle;
import org.renjin.primitives.annotations.processor.scalars.ScalarType;
import org.renjin.primitives.annotations.processor.scalars.ScalarTypes;
import org.renjin.sexp.Null;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;

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
      return vector.invoke("isElementNA").arg(currentElementIndex);
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

  private List<RecycledArgument> recycledArguments = Lists.newArrayList();
  private Map<JvmMethod.Argument, JExpression> argumentMap = Maps.newHashMap();

  private JClass vectorType;
  private JVar cycleCount;
  private JVar cycleIndex;

  private ScalarType resultType;

  private JVar builder;

  public RecycleLoopBuilder(JCodeModel codeModel, JBlock parent, PrimitiveModel primitive, JvmMethod overload,
                            Map<JvmMethod.Argument, JExpression> argumentMap) {

    this.codeModel = codeModel;
    this.parent = parent;
    this.primitive = primitive;
    this.overload = overload;
    this.vectorType = codeModel.ref(Vector.class);
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


//  // compute longest vector
//  Vector longest = Null.INSTANCE;
//  int cycles = 0;
//  if(arg0_length == 0) {
//    return org.renjin.sexp.DoubleVector.EMPTY;
//  }
//  if(arg0_length > cycles) {
//    cycles = arg0_length;
//  }
//  if(arg1_length == 0) {
//    return org.renjin.sexp.DoubleVector.EMPTY;
//  }
//  if(arg1_length > cycles) {
//    cycles = arg1_length;
//  }

    cycleCount = parent.decl(codeModel._ref(int.class), "cycles", lit(0));

    for(RecycledArgument arg : recycledArguments) {
      parent._if(arg.length.eq(lit(0)))._then()._return(emptyResult());
      parent._if(arg.length.gt(cycleCount))._then().assign(cycleCount, arg.length);
    }

    if(overload.isDeferrable()) {
      DeferredVectorBuilder deferred = new DeferredVectorBuilder(codeModel, primitive, overload);
      deferred.buildClass();
      deferred.maybeReturn(parent, cycleCount, deferredArgumentList());
    }
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

  private JExpression nullInstance() {
    return codeModel.ref(Null.class).staticRef("INSTANCE");
  }

  private JExpression emptyResult() {
    return codeModel.ref(resultType.getVectorType()).staticRef("EMPTY");
  }


  private void initializeBuilder() {


//
//  org.renjin.sexp.DoubleArrayVector.Builder result = new org.renjin.sexp.DoubleArrayVector.Builder(cycles);
//  int resultIndex = 0;

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
    if(!overload.acceptsNA()) {
      // by default, primitive implementations do not have to deal
      // with missing values, so we need to handle them here
      JConditional ifNA = loopBody._if(isCurrentElementNA());
      ifNA._then().add(assignNA());
      ifNA._else().add(assignCycleResult());
    } else {
      // if the implementation is marked with @AllowNA, then
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


  private JExpression isCurrentElementNA() {

    assert !recycledArguments.isEmpty();

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
      for(RecycledArgument arg : Lists.reverse(recycledArguments)) {
        parent._if(arg.length.eq(cycleCount))._then().add(copyAttributesFrom(arg));
      }
    }
  }

  private JStatement copyAttributesFrom(RecycledArgument arg) {
    switch(overload.getPreserveAttributesStyle()) {
    case ALL:
      return builder.invoke("copyAttributesFrom").arg(arg.vector);
    case SPECIAL:
      // Symbols.DIM, Symbols.DIMNAMES, Symbols.NAMES)
      return builder.invoke("copySomeAttributesFrom").arg(arg.vector)
              .arg(symbol("DIM"))
              .arg(symbol("DIMNAMES"))
              .arg(symbol("NAMES"));
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
