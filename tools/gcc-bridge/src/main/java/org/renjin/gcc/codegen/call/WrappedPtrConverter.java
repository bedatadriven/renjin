package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;

/**
 * Wraps a pointer array+offset into a Ptr instance 
 */
public class WrappedPtrConverter implements ParamConverter {
  
  private PtrGenerator ptrGenerator;

  public WrappedPtrConverter(ExprGenerator ptrGenerator) {
    this.ptrGenerator = (PtrGenerator) ptrGenerator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    ptrGenerator.emitPushPointerWrapper(mv);
  }
}
