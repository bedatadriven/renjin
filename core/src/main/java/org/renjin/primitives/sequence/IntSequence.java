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


import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

public class IntSequence extends IntVector {

  private int from;
  private int by;
  private int length;

  public IntSequence(int from, int by, int length) {
    this.from = from;
    this.by = by;
    this.length = length;
  }

  public IntSequence(AttributeMap attributes, int from, int by, int length) {
    super(attributes);
    this.from = from;
    this.by = by;
    this.length = length;
  }

  public int getFrom() {
    return from;
  }

  public int getBy() {
    return by;
  }

  public int getLength() {
    return length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return from + i*by;
  }

  @Override
  public boolean isElementNA(int index) {
    return false;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IntSequence(attributes, from, by, length);
  }

  public static AtomicVector fromTo(int n1, int n2) {
    if(n1 <= n2) {
      return new IntSequence(n1, 1, n2-n1+1);
    } else {
      return new IntSequence(n1, -1, n1-n2+1);
    }
  }

  public static AtomicVector fromTo(double n1, double n2) {
    return fromTo((int)n1, (int)n2);
  }
}
