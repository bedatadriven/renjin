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

import org.renjin.eval.EvalException;

/**
 * Specialized PairList used in the course of argument matching.
 * (The DOTSEXP type in the original R interpreter)
 */
public interface PromisePairList extends PairList {
  
  public static class Node extends PairList.Node implements PromisePairList {

    private Node(SEXP tag, SEXP value, PairList nextNode) {
      super(tag, value, nextNode);
    }

    @Override
    public String getTypeName() {
      return "...";
    }
  }
  
  public static class Builder {
    private Node head = null;
    private Node tail = null;
    
    public Builder add(SEXP tag, SEXP promise) {
      if (head == null) {
        head = new Node(tag, promise, Null.INSTANCE);
        tail = head;
      } else {
        Node next = new Node(tag, promise, Null.INSTANCE);
        tail.nextNode = next;
        tail = next;
      }
      return this;
    }
    
    public PromisePairList build() {
      if(head == null) {
        return Null.INSTANCE;
      } else {
        return head;
      }
    } 
    
    public static PromisePairList fromList(ListVector vector) {
      Builder list = new Builder();
      for(NamedValue namedValue : vector.namedValues()) {
        if(namedValue.hasName()) {
          list.add(Symbol.get(namedValue.getName()), Promise.repromise(namedValue.getValue()));
        } else {
          list.add(Null.INSTANCE, Promise.repromise(namedValue.getValue()));
        }
      }
      return list.build();
    }
  }
}
