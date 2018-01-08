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
package org.renjin.sexp;

public class BooleanArrayVector extends LogicalVector {
  
  private boolean array[];

  private BooleanArrayVector(AttributeMap attributes) {
    super(attributes);
  }

  public BooleanArrayVector(boolean[] array, AttributeMap attributes) {
    super(attributes);
    this.array = array;
  }

  @Override
  public int length() {
    return array.length;
  }

  @Override
  public boolean isElementNA(int index) {
    return false;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return array[index] ? 1 : 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new BooleanArrayVector(array, attributes);
  }
  
  public static BooleanArrayVector unsafe(boolean[] array) {
    BooleanArrayVector vector = new BooleanArrayVector(AttributeMap.EMPTY);
    vector.array = array;
    return vector;
  }
}
