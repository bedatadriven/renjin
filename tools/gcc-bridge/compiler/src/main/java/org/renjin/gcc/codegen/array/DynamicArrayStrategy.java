package org.renjin.gcc.codegen.array;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;


public class DynamicArrayStrategy implements ArrayTypeStrategy<DynamicArrayExpr> {

  private GimpleArrayType arrayType;
  private ValueFunction valueFunction;
  private boolean parametersWrapped = true;
  private DynamicArrayValueFunction arrayValueFunction;

  public DynamicArrayStrategy(GimpleArrayType arrayType, ValueFunction valueFunction) {
    this.arrayType = arrayType;
    this.valueFunction = valueFunction;
    this.arrayValueFunction = new DynamicArrayValueFunction(valueFunction);
  }

  public DynamicArrayStrategy setParametersWrapped(boolean parametersWrapped) {
    this.parametersWrapped = parametersWrapped;
    return this;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public DynamicArrayExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    throw new UnsupportedOperationException("TODO: " + decl);
  }

  @Override
  public DynamicArrayExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new FatPtrStrategy(arrayValueFunction).setParametersWrapped(parametersWrapped);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, arrayValueFunction);
  }

  @Override
  public DynamicArrayExpr cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr elementAt(GExpr array, GExpr index) {
    DynamicArrayExpr arrayFatPtr = (DynamicArrayExpr) array;
    PrimitiveValue indexValue = (PrimitiveValue) index;

    // New offset  = ptr.offset + (index * value.length)
    // for arrays of doubles, for example, this will be the same as ptr.offset + index
    // but for arrays of complex numbers, this will be ptr.offset + (index * 2)
    JExpr newOffset = Expressions.sum(
        arrayFatPtr.getOffset(),
        Expressions.product(
            Expressions.difference(indexValue.unwrap(), arrayType.getLbound()),
            valueFunction.getElementLength()));

    return valueFunction.dereference(arrayFatPtr.getArray(), newOffset);
  }

}
