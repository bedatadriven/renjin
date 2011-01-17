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

package r.lang.primitive;

import r.lang.AtomicVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.Vector;

public class Match {

  public static int[] match(AtomicVector search, AtomicVector table, int noMatch, AtomicVector incomparables) {
    //For historical reasons, FALSE is equivalent to NULL.
    if(incomparables.equals( LogicalVector.FALSE ) ) {
      incomparables = Null.INSTANCE;
    }

    int[] matches = new int[search.length()];
    for(int i=0;i!=search.length();++i) {
      if( incomparables.contains(search, i)) {
        matches[i] = noMatch;
      } else {
        int pos = table.indexOf(search, i, 0);
        matches[i] = pos >= 0 ? pos+1 : noMatch;
      }
    }
    return matches;
  }

  public static Vector unique(AtomicVector vector, AtomicVector incomparables, boolean fromLast) {
    Vector.Builder result = vector.newBuilder(0);
    int resultIndex=0;
    for(int i=0;i!=vector.length();++i) {
      if(   incomparables.contains(vector, i) ||
           (fromLast && vector.indexOf(vector, i, i+1) == -1) ||
          (!fromLast && vector.indexOf(vector, i, 0) == i)) {

        result.setFrom(resultIndex++, vector, i);

      }
    }
    return result.build();
  }
}
