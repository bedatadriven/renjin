package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ICONST_0;

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
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ICONST_0);
  }
}
