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

import static org.renjin.repackaged.asm.Type.*;

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
  public GExpr memberExpr(final JExpr instance, final int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }

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

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    // call super.set()
    dest.load(mv);
    source.load(mv);
    mv.invokevirtual(fieldTypeStrategy.getJvmType(), "set", 
        getMethodDescriptor(VOID_TYPE, fieldTypeStrategy.getJvmType()), false);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr count) {
    // super.memset(byteValue, count)
    Type superType = fieldTypeStrategy.getJvmType();
    instance.load(mv);
    byteValue.load(mv);
    count.load(mv);
    mv.invokevirtual(superType, "memset", getMethodDescriptor(VOID_TYPE, INT_TYPE, INT_TYPE), false);
  }

  public Type getType() {
    return fieldTypeStrategy.getJvmType();
  }
}
