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
package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;

/**
 * Lazy double vector that computes the imaginary part of each element 
 * in a complex vector.
 */
public class ImaginaryVector extends DoubleVector {

  private ComplexVector vector;

  public ImaginaryVector(ComplexVector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ImaginaryVector(vector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return vector.getElementAsComplexIm(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }

  @Override
  public int length() {
    return vector.length();
  }
}
