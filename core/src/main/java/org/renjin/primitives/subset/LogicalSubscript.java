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
package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;

/**
 * Matrix subscript 
 */
class LogicalSubscript implements Subscript {
  
  private LogicalVector subscript;
  private int resultLength;

  public LogicalSubscript(LogicalVector subscript, int sourceLength) {
    this.subscript = subscript;
    this.resultLength = Math.max(sourceLength, subscript.length());
  }

  @Override
  public int computeUniqueIndex() {
    // In the context of the [[ operator, we treat logical subscripts as integers
    SubsetAssertions.checkUnitLength(subscript);

    int oneBasedIndex = subscript.getElementAsInt(0);
    if(IntVector.isNA(oneBasedIndex)) {
      throw new EvalException("subscript out of bounds");
    }
    if(oneBasedIndex == 0) {
      throw new EvalException("attempt to select less than one element");
    }

    return oneBasedIndex - 1;
  }

  @Override
  public IndexIterator computeIndexes() {
    if(subscript.length() == 0) {
      return EmptyIndexIterator.INSTANCE;
    }
    return new Iterator();
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    return new LogicalPredicate(subscript);
  }
  
  private class Iterator implements IndexIterator {

    int nextIndex = 0;

    @Override
    public int next() {
      while(true) {
        if(nextIndex >= resultLength) {
          return EOF;
        }
        int sourceIndex = nextIndex++;
        int subscriptValue = subscript.getElementAsRawLogical(sourceIndex % subscript.length());
        if(subscriptValue == 1) {
          return sourceIndex;
        }
        if(IntVector.isNA(subscriptValue)) {
          return IntVector.NA;
        }
      }
    }

    @Override
    public void restart() {
      nextIndex = 0;
    }
  }

}
