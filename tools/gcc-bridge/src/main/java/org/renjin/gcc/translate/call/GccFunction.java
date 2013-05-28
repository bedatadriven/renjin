package org.renjin.gcc.translate.call;

import java.util.List;

import org.renjin.gcc.jimple.JimpleType;

public class GccFunction extends MethodRef {

  private String className;
  private String methodName;
  private JimpleType returnType;
  private List<JimpleType> parameterTypes;

  public GccFunction(String className, String methodName, JimpleType returnType, List<JimpleType> parameterTypes) {
    this.className = className;
    this.methodName = methodName;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  @Override
  public JimpleType getReturnType() {
    return returnType;
  }

  @Override
  public List<JimpleType> getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public String getDeclaringClass() {
    return className;
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public String getClassName() {
    return className;
  }
}
