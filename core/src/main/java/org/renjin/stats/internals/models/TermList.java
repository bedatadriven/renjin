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

import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Iterators;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TermList implements Iterable<Term> {
  private List<Term> list = Lists.newArrayList();
  
  public void add(Term term) {
    if(!list.contains(term)) {
      list.add(term);
    }
  }
  
  public void add(TermList termsToAdd) {
    Iterables.addAll(this.list, termsToAdd);
  }
  
  public void subtract(Term term) {
    list.remove(term);
  }

  public void subtract(TermList toRemove) {
    for(Term term : toRemove) {
      subtract(term);
    }
  }
  
  public TermList sorted() {
    Collections.sort(list, new Comparator<Term>() {

      @Override
      public int compare(Term a, Term b) {
        return a.getExpressions().size() - b.getExpressions().size();
      }
    });
    return this;
  }
  
  @Override
  public Iterator<Term> iterator() {
    return Iterators.unmodifiableIterator(list.iterator());
  }
}
