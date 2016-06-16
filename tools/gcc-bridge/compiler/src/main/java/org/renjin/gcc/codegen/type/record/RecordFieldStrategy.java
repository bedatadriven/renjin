package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Preconditions;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.FieldStrategy;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Generates a field with a record type
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
    SimpleExpr thisRef = Expressions.thisValue(declaringClass);
    SimpleLValue fieldRef = Expressions.field(thisRef, strategy.getJvmType(), fieldName);
    
    SimpleExpr newInstance = Expressions.newObject(strategy.getJvmType());

    fieldRef.store(mv, newInstance);
  }

  @Override
  public RecordClassValueExpr memberExprGenerator(SimpleExpr instance) {
    SimpleLValue value = Expressions.field(instance, strategy.getJvmType(), fieldName);
    Expr address = value;
    
    return new RecordClassValueExpr(value, address);
  }
}
