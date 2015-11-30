package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleParameter {
  private GimpleType type;
  private String name;
  private int id;
  private boolean addressable;
  
  /**
   * Compiler-assigned unique identifier for this parameter.
   */
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public GimpleType getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public boolean isAddressable() {
    return addressable;
  }

  public void setAddressable(boolean addressable) {
    this.addressable = addressable;
  }

  @Override
  public String toString() {
    return type + " " + name;
  }
}
