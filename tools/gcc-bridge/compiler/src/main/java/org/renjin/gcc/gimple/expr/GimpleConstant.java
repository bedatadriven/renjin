package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleType;

public abstract class GimpleConstant extends GimpleExpr {
  private GimpleType type;

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public boolean isNull() {
    return false;
  }

}
