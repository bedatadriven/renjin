package org.renjin.gcc.translate.call;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;

/**
 * Call parameter of simple primitive type (double, int, etc)
 */
public class PrimitiveCallParam extends CallParam {

  private final Class type;

  public PrimitiveCallParam(Class type) {
    this.type = type;
  }

  public JimpleType getType() {
    return new RealJimpleType(type);
  }
  
  @Override
  public String toString() {
    return "PrimitiveCallParam: " + type;
  }
}
