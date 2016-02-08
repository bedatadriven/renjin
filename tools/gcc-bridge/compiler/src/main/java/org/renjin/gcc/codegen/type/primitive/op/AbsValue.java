package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;


public class AbsValue implements SimpleExpr {

  private SimpleExpr x;

  public AbsValue(SimpleExpr x) {
    this.x = x;
  }

  @Nonnull
  @Override
  public Type getType() {
    return x.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    mv.invokestatic(Math.class, "abs", Type.getMethodDescriptor(x.getType(), x.getType()));
  }
}
