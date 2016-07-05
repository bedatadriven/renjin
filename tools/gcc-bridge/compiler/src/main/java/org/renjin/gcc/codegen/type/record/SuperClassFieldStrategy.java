package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Type;

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
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    return new RecordValue(instance, new RecordUnitPtr(instance));
  }

  public Type getType() {
    return fieldTypeStrategy.getJvmType();
  }
}
