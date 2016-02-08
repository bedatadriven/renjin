package org.renjin.gcc.codegen.array;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import java.util.List;

/**
 * Type strategy for arrays of values
 */
public class ArrayTypeStrategy implements TypeStrategy<FatPtrExpr> {

  private int lowerBound;
  private final ValueFunction valueFunction;
  private boolean parameterWrapped = true;

  public ArrayTypeStrategy(GimpleArrayType arrayType, ValueFunction valueFunction) {
    this.lowerBound = arrayType.getLbound();
    this.valueFunction = valueFunction;
  }

  public boolean isParameterWrapped() {
    return parameterWrapped;
  }

  public ArrayTypeStrategy setParameterWrapped(boolean parameterWrapped) {
    this.parameterWrapped = parameterWrapped;
    return this;
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new ArrayValueFunction(valueFunction))
        .setParametersWrapped(parameterWrapped);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return null;
  }

  @Override
  public FatPtrExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor constructor) {
    List<SimpleExpr> values = Lists.newArrayList();
    for (GimpleConstructor.Element element : constructor.getElements()) {
     values.add(exprFactory.findValueGenerator(element.getValue())); 
    }
    
    SimpleExpr array = Expressions.newArray(valueFunction.getValueType(), values);
    SimpleExpr offset = Expressions.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
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
  public FatPtrExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    int arrayLength = ((GimpleArrayType) decl.getType()).getElementCount() * valueFunction.getElementLength();

    SimpleExpr initialValue = Expressions.newArray(valueFunction.getValueType(), arrayLength);
    SimpleExpr array = allocator.reserve(decl.getName(), arrayType, initialValue);
    SimpleExpr offset = Expressions.zero();
    
    return new FatPtrExpr(new FatPtrExpr(array, offset), array, offset);
  }

  public Expr elementAt(Expr array, Expr index) {
    FatPtrExpr arrayFatPtr = (FatPtrExpr) array;
    
    SimpleExpr indexValue = (SimpleExpr) index;
    
    // New offset  = ptr.offset + (index * value.length)
    // for arrays of doubles, for example, this will be the same as ptr.offset + index
    // but for arrays of complex numbers, this will be ptr.offset + (index * 2)
    SimpleExpr newOffset = Expressions.sum(
        arrayFatPtr.getOffset(),
        Expressions.product(
            Expressions.difference(indexValue, lowerBound),
            valueFunction.getElementLength()));
    
    return valueFunction.dereference(arrayFatPtr.getArray(), newOffset);
  }
}
