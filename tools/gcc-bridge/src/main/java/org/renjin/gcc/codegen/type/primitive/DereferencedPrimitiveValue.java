package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generator for a primitive value dereferenced from a pointer
 */
public class DereferencedPrimitiveValue extends AbstractExprGenerator {

  private final ExprGenerator pointer;
  private final GimplePrimitiveType type;

  public DereferencedPrimitiveValue(ExprGenerator pointer) {
    this.pointer = pointer;
    this.type = pointer.getGimpleType().getBaseType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    pointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(type.jvmType().getOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    pointer.emitPushPtrArrayAndOffset(mv);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.jvmType().getOpcode(Opcodes.IASTORE));
  }

  @Override
  public ExprGenerator addressOf() {
    return pointer;
  }
}
