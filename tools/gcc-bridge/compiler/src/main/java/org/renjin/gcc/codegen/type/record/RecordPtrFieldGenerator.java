package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class RecordPtrFieldGenerator extends FieldGenerator {
  private String className;
  private String fieldName;
  private RecordClassGenerator recordGenerator;

  public RecordPtrFieldGenerator(String className, String fieldName, RecordClassGenerator recordGenerator) {
    this.className = className;
    this.fieldName = fieldName;
    this.recordGenerator = recordGenerator;
  }

  @Override
  public GimpleType getType() {
    return new GimplePointerType(recordGenerator.getGimpleType());
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, recordGenerator.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new Member(instanceGenerator);
  }

  private class Member extends AbstractExprGenerator implements RecordUnitPtrGenerator {

    private ExprGenerator instanceGenerator;

    public Member(ExprGenerator instanceGenerator) {
      this.instanceGenerator = instanceGenerator;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(recordGenerator.getGimpleType());
    }

    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      instanceGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, recordGenerator.getDescriptor());
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      instanceGenerator.emitPushRecordRef(mv);
      valueGenerator.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, recordGenerator.getDescriptor());

    }
  }
}
