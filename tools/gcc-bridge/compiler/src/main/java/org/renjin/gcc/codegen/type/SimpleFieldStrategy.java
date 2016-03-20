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
  
  private Type type;
  private String name;

  public SimpleFieldStrategy(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, type.getDescriptor(), null, null);
  }

  @Override
  public Expr memberExprGenerator(SimpleExpr instance) {
    return new FieldValue(instance, name, type);
  }
}
