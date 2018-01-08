/*
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

import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

public class GimpleRealType extends GimplePrimitiveType {

  public GimpleRealType() {
  }

  public GimpleRealType(int precision) {
    setSize(precision);
  }

  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return getSize();
  }

  public void setPrecision(int precision) {
    Preconditions.checkArgument(precision > 0);
    setSize(precision);
  }

  @Override
  public String toString() {
    return "real" + getPrecision();
  }

  @Override
  public int hashCode() {
    return getSize();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GimpleRealType other = (GimpleRealType) obj;
    return getSize() == other.getSize();
  }

  @Override
  public int localVariableSlots() {
    return jvmType().getSize();
  }

  @Override
  public Type jvmType() {
    if(getPrecision() <= 32) {
      return Type.FLOAT_TYPE;
    } else {
      return Type.DOUBLE_TYPE;
    }
  }

  @Override
  public int sizeOf() {
    return getSize() / 8;
  }


  @Override
  public GimpleRealConstant zero() {
    return new GimpleRealConstant(this, 0);
  }
}
