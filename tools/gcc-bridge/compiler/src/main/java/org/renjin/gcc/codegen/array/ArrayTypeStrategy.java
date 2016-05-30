package org.renjin.gcc.codegen.array;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

/**
 * Type strategy for arrays of values
 */
public class ArrayTypeStrategy implements TypeStrategy<FatPtrExpr> {
  
  private final ValueFunction valueFunction;
  private GimpleArrayType arrayType;
  private int arrayLength = -1;
  private boolean parameterWrapped = true;

  public ArrayTypeStrategy(GimpleArrayType arrayType, ValueFunction valueFunction) {
    this.arrayType = arrayType;
    this.valueFunction = valueFunction;
    if(arrayType.isStatic()) {
      this.arrayLength = arrayType.getElementCount();
    }
  }
  
  public ArrayTypeStrategy(GimpleArrayType arrayType, int arrayLength, ValueFunction valueFunction) {
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
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
    return new FatPtrStrategy(new ArrayValueFunction(arrayType, valueFunction))
        .setParametersWrapped(parameterWrapped);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    // Multidimensional arrays are layed out in contiguous memory blocks
    return new ArrayTypeStrategy(arrayType, this.arrayLength * arrayType.getElementCount(),
          new ArrayValueFunction(this.arrayType, valueFunction));
  }

  @Override
  public FatPtrExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if (typeStrategy instanceof FatPtrStrategy ||
        typeStrategy instanceof ArrayTypeStrategy) {
      
      return (FatPtrExpr)value;
    }
    
    throw new UnsupportedCastException();
  }

  @Override
  public FatPtrExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor constructor) {
    List<SimpleExpr> values = Lists.newArrayList();
    addElementConstructors(values, exprFactory, constructor);

    SimpleExpr array = Expressions.newArray(valueFunction.getValueType(), values);
    SimpleExpr offset = Expressions.zero();
    
    return new FatPtrExpr(array, offset);
  }

  private void addElementConstructors(List<SimpleExpr> values, ExprFactory exprFactory, GimpleConstructor constructor) {
    for (GimpleConstructor.Element element : constructor.getElements()) {
      if(element.getValue() instanceof GimpleConstructor &&
          element.getValue().getType() instanceof GimpleArrayType) {
        GimpleConstructor elementConstructor = (GimpleConstructor) element.getValue();

        addElementConstructors(values, exprFactory, elementConstructor);

      } else {
        Expr elementExpr = exprFactory.findGenerator(element.getValue());
        List<SimpleExpr> arrayValues = valueFunction.toArrayValues(elementExpr);
        values.addAll(arrayValues);
      }
    }
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) { 
    if(arrayType.getUbound() == null) {
      throw new UnsupportedOperationException(
          String.format("Array field '%s' with type %s does not have a fixed size",
              fieldName, arrayType));
    }
    return new ArrayField(className, fieldName, arrayType.getElementCount(), valueFunction);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return fieldGenerator(className, fieldName);
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

    SimpleExpr array;
    if(decl.getValue() == null) {
      array = allocator.reserve(decl.getName(), arrayType, allocArray(arrayLength));
    } else {
      array = allocator.reserve(decl.getName(), arrayType);
    }
    
    SimpleExpr offset = Expressions.zero();
    
    return new FatPtrExpr(new FatPtrExpr(array, offset), array, offset);
  }

  private SimpleExpr allocArray(int arrayLength) {
    if(valueFunction.getValueConstructor().isPresent()) {
      // For reference types like records or fat pointers we have to 
      // initialize each element of the array
      List<SimpleExpr> valueConstructors = Lists.newArrayList();
      for (int i = 0; i < arrayLength; i++) {
        valueConstructors.add(valueFunction.getValueConstructor().get());
      }
      return Expressions.newArray(valueFunction.getValueType(), valueConstructors);
    
    } else {
      // For primitive types, we can just allocate the array
      return Expressions.newArray(valueFunction.getValueType(), arrayLength);
    }
    
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
            Expressions.difference(indexValue, arrayType.getLbound()),
            valueFunction.getElementLength()));
    
    return valueFunction.dereference(arrayFatPtr.getArray(), newOffset);
  }
}
