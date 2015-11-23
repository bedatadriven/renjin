package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;

public class GimpleIntegerConstant extends GimplePrimitiveConstant {
  private long value;

  public GimpleIntegerConstant() {
  }
  
  
  public GimpleIntegerConstant(GimpleIntegerType type, long value) {
    setType(type);
    this.value = value;
  }
  
  public Long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  @Override
  public boolean isNull() {
    return getType() instanceof GimpleIndirectType && value == 0;
  }

  public static GimpleIntegerConstant nullValue(GimpleIndirectType type) {
    GimpleIntegerConstant constant = new GimpleIntegerConstant();
    constant.setValue(0);
    constant.setType(type);
    return constant;
  }

}
