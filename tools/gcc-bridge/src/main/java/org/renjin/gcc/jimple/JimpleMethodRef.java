package org.renjin.gcc.jimple;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class JimpleMethodRef {

  private final String className;
  private final String methodName;
  private final JimpleType returnType;
  private final List<JimpleType> parameterTypes;

  public JimpleMethodRef(Method method) {
    this.className = method.getDeclaringClass().getName();
    this.methodName = method.getName();
    this.returnType = new JimpleType(method.getReturnType());
    this.parameterTypes = Lists.newArrayList();
    for(Type type : method.getParameterTypes()) {
      parameterTypes.add(new JimpleType(type));
    }
  }

  public JimpleMethodRef(String className, String methodName, JimpleType returnType, List<JimpleType> parameterTypes) {
    this.className = className;
    this.methodName = methodName;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public JimpleType getReturnType() {
    return returnType;
  }

  public List<JimpleType> getParameterTypes() {
    return parameterTypes;
  }

  public String signature() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    sb.append(className);
    sb.append(": ");
    sb.append(returnType);
    sb.append(" ");
    sb.append(methodName);
    sb.append("(");
    boolean needsComma = false;
    for(JimpleType paramType : parameterTypes) {
      if(needsComma) {
        sb.append(", ");
      }
      sb.append(paramType);
      needsComma = true;
    }
    sb.append(")>");
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JimpleMethodRef that = (JimpleMethodRef) o;

    if (!className.equals(that.className)) return false;
    if (!methodName.equals(that.methodName)) return false;
    if (!parameterTypes.equals(that.parameterTypes)) return false;
    if (!returnType.equals(that.returnType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = className.hashCode();
    result = 31 * result + methodName.hashCode();
    result = 31 * result + returnType.hashCode();
    result = 31 * result + parameterTypes.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return returnType + " " + className + "." + methodName + "(" + Joiner.on(", ").join(parameterTypes) + ")";
  }
}
