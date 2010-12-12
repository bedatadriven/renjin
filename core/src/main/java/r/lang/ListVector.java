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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.math.complex.Complex;
import r.lang.exception.EvalException;
import r.lang.primitive.Parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generic vector of {@code SEXP}s
 */
public class ListVector extends AbstractSEXP implements Vector, Iterable<SEXP> {

  private static final int TYPE_CODE = 19;
  public static final String TYPE_NAME = "list";

  public static final Vector.Type VECTOR_TYPE = new ListType();

  private final ArrayList<SEXP> values;

  public ListVector(Iterable<SEXP> values,  PairList attributes) {
    super(Null.INSTANCE, attributes);
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
  }

  public ListVector(Iterable<SEXP> values) {
    this(values, Null.INSTANCE);
  }

  public ListVector(SEXP[] values, SEXP tag, PairList attributes) {
    super(tag, attributes);
    this.values = new ArrayList<SEXP>();
    Collections.addAll(this.values, values);
  }

  public ListVector(SEXP[] values, PairList attributes) {
    this(values, Null.INSTANCE, attributes);
  }

  public ListVector(SEXP... values) {
    this(values, Null.INSTANCE);
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
    SEXP names = attributes.findByTag(SymbolExp.NAMES);
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
  public Logical getElementAsLogical(int index) {
    SEXP value = values.get(index);
    if(value.length() == 1 && value instanceof AtomicVector) {
      return ((AtomicVector) value).getElementAsLogical(0);
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
  public Iterable<SEXP> elements() {
    return this;
  }

  /**
   * @return the length of the longest element
   */
  public int longestElementLength() {
    int max = 0;
    for(SEXP element : this) {
      if(element.length() > max) {
        max = element.length();
      }
    }
    return max;
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

  public static class Builder implements Vector.Builder<SEXP> {
    private PairList attributes = Null.INSTANCE;
    private boolean haveNames = false;
    private List<SEXP> values = Lists.newArrayList();
    private List<String> names = Lists.newArrayList();

    public Builder() {
    }

    private Builder(ListVector toClone) {
      Iterables.addAll(values, toClone);
      SEXP names = toClone.getAttribute(SymbolExp.NAMES);
      if(names instanceof StringVector) {
        Iterables.addAll(this.names, (StringVector)names);
        haveNames = true;
      } else {
        for(SEXP value : values) { this.names.add(""); }
      }
      this.attributes = toClone.getAttributes();
    }

    public Builder setAttributes(PairList attributes) {
      this.attributes = attributes;
      return this;
    }

    public Builder add(String name, SEXP value) {
      values.add(value);
      names.add(name);
      if(!name.isEmpty()) {
        haveNames = true;
      }
      return this;
    }

    public Builder add(SymbolExp name, SEXP value) {
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

    public Builder add(SEXP value) {
      values.add(value);
      names.add("");
      return this;
    }

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
      if(source instanceof Vector) {
        return this.set(destinationIndex, ((Vector)source).getElementAsSEXP(sourceIndex));
      } else {
        return this.set(destinationIndex, source);
      }
    }

    public Builder replace(int i, SEXP value) {
      values.set(i, value);
      return this;
    }

    public ListVector build() {
      if(haveNames) {
        return new ListVector(values,  PairList.Node.buildList(SymbolExp.NAMES, new StringVector(names)).build());
      } else {
        return new ListVector(values);
      }
    }
  }

  private static class ListType extends Vector.Type {
    public ListType() {
      super(Order.LIST);
    }

    public Builder newBuilder() {
      return new Builder();
    }
  }
}
