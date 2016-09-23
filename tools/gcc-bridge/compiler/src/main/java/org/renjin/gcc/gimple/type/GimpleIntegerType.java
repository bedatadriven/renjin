/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.repackaged.asm.Type;

public class GimpleIntegerType extends GimplePrimitiveType {
  private boolean unsigned;
  
  public GimpleIntegerType() {
    
  }
  
  public GimpleIntegerType(int precision) {
    setSize(precision);
  }

  
  
  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return getSize();
  }
  
  public boolean isUnsigned() {
    return unsigned;
  }

  public void setUnsigned(boolean unsigned) {
    this.unsigned = unsigned;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    if (unsigned) {
      s.append("unsigned ");
    }
    s.append("int" + getPrecision());
    return s.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getSize();
    result = prime * result + (unsigned ? 1231 : 1237);
    return result;
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
    GimpleIntegerType other = (GimpleIntegerType) obj;
    if (getPrecision() != other.getPrecision()) {
      return false;
    }
    if (unsigned != other.unsigned) {
      return false;
    }
    return true;
  }

  @Override
  public int localVariableSlots() {
    if(getPrecision() > 32) {
      return 2;
    } else {
      return 1;
    }
  }

  @Override
  public Type jvmType() {
    if(getPrecision() == 64) {
      return Type.LONG_TYPE;
      
    } else if(getPrecision() == 8) {
      return Type.BYTE_TYPE;

    } else if(getPrecision() == 16) {
      if(unsigned) {
        return Type.CHAR_TYPE;
      } else {
        return Type.SHORT_TYPE;
      }
      
    } else {
      return Type.INT_TYPE;
    }
  }

  @Override
  public int sizeOf() {
    return Math.max(1, getSize() / 8);
  }
  
  public static GimpleIntegerType unsigned(int bits) {
    GimpleIntegerType type = new GimpleIntegerType(bits);
    type.unsigned = true;
    return type;
  }
  
  
}
