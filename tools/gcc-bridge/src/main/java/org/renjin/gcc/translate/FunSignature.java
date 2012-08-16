package org.renjin.gcc.translate;


import org.renjin.gcc.jimple.JimpleMethodRef;
import org.renjin.gcc.jimple.JimpleType;

import java.util.List;

public class FunSignature {
  private final JimpleType returnType;
  private final List<JimpleType> parameterTypes;

  public FunSignature(JimpleMethodRef ref) {
    this.returnType = ref.getReturnType();
    this.parameterTypes = ref.getParameterTypes();
  }

  public FunSignature(JimpleType returnType, List<JimpleType> parameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public JimpleType getReturnType() {
    return returnType;
  }

  public List<JimpleType> getParameterTypes() {
    return parameterTypes;
  }

  public String interfaceName() {
    StringBuilder sb = new StringBuilder("FunPtr");
    sb.append(typeAbbrev(returnType));
    for(JimpleType paramType : parameterTypes) {
      sb.append(typeAbbrev(paramType));
    }
    return sb.toString();
  }

  private String typeAbbrev(JimpleType type) {
    if(type.toString().equals("double")) {
      return "D";
    } else if(type.toString().equals("int")) {
      return "I";
    } else if(type.toString().equals("void")) {
      return "V";
    } else if(type.toString().equals("org.renjin.gcc.runtime.DoublePtr")) {
      return "d";
    } else if(type.toString().equals("org.renjin.gcc.runtime.Ptr")) {
      return "v";
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunSignature that = (FunSignature) o;

    if (!parameterTypes.equals(that.parameterTypes)) return false;
    if (!returnType.equals(that.returnType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = returnType.hashCode();
    result = 31 * result + parameterTypes.hashCode();
    return result;
  }
}
