package org.renjin.primitives.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.renjin.sexp.SEXP;


import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Term other = (Term) obj;
    return expressions.equals(other.expressions);
  }

  @Override
  public String toString() {
    return "Term(" + getLabel() + ")";
  }

  public String getLabel() {
    return Joiner.on(" : ").join(expressions);
  }
  
  @Override
  public Iterator<SEXP> iterator() {
    return Iterators.unmodifiableIterator(expressions.iterator());
  }

}
