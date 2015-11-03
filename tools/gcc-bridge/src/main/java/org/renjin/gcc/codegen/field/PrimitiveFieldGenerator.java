package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;


public class PrimitiveFieldGenerator implements FieldGenerator {

  private String fieldName;
  private String className;
  private GimplePrimitiveType gimpleType;
  private Type type;

  public PrimitiveFieldGenerator(String className, String fieldName, GimpleType gimpleType, Type type) {
    this.fieldName = fieldName;
    this.className = className;
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = type;
  }


  @Override
  public void emitStaticField(ClassVisitor cv) {
    emitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, cv);
  }


  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, type.getDescriptor(), null, null).visitEnd();
  }


  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticMemberExpr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr(instanceGenerator);
  }

  private class StaticMemberExpr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return gimpleType;
    }

    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, type.getDescriptor());
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, type.getDescriptor());
    }
  }

  public class MemberExpr extends AbstractExprGenerator{

    private ExprGenerator instance;

    public MemberExpr(ExprGenerator instance) {
      this.instance = instance;
    }

    @Override
    public GimpleType getGimpleType() {
      return gimpleType;
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      instance.emitPushRecordRef(mv);
      valueGenerator.emitPrimitiveValue(mv);
      mv.visitFieldInsn(PUTFIELD, className, fieldName, type.getDescriptor());
    }

    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(GETFIELD, className, fieldName, type.getDescriptor());
    }
  }
}
