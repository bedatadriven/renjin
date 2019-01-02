/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;

import java.util.HashSet;
import java.util.Set;


class IndexSubscript implements Subscript {
  private AtomicVector subscript;
  private int sourceLength;

  public IndexSubscript(AtomicVector subscript, int sourceLength) {
    this.subscript = subscript;
    this.sourceLength = sourceLength;
  }

  @Override
  public int computeUniqueIndex() {
    SubsetAssertions.checkUnitLength(subscript);

    int index = subscript.getElementAsInt(0);
    if(index == 0) {
      throw new EvalException("attempt to select less than one element");
    }

    if(index > 0) {
      // Positive indexes select a single element
      return index-1;
    }

    if(index < 0) {
      // Negative indexes can only be used in very special cases
      int excludedIndex = (-index) - 1;
      if(sourceLength == 1 && excludedIndex != 0) {
        // Not excluding anything...
        return 0;
      }
      if(sourceLength == 2) {
        if(excludedIndex == 0) {
          return 1;
        } else if(excludedIndex == 1) {
          return 0;
        }
      }
    }
    throw new EvalException("attempt to select more than one element");
  }

  @Override
  public IndexIterator computeIndexes() {
    int sign = computeIndexSign();
    
    if(sign == -1) {
      return new HashedNegativeIndexIterator();
    
    } else if(sign == +1) {
      return new PositiveIndexIterator();

    } else {
      return EmptyIndexIterator.INSTANCE;
    }
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    int sign = computeIndexSign();
    
    if(sign == -1) {
      return new HashedNegativeIndexPredicate();

    } else {
      return new PositiveHashPredicate();
    }
  }

  @Override
  public int computeCount() {
    int count = 0;
    IndexIterator it = computeIndexes();
    while(it.next() != IndexIterator.EOF) {
      count++;
    }
    return count;
  }

  private int computeIndexSign() {
    for (int i = 0; i < subscript.length(); i++) {
      int index = subscript.getElementAsInt(i);
      if(IntVector.isNA(index)) {
        return 1;
      }
      if(index == 0) {
        continue;
      }
      if(index < 0) {
        return -1;

      } else if(index > 0) {
        return +1;
      }
    }
    return 0;
  }

  private Set<Integer> buildExcludedSet() {
    Set<Integer> excludedSet = new HashSet<>();
    for (int i = 0; i < subscript.length(); i++) {
      int subscript = IndexSubscript.this.subscript.getElementAsInt(i);
      if (subscript < 0) {
        int excludedIndex = (-subscript) - 1;
        excludedSet.add(excludedIndex);
      } else if (subscript != 0) {
        throw new EvalException("only 0's may be mixed with negative subscripts");
      }
    }
    return excludedSet;
  }
  
  private class HashedNegativeIndexIterator implements IndexIterator {

    private int nextIndex = 0;
    private Set<Integer> excludeSet = buildExcludedSet();

    @Override
    public int next() {
      while(true) {
        if(nextIndex >= sourceLength) {
          return EOF;
        }
        if(!excludeSet.contains(nextIndex)) {
          return nextIndex++;
        }
        nextIndex++;
      }
    }

    @Override
    public void restart() {
      nextIndex = 0;
    }
  }
  
  private class HashedNegativeIndexPredicate implements IndexPredicate {
    
    private Set<Integer> excludeSet = buildExcludedSet();

    @Override
    public boolean apply(int index) {
      return excludeSet.contains(index);
    }
  }

  private class PositiveIndexIterator implements IndexIterator {
    
    private int nextSubscriptIndex = 0;
    
    @Override
    public int next() {
      while(true) {
        if(nextSubscriptIndex >= subscript.length()) {
          return EOF;
        }
        int nextSubscript = subscript.getElementAsInt(nextSubscriptIndex++);
        if(IntVector.isNA(nextSubscript)) {
          return nextSubscript;
          
        } else if(nextSubscript > 0) {
          // Convert from 1-based to zero-based
          return nextSubscript - 1;
          
        } else if(nextSubscript < 0) {
          throw new EvalException("only 0's may be mixed with negative subscripts");
        }
      }
    }

    @Override
    public void restart() {
      nextSubscriptIndex = 0;
    }
  }
  
  private class PositiveHashPredicate implements IndexPredicate {

    private Set<Integer> includeSet = new HashSet<>();
    
    public PositiveHashPredicate() {
      for(int i=0;i<subscript.length();++i) {
        int index = subscript.getElementAsInt(i);
        if(!IntVector.isNA(index) && index > 0) {
          includeSet.add(index - 1);
        }
      }
    }

    @Override
    public boolean apply(int index) {
      return includeSet.contains(index);
    }
  }
}

