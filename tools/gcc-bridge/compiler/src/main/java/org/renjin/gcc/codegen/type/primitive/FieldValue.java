package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Var;


public class FieldValue implements Var {
  
  private Value instance;
  private String fieldName;
  private Type fieldType;

  public FieldValue(Value instance, String fieldName, Type fieldType) {
    this.instance = instance;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  @Override
  public Type getType() {
    return fieldType;
  }

  @Override
  public void load(MethodGenerator mv) {
    instance.load(mv);
    mv.getfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }

  @Override
  public void store(MethodGenerator mv, Value value) {
    instance.load(mv);
    value.load(mv);
    mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }
}
