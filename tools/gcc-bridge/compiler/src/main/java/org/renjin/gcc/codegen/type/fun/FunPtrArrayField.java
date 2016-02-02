package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveConstGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.runtime.ObjectPtr;

/**
 * Field storing an array of function pointers
 */
public class FunPtrArrayField extends FieldStrategy {

  private String className;
  private String fieldName;
  private GimpleArrayType arrayType;
  private final String fieldDescriptor;

  public FunPtrArrayField(String className, String fieldName, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.fieldDescriptor = "[" + Type.getDescriptor(ObjectPtr.class);
    this.arrayType = arrayType;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, className, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    PrimitiveConstGenerator.emitInt(mv, arrayType.getElementCount());
    mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getDescriptor(ObjectPtr.class));
    mv.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, fieldDescriptor);
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException();
  }

}
