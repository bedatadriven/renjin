package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;

import javax.annotation.Nonnull;

/**
 * Container for {@link SimpleExpr} which also have an address.
 */
public final class SimpleAddressableExpr implements Addressable, SimpleExpr, LValue<SimpleExpr> {
  
  private SimpleExpr value;
  private Expr address;

  public SimpleAddressableExpr(SimpleExpr value, Expr address) {
    this.value = value;
    this.address = address;
  }

  @Override
  public Expr addressOf() {
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
  public void store(MethodGenerator mv, SimpleExpr rhs) {
    if(!(value instanceof LValue)) {
      throw new InternalCompilerException("not addressable");
    }
    ((LValue) value).store(mv, rhs);
  }
}
