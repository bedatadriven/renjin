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

import org.renjin.eval.Profiler;

import java.util.Arrays;
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
    this(bitSet, length, attributes, true);
  }

  private LogicalBitSetVector(BitSet bitSet, int length, AttributeMap attributes, boolean copy) {
    super(attributes);
    this.length = length;
    this.bitSet = copy ? (BitSet) bitSet.clone() : bitSet;
  }

  /**
   * Creates a new LogicalBitSetVector from a {@link BitSet} instance, without creating a copy. The provided
   * {@code bitset} must NOT be subsequently modified.
   */
  public static LogicalBitSetVector unsafe(BitSet bitSet, int length, AttributeMap attributeMap) {
    return new LogicalBitSetVector(bitSet, length, attributeMap, false);
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
  public boolean isElementNA(int index) {
    return false;
  }

  @Override
  public boolean isElementTrue(int index) {
    return bitSet.get(index);
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

  public static class Builder
      extends AbstractAtomicBuilder {

    private BitSet bitSet = new BitSet();
    private int size;

    public Builder(int initialSize) {
      size = initialSize;
    }

    public Builder add(int value) {
      return set(size, value);
    }

    public Builder add(boolean value) {
      return set(size, value);
    }

    public Builder add(Number value) {
      return add(value.intValue() != 0 ? 1 : 0);
    }

    public Builder set(int index, boolean value) {
      bitSet.set(index, value);
      if(index+1 > size) {
        size = index+1;
      }
      return this;
    }

    public Builder set(int index, int value) {
      if(IntVector.isNA(value)) {
        throw new IllegalArgumentException("LogicalBitSetVector cannot accept NA values");
      }
      return set(index, value != 0);
    }

    public Builder set(int index, Logical value) {
      return set(index, value.getInternalValue());
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsRawLogical(sourceIndex));
    }

    @Override
    public int length() {
      return size;
    }

    @Override
    public LogicalVector build() {
      return new LogicalBitSetVector(bitSet, size, buildAttributes(), false);
    }
  }
}
