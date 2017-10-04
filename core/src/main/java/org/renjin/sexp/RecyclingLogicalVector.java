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
package org.renjin.sexp;

public class RecyclingLogicalVector extends LogicalVector implements RecyclingVector {
  private int length;
  private int baseLength;
  private LogicalVector underlying;

  public RecyclingLogicalVector(int length, LogicalVector recycled) {
    this(length, recycled, recycled.getAttributes());
  }

  private RecyclingLogicalVector(int length, LogicalVector recycled, AttributeMap attributes) {
    super(attributes);
    this.length = length;
    this.underlying = recycled;
    this.baseLength = recycled.length();
  }

  @Override
  public Logical getElementAsLogical(int index) {
    return underlying.getElementAsLogical(index % baseLength);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return underlying.getElementAsRawLogical(index % baseLength);
  }

  @Override
  public boolean isConstantAccessTime() {
    return underlying.isConstantAccessTime();
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RecyclingLogicalVector(length, underlying, attributes);
  }
}
