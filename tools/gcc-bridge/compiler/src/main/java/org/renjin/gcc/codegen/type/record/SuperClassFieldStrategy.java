package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;

/**
 * Models a field at the beginning of a record as a JVM superclass
 */
public class SuperClassFieldStrategy extends FieldStrategy {
  
  private RecordClassTypeStrategy fieldTypeStrategy;

  public SuperClassFieldStrategy(RecordClassTypeStrategy fieldTypeStrategy) {
    this.fieldTypeStrategy = fieldTypeStrategy;
  }


  @Override
  public void writeFields(ClassVisitor cv) {
    // NOOP 
  }

  @Override
  public GExpr memberExprGenerator(JExpr instance) {
    return new RecordValue(instance, new RecordUnitPtr(instance));
  }

  public Type getType() {
    return fieldTypeStrategy.getJvmType();
  }
}
