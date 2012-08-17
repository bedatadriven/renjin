package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.runtime.BooleanPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.NumericPtrVar;
import org.renjin.gcc.translate.var.Variable;


public class NumericPtrTypeTranslator extends TypeTranslator {

  private Class wrapperClass;
  private PrimitiveType primitiveType;

  public NumericPtrTypeTranslator(PointerType type) {
    this.primitiveType = (PrimitiveType)type.getInnerType();
    switch ((PrimitiveType) type.getInnerType()) {

      case DOUBLE_TYPE:
        wrapperClass = DoublePtr.class;
        break;
      case INT_TYPE:
        wrapperClass = IntPtr.class;
        break;
      case BOOLEAN:
        wrapperClass = BooleanPtr.class;
        break;
      case VOID_TYPE:
        wrapperClass = Ptr.class;
        break;

      default:
        throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public JimpleType returnType() {
    return new JimpleType(wrapperClass);
  }

  @Override
  public JimpleType paramType() {
    return new JimpleType(wrapperClass);
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName) {
    return new NumericPtrVar(functionContext, gimpleName, primitiveType);
  }
}
