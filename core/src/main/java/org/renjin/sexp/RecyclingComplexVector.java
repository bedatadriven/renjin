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

import org.apache.commons.math.complex.Complex;

public class RecyclingComplexVector extends ComplexVector implements RecyclingVector {
  private int length;
  private int baseLength;
  private ComplexVector underlying;

  public RecyclingComplexVector(int length, ComplexVector recycled) {
    this(length, recycled, recycled.getAttributes());
  }

  private RecyclingComplexVector(int length, ComplexVector recycled, AttributeMap attributes) {
    super(attributes);
    this.length = length;
    this.underlying = recycled;
    this.baseLength = recycled.length();
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return underlying.getElementAsComplex(index % baseLength);
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
    return new RecyclingComplexVector(length, underlying, attributes);
  }
}
