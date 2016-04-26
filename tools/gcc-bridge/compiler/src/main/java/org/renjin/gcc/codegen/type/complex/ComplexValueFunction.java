package org.renjin.gcc.codegen.type.complex;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public class ComplexValueFunction implements ValueFunction {
  
  private final Type valueType;

  public ComplexValueFunction(Type valueType) {
    Preconditions.checkArgument(valueType.equals(Type.DOUBLE_TYPE) || valueType.equals(Type.FLOAT_TYPE));
    this.valueType = valueType;
  }

  @Override
  public Type getValueType() {
    return valueType;
  }

  @Override
  public GimpleType getGimpleValueType() {
    return new GimpleComplexType(new GimpleRealType(valueType == Type.DOUBLE_TYPE ? 64 : 32));
  }

  @Override
  public int getElementLength() {
    return 2;
  }

  @Override
  public int getElementSize() {
    switch (valueType.getSort()) {
      case Type.DOUBLE:
        return 16;
      case Type.FLOAT:
        return 8;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {

    FatPtrExpr address = new FatPtrExpr(array, offset);
    
    // Real element is at i
    SimpleExpr realOffset = offset;
    // Complex element is at i+1
    SimpleExpr imaginaryOffset = Expressions.sum(realOffset, Expressions.constantInt(1));
    
    SimpleExpr real = Expressions.elementAt(array, realOffset);
    SimpleExpr imaginary = Expressions.elementAt(array, imaginaryOffset);
    
    return new ComplexValue(address, real, imaginary);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    ComplexValue value = (ComplexValue) expr;
    return Lists.newArrayList(value.getRealValue(), value.getImaginaryValue());
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return Optional.absent();
  }
}
