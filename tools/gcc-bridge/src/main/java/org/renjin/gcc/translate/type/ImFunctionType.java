package org.renjin.gcc.translate.type;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.SyntheticJimpleType;
import org.renjin.gcc.translate.FunPtrTable;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.VarUsage;
import org.renjin.gcc.translate.call.CallParam;
import org.renjin.gcc.translate.call.GccFunction;
import org.renjin.gcc.translate.call.MethodRef;
import org.renjin.gcc.translate.var.Variable;

import java.util.List;

/**
 * An intermediate representation of a function type.
 */
public class ImFunctionType implements ImType {
  private final JimpleType returnType;
  private final List<JimpleType> parameterTypes;

  public ImFunctionType(MethodRef ref) {
    this.returnType = ref.getReturnType();
    this.parameterTypes = ref.getParameterTypes();
  }

  public ImFunctionType(JimpleType returnType, List<JimpleType> parameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public JimpleType getReturnType() {
    return returnType;
  }

  public List<JimpleType> getParameterTypes() {
    return parameterTypes;
  }

  public String jimpleSignature() {
    return methodRef().signature();
  }

  private GccFunction methodRef() {
    return new GccFunction(FunPtrTable.PACKAGE_NAME + "." + interfaceName(), "apply", getReturnType(),
        getParameterTypes());
  }

  public List<CallParam> getParams() {
    return methodRef().getParams();
  }

  public String interfaceName() {
    StringBuilder sb = new StringBuilder("FunPtr");
    sb.append(typeAbbrev(returnType));
    for (JimpleType paramType : parameterTypes) {
      sb.append(typeAbbrev(paramType));
    }
    return sb.toString();
  }

  private String typeAbbrev(JimpleType type) {
    if (type.toString().equals("double")) {
      return "D";
    } else if (type.toString().equals("int")) {
      return "I";
    } else if (type.toString().equals("void")) {
      return "V";
    } else if (type.toString().equals("org.renjin.gcc.runtime.DoublePtr")) {
      return "d";
    } else if (type.toString().equals("org.renjin.gcc.runtime.Ptr")) {
      return "v";
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ImFunctionType that = (ImFunctionType) o;

    if (!parameterTypes.equals(that.parameterTypes))
      return false;
    if (!returnType.equals(that.returnType))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = returnType.hashCode();
    result = 31 * result + parameterTypes.hashCode();
    return result;
  }

  @Override
  public JimpleType paramType() {
    throw new UnsupportedOperationException("Function values cannot be passed as parameters, " +
        "only function pointers.");
  }

  @Override
  public JimpleType returnType() {
    throw new UnsupportedOperationException("Function values cannot be return values, " +
        "only function pointers.");
  }

  @Override
  public Variable createLocalVariable(FunctionContext functionContext, String gimpleName, VarUsage varUsage) {
    throw new UnsupportedOperationException("Function values cannot be local variables, " +
        "only function pointers.");
  }

  @Override
  public ImType pointerType() {
    return new ImFunctionPtrType(this);
  }

  @Override
  public ImType arrayType(Integer lowerBound, Integer upperBound) {
    throw new UnsupportedOperationException();
  }

  public JimpleType interfaceType() {
    return new SyntheticJimpleType(FunPtrTable.PACKAGE_NAME + "." + interfaceName());
  }
}
