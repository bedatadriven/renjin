/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Gimple parameter to a {@link GimpleFunction}
 */
public class GimpleParameter {
  private GimpleType type;
  private String name;
  private long id;
  private boolean addressable;
  
  /**
   * Compiler-assigned unique identifier for this parameter.
   */
  public long getId() {
    return id;
  }

  public void setId(long id) {
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
