package org.renjin.gcc.gimple.expr;

import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public abstract class GimpleConstant extends GimpleExpr {
  private GimpleType type;

  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public abstract Object getValue();

  public Number getNumberValue() {
    if (getValue() instanceof Number) {
      return (Number) getValue();
    } else {
      throw new UnsupportedOperationException("Can't coerce constant to number: " + getValue());
    }
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
