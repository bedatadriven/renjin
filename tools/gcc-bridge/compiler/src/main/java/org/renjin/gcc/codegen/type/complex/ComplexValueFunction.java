package org.renjin.gcc.codegen.type.complex;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

import java.util.Collections;
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
  public int getElementLength() {
    return 2;
  }

  @Override
  public int getElementSize() {
    switch (valueType.getSort()) {
      case Type.DOUBLE:
        return 128;
      case Type.FLOAT:
        return 64;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public ExprGenerator dereference(Value array, Value offset) {
    // Real element is at i
    Value realOffset = offset;
    // Complex element is at i+1
    Value imaginaryOffset = Values.sum(realOffset, Values.constantInt(1));
    
    Value real = Values.elementAt(array, realOffset);
    Value imaginary = Values.elementAt(array, imaginaryOffset);
    
    return new ComplexValue(real, imaginary);
  }

  @Override
  public List<Value> getDefaultValue() {
    return Collections.emptyList();
  }
}
