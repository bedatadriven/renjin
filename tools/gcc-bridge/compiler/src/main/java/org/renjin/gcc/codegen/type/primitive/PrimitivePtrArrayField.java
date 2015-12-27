package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldGenerator;
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
  public void emitInstanceField(ClassVisitor cv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new UnsupportedOperationException();
  }
}
