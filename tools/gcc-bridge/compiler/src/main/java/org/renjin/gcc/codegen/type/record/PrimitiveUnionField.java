package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

public class PrimitiveUnionField extends FieldStrategy {
  
  private Type declaringClass;
  private Type fieldType;
  private String fieldName;

  public PrimitiveUnionField(Type declaringClass, Type fieldType, String fieldName) {
    this.declaringClass = declaringClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, fieldType.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    throw new UnsupportedOperationException();
  }
}
