package org.renjin.gcc.gimple.type;

public class GimpleBooleanType extends GimplePrimitiveType {

  @Override
  public String toString() {
    return "bool";
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GimpleBooleanType;
  }

  @Override
  public int hashCode() {
    return 1;
  }
  
  
}
