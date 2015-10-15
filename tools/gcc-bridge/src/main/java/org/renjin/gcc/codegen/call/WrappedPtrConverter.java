package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;

/**
 * Wraps a pointer array+offset into a Ptr instance 
 */
public class WrappedPtrConverter implements ParamConverter {
  
  private PtrGenerator ptrGenerator;
  private WrapperType wrapperType;

  public WrappedPtrConverter(WrapperType wrapperType, ExprGenerator ptrGenerator) {
    this.wrapperType = wrapperType;
    this.ptrGenerator = (PtrGenerator) ptrGenerator;
  }

  @Override
  public void emitPushParam(MethodVisitor mv) {
    wrapperType.emitPushWrapper(mv, ptrGenerator);
  }
}
