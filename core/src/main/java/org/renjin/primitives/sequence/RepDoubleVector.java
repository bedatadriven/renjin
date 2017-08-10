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
package org.renjin.primitives.sequence;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.Arrays;

public class RepDoubleVector extends DoubleVector implements DeferredComputation {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepDoubleVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }
  
  private RepDoubleVector(double constant, int length) {
    super(AttributeMap.EMPTY);
    this.source = DoubleVector.valueOf(constant);
    this.length = length;
    this.each = 1;
  }

  public static DoubleVector createConstantVector(double constant, int length) {
    if (length <= 0) {
      return DoubleVector.EMPTY;
    }
    if (length < LENGTH_THRESHOLD) {
      double array[] = new double[length];
      Arrays.fill(array, constant);
      return new DoubleArrayVector(array);
    
    } else {
      return new RepDoubleVector(constant, length);
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepDoubleVector(source, length, each, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return source.getElementAsDouble((index / each) % source.length());
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, new IntArrayVector(length/each/source.length()), new IntArrayVector(each) };
  }

  @Override
  public String getComputationName() {
    return "rep";
  }
}
