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

import java.util.BitSet;

/**
 * Implementation of the LogicalVector that uses
 * a BitSet as a backing storage. 
 *
 */
public class LogicalBitSetVector extends LogicalVector {

  private final BitSet bitSet;
  private final int length;
  
  public LogicalBitSetVector(BitSet bitSet, int length, AttributeMap attributes) {
    super(attributes);
    this.length = length;
    this.bitSet = (BitSet) bitSet.clone();
  }

  public LogicalBitSetVector(BitSet bitSet, int length) {
    this.bitSet = (BitSet) bitSet.clone();
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return bitSet.get(index) ? 1 : 0;
  }
  
  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public Logical getElementAsLogical(int index) {
    return bitSet.get(index) ? Logical.TRUE : Logical.FALSE;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new LogicalBitSetVector(this.bitSet, length, attributes);
  }
  
}
