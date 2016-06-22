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
