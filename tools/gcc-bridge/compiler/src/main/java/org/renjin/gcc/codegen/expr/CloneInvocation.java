package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class CloneInvocation implements JExpr {
  
  private JExpr instance;

  public CloneInvocation(JExpr instance) {
    this.instance = instance;
  }

  @Nonnull
  @Override
  public Type getType() {
    return instance.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    instance.load(mv);
    mv.invokevirtual(instance.getType().getInternalName(), "clone", Type.getMethodDescriptor(instance.getType()), false);
  }
}
