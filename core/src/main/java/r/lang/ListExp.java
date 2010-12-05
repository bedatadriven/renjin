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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generic vector of {@code SEXP}s
 */
public class ListExp extends AbstractSEXP implements Iterable<SEXP>, HasElements {

  private static final int TYPE_CODE = 19;
  private static final String TYPE_NAME = "list";

  private final ArrayList<SEXP> values;

  public ListExp(Iterable<SEXP> values,  PairList attributes) {
    super(NullExp.INSTANCE, attributes);
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
  }

  public ListExp(Iterable<SEXP> values) {
    this(values, NullExp.INSTANCE);
  }

  public ListExp(SEXP[] values, SEXP tag, PairList attributes) {
    super(tag, attributes);
    this.values = new ArrayList<SEXP>();
    Collections.addAll(this.values, values);
  }

  public ListExp(SEXP[] values, PairList attributes) {
    this(values, NullExp.INSTANCE, attributes);
  }

  public ListExp(SEXP... values) {
    this(values, NullExp.INSTANCE);
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
  public boolean isWiderThan(Object vector) {
    return vector instanceof HasElements;
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
    if(names instanceof StringExp) {
      for(int i=0;i!=names.length();++i) {
        if(((StringExp) names).get(i).equals(name)) {
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
      return NullExp.INSTANCE;
    }
    return values.get(index);
  }

  @Override
  public SEXP getExp(int index) {
    return values.get(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ListExp listExp = (ListExp) o;

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

  public static Builder buildFromClone(ListExp toClone) {
    return new Builder(toClone);
  }

  @Override
  public Builder newCopyBuilder() {
    return new Builder(this);
  }

  @Override
  public HasElements.Builder newBuilder(int initialSize) {
    return new Builder();
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new ListExp(values, attributes);
  }

  public static class Builder implements HasElements.Builder<ListExp,HasElements> {
    private PairList attributes = NullExp.INSTANCE;
    private boolean haveNames = false;
    private List<SEXP> values = Lists.newArrayList();
    private List<String> names = Lists.newArrayList();

    private Builder() {
    }

    private Builder(ListExp toClone) {
      Iterables.addAll(values, toClone);
      SEXP names = toClone.getAttribute(SymbolExp.NAMES);
      if(names instanceof StringExp) {
        Iterables.addAll(this.names, (StringExp)names);
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
      return add(name, new IntExp(value));
    }

    public Builder add(String name, String value) {
      return add(name, new StringExp(value));
    }

    public Builder add(String name, boolean value) {
      return add(name, new LogicalExp(value));
    }

    public Builder add(String name, Logical value) {
      return add(name, new LogicalExp(value));
    }

    public Builder addAll(ListExp list) {
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
        add(NullExp.INSTANCE);
      }
      values.set(index, value);
      return this;
    }

    @Override
    public Builder setNA(int index) {
      return set(index, NullExp.INSTANCE);
    }

    @Override
    public Builder setFrom(int destinationIndex, HasElements source, int sourceIndex) {
      return set(destinationIndex, source.getExp(sourceIndex));
    }

    public SEXP build(int length) {
      return null;
    }

    public Builder replace(int i, SEXP value) {
      values.set(i, value);
      return this;
    }

    public ListExp build() {
      if(haveNames) {
        return new ListExp(values,  PairListExp.buildList(SymbolExp.NAMES, new StringExp(names)).build());
      } else {
        return new ListExp(values);
      }
    }
  }
}
