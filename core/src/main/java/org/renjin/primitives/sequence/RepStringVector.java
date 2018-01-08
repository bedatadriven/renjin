/**
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
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class RepStringVector extends StringVector {

  public Vector source;
  private int sourceLength;
  private int length;
  private int each;

  public RepStringVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceLength = source.length();
    this.length = length;
    this.each = each;
  }
  
  private RepStringVector(String constant, int length) {
    super(AttributeMap.EMPTY);
    this.source = StringVector.valueOf(constant);
    this.sourceLength = source.length();
    this.length = length;
    this.each = 1;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new RepStringVector(source, length, each, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    sourceLength = source.length();
    return source.getElementAsString( (index / each) % sourceLength);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  public static StringVector createConstantVector(String constant,
      int length) {
    if (length <= 0) {
      return StringVector.EMPTY;
    }
    return new RepStringVector(constant, length);
  }
}
