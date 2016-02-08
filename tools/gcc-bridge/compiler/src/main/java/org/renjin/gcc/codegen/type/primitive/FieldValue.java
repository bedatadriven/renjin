package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;

import javax.annotation.Nonnull;


public class FieldValue implements SimpleLValue {
  
  private SimpleExpr instance;
  private String fieldName;
  private Type fieldType;

  public FieldValue(SimpleExpr instance, String fieldName, Type fieldType) {
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
  public void store(MethodGenerator mv, SimpleExpr value) {
    instance.load(mv);
    value.load(mv);
    mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }
}
