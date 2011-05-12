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
import org.apache.commons.math.complex.Complex;
import r.parser.ParseUtil;

import java.util.Arrays;
import java.util.Iterator;

public class IntVector extends AbstractAtomicVector implements Iterable<Integer> {

  public static final String TYPE_NAME = "integer";
  public static final int TYPE_CODE = 13;
  public static final Vector.Type VECTOR_TYPE = new IntType();

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
    return values[index];
  }

  @Override
  public String getElementAsString(int index) {
    return ParseUtil.toString(values[index]);
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
    return value;
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder(initialSize);
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

  public static class Builder extends AbstractAtomicBuilder<Integer> {
    private int values[];

    public Builder(int initialSize) {
      values = new int[initialSize];
      Arrays.fill(values, NA);
    }

    private Builder(IntVector exp) {
      this.values = Arrays.copyOf(exp.values, exp.values.length);
      copyAttributesFrom(exp);
    }

    public Builder() {
      this.values = new int[0];
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

    public Builder add(int value) {
      return set(values.length, value);
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
      return values.length;
    }

    @Override
    public IntVector build() {
      return new IntVector(values, buildAttributes());
    }
  }

  private static class IntType extends Vector.Type {
    private IntType() {
      super(Order.INTEGER);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new Builder(0);
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
