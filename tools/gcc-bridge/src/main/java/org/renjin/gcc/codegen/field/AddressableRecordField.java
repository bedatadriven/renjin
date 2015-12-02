package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedUnitRecordPtr;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class AddressableRecordField extends FieldGenerator {

  private final String className;
  private final String fieldName;
  private final String fieldDescriptor;
  private RecordClassGenerator generator;

  public AddressableRecordField(String className, String fieldName, RecordClassGenerator generator) {
    this.className = className;
    this.fieldName = fieldName;
    this.generator = generator;
    this.fieldDescriptor = "[" + generator.getType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return generator.getGimpleType();
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    cv.visitField(ACC_STATIC | ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }


  @Override
  public void emitStaticInit(MethodVisitor mv) {
    emitNewArray(mv);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, generator.getType().getInternalName(), fieldName, fieldDescriptor);
  }
  
  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    emitNewArray(mv);
    mv.visitFieldInsn(Opcodes.PUTFIELD, generator.getType().getInternalName(), fieldName, fieldDescriptor);
  }

  private void emitNewArray(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, generator.getType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ICONST_0);
    mv.visitTypeInsn(Opcodes.NEW, generator.getType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, generator.getType().getInternalName(), "<init>", "()V", false);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    throw new UnsupportedOperationException();
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
      return generator.getGimpleType();
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, fieldName, fieldDescriptor);
      mv.visitInsn(Opcodes.ICONST_0); 
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedUnitRecordPtr(this);
    }
  }
}
