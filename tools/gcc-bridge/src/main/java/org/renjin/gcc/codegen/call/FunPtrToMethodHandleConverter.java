package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;

/**
 * Pushes a function pointer onto the stack as a MethodHandle
 */
public class FunPtrToMethodHandleConverter implements ParamConverter {
  private ExprGenerator generator;

  public FunPtrToMethodHandleConverter(ExprGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    generator.emitPushMethodHandle(mv);    
  }
}
