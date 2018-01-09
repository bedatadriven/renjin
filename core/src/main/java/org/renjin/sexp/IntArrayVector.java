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
package org.renjin.sexp;

import org.renjin.eval.Profiler;

import java.util.Arrays;

public class IntArrayVector extends IntVector {

  public static final IntArrayVector NA_VECTOR = new IntArrayVector(IntVector.NA);

  private int[] values;

  private IntArrayVector(AttributeMap attributes) {
    super(attributes);
  }

  public IntArrayVector(int... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public IntArrayVector(IntVector vector) {
    super(vector.getAttributes());
    this.values = vector.toIntArray();
  }

  public IntArrayVector(int[] values, int length, AttributeMap attributes) {
    super(attributes);

    if (Profiler.ENABLED) {
      Profiler.memoryAllocated(Integer.SIZE, length);
    }

    this.values = Arrays.copyOf(values, length);
  }

  public IntArrayVector(int[] values, AttributeMap.Builder attributes) {
    this(values, values.length, attributes.validateAndBuildForVectorOfLength(values.length));
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
    IntArrayVector clone = new IntArrayVector(attributes);
    clone.values = values;
    return clone;
  }

  /**
   * @return a pointer to the underlying array. DO NOT MODIFY!!
   */
  public int[] toIntArrayUnsafe() {
    return values;
  }

  /**
   * Creates a new IntArrayVector from the given array, without copying.
   * {@code array} MUST NOT be subsequently modified.
   */
  public static IntArrayVector unsafe(int[] array) {
    return unsafe(array, AttributeMap.EMPTY);
  }

  /**
   * Creates a new IntArrayVector from the given array, without copying.
   * {@code array} MUST NOT be subsequently modified.
   */
  public static IntArrayVector unsafe(int[] array, AttributeMap attributes) {
    IntArrayVector vector = new IntArrayVector(attributes);
    vector.values = array;
    return vector;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
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
      Arrays.fill(values, IntVector.NA);
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

    public Builder set(int index, double value) {
      if (Double.isNaN(value)) {
        set(index, IntVector.NA);
      } else {
        set(index, (int)value);
      }
      return this;
    }

    public Builder add(int value) {
      return set(size, value);
    }

    public Builder add(double value) {
      return set(size, value);
    }

    @Override
    public Builder add(Number value) {
      if (value instanceof Integer) {
        add(value.intValue());
      } else {
        add(value.doubleValue());
      }
      return this;
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
    public Builder setAttribute(Symbol name, SEXP value) {
      return (Builder) super.setAttribute(name, value);
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
        if (newCapacity < minCapacity) {
          newCapacity = minCapacity;
        }
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(oldData, newCapacity);
        Arrays.fill(values, oldCapacity, values.length, NA);
      }
    }

    @Override
    public IntVector build() {
      if(Profiler.ENABLED) {
        Profiler.memoryAllocated(Integer.SIZE, values.length);
      }
      if(size == values.length) {
        IntArrayVector vector = new IntArrayVector(buildAttributes());
        vector.values = values;
        // subsequent edits will throw error!
        this.values = null;
        return vector;
      } else {
        return new IntArrayVector(values, size, buildAttributes());
      }
    }

    public static Builder withInitialSize(int size) {
      return new Builder(size, size);
    }

    public static Builder withInitialCapacity(int capacity) {
      return new Builder(0, capacity);
    }
  }
}
