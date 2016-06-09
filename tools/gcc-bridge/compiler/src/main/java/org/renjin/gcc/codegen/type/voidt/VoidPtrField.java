package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.primitive.FieldValue;

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
  public Expr memberExprGenerator(SimpleExpr instance) {
    return new FieldValue(instance, fieldName, Type.getType(Object.class));
  }
}
