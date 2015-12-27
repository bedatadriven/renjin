package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
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
  private WrapperType wrapperType;

  public PrimitivePtrFieldGenerator(String className, String fieldName, GimpleType type) {
    this.className = className;
    this.arrayFieldName = fieldName;
    this.offsetFieldName = fieldName + "$offset";
    this.gimpleType = type;
    this.baseType = ((GimplePrimitiveType)type.getBaseType()).jvmType();
    wrapperType = WrapperType.of(baseType);
  }

  @Override
  public GimpleType getType() {
    return gimpleType;
  }

  private boolean isNull(GimpleExpr initialValue) {
    if(initialValue == null) {
      return true;
      
    } else if(initialValue instanceof GimpleIntegerConstant) {
      GimpleIntegerConstant constant = (GimpleIntegerConstant) initialValue;
      if(constant.getValue() == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, arrayFieldName, arrayTypeDescriptor(), null, null).visitEnd();
    cv.visitField(access, offsetFieldName, "I", null, 0).visitEnd();
  }

  
  @Override
  public void emitStoreMember(MethodVisitor mv, ExprGenerator ptr) {
    
    // Need two copies of the instance on the stack for the store stores
    mv.visitInsn(Opcodes.DUP);
    
    // Push array and offset onto the stack
    ptr.emitPushPtrArrayAndOffset(mv);

    // DUP_X2 + POP
    //             value3,  value2, value1 →            value1,   value3,  value2,  value1
    // instance, instance,   array, offset → instance,  offset, instance ,  array,  offset
    //                                     → instance,  offset, instance ,  array            
    mv.visitInsn(Opcodes.DUP_X2);
    mv.visitInsn(Opcodes.POP);

    // Consume [instance, array] -> 
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, arrayFieldName, arrayTypeDescriptor());
    
    // Consume [instance, offset] 
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, offsetFieldName, "I");
  }

  private String arrayTypeDescriptor() {
    return "[" + baseType.getDescriptor();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new MemberPtrExpr(instanceGenerator);
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

    @Override
    public WrapperType getPointerType() {
      return wrapperType;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      instance.emitPushRecordRef(mv);
      mv.visitInsn(Opcodes.DUP);
      mv.visitFieldInsn(Opcodes.GETFIELD, className, arrayFieldName, arrayTypeDescriptor());
      // stack : [instance, array]
      mv.visitInsn(Opcodes.SWAP);
      // stack: [array, instance]
      mv.visitFieldInsn(Opcodes.GETFIELD, className, offsetFieldName, "I");
    }

    @Override
    public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
      instance.emitPushRecordRef(mv);
      emitStoreMember(mv, valueGenerator);
    }
    
    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitiveValue(this);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPlus(this, offsetInBytes);
    }
  }

}
