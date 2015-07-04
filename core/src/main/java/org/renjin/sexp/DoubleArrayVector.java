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
import java.util.Collection;


public final class DoubleArrayVector extends DoubleVector {

  public static DoubleArrayVector ZERO = new DoubleArrayVector(0);

  public static DoubleArrayVector ONE = new DoubleArrayVector(1);

  private double[] values;

  private DoubleArrayVector(AttributeMap attributes) {
    super(attributes);
  }

  public static DoubleArrayVector valueOf(double x) {
    if(x == 0) {
      return ZERO;
    } else if(x == 1) {
      return ONE;
    } else {
      return new DoubleArrayVector(x);
    }
  }

  public DoubleArrayVector(double... values) {
    this(values, AttributeMap.EMPTY);
  }

  public DoubleArrayVector(double[] values, AttributeMap attributes) {
    this(values, values.length, attributes);
  }

  public DoubleArrayVector(double[] values, int length, AttributeMap attributes) {
    this(attributes);
    this.values = Arrays.copyOf(values, length);
    if(Vector.DEBUG_ALLOC && length >= 5000) {
      System.out.println("new double array length = " + length);
    }
  }

  public DoubleArrayVector(Collection<Double> values) {
    this.values = new double[values.size()];
    int i = 0;
    for(Double value : values) {
      this.values[i++] = value;
    }
  }

  /**
   * Creates a new DoubleArrayVector by wrapping an existing
   * array, without copying. The array provided CAN NOT BE SUBSEQUENTLY
   * MODIFIED.
   */
  public static DoubleArrayVector unsafe(double[] array) {
    return unsafe(array, AttributeMap.EMPTY);
  }

  /**
   * Creates a new DoubleArrayVector by wrapping an existing
   * array, without copying. The array provided CAN NOT BE SUBSEQUENTLY
   * MODIFIED.
   */
  public static DoubleArrayVector unsafe(double[] array, AttributeMap attributes) {
    DoubleArrayVector vector = new DoubleArrayVector(attributes);
    vector.values = array;
    return vector;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    DoubleArrayVector clone = new DoubleArrayVector(attributes);
    clone.values = values;
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    return values[index];
  }

  @Override
  public int length() {
    return values.length;
  }


  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder(initialSize, initialSize);
  }
  
  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new Builder(0, initialCapacity);
  }

  public static DoubleVector newMatrix(double[] values, int nRows, int nCols) {
    return new DoubleArrayVector(values, AttributeMap.builder().setDim(nRows, nCols).build());
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }
  
  @Override
  public boolean isElementNaN(int i) {
    return Double.isNaN(values[i]);
  }

  @Override
  public String toString() {
    return toString(this);
  }

  /**
   * @return a pointer to the underlying array. DO NOT MODIFY!!
   */
  public double[] toDoubleArrayUnsafe() {
    return values;
  }

  @Override
  public double[] toDoubleArray() {
    return Arrays.copyOf(this.values, this.values.length);
  }


  public static class Builder extends AbstractAtomicBuilder {
    private static final int MIN_INITIAL_CAPACITY = 50;
    private double values[];
    private int size;

    public Builder(int initialSize, int initialCapacity) {
      if(initialCapacity < MIN_INITIAL_CAPACITY) {
        initialCapacity = MIN_INITIAL_CAPACITY;
      }
      if(initialSize > initialCapacity) {
        initialCapacity = initialSize;
      }
      values = new double[initialCapacity];
      size = initialSize;
      Arrays.fill(values, NA);
    }
    

    public Builder() {
      this(0, MIN_INITIAL_CAPACITY);
    }

    public Builder(int initialSize) {
      this(initialSize, initialSize);
    }

    public static Builder withInitialSize(int size) {
      return new Builder(size, size);
    }
    
    public static Builder withInitialCapacity(int capacity) {
      return new Builder(0, capacity);
    }
    
    public Builder(DoubleVector exp) {
      this.values = exp.toDoubleArray();
      this.size = this.values.length;

      copyAttributesFrom(exp);
    }

    public Builder set(int index, double value) {
      ensureCapacity(index+1);
      if(index+1 > size) {
        size = index+1;
      }
      values[index] = value;
      return this;
    }

    public Builder add(double value) {
      return set(size, value);
    }

    @Override
    public Builder add(Number value) {
      return add(value.doubleValue());
    }

    @Override
    public Builder setNA(int index) {
      return set(index, Double.longBitsToDouble(NA_BITS));
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      if(source.isElementNA(sourceIndex)) {
        return setNA(destinationIndex);
      } else {
        return set(destinationIndex, source.getElementAsDouble(sourceIndex));
      }
    }

    public Builder set(int index, Double value) {
      return set(index, (double)value);
    }

    @Override
    public int length() {
      return size;
    }

    public void ensureCapacity(int minCapacity) {
      int oldCapacity = values.length;
      if (minCapacity > oldCapacity) {
        double oldData[] = values;
        int newCapacity = (oldCapacity * 3)/2 + 1;
        if (newCapacity < minCapacity)
          newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        values = Arrays.copyOf(oldData, newCapacity);
        Arrays.fill(values, oldCapacity, values.length, NA);
      }
    }

    @Override
    public DoubleVector build() {
      if(values.length == size) {
        if(Vector.DEBUG_ALLOC && values.length >= 5000) {
          System.out.println("building DoubleVector = " + values.length);
        }
        // Do not make an extra copy of the array
        DoubleArrayVector vector = new DoubleArrayVector(buildAttributes());
        vector.values = values;
        values = null; // will trigger an error if the caller attempts subsequent modification
        return vector;
      } else {
        return new DoubleArrayVector(values, size, buildAttributes());
      }
    }
  }
}
