package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.DereferencedPrimitiveValue;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

/**
 * FieldGenerator for primitive fields that must be addressed. 
 */
public class AddressablePrimitiveField extends FieldGenerator {

  private String fieldName;
  private String className;
  private GimplePrimitiveType gimpleType;
  private Type type;
  private String fieldDescriptor;

  public AddressablePrimitiveField(String className, String fieldName, GimpleType gimpleType, Type type) {
    this.fieldName = fieldName;
    this.className = className;
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = type;
    this.fieldDescriptor = "[" + type.getDescriptor();
  }


  @Override
  public GimpleType getType() {
    return gimpleType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl varDecl) {
    cv.visitField(ACC_STATIC | ACC_PUBLIC | isFinal(varDecl), fieldName, fieldDescriptor, null, initialValue(varDecl)).visitEnd();
  }

  @Override
  public void emitStaticInit(MethodVisitor mv) {
    mv.visitInsn(ICONST_1);
    MallocGenerator.emitNewArray(mv, type);
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, fieldDescriptor);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(ACC_PUBLIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    mv.visitFieldInsn(GETFIELD, className, fieldName, fieldDescriptor);
    mv.visitInsn(Opcodes.ICONST_0);
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.getOpcode(IASTORE));
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
    return new StaticMemberPtr().valueOf();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnimplementedException(getClass(), "memberExprGenerator");
  }
  
  private class StaticMemberPtr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(gimpleType);
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitiveValue(this);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, fieldDescriptor);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
}
