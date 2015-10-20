package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;

/**
 * Wraps a pointer array+offset into a Ptr instance 
 */
public class WrappedPtrConverter implements ParamConverter {
  
  private ExprGenerator ptrGenerator;

  public WrappedPtrConverter(ExprGenerator ptrGenerator) {
    this.ptrGenerator = ptrGenerator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    ptrGenerator.emitPushPointerWrapper(mv);
  }
}
