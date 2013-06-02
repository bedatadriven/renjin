package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.FunPtrVar;
import org.renjin.gcc.translate.var.Variable;

public class ImFunctionPtrType implements ImType {

  private ImFunctionType baseType;

  public ImFunctionPtrType(ImFunctionType type) {
    this.baseType = type;
  }

  @Override
  public JimpleType returnType() {
    return baseType.interfaceType();
  }

  @Override
  public JimpleType paramType() {
    return baseType.interfaceType();
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new FunPtrVar(functionContext, gimpleName, this);
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }

  public JimpleType interfaceType() {
    return baseType.interfaceType();
  }

  public ImFunctionType baseType() {
    return baseType;
  }
}
