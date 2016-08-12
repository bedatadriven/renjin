package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

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
  public GExpr memberExpr(final JExpr instance, final int fieldOffset, TypeStrategy expectedType) {
    
    if(expectedType != null) {
      RecordClassTypeStrategy expectedRecordType = (RecordClassTypeStrategy) expectedType;
      if(!expectedRecordType.equals(fieldTypeStrategy)) {
        throw new UnsupportedOperationException("expectedType: " + expectedType);
      }
    }
    
    JExpr superInstance = new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return fieldTypeStrategy.getJvmType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        instance.load(mv);
      }
    };
    
    return new RecordValue(superInstance, new RecordUnitPtr(superInstance));
  }

  public Type getType() {
    return fieldTypeStrategy.getJvmType();
  }
}
