package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

public class NullPtrGenerator implements PtrGenerator {

  private GimpleType baseType;

  public NullPtrGenerator(GimpleType pointerType) {
    this.baseType = pointerType.getBaseType();
  }

  @Override
  public GimpleType gimpleBaseType() {
    return baseType;
  }

  @Override
  public Type baseType() {
    if(baseType instanceof GimplePrimitiveType) {
      return ((GimplePrimitiveType) baseType).jvmType();
    } else {
      throw new UnsupportedOperationException("baseType: " + baseType);
    }
  }

  @Override
  public boolean isSameArray(PtrGenerator other) {
    return false;
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
  }

  @Override
  public void emitPushOffset(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_0);
  }
}
