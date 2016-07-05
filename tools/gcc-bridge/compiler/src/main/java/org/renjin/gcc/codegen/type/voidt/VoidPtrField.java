package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for {@code void* } fields, compiled as a field of type
 * {@code java.lang.Object}
 */
public class VoidPtrField extends FieldStrategy {
  
  private String fieldName;

  public VoidPtrField(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, "Ljava/lang/Object;", null, null);
  }

  @Override
  public VoidPtr memberExpr(JExpr instance, GimpleType expectedType) {
    FieldValue ref = new FieldValue(instance, fieldName, Type.getType(Object.class));
    return new VoidPtr(ref);
  }


}
