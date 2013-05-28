package org.renjin.gcc.gimple.expr;

public class GimpleIntegerConstant extends GimpleConstant {
  private long value;

  public Long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

}
