package org.renjin.gcc.codegen.array;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.repackaged.guava.base.Optional;

import java.util.List;


public class ArrayValueFunction implements ValueFunction {

  private final GimpleArrayType arrayType;
  private final ValueFunction elementValueFunction;

  public ArrayValueFunction(GimpleArrayType arrayType, ValueFunction elementValueFunction) {
    this.arrayType = arrayType;
    this.elementValueFunction = elementValueFunction;
    
//    if(arrayType.getUbound() == null) {
//      throw new UnsupportedOperationException("Array type is not fixed: " + arrayType);
//    }
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public int getElementLength() {
    return elementValueFunction.getElementLength() * arrayType.getElementCount();
  }

  @Override
  public int getArrayElementBytes() {
    return elementValueFunction.getArrayElementBytes();
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public List<SimpleExpr> toArrayValues(Expr expr) {
    return elementValueFunction.toArrayValues(expr);
  }

  @Override
  public Optional<SimpleExpr> getValueConstructor() {
    return elementValueFunction.getValueConstructor();
  }

}