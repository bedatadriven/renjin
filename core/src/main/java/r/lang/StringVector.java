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
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import r.lang.exception.EvalException;
import r.parser.ParseUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class StringVector extends AbstractAtomicVector implements Iterable<String> {
  public static final String TYPE_NAME = "character";
  public static final int TYPE_CODE = 16;
  public static final String NA = null;

  public static final Vector.Type VECTOR_TYPE = new StringType();

  private final String values[];

  public StringVector(String... values) {
    this.values = Arrays.copyOf(values, values.length, String[].class);
  }

  public StringVector(Collection<String> values) {
    this.values = values.toArray(new String[values.size()]);
  }

  public StringVector(String[] values, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, values.length, String[].class);
  }

  @Override
  public int length() {
    return values.length;
  }

  public StringVector setLength(int newLength) {
    if(newLength == values.length) {
      return this;
    }
    String newValues[] = new String[newLength];
    for(int i=0;i!=newValues.length;++i){
      if(i < this.values.length) {
        newValues[i] = values[i];
      } else {
        newValues[i] = StringVector.NA;
      }
    }
    return new StringVector(newValues);
  }

  public String getElement(int index) {
    return values[index];
  }

  public String getElementAsString(int index) {
    return values[index];
  }

  @Override
  public int getElementAsRawLogical(int index) {
    String value = values[index];
    if(isNA(value)) {
      return IntVector.NA;
    } else if(value.equals("T") || value.equals("TRUE")) {
      return 1;
    } else if(value.equals("F") || value.equals("FALSE")) {
      return 0;
    } else {
      return IntVector.NA;
    }
  }

  @Override
  public int getElementAsInt(int index) {
    if(isElementNA(index)) {
      return IntVector.NA;
    } else {
      return (int)ParseUtil.parseDouble(values[index]);
    }
  }

  @Override
  public double getElementAsDouble(int index) {
    if(isElementNA(index)) {
      return DoubleVector.NA;
    } else {
      return ParseUtil.parseDouble(values[index]);
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
  public double asReal() {
    if(values.length > 0 &&
        values[0] != null &&
        values[0].length() > 0)
    {
      return ParseUtil.parseDouble(values[0]);
    } else {
      return DoubleVector.NA;
    }
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<String> iterator() {
    return Iterators.forArray(values);
  }

  @Override
  public String toString() {
    if (values.length == 1) {
      return ParseUtil.formatStringLiteral(values[0], "NA_character_");
    } else {
      return "c(" + Joiner.on(", ").join(Iterables.transform(this, new ParseUtil.StringDeparser())) + ")";
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StringVector stringExp = (StringVector) o;

    if (!Arrays.equals(values, stringExp.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(values);
  }

  public static boolean isNA(String s) {
    // yes this is an identity comparison because NA_character_ is null
    return s == NA;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new StringVector(values[index]);
  }

  @Override
  public String getElementAsObject(int index) {
    return values[index];
  }

  @Override
  public Builder newCopyBuilder() {
    Builder builder = new Builder();
    builder.addAll(this);

    return builder;
  }

  @Override
  public Builder newBuilder(int initialSize) {
    return new Builder();
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(values[index]);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex) {
    if(vector.isElementNA(vectorIndex)) {
      return indexOfNA();
    } else {
      String value = vector.getElementAsString(vectorIndex);
      for(int i=0;i!=values.length;++i) {
        if(values[i].equals(value)) {
          return i;
        }
      }
      return -1;
    }
  }

  public static class Builder implements Vector.Builder<AtomicVector> {
    private ArrayList<String> values = Lists.newArrayList();
    private ArrayList<String> names = Lists.newArrayList();

    public Builder() {
    }

    public Builder set(int index, String value) {
      while(values.size() <= index) {
        values.add(NA);
      }
      values.set(index, value);
      return this;
    }

    public void add(String value) {
      values.add(value);
      names.add(StringVector.NA);
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, AtomicVector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsString(sourceIndex) );
    }

    public Builder addAll(Iterable<String> strings) {
      Iterables.addAll(values, strings);
      return this;
    }

    @Override
    public StringVector build() {
      return new StringVector(values);
    }
  }

  private static class StringType extends Vector.Type {
    public StringType() {
      super(Order.CHARACTER);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new Builder();
    }
  }

  public static StringVector coerceFrom(SEXP exp) {

    if(exp instanceof Vector) {
      return fromVector((Vector) exp);
    } else if(exp instanceof SymbolExp) {
      return new StringVector( ((SymbolExp)exp).getPrintName() );
    }
    throw new EvalException("cannot coerce type '%s' to vector of type 'character'", exp.getTypeName());
  }

  public static StringVector fromVector(Vector vector) {
    StringVector.Builder result = new Builder();
    for(int i=0;i!=vector.length();++i) {
      result.add(vector.getElementAsString(i));
    }
    return result.build();
  }
}
