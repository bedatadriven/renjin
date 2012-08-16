package org.renjin.gcc.gimple.type;


public class GimpleStructType implements GimpleType {
  private final String name;

  public GimpleStructType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "struct " + name;
  }
}
