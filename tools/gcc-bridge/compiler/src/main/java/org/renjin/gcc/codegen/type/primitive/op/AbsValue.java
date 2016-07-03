package org.renjin.gcc.codegen.type.primitive.op;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class AbsValue implements JExpr {

  private JExpr x;

  public AbsValue(JExpr x) {
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
