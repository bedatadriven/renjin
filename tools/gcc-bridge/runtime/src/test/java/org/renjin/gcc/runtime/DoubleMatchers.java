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
package org.renjin.gcc.runtime;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class DoubleMatchers {


  public static Matcher<Double> isNaN() {
    return new TypeSafeMatcher<Double>() {


      @Override
      protected boolean matchesSafely(Double aDouble) {
        return Double.isNaN(aDouble);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("NaN");
      }
    };
  }
  
  public static Matcher<Double> isPositiveZero() {
    return new TypeSafeMatcher<Double>() {
      @Override
      protected boolean matchesSafely(Double aDouble) {
        return aDouble.equals(new Double(+0.0));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("+0.0");
      }
    };
  }


  public static Matcher<Double> isNegativeZero() {
    return new TypeSafeMatcher<Double>() {
      @Override
      protected boolean matchesSafely(Double aDouble) {
        return aDouble.equals(new Double(-0.0));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("-0.0");
      }
    };
  }

}
