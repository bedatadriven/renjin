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


import org.renjin.sexp.*;

/**
 */
public class DoubleSequence extends DoubleVector {

  private double from;
  private double by;
  private int length;

  public DoubleSequence(AttributeMap attributes, double from, double by, int length) {
    super(attributes);
    this.from = from;
    this.by = by;
    this.length = length;
  }

  public DoubleSequence(double from, double by, int length) {
    this.from = from;
    this.by = by;
    this.length = length;
  }

  @Override
  public double getElementAsDouble(int index) {
    return from + index * by;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleSequence(attributes, from, by, length);
  }

  public static AtomicVector fromTo(double n1, double n2) {
    if(n1 == n2) {
      return new DoubleArrayVector(n1);
    } else if(n1 <= n2) {
      return new DoubleSequence(n1, 1d, (int)Math.ceil(n2-n1));
    } else {
      return new DoubleSequence(n1, -1d, (int)(Math.floor(n1-n2)+1));
    }
  }
}
