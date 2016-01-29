package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Wraps an existing Pointer generator with an additional offset.
 */
public class PrimitivePtrPtrPlus extends AbstractExprGenerator {
  
  private ExprGenerator basePointer;
  private ExprGenerator offsetInBytes;

  public PrimitivePtrPtrPlus(ExprGenerator basePointer, ExprGenerator offsetInBytes) {
    this.basePointer = basePointer;
    this.offsetInBytes = offsetInBytes;
  }

  @Override
  public GimpleType getGimpleType() {
    return basePointer.getGimpleType();
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedPrimitivePtr(this);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    basePointer.emitPushPtrArrayAndOffset(mv);
    addOffsetInBytes(mv, offsetInBytes);
  }
}
