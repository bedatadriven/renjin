package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates two fields for a global pointer variable, one for an array, and the other for 
 * an offset into the array.
 */
public class PrimitivePtrFieldGenerator extends FieldGenerator {

  private String className;
  private String arrayFieldName;
  private String offsetFieldName;
  private GimpleType gimpleType;
  private Type baseType;
  
  public PrimitivePtrFieldGenerator(String className, String fieldName, GimpleType type) {
    this.className = className;
    this.arrayFieldName = fieldName;
    this.offsetFieldName = fieldName + "$offset";
    this.gimpleType = type;
    this.baseType = ((GimplePrimitiveType)type.getBaseType()).jvmType();
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    assertNoInitialValue(decl);

    emitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, cv);
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, arrayFieldName, arrayTypeDescriptor(), null, null).visitEnd();
    cv.visitField(access, offsetFieldName, "I", null, 0).visitEnd();
  }

  private String arrayTypeDescriptor() {
    return "[" + baseType.getDescriptor();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    return new StaticMemberExpr();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberPtrExpr(instanceGenerator);
  }

  private class StaticMemberExpr extends AbstractExprGenerator {


    @Override
    public GimpleType getGimpleType() {
      return gimpleType;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, arrayFieldName, arrayTypeDescriptor());
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, offsetFieldName, "I");
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator ptr) {

      // Store field
      ptr.emitPushPtrArrayAndOffset(mv);

      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, offsetFieldName, "I");
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, arrayFieldName, arrayTypeDescriptor());
    }
  }

  private class MemberPtrExpr extends AbstractExprGenerator {
    private ExprGenerator instance;

    public MemberPtrExpr(ExprGenerator instance) {
      this.instance = instance;
    }

    @Override
    public GimpleType getGimpleType() {
      return gimpleType;
    }
    
  }
}
