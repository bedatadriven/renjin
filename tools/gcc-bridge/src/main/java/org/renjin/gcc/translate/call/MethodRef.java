package org.renjin.gcc.translate.call;

import java.util.List;

import org.renjin.gcc.jimple.JimpleType;

import com.google.common.collect.Lists;

public abstract class MethodRef {

  public abstract JimpleType getReturnType();

  public abstract List<JimpleType> getParameterTypes();

  public abstract String getDeclaringClass();

  public String signature() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    sb.append(getDeclaringClass());
    sb.append(": ");
    sb.append(getReturnType());
    sb.append(" ");
    sb.append(getMethodName());
    sb.append("(");
    boolean needsComma = false;
    for (JimpleType paramType : getParameterTypes()) {
      if (needsComma) {
        sb.append(", ");
      }
      sb.append(paramType);
      needsComma = true;
    }
    sb.append(")>");
    return sb.toString();

  }

  public List<CallParam> getParams() {
    List<CallParam> params = Lists.newArrayList();
    for (JimpleType paramType : getParameterTypes()) {
      params.add(new SimpleParam(paramType));
    }
    return params;
  }


  public abstract String getMethodName();

  public abstract String getClassName();
}
