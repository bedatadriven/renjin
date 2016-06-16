package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;

import javax.annotation.Nonnull;


public class CloneInvocation implements SimpleExpr {
  
  private SimpleExpr instance;

  public CloneInvocation(SimpleExpr instance) {
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
