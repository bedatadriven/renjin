package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the value of a primitive pointer, such as {@code double*}, dereferenced
 * from the provided pointer generator.
 */
public class DereferencedPrimitivePtr extends AbstractExprGenerator {
  
  private ExprGenerator pointerPointer;
  private WrapperType wrapperType;

  public DereferencedPrimitivePtr(ExprGenerator pointerPointer) {
    this.pointerPointer = pointerPointer;
    this.wrapperType = WrapperType.of(pointerPointer.getGimpleType().getBaseType().getBaseType());
  }

  @Override
  public GimpleType getGimpleType() {
    return pointerPointer.getGimpleType().getBaseType();
  }


  @Override
  public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
    return new PrimitivePtrPlus(this, offsetInBytes);
  }

  @Override
  public ExprGenerator addressOf() {
    return pointerPointer;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public void emitPushPointerWrapper(MethodVisitor mv) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    emitPushPointerWrapper(mv);
    wrapperType.emitUnpackArrayAndOffset(mv);
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedPrimitiveValue(this);
  }
}
