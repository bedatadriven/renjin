package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

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
