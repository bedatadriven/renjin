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

import org.apache.commons.math.complex.Complex;

import java.util.Arrays;
import java.util.Iterator;

public class LogicalVector extends AbstractAtomicVector implements Iterable<Logical> {
  public static final String TYPE_NAME = "logical";
  public static final int TYPE_CODE = 10;
  public static final Vector.Type VECTOR_TYPE = new LogicalType();

  public static int NA = IntVector.NA;

  public static LogicalVector TRUE = new LogicalVector(1);
  public static LogicalVector FALSE = new LogicalVector(0);

  private int[] values;

  /**
   * Constructs a Logical vector from a list of boolean values
   */
  public LogicalVector(boolean... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i] ? 1 : 0;
    }
  }

  public LogicalVector(int[] values, PairList attributes) {
     super(attributes);
     this.values = Arrays.copyOf(values, values.length);
  }

  public LogicalVector(int... values) {
    this(values, Null.INSTANCE);
  }

  public LogicalVector(Logical... values) {
    this.values = new int[values.length];
    for (int i = 0; i != values.length; ++i) {
      this.values[i] = values[i].getInternalValue();
    }
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getElementAsInt(int index) {
    return values[index];
  }

  @Override
  public double getElementAsDouble(int index) {
    int value = values[index];
    return value == IntVector.NA ? DoubleVector.NA : (double) value;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new LogicalVector(values[index]);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    int value = vector.getElementAsRawLogical(startIndex);
    for(int i=0;i<values.length;++i) {
      if(value == values[i]) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Boolean getElementAsObject(int index) {
    int value = values[index];
    if(IntVector.isNA(value)) {
      throw new IllegalStateException(String.format("The element at index %d is NA," +
          " and cannot be represented as a Boolean. Make sure you are calling isElementNA() first.", index));
    } else {
      return value != 0;
    }
  }

  @Override
  public Logical getElementAsLogical(int index) {
    return Logical.valueOf(values[index]);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return values[index];
  }

  @Override
  public Complex getElementAsComplex(int index) {
    if(IntVector.isNA(values[index])) {
      return ComplexVector.NA;  
    }
    return new Complex(values[index], 0);
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public String getElementAsString(int index) {
    int value = values[index];
    if(value == IntVector.NA) {
      return StringVector.NA;
    } else if(value == 0) {
      return "FALSE";
    } else {
      return "TRUE";
    }
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    if (values[0] == IntVector.NA) {
      return Logical.NA;
    } else {
      return values[0] == 0 ? Logical.FALSE : Logical.TRUE;
    }
  }

  @Override
  public double asReal() {
    if(values.length == 0) {
      return DoubleVector.NA;
    } else {
      return values[0];
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Logical> iterator() {
    return new Iterator<Logical>() {
      private int i=0;

      @Override
      public boolean hasNext() {
        return i<values.length;
      }

      @Override
      public Logical next() {
        return Logical.valueOf(values[i++]);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LogicalVector that = (LogicalVector) o;

    if (!Arrays.equals(values, that.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  @Override
  public String toString() {
    if (length() == 1) {
      return toString(values[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i != values.length; ++i) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(toString(values[i]));
      }
      sb.append("]");
      return sb.toString();
    }
  }

  @Override
  public Builder newCopyBuilder() {
    return null;
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(this);
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new LogicalVector(values, attributes);
  }

  private String toString(int x) {
    if (x == 1) {
      return "TRUE";
    } else if (x == 0) {
      return "FALSE";
    } else {
      return "NA";
    }
  }

  @Override
  public boolean isElementNA(int index) {
    return IntVector.isNA(values[index]);
  }

  public static class Builder<T extends AbstractBuilder, E>
      extends AbstractAtomicBuilder<E> {
    private int values[];

    public Builder(int initialSize) {
      values = new int[initialSize];
      Arrays.fill(values, NA);
    }

    public Builder() {
      this(0);
    }

    private Builder(LogicalVector toClone) {
      this.values = Arrays.copyOf(toClone.values, toClone.values.length);
      copyAttributesFrom(toClone);
    }

    public Builder add(int value) {
      return set(values.length, value);
    }

    public Builder add(boolean value) {
      return add(value ? 1 : 0);
    }

    public Builder set(int index, int value) {
      if(values.length <= index) {
        int copy[] = Arrays.copyOf(values, index+1);
        Arrays.fill(copy, values.length, copy.length, NA);
        values = copy;
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
      return values.length;
    }

    @Override
    public LogicalVector build() {
      return new LogicalVector(values, buildAttributes());
    }

  }

  private static class LogicalType extends Vector.Type {
    public LogicalType() {
      super(Order.LOGICAL);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new Builder(0);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new LogicalVector(vector.getElementAsRawLogical(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsRawLogical(index1) - vector2.getElementAsRawLogical(index2);
    }
  }
}
