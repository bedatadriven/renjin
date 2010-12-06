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
import com.google.common.collect.UnmodifiableIterator;
import r.parser.ParseUtil;

import java.util.Arrays;
import java.util.Iterator;

public class IntVector extends AbstractSEXP implements AtomicVector, Iterable<Integer>, WidensToInt {

  public static final String TYPE_NAME = "integer";
  public static final int TYPE_CODE = 13;
  public static final String IMPLICIT_CLASS = "integer";

  /**
   * NA_INTEGER:= INT_MIN currently
   */
  public static final int NA = Integer.MIN_VALUE;

  private int[] values;

  public IntVector(int... values) {
    this.values = Arrays.copyOf(values, values.length);
  }

  public IntVector(int[] values, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, values.length);
  }

  public static SEXP parseInt(String s) {
    if (s.startsWith("0x")) {
      return new IntVector(Integer.parseInt(s.substring(2), 16));
    } else {
      return new IntVector(Integer.parseInt(s));
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
  public int getInt(int i) {
    return values[i];
  }

  @Override
  public Logical asLogical() {
    if(values.length == 0) {
      return Logical.NA;
    }
    return Logical.valueOf(values[0]);
  }

  @Override
  public double getDouble(int index) {
    return values[index];
  }

  @Override
  public String getString(int index) {
    return ParseUtil.toString(index);
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new IntVector(values[index]);
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(initialSize);
  }

  @Override
  public boolean isWiderThan(Object vector) {
    return vector instanceof WidensToLogical;
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
  protected String getImplicitClass() {
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
    return new IntVector(values, attributes);
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
  public String toString() {
    if (values.length == 1) {
      return Integer.toString(values[0]);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("c(");
      Joiner.on(", ").appendTo(sb, this);
      return sb.append(")").toString();
    }
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

  private static class Builder implements Vector.Builder<LogicalVector, WidensToLogical> {
    private PairList attributes;
    private int values[];

    private Builder(int initialSize) {
      values = new int[initialSize];
      Arrays.fill(values, NA);
    }

    private Builder(IntVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      this.attributes = exp.attributes;
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

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, WidensToLogical source, int sourceIndex) {
      return set(destinationIndex, source.getInt(sourceIndex));
    }

    @Override
    public LogicalVector build() {
      return new LogicalVector(values, attributes);
    }
  }
}
