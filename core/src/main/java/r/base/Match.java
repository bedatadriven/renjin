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

package r.base;

import r.lang.*;

public class Match {

  private static final int UNMATCHED = -1;

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

  public static IntVector pmatch(StringVector search, StringVector table, int noMatch, boolean duplicatesOk) {
    IntVector.Builder result = new IntVector.Builder(search.length());
    boolean matchedTable[] = new boolean[table.length()];
    boolean matchedSearch[] = new boolean[search.length()];

    // first pass : exact matches
    for(int i=0;i!=search.length();++i) {
      String toMatch = pmatchElementAt(search, i);
      int match = exactMatch(toMatch, table);
      if(match != UNMATCHED && (duplicatesOk || !matchedTable[match])) {
        result.set(i, match+1);
        matchedTable[match] = true;
        matchedSearch[i] = true;
      }
    }

    // second pass : partial matches
    for(int i=0;i!=search.length();++i) {
      if(!matchedSearch[i]) {
        String toMatch = pmatchElementAt(search, i);
        int match = uniquePartialMatch(toMatch, table);
        if(match != UNMATCHED && (duplicatesOk || !matchedTable[match])) {
          result.set(i, match+1);
          matchedTable[match] = true;
        } else {
          result.set(i, noMatch);
        }
      }
    }
    return result.build();
  }

  private static int exactMatch(String toMatch, StringVector table) {
    for(int i=0;i!=table.length();++i) {
         String t = pmatchElementAt(table, i);
         if(toMatch.equals(t)) {
           return i;
         }
    }
    return -1;
  }

  private static int uniquePartialMatch(String toMatch, StringVector table) {
    int partialMatch = UNMATCHED;
    for(int i=0;i!=table.length();++i) {
      String t = pmatchElementAt(table, i);
      if(t.startsWith(toMatch)) {
        // if we've previously found a partial match, abort
        if(partialMatch != UNMATCHED) {
          return UNMATCHED;
        }
        partialMatch = i;
      }
    }
    return partialMatch;
  }

  // NA values are treated as if they were the string constant "NA".
  private static String pmatchElementAt(StringVector vector, int i) {
    return vector.isElementNA(i) ? "NA" : vector.getElementAsString(i);
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
