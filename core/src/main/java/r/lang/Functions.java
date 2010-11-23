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

package r.lang;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Functions {
  private Functions() {}

  public static Function<SEXP, Integer> length() {
    return new Function<SEXP, Integer>() {
      @Override
      public Integer apply(SEXP input) {
        return input.length();
      }
    };
  }

  public static Function<StringExp, String> elementAt(final int index) {
    return new Function<StringExp, String>() {
      @Override
      public String apply(StringExp input) {
        return input.get( index % input.length() );
      }
    };
  }

  public static Predicate<SEXP> modePredicate(String mode) {
    if(mode.equals("any")) {
      return Predicates.alwaysTrue();
    } else {
      throw new UnsupportedOperationException("only mode 'any' as a predicate is implemented.");
    }
  }
}
