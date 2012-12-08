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
    for(JimpleType paramType : getParameterTypes()) {
      if(needsComma) {
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
    for(JimpleType paramType : getParameterTypes()) {
      if(paramType.isPrimitive()) {
        params.add(new PrimitiveCallParam(paramType.asPrimitiveClass()));
      } else if(paramType.isPointerWrapper()) {
        params.add(new WrappedPtrCallParam());
      } else if(paramType.isFunctionPointer()) {
        params.add(new FunPtrCallParam());
      } else {
        params.add(new JvmObjectCallParam(paramType));
      }
    }
    return params;
  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
//
//    MethodRef that = (MethodRef) o;
//
//    if (!className.equals(that.className)) return false;
//    if (!methodName.equals(that.methodName)) return false;
//    if (!parameterTypes.equals(that.parameterTypes)) return false;
//    if (!returnType.equals(that.returnType)) return false;
//
//    return true;
//  }
//
//  @Override
//  public int hashCode() {
//    int result = className.hashCode();
//    result = 31 * result + methodName.hashCode();
//    result = 31 * result + returnType.hashCode();
//    result = 31 * result + parameterTypes.hashCode();
//    return result;
//  }
//
//  @Override
//  public String toString() {
//    return returnType + " " + className + "." + methodName + "(" + Joiner.on(", ").join(parameterTypes) + ")";
//  }
  
  public abstract String getMethodName();

  public abstract String getClassName();
}
