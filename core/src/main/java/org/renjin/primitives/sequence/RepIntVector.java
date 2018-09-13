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
package org.renjin.primitives.sequence;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class RepIntVector extends IntVector {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepIntVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepIntVector(source, length, each, attributes);
  }

  @Override
  public int getElementAsInt(int index) {
    return source.getElementAsInt((index / each) % source.length());
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public boolean anyNA() {
    return source.anyNA();
  }

  @Override
  public int length() {
    return length;
  }
}
