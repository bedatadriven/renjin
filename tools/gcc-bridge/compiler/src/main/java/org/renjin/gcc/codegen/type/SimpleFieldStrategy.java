package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.primitive.FieldValue;


/**
 * Strategy for fields that can be represented by a {@link SimpleExpr}
 */
public class SimpleFieldStrategy extends FieldStrategy {
  
  private Type fieldType;
  private String name;

  /**
   * @param name the name of the field
   * @param fieldType the type of the field
   */
  public SimpleFieldStrategy(String name, Type fieldType) {
    this.fieldType = fieldType;
    this.name = name;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, fieldType.getDescriptor(), null, null);
  }

  @Override
  public Expr memberExprGenerator(SimpleExpr instance) {
    return new FieldValue(instance, name, fieldType);
  }
}
