package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.runtime.BooleanPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.var.PrimitivePtrVar;
import org.renjin.gcc.translate.var.Variable;


public class PrimitivePtrTypeTranslator extends TypeTranslator {

  private JimpleType wrapperClass;
  private PrimitiveType primitiveType;

  public PrimitivePtrTypeTranslator(PointerType type) {
    this.primitiveType = (PrimitiveType)type.getInnerType();
    this.wrapperClass = PrimitiveTypes.getWrapperType(primitiveType);
  }

  @Override
  public JimpleType returnType() {
    return wrapperClass;
  }

  @Override
  public JimpleType paramType() {
    return wrapperClass;
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage usage) {
    return new PrimitivePtrVar(functionContext, gimpleName, primitiveType);
  }
}
