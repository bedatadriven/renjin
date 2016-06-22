package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Gimple parameter to a {@link GimpleFunction}
 */
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

  /**
   * @return the parameter's type
   */
  public GimpleType getType() {
    return type;
  }

  public void setType(GimpleType type) {
    this.type = type;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * 
   * @return the name of the parameter
   */
  public String getName() {
    return name;
  }

  /**
   * 
   * @return true if this parameter is addressed within the function. Updated
   * by {@link org.renjin.gcc.analysis.AddressableFinder}
   */
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
