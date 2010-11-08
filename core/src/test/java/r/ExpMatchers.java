/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import r.lang.*;

public class ExpMatchers {

  public static BaseMatcher<SEXP> symbolNamed(final String name) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof SymbolExp && ((SymbolExp) o).getPrintName().equals(name);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("symbol named ").appendValue(name);
      }
    };
  }

  public static BaseMatcher<SEXP> intVectorOf(final int value) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof IntExp && ((IntExp) o).get(0) == value;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(value);
      }
    };
  }

  public static BaseMatcher<SEXP> realVectorEqualTo(final double value) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof DoubleExp && ((DoubleExp) o).get(0) == value;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("real vector [").appendValue(value).appendValue("]");
      }
    };
  }

  public static BaseMatcher<SEXP> stringVectorOf(final String string) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof StringExp && ((StringExp) o).get(0).equals(string);
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(string);
      }
    };
  }

  public static BaseMatcher<SEXP> anyExp() {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof SEXP;
      }

      @Override
      public void describeTo(Description description) {

      }
    };
  }

  public static Matcher<SEXP> logicalVectorOf(final Logical value) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof LogicalExp && ((LogicalExp) o).get(0) == value.getInternalValue();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("logical vector [").appendValue(value).appendValue("]");
      }
    };
  }
}
