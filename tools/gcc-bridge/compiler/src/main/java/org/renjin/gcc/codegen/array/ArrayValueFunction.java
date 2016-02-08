package org.renjin.gcc.codegen.array;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.Value;

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
  public ExprGenerator dereference(Value array, Value offset) {
    return new FatPtrExpr(array, offset);
  }

  @Override
  public List<Value> getDefaultValue() {
    throw new InternalCompilerException();
  }
}
