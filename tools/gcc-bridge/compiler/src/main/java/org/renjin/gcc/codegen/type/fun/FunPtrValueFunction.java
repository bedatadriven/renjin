package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;


public class FunPtrValueFunction implements ValueFunction {
  
  private final int pointerSize;

  /**
   * 
   * @param pointerSize the size, in bytes, of the function pointer as understood by GCC. 
   */
  public FunPtrValueFunction(int pointerSize) {
    this.pointerSize = pointerSize;
  }

  @Override
  public Type getValueType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public int getElementLength() {
    return 1;
  }

  @Override
  public int getElementSize() {
    return pointerSize;
  }

  @Override
  public ExprGenerator dereference(Value array, Value offset) {
    return Values.elementAt(array, offset);
  }

  @Override
  public List<Value> getDefaultValue() {
    return Collections.emptyList();
  }
}
