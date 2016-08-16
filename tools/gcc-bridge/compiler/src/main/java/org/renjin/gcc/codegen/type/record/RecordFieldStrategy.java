package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.repackaged.asm.Type;

/**
 * Generates a field with a record *value* type
 */
public class RecordFieldStrategy extends SingleFieldStrategy {
  private RecordClassTypeStrategy strategy;

  public RecordFieldStrategy(RecordClassTypeStrategy strategy, Type declaringClass, String fieldName) {
    super(declaringClass, fieldName, strategy.getJvmType());
    this.strategy = strategy;
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr thisRef = Expressions.thisValue(this.ownerClass);
    JLValue fieldRef = Expressions.field(thisRef, strategy.getJvmType(), fieldName);
    
    JExpr newInstance = Expressions.newObject(strategy.getJvmType());

    fieldRef.store(mv, newInstance);
  }

  @Override
  public RecordValue memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }

    JLValue value = Expressions.field(instance, strategy.getJvmType(), fieldName);
    RecordUnitPtr address = new RecordUnitPtr(value);
    
    return new RecordValue(value, address);
  }
}
