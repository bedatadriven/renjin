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

import com.google.common.collect.ImmutableList;
import r.util.collect.PrimitiveArrays;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class DoubleExp extends SEXP implements AtomicExp, Iterable<Double> {
  public static final String TYPE_NAME = "double";
  public static final int TYPE_CODE = 14;

  public static final double NA = createNA();
  public static final double NaN = Double.NaN;
  public static final double EPSILON =  0.00001;

  private double[] values;

  public DoubleExp(double... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public DoubleExp(Collection<Double> values) {
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

  /**
   * Returns a RealVector with a single double value parsed from the
   * supplied string.
   *
   * @param text the string representation to parse
   * @return a RealVector of length one
   */
  public static SEXP parseDouble(String text) {
    return new DoubleExp(Double.parseDouble(text));
  }

  public double get(int i) {
    return values[i];
  }

  public void set(int i, double value) {
    values[i] = value;
  }

  @Override
  public int length() {
    return values.length;
  }

  public static DoubleExp ofLength(int length) {
    return new DoubleExp(new double[length]);
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
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<Double> iterator() {
    return PrimitiveArrays.asUnmodifiableIterator(values);
  }

  public List<Double> asListOfDoubles() {
    return ImmutableList.copyOf(iterator());
  }

  public double asReal() {
    if(values.length == 0) {
      return NA;
    } else {
      return values[0];
    }
  }

  @Override
  public SEXP subset(int index) {
    return new DoubleExp(values[index-1]);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DoubleExp realExp = (DoubleExp) o;

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
      return Arrays.toString(values);
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

}
