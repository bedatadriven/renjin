package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class FieldValue implements JLValue {
  
  private JExpr instance;
  private String fieldName;
  private Type fieldType;

  public FieldValue(JExpr instance, String fieldName, Type fieldType) {
    this.instance = instance;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  @Nonnull
  @Override
  public Type getType() {
    return fieldType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    instance.load(mv);
    mv.getfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }

  @Override
  public void store(MethodGenerator mv, JExpr value) {
    instance.load(mv);
    value.load(mv);
    mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }
}
