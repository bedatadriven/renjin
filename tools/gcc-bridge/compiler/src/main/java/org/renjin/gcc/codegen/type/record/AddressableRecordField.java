package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.codegen.type.record.unit.DereferencedUnitRecordPtr;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class AddressableRecordField extends FieldGenerator {

  private final String className;
  private final String fieldName;
  private final String fieldDescriptor;
  private RecordClassTypeStrategy strategy;

  public AddressableRecordField(String className, String fieldName, RecordClassTypeStrategy strategy) {
    this.className = className;
    this.fieldName = fieldName;
    this.strategy = strategy;
    this.fieldDescriptor = "[" + strategy.getJvmType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return strategy.getRecordType();
  }


  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    emitNewArray(mv);
    mv.visitFieldInsn(Opcodes.PUTFIELD, strategy.getJvmType().getInternalName(), fieldName, fieldDescriptor);
  }

  private void emitNewArray(MethodGenerator mv) {
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, strategy.getJvmType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitTypeInsn(Opcodes.NEW, strategy.getJvmType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, strategy.getJvmType().getInternalName(), "<init>", "()V", false);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExprPtr(instanceGenerator).valueOf();
  }
  
  private class MemberExprPtr extends AbstractExprGenerator {
    private ExprGenerator instance;

    public MemberExprPtr(ExprGenerator instance) {
      this.instance = instance;
    }

    @Override
    public GimpleType getGimpleType() {
      return strategy.getRecordType();
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, fieldDescriptor);
      mv.visitInsn(Opcodes.ICONST_0); 
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedUnitRecordPtr(strategy, this);
    }
  }
}
