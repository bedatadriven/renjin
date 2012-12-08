package org.renjin.gcc.translate.call;

import org.renjin.gcc.jimple.JimpleType;

/**
 * A call parameter of a JVM reference type (usually to an external JVM method)
 */
public class JvmObjectCallParam extends CallParam {
  private JimpleType paramClass;
  
  public JvmObjectCallParam(JimpleType paramType) {
    this.paramClass = paramType;
  }
  
  public JimpleType getParamClass() {
    return paramClass;
  }

  @Override
  public String toString() {
    return "JvmObjectCallParam: " + paramClass;
  }
}
