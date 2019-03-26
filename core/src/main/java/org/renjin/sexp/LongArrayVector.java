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
package org.renjin.sexp;

/**
 * Wrapper for 64-bit integer values. 
 */
public class LongArrayVector extends DoubleVector {

  private long[] values;
  
  public LongArrayVector(long value) {
    super();
    this.values = new long[] { value };
  }
  
  private LongArrayVector(long[] values, AttributeMap attributes) {
    super(attributes);
    this.values = values;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getElementAsInt(int i) {
    long value = values[i];
    if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
      return IntVector.NA;
    } 
    return (int)value;
  }
  
  @Override
  public double getElementAsDouble(int index) {
    return (double)values[index];
  }

  @Override
  public String getElementAsString(int index) {
    return Long.toString(values[index]);
  }
 
  public long getElementAsLong(int index) {
    return values[index];
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
    return new LongArrayVector(this.values, attributes);
  }

}
