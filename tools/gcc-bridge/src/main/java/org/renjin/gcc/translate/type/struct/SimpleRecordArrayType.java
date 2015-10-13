package org.renjin.gcc.translate.type.struct;


import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.field.SimpleRecordArrayFieldExpr;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.var.Variable;

public class SimpleRecordArrayType implements ImType {

  private final SimpleRecordType componentType;

  public SimpleRecordArrayType(SimpleRecordType componentType, int lowerBound, int upperBound) {
    this.componentType = componentType;
    if(lowerBound != 0) {
      throw new UnsupportedOperationException("lowerBound = " + lowerBound);
    }
  }

  @Override
  public JimpleType paramType() {
    return jimpleType();
  }

  @Override
  public JimpleType returnType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    JimpleFieldBuilder field = classBuilder.newField();
    field.setName(memberName);
    field.setType(jimpleType());
    if(isStatic) {
      field.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    } else {
      field.setModifiers(JimpleModifiers.PUBLIC);
    }
  }

  public JimpleType jimpleType() {
    return componentType.getJimpleType().arrayType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    return new SimpleRecordArrayFieldExpr(this, memberName, classType, instanceExpr);
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }
}
