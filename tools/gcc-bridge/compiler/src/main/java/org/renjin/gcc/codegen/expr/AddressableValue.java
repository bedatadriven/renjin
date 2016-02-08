package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.LValue;
import org.renjin.gcc.codegen.var.Value;

import javax.annotation.Nonnull;


public class AddressableValue implements Addressable, Value, LValue<Value> {
  
  private Value value;
  private ExprGenerator address;

  public AddressableValue(Value value, ExprGenerator address) {
    this.value = value;
    this.address = address;
  }

  @Override
  public ExprGenerator addressOf() {
    return address;
  }

  @Nonnull
  @Override
  public Type getType() {
    return value.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    value.load(mv);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void store(MethodGenerator mv, Value rhs) {
    if(!(value instanceof LValue)) {
      throw new InternalCompilerException("not addressable");
    }
    ((LValue) value).store(mv, rhs);
  }
}
