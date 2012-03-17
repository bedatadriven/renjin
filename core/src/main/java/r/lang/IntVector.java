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

package r.lang;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math.complex.Complex;

import r.lang.Vector.Builder;
import r.parser.ParseUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.UnmodifiableIterator;

public class IntVector extends AbstractAtomicVector implements Iterable<Integer> {

  public static final String TYPE_NAME = "integer";
  public static final int TYPE_CODE = 13;
  public static final Vector.Type VECTOR_TYPE = new IntType();
  public static final IntVector EMPTY = new IntVector();

  public static final String IMPLICIT_CLASS = "integer";

  /**
   * The integer constant used to designate elements or values that are
   * missing in the statistical sense, or literally "Not Available".
   */
  public static final int NA = Integer.MIN_VALUE;

  private int[] values;

  public IntVector(int... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public IntVector(int[] values, int length, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, length);
  }

  public IntVector(int[] values, PairList attributes) {
    this(values, values.length, attributes);
  }
  
 

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
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
  public Logical asLogical() {
    if(values.length == 0) {
      return Logical.NA;
    }
    return getElementAsLogical(0);
  }

  @Override
  public double getElementAsDouble(int index) {
    return isElementNA(index) ? DoubleVector.NA : values[index];
  }

  @Override
  public String getElementAsString(int index) {
    return isElementNA(index) ? StringVector.NA : ParseUtil.toString(values[index]);
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new IntVector(values[index]);
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return new Complex(values[index], 0);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    int value = values[index];
    if(value == 0 || isNA(value)) {
      return value;
    } else {
      return 1;
    }
  }

  @Override
  public Integer getElementAsObject(int index) {
    return values[index];
  }
  
  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    int value = vector.getElementAsInt(vectorIndex);
    for(int i=startIndex;i<values.length;++i) {
      if(value == values[i]) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int compare(int index1, int index2) {
    return values[index1] - values[index2];
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder(initialSize, initialSize);
  }
  
  @Override
  public Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new Builder(0, initialCapacity);
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean isNumeric() {
    return !inherits("factor");
  }

  @Override
  public String getImplicitClass() {
    return IMPLICIT_CLASS;
  }

  @Override
  public double asReal() {
    if(length() == 0 || values[0] == NA) {
      return DoubleVector.NA;
    } else {
      return values[0];
    }
  }

  public int[] toIntArray() {
    return Arrays.copyOf(values, values.length);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new IntVector(values, values.length, attributes);
  }

  @Override
  public Iterator<Integer> iterator() {
    return new ValueIterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IntVector intExp = (IntVector) o;

    if (!Arrays.equals(values, intExp.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }
  
  @Override
  public double[] toDoubleArray() {
    double[] d = new double[this.values.length];
    for(int i=0;i!=d.length;++i) {
      int x = this.values[i];
      if(x == NA){
        d[i] = DoubleVector.NA;
      } else {
        d[i] = x;
      }
    }
    return d;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("c(");
    for(int i=0;i<Math.min(5, length());++i) {
      if(i > 0) {
        sb.append(", ");
      }
      if(isElementNA(i)) {
        sb.append("NA_integer_");
      } else {
        sb.append(getElementAsInt(i)).append("L");
      }
    }
    if(length() > 5) {
      sb.append("...").append(length()).append(" elements total");
    }
    sb.append(")");
    return sb.toString();
  }

  public static boolean isNA(int value) {
    return value == NA;
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }

  private class ValueIterator extends UnmodifiableIterator<Integer> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < values.length;
    }

    @Override
    public Integer next() {
      return values[i++];
    }
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

    private Builder(IntVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      this.size = this.values.length;

      copyAttributesFrom(exp);
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
      return new IntVector(values, size, buildAttributes());
    }
  }

  private static class IntType extends Vector.Type {
    private IntType() {
      super(Order.INTEGER);
    }

    @Override
    public Builder newBuilder() {
      return new Builder(0, 0);
    }
    
    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new Builder(initialSize);
    }
   
    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new Builder(0, initialCapacity);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new IntVector(vector.getElementAsInt(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsInt(index1) - vector2.getElementAsInt(index2);
    }
  }
}
