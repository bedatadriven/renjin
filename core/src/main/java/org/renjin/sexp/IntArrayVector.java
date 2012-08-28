/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

import java.util.Arrays;

public class IntArrayVector extends IntVector {

  private int[] values;

  public IntArrayVector(int... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public IntArrayVector(int[] values, int length, AttributeMap attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, length);

    if(Vector.DEBUG_ALLOC && length > 5000) {
      System.out.println("IntArrayVector alloc = " + length);
    }
  }

  public IntArrayVector(int[] values, AttributeMap attributes) {
    this(values, values.length, attributes);
  }


  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getElementAsInt(int i) {
    return values[i];
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IntArrayVector(values, values.length, attributes);
  }

  public static class Builder extends AbstractAtomicBuilder {
    private static final int MIN_INITIAL_CAPACITY = 50;
    private int values[];
    private int size;

    public Builder(int initialSize, int initialCapacity) {
      if(initialCapacity < MIN_INITIAL_CAPACITY) {
        initialCapacity = MIN_INITIAL_CAPACITY;
      }
      if(initialSize > initialCapacity) {
        initialCapacity = initialSize;
      }
      values = new int[initialCapacity];
      size = initialSize;
      Arrays.fill(values, NA);
    }
    
    public Builder(int initialSize) {
      this(initialSize, initialSize);
    }

    public Builder(IntVector vector) {
      this.values = vector.toIntArray();
      this.size = this.values.length;

      copyAttributesFrom(vector);
    }

    public Builder() {
      this(0, MIN_INITIAL_CAPACITY);
    }

    public Builder set(int index, int value) {
      ensureCapacity(index+1);
      if(index+1 > size) {
        size = index+1;
      }
      values[index] = value;
      return this;
    }

    public Builder add(int value) {
      return set(size, value);
    }
    
    @Override
    public Builder add(Number value) {
      return add(value.intValue());
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsInt(sourceIndex));
    }

    @Override
    public int length() {
      return size;
    }

    public void ensureCapacity(int minCapacity) {
      int oldCapacity = values.length;
      if (minCapacity > oldCapacity) {
        int oldData[] = values;
        int newCapacity = (oldCapacity * 3)/2 + 1;
        if (newCapacity < minCapacity)
          newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(oldData, newCapacity);
        Arrays.fill(values, oldCapacity, values.length, NA);
      }
    }

    @Override
    public IntVector build() {
      return new IntArrayVector(values, size, buildAttributes());
    }
  }
}
