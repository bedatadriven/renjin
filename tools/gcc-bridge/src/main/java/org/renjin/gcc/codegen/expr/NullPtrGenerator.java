package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ICONST_0;

public class NullPtrGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimpleIndirectType type;

  public NullPtrGenerator(GimpleType pointerType) {
    this.type = (GimpleIndirectType) pointerType;
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ICONST_0);
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_0);
  }

  @Override
  public void emitPushMethodHandle(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitPushPointerWrapper(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
  }
}
