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

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.commons.math.complex.Complex;
import r.parser.ParseUtil;
import r.util.collect.PrimitiveArrays;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class DoubleVector extends AbstractAtomicVector implements Iterable<Double> {

  public static final String TYPE_NAME = "double";

  public static final Vector.Type VECTOR_TYPE = new DoubleType();
  public static final DoubleVector EMPTY = new DoubleVector();

  /**
   * This is the internal representation R uses to
   * represent NAs: a "quiet NaN" with a payload of 0x1954.
   * 
   * <p>Note that this is slightly different than the C implementation of R,
   * which uses a "signaled" NaN with the same payload. The serialized XDR form of NA is 
   * different still: see {@link r.io.SerializationFormat#XDR_NA_BITS}.
   * 
   * <p>The Java Language Spec is somewhat ambiguous regarding the extent to which
   * non-canonical NaNs will be preserved. What is clear though, is that signaled bit
   * (bit 12) is dropped by {@link Double#longBitsToDouble(long)}, at least on the few
   * platforms on which I have tested the Sun JDK 1.6.
   *
   * <p>The payload, however, does appear to be preserved by the JVM.
   */
  private static final long NA_BITS = 0x7ff8000000001954L;

  /**
   * The double constant used to designate elements or values that are
   * missing in the statistical sense, or literally "Not Available". The following
   * has the relationships hold true:
   *
   * <ul>
   * <li>isNaN(NA) is <i>true</i>
   * <li>isNA(Double.NaN) is <i>false</i>
   * </ul>
   
   *
   */
  public static final double NA = Double.longBitsToDouble(NA_BITS);

  public static final double NaN = Double.NaN;
  public static final double EPSILON = 2.220446e-16;


  private double[] values;

  private DoubleVector(PairList attributes) {
    super(attributes);
  }

  public DoubleVector(double... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public DoubleVector(double[] values, PairList attributes) {
    this(attributes);
    this.values = Arrays.copyOf(values, values.length);
  }

  public DoubleVector(double[] values, int length, PairList attributes) {
    this(attributes);
    this.values = Arrays.copyOf(values, length);
  }

  public DoubleVector(Collection<Double> values) {
    this.values = new double[values.size()];
    int i = 0;
    for(Double value : values) {
      this.values[i++] = value;
    }
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  /**
   * Returns a RealVector with a single double value parsed from the
   * supplied string.
   *
   * @param text the string representation to parse
   * @return a RealVector of length one
   */
  public static SEXP parseDouble(String text) {
    return new DoubleVector(Double.parseDouble(text));
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    DoubleVector clone = new DoubleVector(attributes);
    clone.values = values;
    return clone;
  }

  public double get(int i) {
    return values[i];
  }

  @Override
  public Logical getElementAsLogical(int index) {
    double value = values[index];
    if(isNA(value)) {
      return Logical.NA;
    } else if(value == 0) {
      return Logical.FALSE;
    } else {
      return Logical.TRUE;
    }
  }

  @Override
  public int getElementAsRawLogical(int index) {
    double value = values[index];
    if(isNaN(value)) {
      return IntVector.NA;
    } else if(value == 0) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public String getElementAsString(int index) {
    double value = values[index];
    return isNaN(value) ? StringVector.NA :
        ParseUtil.toString(value);
  }

  @Override
  public int getElementAsInt(int index) {
    double value = values[index];
    return isNaN(value) ? IntVector.NA : (int) value;
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return new Complex(index, 0);
  }

  @Override
  public double getElementAsDouble(int index) {
    return values[index];
  }

  public DoubleVector getElementAsSEXP(int index) {
    return new DoubleVector(values[index]);
  }

  @Override
  public Double getElementAsObject(int index) {
    return values[index];
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    double value = vector.getElementAsDouble(vectorIndex);
    for(int i=startIndex;i<values.length;++i) {
      if(value == values[i]) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public int compare(int index1, int index2) {
    return Double.compare(values[index1], values[index2]);
  }

  public int[] coerceToIntArray() {
    int integers[] = new int[values.length];
    for(int i=0; i!=values.length; ++i) {
      integers[i] = isNaN(values[i]) ? IntVector.NA : (int)values[i];
    }
    return integers;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public boolean isNumeric() {
    return true;
  }

  @Override
  public Logical asLogical() {
    double x = values[0];

    if (Double.isNaN(x)) {
      return Logical.NA;
    } else if (x == 0) {
      return Logical.FALSE;
    } else {
      return Logical.TRUE;
    }
  }

  @Override
  public String getImplicitClass() {
    return "numeric";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Double> iterator() {
    return PrimitiveArrays.asUnmodifiableIterator(values);
  }

  public double asReal() {
    if(values.length == 0) {
      return NA;
    } else {
      return values[0];
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DoubleVector vector = (DoubleVector) o;
    
    if(this.length() != vector.length()) {
      return false;
    }
    for(int i=0;i!=values.length;++i) {
      double this_i = values[i];
      double that_i = vector.values[i];
      
      if( isNA(this_i)  != isNA(that_i) ) {
        return false;
      }
      if( isNaN(this_i) != isNaN(that_i) ) {
        return false;
      }
      if( !isNaN(this_i) && !isNaN(that_i) && this_i != that_i) {
        return false;
      }
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  public double[] toDoubleArray() {
    return Arrays.copyOf(values, values.length);
  }
  
  @Override
  public String toString() {
    if (values.length == 1) {
      return Double.toString(values[0]);
    } else {
      StringBuilder sb = new StringBuilder("c(");
      Joiner.on(", ").appendTo(sb, Iterables.transform(this, new ParseUtil.RealPrinter()));
      return sb.append(")").toString();
    }
  }

  public static boolean isNaN(double x) {
    return Double.isNaN(x);
  }

  public static boolean isNA(double input) {
    long bits = Double.doubleToRawLongBits(input);
    return bits == NA_BITS;
  }

  public static boolean isFinite(double d) {
    return !Double.isInfinite(d) && !Double.isNaN(d);
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(initialSize);
  }
  
  public static DoubleVector newMatrix(double[] values, int nRows, int nCols) {
    PairList attributes = new PairList.Node(Symbol.DIM, new IntVector(nRows,nCols), Null.INSTANCE);
    return new DoubleVector(values, attributes);
  }


  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }
  
  public boolean isElementNaN(int i) {
    return Double.isNaN(values[i]);
  }

  public static class Builder extends AbstractAtomicBuilder {
    private static final int MIN_INITIAL_CAPACITY = 50;
    private double values[];
    private int size;

    public Builder(int initialSize) {
      int initialCapacity = MIN_INITIAL_CAPACITY;
      if(initialSize > initialCapacity) {
        initialCapacity = initialSize;
      }
      values = new double[initialCapacity];
      size = initialSize;
      Arrays.fill(values, NA);
    }

    public Builder() {
      this(0);
    }
    
    private Builder(DoubleVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
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
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsDouble(sourceIndex));
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
      return new DoubleVector(values, size, buildAttributes());
    }
  }

  private static class DoubleType extends Vector.Type {
    public DoubleType() {
      super(Order.DOUBLE);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new Builder(0);
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return Double.compare(vector1.getElementAsDouble(index1), vector2.getElementAsDouble(index2));
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new DoubleVector(vector.getElementAsDouble(index));
    }

  }

}
