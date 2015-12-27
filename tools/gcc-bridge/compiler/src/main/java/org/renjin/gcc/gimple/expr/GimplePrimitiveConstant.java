package org.renjin.gcc.gimple.expr;

/**
 * Superclass of GimpleConstants storing a primitive value
 */
public abstract class GimplePrimitiveConstant extends GimpleConstant {

  public abstract Number getValue();

  public Number getNumberValue() {
    return getValue();
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
