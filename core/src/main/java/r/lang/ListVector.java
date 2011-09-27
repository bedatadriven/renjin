/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.math.complex.Complex;
import r.base.Parse;
import r.lang.exception.EvalException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generic vector of {@code SEXP}s
 */
public class ListVector extends AbstractVector implements Iterable<SEXP>, HasNamedValues {

  public static final String TYPE_NAME = "list";
  public static final ListVector EMPTY = new ListVector();
  public static final Vector.Type VECTOR_TYPE = new ListType();

  private final ArrayList<SEXP> values;

  public ListVector(Iterable<? extends SEXP> values,  PairList attributes) {
    super(Null.INSTANCE, attributes);
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
  }

  public ListVector(Iterable<? extends SEXP> values) {
    this(values, Null.INSTANCE);
  }

  public ListVector(SEXP[] values, SEXP tag, PairList attributes) {
    super(tag, attributes);
    this.values = new ArrayList<SEXP>();
    Collections.addAll(this.values, values);

    assert checkDims() : "dim do not match length of object";
  }

  public ListVector(SEXP[] values, PairList attributes) {
    this(values, Null.INSTANCE, attributes);

    assert checkDims() : "dim do not match length of object";
  }

  public ListVector(SEXP... values) {
    this(values, Null.INSTANCE);
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public final boolean isWiderThan(Vector vector) {
    return getVectorType().isWiderThan(vector);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Iterator<SEXP> iterator() {
    return values.iterator();
  }

  @Override
  public int length() {
    return values.size();
  }

  public int indexOfName(String name) {
    SEXP names = attributes.findByTag(Symbols.NAMES);
    if(names instanceof StringVector) {
      for(int i=0;i!=names.length();++i) {
        if(((StringVector) names).getElement(i).equals(name)) {
          return i;
        }
      }
    }
    return -1;
  }

  public SEXP get(int index) {
    return values.get(index);
  }

  public SEXP get(String name) {
    int index = indexOfName(name);
    if(index == -1) {
      return Null.INSTANCE;
    }
    return values.get(index);
  }

  @Override
  public Type getVectorType() {
    return VECTOR_TYPE;
  }

  @Override
  public SEXP getElementAsSEXP(int index) {
    return values.get(index);
  }

  @Override
  public double getElementAsDouble(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsDouble(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'double'");
  }

  @Override
  public int getElementAsInt(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsInt(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'int'");
  }

  @Override
  public String getElementAsString(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsString(0);
    }
    return Parse.deparse(value);
  }

  @Override
  public Object getElementAsObject(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsObject(0);
    }
    return Parse.deparse(value);
  }

  @Override
  public Logical getElementAsLogical(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsLogical(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'logical'");
  }

  @Override
  public int getElementAsRawLogical(int index) {
  SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsRawLogical(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'logical'");
  }

  @Override
  public Complex getElementAsComplex(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsComplex(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'complex'");
  }

  @Override
  public boolean isElementNA(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).isElementNA(0);
    } else {
      return false;
    }
  }

  @Override
  public Iterable<SEXP> elements() {
    return this;
  }

  /**
   * @return the length of the longest element
   */
  public int maxElementLength() {
    int max = 0;
    for(SEXP element : this) {
      if(element.length() > max) {
        max = element.length();
      }
    }
    return max;
  }


  public int minElementLength() {
    int min = Integer.MAX_VALUE;
    for(SEXP element : this) {
      if(element.length() < min) {
        min = element.length();
      }
    }
    return min;
  }

  public Iterable<NamedValue> namedValues() {
    return new Iterable<NamedValue>() {
      @Override
      public Iterator<NamedValue> iterator() {
        return new UnmodifiableIterator<NamedValue>() {
          private int index = 0;

          @Override
          public boolean hasNext() {
            return index < length();
          }

          @Override
          public NamedValue next() {
            NamedValue pair = new NameValuePair(getName(index), getElementAsSEXP(index));
            index++;
            return pair;
          }
        };
      }
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ListVector listExp = (ListVector) o;

    if (!values.equals(listExp.values)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("list(");
    Joiner.on(", ").appendTo(sb, values);
    return sb.append(")").toString();
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder buildFromClone(ListVector toClone) {
    return new Builder(toClone);
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Vector.Builder newBuilder(int initialSize) {
    return new Builder();
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new ListVector(values, attributes);
  }


  public static class Builder extends AbstractVector.AbstractBuilder<SEXP> {
    private boolean haveNames = false;
    private List<SEXP> values = Lists.newArrayList();
    private List<String> names = Lists.newArrayList();

    public Builder() {
    }

    private Builder(ListVector toClone) {
      Iterables.addAll(values, toClone);
      copyAttributesFrom(toClone);
      SEXP names = toClone.getAttribute(Symbols.NAMES);
      if(names instanceof StringVector) {
        Iterables.addAll(this.names, (StringVector)names);
        haveNames = true;
      } else {
        for(SEXP value : values) { this.names.add(""); }
      }
    }

    public Builder(int initialLength) {
      for(int i=0;i!=initialLength;++i) {
        add(Null.INSTANCE);
      }
    }

    public Builder add(String name, SEXP value) {
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(value);

      values.add(value);
      names.add(name);
      if(!name.isEmpty()) {
        haveNames = true;
      }
      return this;
    }

    public Builder add(String name, Vector.Builder builder) {
      return add(name, builder.build());
    }

    public Builder add(Symbol name, SEXP value) {
      return add(name.getPrintName(), value);
    }

    public Builder add(String name, int value) {
      return add(name, new IntVector(value));
    }

    public Builder add(String name, String value) {
      return add(name, new StringVector(value));
    }

    public Builder add(String name, boolean value) {
      return add(name, new LogicalVector(value));
    }

    public Builder add(String name, Logical value) {
      return add(name, new LogicalVector(value));
    }
    
    public Builder addAll(ListVector list) {
      for(int i=0;i!=list.length();++i) {
        add(list.getName(i),  list.get(i));
      }
      return this;
    }

    @Override
    public Builder add(SEXP value) {
      values.add(value);
      names.add("");
      return this;
    }

    @Override
    public Builder set(int index, SEXP value) {
      while(values.size() <= index) {
        add(Null.INSTANCE);
      }
      values.set(index, value);
      return this;
    }

    @Override
    public Builder setNA(int index) {
      return set(index, Null.INSTANCE);
    }

    @Override
    public Vector.Builder setFrom(int destinationIndex, SEXP source, int sourceIndex) {
      return this.set(destinationIndex, source.getElementAsSEXP(sourceIndex));
    }

    @Override
    public int length() {
      return values.size();
    }

    public Builder replace(int i, SEXP value) {
      values.set(i, value);
      return this;
    }

    public ListVector build() {
      if(haveNames) {
        setAttribute(Symbols.NAMES, new StringVector(names));
      }

      return new ListVector(values, buildAttributes());
    }
  }

  private static class ListType extends Vector.Type {
    public ListType() {
      super(Order.LIST);
    }

    public Builder newBuilder() {
      return new Builder();
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      // TODO: should compareElements be a method on some AtomicVectorType class??
      throw new UnsupportedOperationException();
    }

    @Override
    public Vector getElementAsVector(Vector vector, int index) {
      return new ListVector(vector.getElementAsSEXP(index));
    }
  }

  private static class NameValuePair implements NamedValue {
    private final String name;
    private final SEXP value;

    public NameValuePair(String name, SEXP value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean hasName() {
      return !name.isEmpty();
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public SEXP getValue() {
      return value;
    }
  }
}
