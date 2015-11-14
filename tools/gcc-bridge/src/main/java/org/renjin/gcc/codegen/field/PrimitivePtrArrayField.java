package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;


/**
 * Field storing an array of primitive pointers, for example, double*[] or char*[]
 */
public class PrimitivePtrArrayField extends FieldGenerator {

  private final String className;
  private final String fieldName;
  private final GimpleArrayType arrayType;
  private final GimplePrimitiveType primitiveType;
  private final String fieldDescriptor;

  public PrimitivePtrArrayField(String className, String fieldName, GimpleArrayType arrayType) {
    this.className = className;
    this.fieldName = fieldName;
    this.arrayType = arrayType;
    this.primitiveType = arrayType.getComponentType().getBaseType();
    this.fieldDescriptor = "[" + WrapperType.of(primitiveType).getWrapperType().getDescriptor();
  }

  @Override
  public GimpleType getType() {
    return arrayType;
  }

  @Override
  public void emitStaticField(ClassVisitor cv, GimpleVarDecl decl) {
    cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, fieldName, fieldDescriptor, null, null).visitEnd();
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator staticExprGenerator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException();
  }
}
