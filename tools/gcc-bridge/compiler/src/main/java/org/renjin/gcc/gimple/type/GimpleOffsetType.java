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

/**
 * This node is used to represent a data member; for example a pointer-to-data-member is
 * represented by a POINTER_TYPE whose TREE_TYPE is an OFFSET_TYPE. For a data member
 * X::m the TYPE_OFFSET_BASETYPE is X and the TREE_TYPE is the type of m.
 */
public class GimpleOffsetType extends AbstractGimpleType implements GimpleIndirectType {

  private GimpleType baseType;
  private GimpleType offsetBaseType;

  @Override
  public GimpleConstant nullValue() {
    return GimpleIntegerConstant.nullValue(this);
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    return (X)baseType;
  }

  public void setBaseType(GimpleType baseType) {
    this.baseType = baseType;
  }

  public GimpleType getOffsetBaseType() {
    return offsetBaseType;
  }

  public void setOffsetBaseType(GimpleType offsetBaseType) {
    this.offsetBaseType = offsetBaseType;
  }

  @Override
  public int sizeOf() {
    // We require the generated gimple to be compiled for 32-bit platforms so we get 32 bit pointers.
    return 4;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleOffsetType that = (GimpleOffsetType) o;

    return baseType.equals(that.baseType) &&
           offsetBaseType.equals(that.offsetBaseType);
  }

  @Override
  public int hashCode() {
    int result = baseType.hashCode();
    result = 31 * result + offsetBaseType.hashCode();
    return result;
  }
}
