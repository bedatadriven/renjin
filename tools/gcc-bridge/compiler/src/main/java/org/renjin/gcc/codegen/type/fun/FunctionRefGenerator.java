package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

/**
 * Emits the bytecode necessary to push a method handle onto the stack
 */
public class FunctionRefGenerator implements JExpr {

  private Handle handle;

  public FunctionRefGenerator(Handle handle) {
    this.handle = handle;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    mv.visitLdcInsn(handle);
  }
}
