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

import r.lang.*;
import r.lang.primitive.binding.AtomicAccessor;
import r.lang.primitive.binding.AtomicAccessors;
import r.lang.primitive.binding.TypeConverter;

public class Match {

  

  public static int[] match(AtomicExp searchExp, AtomicExp tableExp, int noMatch, AtomicExp incomparablesExp) {
    //For historical reasons, FALSE is equivalent to NULL.
    if(incomparablesExp.equals( LogicalExp.FALSE ) ) {
      incomparablesExp = NullExp.INSTANCE;
    }

    // I'm not entirely comfortable with using AtomicAccessors inside primitive definitions as
    // I imagine the boxing-unboxing plus virtual function call overhead is considerable,
    // but at this stage I think the performance gain is worth trading for simpler code..
    // (The alternative is 5 separate implementations for int, double, complex, String, etc)

    Class commonType = TypeConverter.commonAtomicElementType(searchExp, tableExp);
    AtomicAccessor search = AtomicAccessors.create(searchExp, commonType);
    AtomicAccessor table = AtomicAccessors.create(tableExp, commonType);
    AtomicAccessor incomparables = AtomicAccessors.create(incomparablesExp, commonType);

    int[] matches = new int[search.length()];
    for(int i=0;i!=search.length();++i) {
      if( find(incomparables, search.get(i), search.isNA(i)) != -1 ) {
        matches[i] = noMatch;
      } else {
        int pos = find(table, search.get(i), search.isNA(i));
        matches[i] = pos >= 0 ? pos+1 : noMatch;
      }
    }
    return matches;
  }


  private static int find(AtomicAccessor values, Object value, boolean valueIsNA) {
    for(int i=0;i!=values.length();++i) {
      if(valueIsNA) {
        if(values.isNA(i)) {
          return i;
        }
      } else {
        if(values.get(i).equals(value)) {
          return i;
        }
      }
    }
    return -1;
  }


  private static Class commonAtomicTypeOrStringExp(SEXP x, SEXP table) {
    Class commonType;
    if(TypeConverter.allAreAtomic(x, table)) {
      commonType = TypeConverter.commonAtomicType(x, table);
    }  else {
      commonType = StringExp.class;
    }
    return commonType;
  }


}
