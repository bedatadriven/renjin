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
package org.renjin.stats.internals.models;

import org.renjin.primitives.Deparse;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Iterators;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.SEXP;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Term implements Iterable<SEXP> {
  private List<SEXP> expressions;
  
  public Term(SEXP... expressions) {
    this.expressions = Lists.newArrayList(expressions);
  }
  
  public Term(Iterable<SEXP> expressions) {
    this.expressions = Lists.newArrayList(expressions);
  }

  public Term(Term a, Term b) {
    this.expressions = Lists.newArrayList();
    this.expressions.addAll(a.getExpressions());
    this.expressions.addAll(b.getExpressions());
  }

  public Collection<SEXP> getExpressions() {
    return Collections.unmodifiableCollection(this.expressions);
  }

  public int getOrder() {
    return expressions.size();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((expressions == null) ? 0 : expressions.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Term other = (Term) obj;
    return expressions.equals(other.expressions);
  }

  @Override
  public String toString() {
    return "Term(" + getLabel() + ")";
  }

  public String getLabel() {
    return Deparse.deparseExp(null, expressions.get(0));
  }
  
  @Override
  public Iterator<SEXP> iterator() {
    return Iterators.unmodifiableIterator(expressions.iterator());
  }

}
