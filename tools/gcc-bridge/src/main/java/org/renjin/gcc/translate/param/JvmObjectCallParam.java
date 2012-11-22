package org.renjin.gcc.translate.param;

/**
 * A call parameter of a JVM reference type (usually to an external JVM method)
 */
public class JvmObjectCallParam extends CallParam {
  private Class paramClass;
  
  public JvmObjectCallParam(Class paramClass) {
    this.paramClass = paramClass;
  }
  
  public Class getParamClass() {
    return paramClass;
  }
}
