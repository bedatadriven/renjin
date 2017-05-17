/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.apache.commons.math.complex.Complex;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.repackaged.guava.base.Objects;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class StringVector extends AbstractAtomicVector implements Iterable<String> {

  public static final String TYPE_NAME = "character";
  public static final String NA = null;

  public static final Vector.Type VECTOR_TYPE = new StringType();
  public static final StringVector EMPTY = new StringArrayVector();


  public StringVector(AttributeMap attributes) {
    super(attributes);
  }

  public static StringVector valueOf(String string) {
    return new StringArrayVector(string);
  }

  @Override
  public int getElementAsRawLogical(int index) {
    String value = getElementAsString(index);
    return logicalFromString(value);
  }

  public static int logicalFromString(String value) {
    if(isNA(value)) {
      return IntVector.NA;
    } else if(value.equals("T") || value.equals("TRUE") || value.equals("true")) {
      return 1;
    } else if(value.equals("F") || value.equals("FALSE") || value.equals("false")) {
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
      String value = getElementAsString(index);
      double doubleValue = NumericLiterals.parseDouble(value, 0, value.length(), '.', true);
      if(DoubleVector.isFinite(doubleValue)) {
        return (int)doubleValue;
      } else {
        return IntVector.NA;
      }
    }
  }

  @Override
  public double getElementAsDouble(int index) {
    if(isElementNA(index)) {
      return DoubleVector.NA;
    } else {
      return NumericLiterals.parseDouble(getElementAsString(index));
    }
  }

  @Override
  public Complex getElementAsComplex(int index) {
    String stringValue = getElementAsString(index);
    if(StringVector.isNA(stringValue)) {
      return ComplexVector.NA;
    }
    return NumericLiterals.parseComplex(stringValue);
  }

  public static boolean isNA(String s) {
    // yes this is an identity comparison because NA_character_ is null
    return s == NA;
  }

  @Override
  public Logical asLogical() {
    if(length() >= 1) {
      String value = getElementAsString(0);
      if ("true".equals(value) ||
          "TRUE".equals(value) ||
          "T".equals(value)) {
        return Logical.TRUE;
      }
      if ("false".equals(value) ||
          "FALSE".equals(value) ||
          "F".equals(value)) {
        return Logical.FALSE;
      }
    }
    return Logical.NA;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return new StringArrayVector(getElementAsString(index));
  }

  @Override
  public String asString() {
    if(length() == 1) {
      return getElementAsString(0);
    } else {
      return super.asString();
    }
  }

  @Override
  public String getElementAsObject(int index) {
    return getElementAsString(index);
  }

  @Override
  public Builder newCopyBuilder() {
    return new StringArrayVector.Builder(this);
  }

  @Override
  public Builder newBuilderWithInitialSize(int initialSize) {
    return new StringArrayVector.Builder(initialSize, 0);
  }

  @Override
  public StringArrayVector.Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new StringArrayVector.Builder(0, initialCapacity);
  }

  public static StringArrayVector.Builder newBuilder() {
    return new StringArrayVector.Builder();
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public boolean isElementNA(int index) {
    return isNA(getElementAsString(index));
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
  public abstract int length();

  @Override
  protected abstract StringVector cloneWithNewAttributes(AttributeMap attributes);

  @Override
  public int compare(int index1, int index2) {
    return getElementAsString(index1).compareTo(getElementAsString(index2));
  }

  private int indexOf(String value, int startIndex) {
    for(int i=startIndex;i<length();++i) {
      String value_i = getElementAsString(i);
      if(value_i != null && value_i.equals(value)) {
        return i;
      }
    }
    return -1;
  }

  public int indexOf(String value) {
    return indexOf(value, 0);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof StringVector)) {
      return false;
    }

    StringVector that = (StringVector) o;
    if(that.length() != this.length()) {
      return false;
    }

    for(int i=0;i!=length();++i) {
      if(!Objects.equal(this.getElementAsString(i), that.getElementAsString(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final int hashCode() {
    int hash = 37;
    for(int i=0;i!=length();++i) {
      String s_i = getElementAsString(i);
      hash += s_i == null ? 0 : s_i.hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    if (length() == 1) {
      return isElementNA(0) ? "NA_character_" : getElementAsString(0);
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("c(");
      for (int i = 0; i < Math.min(5, length()); ++i) {
        if (i > 0) {
          sb.append(", ");
        }
        if (isElementNA(i)) {
          sb.append("NA_character_");
        } else {
          sb.append(getElementAsString(i));
        }
      }
      if (length() > 5) {
        sb.append("...").append(length()).append(" elements total");
      }
      sb.append(")");
      return sb.toString();
    }
  }

  public String[] toArray() {
    String[] array = new String[length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = getElementAsString(i);
    }
    return array;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<String> iterator() {
    return new UnmodifiableIterator<String>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < length();
      }

      @Override
      public String next() {
        return getElementAsString(index ++ );
      }
    };
  }

  private static class StringType extends Vector.Type {
    public StringType() {
      super(Order.CHARACTER);
    }

    @Override
    public Vector.Builder newBuilder() {
      return new StringArrayVector.Builder();
    }

    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new StringArrayVector.Builder(initialSize);
    }

    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new StringArrayVector.Builder(0,initialCapacity);
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new StringArrayVector(vector.getElementAsString(index));
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      return vector1.getElementAsString(index1).compareTo(vector2.getElementAsString(index2));
    }

    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
      String s1 = vector1.getElementAsString(index1);
      String s2 = vector2.getElementAsString(index2);
      if(s1 == null || s2 == null) {
        return false;
      }
      return s1.equals(s2);
    }

    @Override
    public Vector to(Vector x) {
      if(x instanceof StringVector) {
        return x;
      } else {
        return new ConvertingStringVector(x, x.getAttributes());
      }
    }
  }

  public static class Builder extends AbstractAtomicBuilder {
    private ArrayList<String> values;
    private boolean haveNonEmpty = false;

    public Builder(int initialSize, int initialCapacity) {
      values = Lists.newArrayListWithCapacity(initialCapacity);
      for(int i=0;i!=initialSize;++i) {
        values.add(StringArrayVector.NA);
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
        values.add(StringArrayVector.NA);
      }
    }

    public Vector.Builder set(int index, String value) {
      while(values.size() <= index) {
        values.add(StringArrayVector.NA);
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
    public Vector.Builder add(Number value) {
      add(NumericLiterals.toString(value.doubleValue()));
      return this;
    }
  
    public StringVector.Builder addAll(Iterable<String> values) {
      for(String value : values) {
        add(value);
      }
      return this;
    }

    @Override
    public Vector.Builder setNA(int index) {
      return set(index, StringArrayVector.NA);
    }

    @Override
    public Vector.Builder setFrom(int destinationIndex, Vector source, int sourceIndex) {
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
    public StringArrayVector build() {
      return new StringArrayVector(values, buildAttributes());
    }
  }
}
