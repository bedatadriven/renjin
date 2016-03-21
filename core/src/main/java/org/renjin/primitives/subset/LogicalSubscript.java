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

package org.renjin.primitives.subset;

import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;

/**
 * Logical subscripts, i.e: x[TRUE, FALSE]
 *
 * <p>
 * Logical subscripts act as filters. So x[TRUE,FALSE] selects
 * the first element but not the second.
 */
public class LogicalSubscript extends Subscript {
  private int dimLength;
  private LogicalVector subscript;
  
  private int count;
  private int[] indices;

  public LogicalSubscript(int dimLength, LogicalVector subscript) {
    this.dimLength = dimLength;
    this.subscript = subscript;
  }

  private void computeArray() {
    count = 0;
    if(subscript.length() == 0) {
      indices = new int[0];
    } else {
      indices = new int[dimLength];
  
      for(int i=0;i!=indices.length;++i) {
        int subscriptIndex = i % subscript.length();
        int value = subscript.getElementAsRawLogical(subscriptIndex);
        if(value == 1) {
          indices[count++] = i;
        } else if(IntVector.isNA(value)) {
          indices[count++] = IntVector.NA;
        }
      }
    }
  }

  @Override
  public int getCount() {
    if(indices == null) {
      computeArray();
    }
    return count;
  }

  @Override
  public int getAt(int i) {
    if(indices == null) {
      computeArray();
    }
    return indices[i];
  }

  @Override
  public IndexIterator iterator() {
    
    if(subscript.length() == 0) {
      return EmptyIndexIterator.INSTANCE;
    }
    
    return new IndexIterator() {
      
      private int i;
      private int subscriptIndex;
      
      private int next = computeNext();

      private int computeNext() {
        while(true) {
          if(i == dimLength) {
            return -1;
          }
          if(subscriptIndex == subscript.length()) {
            subscriptIndex = 0;
          }
          int index = i++;
          int included = subscript.getElementAsRawLogical(subscriptIndex++);
          
          if(included == 1) {
            return index;
          } 
          if(IntVector.isNA(included)) {
            return included;
          }
        }
      }
      
      @Override
      public boolean hasNext() {
        return next != -1;
      }

      @Override
      public int next() {
        int toReturn = next;
        next = computeNext();
        return toReturn;
      }

    };
  }
}
