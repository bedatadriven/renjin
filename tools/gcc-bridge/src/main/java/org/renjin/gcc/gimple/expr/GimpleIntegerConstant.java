package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleIndirectType;

public class GimpleIntegerConstant extends GimplePrimitiveConstant {
  private long value;

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
}
