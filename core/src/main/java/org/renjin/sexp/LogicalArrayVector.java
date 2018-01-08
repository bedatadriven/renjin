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

public class LogicalArrayVector extends LogicalVector {

  private int[] values;


  /**
   * Constructs a Logical vector from a list of boolean values
   */
  public LogicalArrayVector(boolean... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i] ? 1 : 0;
    }
  }

  public LogicalArrayVector(int[] values, int size, AttributeMap attributes) {
    super(attributes);
    if(Profiler.ENABLED) {
      Profiler.memoryAllocated(Integer.SIZE, size);
    }
    this.values = Arrays.copyOf(values, size);
  }

  public LogicalArrayVector(int[] values, AttributeMap attributes) {
     this(values, values.length, attributes);
  }
  
  public LogicalArrayVector(int... values) {
    this(values, values.length, AttributeMap.EMPTY);
  }

  private LogicalArrayVector(AttributeMap attributes) {
    super(attributes);
  }

  public LogicalArrayVector(Logical... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i].getInternalValue();
    }
  }

  /**
   * Constructs a Logical vector from a list of boolean values
   */
  public LogicalArrayVector(Boolean[] values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i] ? 1 : 0;
    }
  }

  public static LogicalArrayVector unsafe(int[] array, AttributeMap attributes) {
    LogicalArrayVector vector = new LogicalArrayVector(AttributeMap.EMPTY);
    vector.values = array;
    return vector;
  }

  public static LogicalArrayVector unsafe(int[] array) {
    return unsafe(array, AttributeMap.EMPTY);
  }



  /**
   * Returns reference to the {@code int[]} array backing this {@code LogicalVector}. 
   * 
   * <p>This array <strong>must not</strong> be modified if there is any chance that 
   * a reference to this Vector is held elsewhere.</p>
   */
  public int[] toIntArrayUnsafe() {
    return values;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return values[index];
  }

  @Override
  public boolean isElementTrue(int index) {
    return values[index] == 1;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new LogicalArrayVector(values, attributes);
  }


  public static class Builder
      extends AbstractAtomicBuilder {
    
    private static final int MIN_INITIAL_CAPACITY = 10;
    
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

    public Builder() {
      this(0, MIN_INITIAL_CAPACITY);
    }
    
    public Builder(int initialSize) {
      this(initialSize, initialSize);
    }

    public Builder(LogicalVector toClone) {
      this.values = new int[toClone.length()];
      for(int i=0;i!=values.length;++i) {
        values[i] = toClone.getElementAsRawLogical(i);
      }
      this.size = this.values.length;
      copyAttributesFrom(toClone);
    }

    public Builder add(int value) {
      return set(size, value);
    }

    public Builder add(boolean value) {
      return add(value ? 1 : 0);
    }
    
    public Builder add(Number value) {
      return add(value.intValue() != 0 ? 1 : 0);
    }

    public Builder set(int index, int value) {
      ensureCapacity(index+1);
      if(index+1 > size) {
        size = index+1;
      }
      values[index] = value;
      return this;
    }

    public Builder set(int index, boolean value) {
      return set(index, value ? 1 : 0);
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
    public LogicalVector build() {
      if(Profiler.ENABLED) {
        Profiler.memoryAllocated(Integer.SIZE, size);
      }
      if(values.length == size) {
        LogicalArrayVector vector = new LogicalArrayVector(buildAttributes());
        vector.values = values;
        // builder shouldn't touch the values after we hand over to vector
        this.values = null;
        return vector;
      } else {
        return new LogicalArrayVector(values, size, buildAttributes());
      }
    }
  }
}
