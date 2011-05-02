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

package r.base.subscripts;

import r.lang.AtomicVector;
import r.lang.IntVector;
import r.lang.exception.EvalException;

public class PositionalSubscript extends Subscript {
  private final int indices[];
  private int count;

  public PositionalSubscript(AtomicVector vector) {
    indices = new int[vector.length()];
    for(int i=0;i!=indices.length;++i) {
      int index = vector.getElementAsInt(i);
      if(index != 0) {
        if(IntVector.isNA(index)
           ) {
          indices[count++] = IntVector.NA;
        } else {
          indices[count++] = index-1;
        }
      }
    }
  }

  static boolean arePositions(AtomicVector indices) {
    boolean hasNeg = false;
    boolean hasPos = false;

    for(int i=0;i!=indices.length();++i) {
      int index = indices.getElementAsInt(i);
      if(index > 0 || IntVector.isNA(index)) {
        hasPos = true;
      } else if(index < 0) {
        hasNeg = true;
      }
    }
    if(hasNeg && hasPos) {
      throw new EvalException("only 0's may be mixed with negative subscripts");
    }
    return !hasNeg;
  }


  @Override
  public int getCount() {
    return count;
  }

  @Override
  public int getAt(int i) {
    return indices[i];
  }
}
