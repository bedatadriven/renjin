package org.renjin.gcc.translate.type;

import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
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

public class ImPrimitivePtrType implements ImType {

  private ImPrimitiveType baseType;

  public ImPrimitivePtrType(ImPrimitiveType baseType) {
    this.baseType = baseType;
  }

  @Override
  public JimpleType returnType() {
    return getWrapperJimpleType();
  }

  @Override
  public JimpleType paramType() {
    return getWrapperJimpleType();
  }

  public Class getWrapperClass() {
    switch (baseType) {
      case DOUBLE:
        return DoublePtr.class;
      case INT:
        return IntPtr.class;
      case LONG:
        return LongPtr.class;
      case CHAR:
        return CharPtr.class;
    }
    throw new UnsupportedOperationException("not implemented: " + baseType);
  }

  public JimpleType getWrapperJimpleType() {
    return new RealJimpleType(getWrapperClass());
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new PrimitivePtrVar(functionContext, gimpleName, this);
  }

  @Override
  public ImType pointerType() {
    throw new UnsupportedOperationException("not implemented (Multiple levels of indirection not yet implemented");
  }

  public ImPrimitiveType getBaseType() {
    return baseType;
  }

  public Class getArrayClass() {
    return baseType.getArrayClass();
  }

  public JimpleType getArrayType() {
    return new RealJimpleType(getArrayClass());
  }
}
