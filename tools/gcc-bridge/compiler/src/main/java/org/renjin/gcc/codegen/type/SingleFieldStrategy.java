package org.renjin.gcc.codegen.type;

import com.google.common.base.Preconditions;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

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
  
  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FieldValue sourceExpr = new FieldValue(source, fieldName, fieldType);
    FieldValue destExpr = new FieldValue(dest, fieldName, fieldType);
    destExpr.store(mv, sourceExpr);
  }
}
