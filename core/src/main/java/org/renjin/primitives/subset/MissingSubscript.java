/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

/**
 * 
 */
public class MissingSubscript implements Subscript {
  
  private int sourceLength;

  public MissingSubscript(int sourceLength) {
    this.sourceLength = sourceLength;
  }

  @Override
  public int computeUniqueIndex() {
    throw new EvalException("[[ ]] with missing subscripts");
  }

  @Override
  public IndexIterator computeIndexes() {
    return new IndexIterator() {
      
      private int i = 0;
      
      @Override
      public int next() {
        if(i >= sourceLength) {
          return EOF;
        }
        return i++;
      }

      @Override
      public void restart() {
        i = 0;
      }
    };
  }

  @Override
  public IndexPredicate computeIndexPredicate() {
    return new IndexPredicate() {
      @Override
      public boolean apply(int index) {
        return true;
      }
    };
  }

  @Override
  public int computeCount() {
    return sourceLength;
  }
}
