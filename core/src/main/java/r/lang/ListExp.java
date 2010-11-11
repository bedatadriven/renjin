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

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Generic vector of {@code SEXP}s
 */
public class ListExp extends SEXP implements Iterable<SEXP> {

  private static final int TYPE_CODE = 19;
  private static final String TYPE_NAME = "list";

  private ArrayList<SEXP> values;

  public ListExp(Iterable<SEXP> values) {
    this.values = new ArrayList<SEXP>();
    Iterables.addAll(this.values, values);
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

  public SEXP get(String name) {
    int index = indexOfName(name);
    if(index == -1) {
      return NullExp.INSTANCE;
    }
    return values.get(index);
  }
}
