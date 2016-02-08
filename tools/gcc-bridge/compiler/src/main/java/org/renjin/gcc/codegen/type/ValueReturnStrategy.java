package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

/**
 * Returns a Value
 */
public final class ValueReturnStrategy implements ReturnStrategy {

  private Type type;

  public ValueReturnStrategy(Type type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Value marshall(ExprGenerator expr) {
    return (Value)expr;
  }

  @Override
  public Value unmarshall(MethodGenerator mv, Value returnValue) {
    return returnValue;
  }

  @Override
  public Value getDefaultReturnValue() {
    switch (type.getSort()) {
      case Type.OBJECT:
      case Type.ARRAY:
      case Type.METHOD:
        return Values.nullRef(type); 
      
      default:
        return Values.zero(type);
    }
  }
}
