package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
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
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl varDecl) {
    cv.visitField(ACC_STATIC | ACC_PUBLIC | isFinal(varDecl), fieldName, type.getDescriptor(), null, initialValue(varDecl)).visitEnd();
  }


  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, type.getDescriptor(), null, null).visitEnd();
  }


  private int isFinal(GimpleVarDecl varDecl) {
    if(varDecl.isConstant()) {
      return ACC_FINAL;
    } else {
      return 0;
    }
  }


  private Object initialValue(GimpleVarDecl varDecl) {
    GimpleExpr initialValue = varDecl.getValue();
    if(initialValue == null) {
      return null;
    } else if(type.equals(Type.INT_TYPE)) {
      return ((GimpleIntegerConstant) initialValue).getNumberValue().intValue();
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return ((GimpleRealConstant) initialValue).getNumberValue().doubleValue();
    } else if(type.equals(Type.LONG_TYPE)) {
      return ((GimpleIntegerConstant) initialValue).getNumberValue().longValue();
    } else if(type.equals(Type.FLOAT_TYPE)) {
      return ((GimpleRealConstant) initialValue).getNumberValue().floatValue();
    } else {
      throw new UnsupportedOperationException("initial value: " + initialValue);
    }
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
