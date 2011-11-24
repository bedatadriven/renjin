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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import r.lang.Vector.Builder;
import r.lang.exception.EvalException;
import r.parser.ParseUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class StringVector extends AbstractAtomicVector implements Iterable<String> {
  public static final String TYPE_NAME = "character";
  public static final String NA = null;
  public static final StringVector EMPTY = new StringVector();

  public static final Vector.Type VECTOR_TYPE = new StringType();

  private final String values[];

  public StringVector(String... values) {
    this.values = Arrays.copyOf(values, values.length, String[].class);
  }

  public StringVector(Collection<String> values, PairList attributes) {
    super(attributes);
    this.values = values.toArray(new String[values.size()]);

    assert checkDims() : "dim do not match length of object";
  }

  public StringVector(Collection<String> values) {
    this(values, Null.INSTANCE);
  }

  public StringVector(String[] values, PairList attributes) {
    super(attributes);
    this.values = Arrays.copyOf(values, values.length, String[].class);

    assert checkDims() : "dim do not match length of object";
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
    return new Builder(this);
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder(initialSize, 0);
  }
  
  @Override
  public Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new Builder(0, initialCapacity);
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
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new StringVector(values, attributes);
  }

  @Override
  public int indexOf(AtomicVector vector, int vectorIndex, int startIndex) {
    if(vector.isElementNA(vectorIndex)) {
      return indexOfNA();
    } else {
      String value = vector.getElementAsString(vectorIndex);
      return indexOf(value, startIndex);
    }
  }

  @Override
  public int compare(int index1, int index2) {
    return values[index1].compareTo(values[index2]);
  }

  private int indexOf(String value, int startIndex) {
    for(int i=startIndex;i<values.length;++i) {
      if(values[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  public int indexOf(String value) {
    return indexOf(value, 0);
  }

  public String[] toArray() {
    return values.clone();

  }

  public static class Builder extends AbstractAtomicBuilder {
    private ArrayList<String> values;
    private boolean haveNonEmpty = false;

    public Builder(int initialSize, int initialCapacity) {
      values = Lists.newArrayListWithCapacity(initialCapacity);
      for(int i=0;i!=initialSize;++i) {
        values.add(NA);
      }
    }
    
    public Builder() {
      this(0, 15);
    }

    public Builder(StringVector toClone) {
      values = Lists.newArrayList();
      Iterables.addAll(values, toClone);
      copyAttributesFrom(toClone);
    }

    public Builder(int initialSize) {
      values = new ArrayList<String>(initialSize);
      for(int i=0;i!=initialSize;++i) {
        values.add(NA);
      }
    }

    public Builder set(int index, String value) {
      while(values.size() <= index) {
        values.add(NA);
      }
      values.set(index, value);
      if(value != null && !value.isEmpty()) {
        haveNonEmpty = true;
      }
      return this;
    }

    public void add(String value) {
      values.add(value);
      if(value != null && !value.isEmpty()) {
        haveNonEmpty = true;
      }
    }

    @Override
    public Builder add(Number value) {
      add(ParseUtil.toString(value.doubleValue()));
      return this;
    }

    public void addAll(Iterable<String> values) {
      for(String value : values) {
        add(value);
      }
    }
    
    @Override
    public Builder setNA(int index) {
      return set(index, NA);
    }

    @Override
    public Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
      return set(destinationIndex, source.getElementAsString(sourceIndex) );
    }

    public boolean haveNonEmpty() {
      return haveNonEmpty;
    }

    @Override
    public int length() {
      return values.size();
    }

    @Override
    public StringVector build() {
      return new StringVector(values, buildAttributes());
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

    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new Builder(initialSize);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new StringVector(vector.getElementAsString(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsString(index1).compareTo(vector2.getElementAsString(index2));
    }
  }

  public static StringVector coerceFrom(SEXP exp) {

    if(exp instanceof Vector) {
      return fromVector((Vector) exp);
    } else if(exp instanceof Symbol) {
      return new StringVector( ((Symbol)exp).getPrintName() );
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
