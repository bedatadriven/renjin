package org.renjin.gcc.gimple.type;

/**
 * Type representing complex numbers
 */
public class GimpleComplexType extends AbstractGimpleType {
  
  @Override
  public int sizeOf() {
    return getSize();
  }

  @Override
  public String toString() {
    return "complex";
  }
}
