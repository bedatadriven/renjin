package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.LongPtr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
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

  @Override
  public JimpleType paramType() {
    return baseType.getPointerWrapperType();
  }


  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new PrimitivePtrVar(functionContext, gimpleName, this);
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
