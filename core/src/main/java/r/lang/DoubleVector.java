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
  public static final int TYPE_CODE = 14;

  public static final Vector.Type VECTOR_TYPE = new DoubleType();

  public static final double NA = createNA();
  public static final double NaN = Double.NaN;
  public static final double EPSILON =  0.00001;

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

  public DoubleVector(Collection<Double> values) {
    this.values = new double[values.size()];
    int i = 0;
    for(Double value : values) {
      this.values[i++] = value;
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
    if(isNA(value)) {
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
    return isNA(value) ? StringVector.NA :
        ParseUtil.toString(values[index]);
  }

  @Override
  public int getElementAsInt(int index) {
    double value = values[index];
    return isNA(value) ? IntVector.NA : (int) value;
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
  public int indexOf(AtomicVector vector, int vectorIndex) {
    double value = vector.getElementAsDouble(vectorIndex);
    for(int i=0;i!=values.length;++i) {
      if(value == values[i]) {
        return i;
      }
    }
    return -1;
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
  protected String getImplicitClass() {
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

    DoubleVector realExp = (DoubleVector) o;

    if (!Arrays.equals(values, realExp.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
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

  private static double createNA() {
//    volatile ieee_double x;
//    x.word[hw] = 0x7ff00000;
//    x.word[lw] = 1954;
//    return x.value;

    return Double.longBitsToDouble(0x7ff0000000001954L);
  }

  public static boolean isNaN(double x) {
    return Double.isNaN(x);
  }

  public static boolean isNA(double input) {
    return Double.doubleToRawLongBits(input) == Double.doubleToRawLongBits(NA);
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(initialSize);
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }

  public static class Builder implements Vector.Builder<AtomicVector> {
    private PairList attributes = Null.INSTANCE;
    private double values[];

    public Builder(int initialSize) {
      values = new double[initialSize];
      Arrays.fill(values, NA);
    }

    private Builder(DoubleVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      this.attributes = exp.attributes;
    }

    public Builder set(int index, double value) {
      if(values.length <= index) {
        double copy[] = Arrays.copyOf(values, index+1);
        Arrays.fill(copy, values.length, copy.length, NA);
        values = copy;
      }
      values[index] = value;
      return this;
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, AtomicVector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsDouble(sourceIndex));
    }

    @Override
    public DoubleVector build() {
      return new DoubleVector(values, attributes);
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
  }
}
