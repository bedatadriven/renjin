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

package org.renjin.sexp;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Deparse;
import org.renjin.util.NamesBuilder;

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

  public ListVector(Iterable<? extends SEXP> values,  AttributeMap attributes) {
    super(Null.INSTANCE, attributes);
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
  }

  public ListVector(Iterable<? extends SEXP> values) {
    this(values, AttributeMap.EMPTY);
  }

  public ListVector(SEXP[] values, SEXP tag, AttributeMap attributes) {
    super(tag, attributes);
    this.values = new ArrayList<SEXP>();
    Collections.addAll(this.values, values);

    assert checkDims() : "dim do not match length of object";
  }

  public ListVector(SEXP[] values, AttributeMap attributes) {
    this(values, Null.INSTANCE, attributes);

    assert checkDims() : "dim do not match length of object";
  }

  public ListVector(SEXP... values) {
    this(values, AttributeMap.EMPTY);
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
 
  @Override
  public int indexOf(Vector vector, int vectorIndex, int startIndex) {
    for(int i=0;i!=values.size();++i) {
      SEXP element = values.get(i); 
      if(element instanceof AtomicVector && element.length() == 1) {
        if(((AtomicVector)element).indexOf(vector, vectorIndex, 0) != -1) {
          return i;
        } 
      } else if(element.equals(vector.getElementAsSEXP(vectorIndex))) {
        return i;
      }
    }
    return -1;
  }
  

  public int indexOfName(String name) {
    SEXP names = attributes.getNamesOrNull();
    if(names instanceof StringVector) {
      for(int i=0;i!=names.length();++i) {
        if(((StringVector) names).getElementAsString(i).equals(name)) {
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
  
  public double getElementAsDouble(String name) {
    return getElementAsDouble(getIndexByName(name));
  }
  
  public ListVector getElementAsList(String name) {
    return (ListVector)getElementAsSEXP(getIndexByName(name));
  }

  @Override
  public int getElementAsInt(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsInt(0);
    }
    throw new EvalException("(list) object cannot be coerced to type 'int'");
  }
  
  public int getElementAsInt(String name) {
    return getElementAsInt(getIndexByName(name));
  }

  @Override
  public String getElementAsString(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsString(0);
    }
    return Deparse.deparseExp(null, value);
  }

  @Override
  public Object getElementAsObject(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsObject(0);
    }
    return Deparse.deparseExp(null, value);
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
  public boolean contains(Vector vector, int vectorIndex) {
    SEXP match = vector.getElementAsSEXP(vectorIndex);
    for(int i=0;i!=this.length();++i) {
      if(values.get(i).equals(match)) {
        return true;
      }
    }
    return false;
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
    for(int i=0;i<Math.min(length(), 20);++i) {
      if(i!=0) {
        sb.append(", ");
      }
      if(!Strings.isNullOrEmpty(getName(i))) {
        sb.append(getName(i)).append(" = ");
      }
      sb.append(getElementAsSEXP(i));
    }
    if(length() > 20) {
      sb.append(", ...").append(length()).append(" elements total");
    }
    sb.append(")");
    return sb.toString();
  }

  public static Builder newBuilder() {
    return new Builder(0, 0);
  }

  public static Builder buildFromClone(ListVector toClone) {
    return new Builder(toClone);
  }
  
  public static NamedBuilder buildNamedFromClone(ListVector toClone) {
    return new NamedBuilder(toClone);
  }
  
  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public Vector.Builder newBuilderWithInitialSize(int initialSize) {
    return new Builder(initialSize, initialSize);
  }
 
  @Override
  public Builder newBuilderWithInitialCapacity(int initialCapacity) {
    return new Builder(0, initialCapacity);
  }
  
  public static NamedBuilder newNamedBuilder() {
    return new NamedBuilder();
  }
  
  public NamedBuilder newCopyNamedBuilder() {
    return new NamedBuilder(this);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ListVector(values, attributes);
  }

  public static class Builder extends AbstractVector.AbstractBuilder<SEXP> {
    private final List<SEXP> values;
    
    public Builder() {
      this(0,0);
    }
    
    public Builder(int initialSize, int initialCapacity) {
      values = new ArrayList<SEXP>(initialCapacity);
      for(int i=0;i!=initialSize;++i) {
        values.add(Null.INSTANCE);
      }
    }

    protected Builder(ListVector toClone) {
      values = Lists.newArrayList(toClone);
      copyAttributesFrom(toClone);
    }

    public Builder(int initialLength) {
      values = Lists.newArrayListWithCapacity(initialLength);
      for(int i=0;i!=initialLength;++i) {
        add(Null.INSTANCE);
      }
    }

    protected Builder remove(int index) {
      this.values.remove(index);
      return this;
    }

    @Override
    public Builder add(SEXP value) {
      values.add(value);
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
    public Builder setFrom(int destinationIndex, SEXP source, int sourceIndex) {
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
    
    protected List<SEXP> getValues() {
      return values;
    }
    
    public ListVector build() {
      return new ListVector(values, buildAttributes());
    }

    @Override
    public Builder add(Number value) {
      if(value instanceof Integer || value instanceof Byte || value instanceof Byte || 
          value instanceof Short) {
        add(new IntArrayVector(value.intValue()));
      } else {
        add(new DoubleArrayVector(value.doubleValue()));
      }
      return this;
    }
  }
  
  /**
   * Convenience builder for constructing lists with names
   *
   */
  public static class NamedBuilder extends Builder implements ListBuilder {
    private final NamesBuilder names;
    
    public NamedBuilder() {
      super();
      names = NamesBuilder.withInitialCapacity(10);
    }

    public NamedBuilder(int initialSize, int initialCapacity) {
      super(initialSize, initialCapacity);
      names = NamesBuilder.withInitialCapacity(initialCapacity);
    }

    protected NamedBuilder(ListVector toClone) {
      super(toClone);
      names = NamesBuilder.clonedFrom(toClone);
    }

    public NamedBuilder(int initialLength) {
      super(initialLength);
      names = NamesBuilder.withInitialLength(initialLength);
    }    
    
    
    public NamedBuilder add(String name, SEXP value) {
      add(value);
      names.set(length()-1, name);
      return this;
    }

    public NamedBuilder add(String name, Vector.Builder builder) {
      return add(name, builder.build());
    }

    public NamedBuilder add(Symbol name, SEXP value) {
      return add(name.getPrintName(), value);
    }

    public NamedBuilder add(String name, int value) {
      return add(name, new IntArrayVector(value));
    }
    
    public NamedBuilder add(String name, double value) {
      return add(name, new DoubleArrayVector(value));
    }
    
    @Override
    public NamedBuilder add(SEXP value) {
      super.add(value);
      return this;
    }
    
    @Override
    public int getIndexByName(String nameToReplace) {
      return names.getIndexByName(nameToReplace);
    }

    public NamedBuilder add(String name, String value) {
      return add(name, new StringArrayVector(value));
    }

    public NamedBuilder add(String name, boolean value) {
      return add(name, new LogicalArrayVector(value));
    }

    public NamedBuilder add(String name, Logical value) {
      return add(name, new LogicalArrayVector(value));
    }
    
    public NamedBuilder addAll(ListVector list) {
      for(int i=0;i!=list.length();++i) {
        add(list.getName(i), list.get(i));
      }
      return this;
    }
    
    @Override
    public NamedBuilder set(int index, SEXP value) {
      super.set(index, value);
      return this;
    }

    @Override
    public NamedBuilder remove(int index) {
      super.remove(index);
      names.remove(index);
      return this;
    }

    @Override
    protected AttributeMap buildAttributes() {
      if(names.haveNames()) {
        setAttribute(Symbols.NAMES, names.build(length()));
      }
      return super.buildAttributes();
    }    
  }

  private static class ListType extends Vector.Type {
    public ListType() {
      super(Order.LIST);
    }

    public Builder newBuilder() {
      return new Builder(0, 0);
    }
    
    @Override
    public Builder newBuilderWithInitialSize(int initialSize) {
      return new Builder(initialSize);
    }
   
    @Override
    public Builder newBuilderWithInitialCapacity(int initialCapacity) {
      return new Builder(0, initialCapacity);
    }

    @Override
    public int compareElements(Vector vector1, int index1, Vector vector2, int index2) {
      // TODO: should compareElements be a method on some AtomicVectorType class??
      throw new UnsupportedOperationException();
    }
  
    @Override
    public boolean elementsEqual(Vector vector1, int index1, Vector vector2,
        int index2) {
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
      return !Strings.isNullOrEmpty(name);
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
