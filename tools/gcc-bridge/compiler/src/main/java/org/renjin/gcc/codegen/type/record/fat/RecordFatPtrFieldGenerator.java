package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Strategy for fat pointer fields, using an record_t[] and record_t$offset field
 */
public class RecordFatPtrFieldGenerator extends FieldGenerator {

  private String ownerClass;
  private String arrayName;
  private String arrayDescriptor;
  private String offsetName;
  private RecordTypeStrategy recordStrategy;

  public RecordFatPtrFieldGenerator(String ownerClass, String name, RecordTypeStrategy recordStrategy) {
    this.ownerClass = ownerClass;
    this.arrayName = name;
    this.offsetName = arrayName + "$offset";
    this.arrayDescriptor = "[" + recordStrategy.getJvmType().getDescriptor();
    this.recordStrategy = recordStrategy;
  }

  @Override
  public GimpleType getType() {
    return recordStrategy.getRecordType().pointerTo();
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayName, arrayDescriptor, null, null).visitEnd();
    cv.visitField(Opcodes.ACC_PUBLIC, offsetName, "I", null, null).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr(instanceGenerator);
  }
  
  private class MemberExpr extends AbstractExprGenerator {

    private final ExprGenerator instance;

    public MemberExpr(ExprGenerator instance) {
      this.instance = instance;
    }

    @Override
    public GimpleType getGimpleType() {
      return recordStrategy.getRecordType().pointerTo();
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, ownerClass, arrayName, arrayDescriptor);
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, ownerClass, offsetName, "I");
    }
  }
}
