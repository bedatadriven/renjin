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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generic vector of {@code SEXP}s
 */
public class ListExp extends SEXP implements Iterable<SEXP> {

  private static final int TYPE_CODE = 19;
  private static final String TYPE_NAME = "list";

  private ArrayList<SEXP> values;


  public ListExp(Iterable<SEXP> values,  PairList attributes) {
    super(attributes);
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
  }

  public ListExp(Iterable<SEXP> values) {
    this(values, NullExp.INSTANCE);
  }

  public ListExp(SEXP[] values, PairList attributes) {
    super(attributes);
    this.values = new ArrayList<SEXP>();
    Collections.addAll(this.values, values);

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

  public static Builder build() {
    return new Builder();
  }



  public static class Builder {
    private boolean haveNames = false;
    private List<SEXP> values = new ArrayList<SEXP>();
    private List<String> names = new ArrayList<String>();


    public Builder add(String name, SEXP value) {
      values.add(value);
      names.add(name);
      haveNames = true;
      return this;
    }

    public Builder add(SEXP value) {
      values.add(value);
      names.add("");
      return this;
    }

    public ListExp build() {
      if(haveNames) {
        return new ListExp(values,  PairListExp.buildList(SymbolExp.NAMES, new StringExp(names)).list());
      } else {
        return new ListExp(values);
      }
    }
  }



}
