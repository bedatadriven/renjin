package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Casts a void pointer to a record type
 */
public class VoidCastExprGenerator extends AbstractExprGenerator {

  private ExprGenerator voidPtr;
  private GimpleType recordType;
  private Type jvmType;

  public VoidCastExprGenerator(ExprGenerator voidPtr, GimpleType recordType, Type jvmType) {
    this.voidPtr = voidPtr;
    this.recordType = recordType;
    this.jvmType = jvmType;
  }

  @Override
  public GimpleType getGimpleType() {
    return recordType;
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    voidPtr.emitPushRecordRef(mv);
    mv.visitTypeInsn(Opcodes.CHECKCAST, jvmType.getInternalName());
  }
}
