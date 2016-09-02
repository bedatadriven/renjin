package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

public abstract class SingleFieldStrategy extends FieldStrategy {
  
  protected final Type ownerClass;
  protected final String fieldName;
  protected final Type fieldType;

  public SingleFieldStrategy(Type ownerClass, String fieldName, Type fieldType) {
    this.ownerClass = ownerClass;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
    Preconditions.checkNotNull(fieldName);
    Preconditions.checkArgument(!fieldName.isEmpty(), "fieldName cannot be empty");
  }

  @Override
  public final void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, fieldType.getDescriptor(), null, null);
  }
  
  protected final void memsetReference(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr count) {
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, fieldName, fieldType);
  }
  
  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FieldValue sourceExpr = new FieldValue(source, fieldName, fieldType);
    FieldValue destExpr = new FieldValue(dest, fieldName, fieldType);
    destExpr.store(mv, sourceExpr);
  }
}
