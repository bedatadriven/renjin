package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;


public class PrimitiveFieldGenerator extends FieldGenerator {

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
  public GimpleType getType() {
    return gimpleType;
  }


  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, type.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitFieldInsn(PUTFIELD, className, fieldName, type.getDescriptor());
  }


  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberExpr(instanceGenerator);
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
      emitStoreMember(mv, valueGenerator);
    }


    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      instance.emitPushRecordRef(mv);
      mv.visitFieldInsn(GETFIELD, className, fieldName, type.getDescriptor());
    }
  }
}
