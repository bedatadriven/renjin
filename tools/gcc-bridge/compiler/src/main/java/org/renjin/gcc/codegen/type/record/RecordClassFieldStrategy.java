package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.repackaged.asm.Type;

import static org.renjin.repackaged.asm.Type.getMethodDescriptor;

/**
 * Generates a field with a record *value* type
 */
public class RecordClassFieldStrategy extends SingleFieldStrategy {
  private RecordClassTypeStrategy strategy;

  public RecordClassFieldStrategy(RecordClassTypeStrategy strategy, Type declaringClass, String fieldName) {
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

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    // Load the field value onto the stack
    instance.load(mv);
    mv.getfield(ownerClass, fieldName, fieldType);
    
    // Invoke the field's class's memset() method
    byteValue.load(mv);
    byteCount.load(mv);
    mv.invokevirtual(fieldType, "memset", getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE), false);
  }

}
