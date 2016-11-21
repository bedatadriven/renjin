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

import java.util.Objects;

public class GimpleArrayType extends AbstractGimpleType {
  private GimpleType componentType;
  private int lbound;
  private Integer ubound;

  public GimpleArrayType() {
  }

  public GimpleArrayType(GimplePrimitiveType componentType) {
    this.componentType = componentType;
  }
  

  public GimpleType getComponentType() {
    return componentType;
  }

  public void setComponentType(GimpleType componentType) {
    this.componentType = componentType;
  }

  public int getLbound() {
    return lbound;
  }

  public void setLbound(int lbound) {
    this.lbound = lbound;
  }

  public Integer getUbound() {
    return ubound;
  }

  public void setUbound(Integer ubound) {
    this.ubound = ubound;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder(componentType.toString());
    s.append("[");
    if(lbound != 0) {
      s.append(lbound).append(":");
    }
    if(ubound != null) {
      s.append(ubound);
    }
    s.append("]");
    return s.toString();
  }
  

  public int getElementCount() {
    return getSize() / componentType.getSize();
  }
  
  public boolean isStatic() {
    return true;
  }


  @Override
  public int sizeOf() {
    return getSize() / 8;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimpleArrayType other = (GimpleArrayType) o;

    if (lbound != other.lbound) {
      return false;
    }
    if (!Objects.equals(componentType, other.componentType)) {
      return false;
    }
    if (!Objects.equals(ubound, other.ubound)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = componentType.hashCode();
    result = 31 * result + lbound;
    result = 31 * result + (ubound != null ? ubound.hashCode() : 0);
    return result;
  }
}
