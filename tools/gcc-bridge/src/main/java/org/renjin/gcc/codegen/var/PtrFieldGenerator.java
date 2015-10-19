package org.renjin.gcc.codegen.var;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates two fields for a global pointer variable, one for an array, and the other for 
 * an offset into the array.
 */
public class PtrFieldGenerator implements FieldGenerator, PtrGenerator, LValueGenerator {

  private String className;
  private String arrayFieldName;
  private String offsetFieldName;
  private GimpleType gimpleBaseType;
  private Type baseType;
  
  public PtrFieldGenerator(String className, GimpleVarDecl gimpleVarDecl) {
    this.className = className;
    this.arrayFieldName = gimpleVarDecl.getName();
    this.offsetFieldName = gimpleVarDecl.getName() + "$offset";
    this.gimpleBaseType = gimpleVarDecl.getType();
    this.baseType = ((GimplePrimitiveType)gimpleVarDecl.getType().getBaseType()).jvmType();
  }

  @Override
  public void emitField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, arrayFieldName, arrayTypeDescriptor(), null, null).visitEnd();
    cv.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, offsetFieldName, "I", null, 0).visitEnd();
  }

  private String arrayTypeDescriptor() {
    return "[" + baseType.getDescriptor();
  }

  @Override
  public GimpleType gimpleBaseType() {
    return gimpleBaseType;
  }

  @Override
  public Type baseType() {
    return baseType;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, arrayFieldName, arrayTypeDescriptor());
    mv.visitFieldInsn(Opcodes.GETSTATIC, className, offsetFieldName, "I");
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator exprGenerator) {
    PtrGenerator ptr = (PtrGenerator) exprGenerator;
    
    // Store field
    ptr.emitPushArrayAndOffset(mv);

    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, offsetFieldName, "I");
    mv.visitFieldInsn(Opcodes.PUTSTATIC, className, arrayFieldName, arrayTypeDescriptor());
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }
}
