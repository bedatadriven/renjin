package org.renjin.gcc.codegen.array;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;

import java.util.List;


public class ArrayValueFunction implements ValueFunction {
  
  private final ValueFunction elementValueFunction;

  public ArrayValueFunction(ValueFunction elementValueFunction) {
    this.elementValueFunction = elementValueFunction;
  }

  @Override
  public Type getValueType() {
    return elementValueFunction.getValueType();
  }

  @Override
  public int getElementLength() {
    // TODO: is this correct?
    // For example:
    // int x[20];
    // int *p[20] = &x;
    
    return elementValueFunction.getElementLength();
  }

  @Override
  public int getElementSize() {
    return elementValueFunction.getElementSize();
  }

  @Override
  public Expr dereference(SimpleExpr array, SimpleExpr offset) {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public List<SimpleExpr> getDefaultValue() {
    return elementValueFunction.getDefaultValue();
  }
}
