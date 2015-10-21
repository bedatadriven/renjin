package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ICONST_0;

public class NullPtrGenerator extends AbstractExprGenerator implements PtrGenerator {

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
  public void emitPushMethodHandle(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
  }

  @Override
  public GimpleType getGimpleType() {
    return null;
  }
}
