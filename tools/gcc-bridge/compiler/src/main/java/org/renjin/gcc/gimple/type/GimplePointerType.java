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
package org.renjin.gcc.gimple.type;

import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;

public class GimplePointerType extends AbstractGimpleType implements GimpleIndirectType {
  
  public static final int SIZE = 32;
  
  private GimpleType baseType;

  public GimplePointerType() {
    
  }
  
  public GimplePointerType(GimpleType baseType) {
    this.baseType = baseType;
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    return (X) baseType;
  }

  @Override
  public int sizeOf() {
    // We require the generated gimple to be compiled for 32-bit platforms so we get 32 bit pointers.
    return 4;
  }

  @Override
  public GimpleConstant nullValue() {
    return GimpleIntegerConstant.nullValue(this);
  }

  public void setBaseType(GimpleType baseType) {
    this.baseType = baseType;
  }

  @Override
  public String toString() {
    return baseType.toString() + "*";
  }

  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return clazz.isAssignableFrom(baseType.getClass());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimplePointerType that = (GimplePointerType) o;

    return baseType.equals(that.baseType);

  }

  @Override
  public int hashCode() {
    return baseType.hashCode();
  }


}
