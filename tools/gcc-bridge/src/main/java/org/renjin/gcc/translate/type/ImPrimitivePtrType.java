package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.field.PrimitivePtrFieldExpr;
import org.renjin.gcc.translate.var.PrimitivePtrVar;
import org.renjin.gcc.translate.var.Variable;

public class ImPrimitivePtrType implements ImIndirectType {

  private ImPrimitiveType baseType;

  public ImPrimitivePtrType(ImPrimitiveType baseType) {
    this.baseType = baseType;
  }

  @Override
  public JimpleType returnType() {
    return baseType.getPointerWrapperType();
  }
  
  public ImPrimitiveType baseType() {
    return baseType;
  }

  @Override
  public void defineField(JimpleClassBuilder classBuilder, String memberName, boolean isStatic) {
    JimpleFieldBuilder arrayField = classBuilder.newField();
    arrayField.setName(memberName);
    arrayField.setType(baseType.jimpleArrayType());
    if(isStatic) {
      arrayField.setModifiers(JimpleModifiers.PUBLIC);
    } else {
      arrayField.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    }
    
    JimpleFieldBuilder offsetField = classBuilder.newField();
    offsetField.setName(indexMemberName(memberName));
    offsetField.setType(JimpleType.INT);
    if(isStatic) {
      offsetField.setModifiers(JimpleModifiers.PUBLIC);
    } else {
      offsetField.setModifiers(JimpleModifiers.PUBLIC, JimpleModifiers.STATIC);
    }
  }

  public String indexMemberName(String memberName) {
    return memberName + "_index";
  }

  @Override
  public JimpleType paramType() {
    return baseType.getPointerWrapperType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new PrimitivePtrVar(functionContext, gimpleName, this);
  }

  @Override
  public ImExpr createFieldExpr(String instanceExpr, JimpleType classType, String memberName) {
    return new PrimitivePtrFieldExpr(instanceExpr, classType, memberName, this);
  }

  @Override
  public ImType pointerType() {
    return new ImPointerType(this);
  }

  public ImPrimitiveType getBaseType() {
    return baseType;
  }

  public Class getArrayClass() {
    return baseType.getArrayClass();
  }

  @Override
  public JimpleType getWrapperType() {
    return baseType.getPointerWrapperType();
  }

  @Override
  public JimpleType getArrayType() {
    return new RealJimpleType(getArrayClass());
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException(this.toString());
  }

  @Override
  public String toString() {
    return baseType + "*";
  }
}
