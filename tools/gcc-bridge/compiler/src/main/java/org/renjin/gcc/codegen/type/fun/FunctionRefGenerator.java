package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;

/**
 * Emits the bytecode necessary to push a method handle onto the stack
 */
public class FunctionRefGenerator implements Value {

  private Handle handle;

  public FunctionRefGenerator(Handle handle) {
    this.handle = handle;
  }

  @Override
  public Type getType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public void load(MethodGenerator mv) {
    mv.visitLdcInsn(handle);
  }
}
