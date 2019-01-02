/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

public abstract class AbstractGimpleType implements GimpleType {
  private int size;

  /**
   * 
   * @return the size of values of this type, in bits
   */
  @Override
  public final int getSize() {
    return size;
  }

  public void setSize(long size) {
    if(size > Integer.MAX_VALUE) {
      // The only time the size will be this large is for variable-length arrays
      // which get pruned anyway
      this.size = Integer.MAX_VALUE;
    } else {
      this.size = (int) size;
    }
  }

  @Override
  public boolean isPointerTo(Class<? extends GimpleType> clazz) {
    return false;
  }

  @Override
  public <X extends GimpleType> X getBaseType() {
    throw new UnsupportedOperationException("this is not pointer type (" + getClass().getSimpleName() + ")");
  }

  @Override
  public GimplePointerType pointerTo() {
    return new GimplePointerType(this);
  }

}
