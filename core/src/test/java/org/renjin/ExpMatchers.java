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
package org.renjin;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.renjin.sexp.*;

public class ExpMatchers {

  public static BaseMatcher<SEXP> symbolNamed(final String name) {
    return new BaseMatcher<SEXP>() {
      @Override
      public boolean matches(Object o) {
        return o instanceof Symbol && ((Symbol) o).getPrintName().equals(name);
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
        return o instanceof IntVector && ((IntVector) o).getElementAsInt(0) == value;
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
        return o instanceof DoubleVector && ((DoubleVector) o).get(0) == value;
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
        return o instanceof StringVector && ((StringVector) o).getElementAsString(0).equals(string);
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
        return o instanceof LogicalVector && ((LogicalVector) o).getElementAsInt(0) == value.getInternalValue();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("logical vector [").appendValue(value).appendValue("]");
      }
    };
  }
}
