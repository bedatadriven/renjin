package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;

/**
 * Since empty records have no data, we don't need a field for them.
 */
public class EmptyRecordField extends FieldStrategy {
  @Override
  public void writeFields(ClassVisitor cv) {
    // NOOP
  }

  @Override
  public Expr memberExprGenerator(SimpleExpr instance) {
    return Expressions.nullRef(Type.getType(Object.class));
  }

}
