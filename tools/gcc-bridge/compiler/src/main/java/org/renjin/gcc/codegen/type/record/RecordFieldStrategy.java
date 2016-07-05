package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Preconditions;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Type;

import static org.renjin.repackaged.asm.Opcodes.ACC_PUBLIC;

/**
 * Generates a field with a record *value* type
 */
public class RecordFieldStrategy extends FieldStrategy {
  private String fieldName;
  private RecordClassTypeStrategy strategy;
  private Type declaringClass;

  public RecordFieldStrategy(RecordClassTypeStrategy strategy, Type declaringClass, String fieldName) {
    this.declaringClass = declaringClass;
    Preconditions.checkNotNull(fieldName);
    Preconditions.checkArgument(!fieldName.isEmpty(), "fieldName cannot be empty");
    
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
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr thisRef = Expressions.thisValue(declaringClass);
    JLValue fieldRef = Expressions.field(thisRef, strategy.getJvmType(), fieldName);
    
    JExpr newInstance = Expressions.newObject(strategy.getJvmType());

    fieldRef.store(mv, newInstance);
  }

  @Override
  public RecordValue memberExpr(JExpr instance, GimpleType expectedType) {
    JLValue value = Expressions.field(instance, strategy.getJvmType(), fieldName);
    RecordUnitPtr address = new RecordUnitPtr(value);
    
    return new RecordValue(value, address);
  }
}
