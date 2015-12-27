package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;


public class PrimitivePtrPlus extends AbstractExprGenerator {
  
  private final ExprGenerator basePointer;
  private final ExprGenerator offsetInBytes;

  public PrimitivePtrPlus(ExprGenerator basePointer, ExprGenerator offsetInBytes) {
    this.basePointer = basePointer;
    this.offsetInBytes = offsetInBytes;
  }

  @Override
  public GimpleType getGimpleType() {
    return basePointer.getGimpleType();
  }

  @Override
  public WrapperType getPointerType() {
    return basePointer.getPointerType();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    basePointer.emitPushPtrArrayAndOffset(mv);
    addOffsetInBytes(mv, offsetInBytes);
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedPrimitiveValue(this);
  }

  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new PrimitivePtrPlus(this, offsetInBytes);
  }
}
