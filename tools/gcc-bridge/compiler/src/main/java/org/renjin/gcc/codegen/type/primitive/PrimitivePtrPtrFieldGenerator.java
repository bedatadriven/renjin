package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates fields that are pointers to pointers
 */
public class PrimitivePtrPtrFieldGenerator extends FieldGenerator {
  
  private String className;
  private String fieldName;
  private GimpleType pointerType;

  /**
   * The underlying base primitive type
   */
  private GimplePrimitiveType primitiveType;
  
  
  private WrapperType wrapperType;

  /**
   * The internal type of array field, which will be an array of fat pointers, 
   * for example "[Lorg.renjin.gcc.runtime/DoublePtr;"
   */
  private String arrayFieldDescriptor;
  
  public PrimitivePtrPtrFieldGenerator(String className, String fieldName, GimpleType type) {
    this.className = className;
    this.fieldName = fieldName;
    this.pointerType = type;
    this.primitiveType = type.getBaseType().getBaseType();
    this.wrapperType = WrapperType.of(primitiveType);
    this.arrayFieldDescriptor = "[" + wrapperType.getWrapperType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return pointerType;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(Opcodes.ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, arrayFieldDescriptor, null, null).visitEnd();
    cv.visitField(access, fieldName + "$offset", "I", null, null).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException("todo");
  }
  
  
  private class StaticFieldPtrPtr extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return pointerType;
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName, arrayFieldDescriptor);
      mv.visitFieldInsn(Opcodes.GETSTATIC, className, fieldName + "$offset", "I");
    }

    @Override
    public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
      valueGenerator.emitPushPtrArrayAndOffset(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName + "$offset", "I");
      mv.visitFieldInsn(Opcodes.PUTSTATIC, className, fieldName, arrayFieldDescriptor);
    }

    @Override
    public ExprGenerator pointerPlus(ExprGenerator offsetInBytes) {
      return new PrimitivePtrPtrPlus(this, offsetInBytes);
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedPrimitivePtr(this);
    }
  }
  
}
