package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.FieldStrategy;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Generates a field with a record type
 */
public class RecordFieldStrategy extends FieldStrategy {
  private String className;
  private String fieldName;
  private RecordClassTypeStrategy strategy;

  public RecordFieldStrategy(String className, String fieldName, RecordClassTypeStrategy strategy) {
    this.className = className;
    this.fieldName = fieldName;
    this.strategy = strategy;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    emitField(ACC_PUBLIC, cv);
  }

  private void emitField(int access, ClassVisitor cv) {
    cv.visitField(access, fieldName, strategy.getJvmType().getDescriptor(), null, null).visitEnd();
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    throw new InternalCompilerException("todo");
  }

}
