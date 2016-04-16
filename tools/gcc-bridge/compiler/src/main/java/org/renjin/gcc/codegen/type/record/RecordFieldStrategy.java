package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Generates a field with a record type
 */
public class RecordFieldStrategy extends FieldStrategy {
  private String fieldName;
  private RecordClassTypeStrategy strategy;

  public RecordFieldStrategy(RecordClassTypeStrategy strategy, String fieldName) {
    this.fieldName = fieldName;
    this.strategy = strategy;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, strategy.getJvmType().getDescriptor(), null, null).visitEnd();
  }

  @Override
  public Expr memberExprGenerator(SimpleExpr instance) {
    return Expressions.field(instance, strategy.getJvmType(), fieldName);
  }
}
