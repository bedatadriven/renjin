package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.repackaged.guava.base.Preconditions;

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
    SimpleAddressableExpr field = memberExprGenerator(thisRef);
    SimpleExpr newInstance = Expressions.newObject(strategy.getJvmType());

    field.store(mv, newInstance);
  }

  @Override
  public SimpleAddressableExpr memberExprGenerator(SimpleExpr instance) {
    SimpleLValue value = Expressions.field(instance, strategy.getJvmType(), fieldName);
    Expr address = value;
    
    return new SimpleAddressableExpr(value, address);
  }
}
