package org.renjin.primitives.text;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class FormatterTest {

  @Test
  public void floatingPointCoercedToIntegerIfIntegralValue() {
    assertThat(sprintf("Sven is %i feet tall", 7.0), equalTo("Sven is 7 feet tall"));
  }

  @Test(expected = EvalException.class)
  public void fractionalValuesCannotBeFormattedWithF() {
    sprintf("Sven is %i feet tall", 7.5);
  }

  @Test
  public void floatingPoint() {
    assertThat(sprintf("%f", Math.PI), equalTo("3.141593"));
    assertThat(sprintf("%.3f", Math.PI), equalTo("3.142"));
    assertThat(sprintf("%1.0f", Math.PI), equalTo("3"));
    assertThat(sprintf("%5.1f", Math.PI), equalTo("  3.1"));

    assertThat(sprintf("%05.1f", Math.PI), equalTo("003.1"));
    assertThat(sprintf("%+f", Math.PI), equalTo("+3.141593"));
    assertThat(sprintf("% f", Math.PI), equalTo(" 3.141593"));
    assertThat(sprintf("%-10f", Math.PI), equalTo("3.141593  "));
    assertThat(sprintf("%e", Math.PI), equalTo("3.141593e+00"));
    assertThat(sprintf("%E", Math.PI), equalTo("3.141593E+00"));
  }

  @Test
  @Ignore("needs to be fixed")
  public void formatG() {
    // In R, the precision is the number of significant digits
    // where as the implementation we're using interprets the 
    // precision as the number of digits after the radix.
    assertThat(sprintf("%g", Math.PI), equalTo("3.14159"));
    assertThat(sprintf("%7.6g", Math.PI), equalTo("3.14159"));
    assertThat(sprintf("%g",   1e6 * Math.PI), equalTo("3.14159e+06"));
    assertThat(sprintf("%.9g", 1e6 * Math.PI), equalTo("3141592.65"));
    assertThat(sprintf("%G", 1e-6 * Math.PI), equalTo("3.14159E-06"));
  }


  private String sprintf(String s, double x) {

    AtomicVector arguments[] = new AtomicVector[] {DoubleVector.valueOf(x) };

    Formatter formatter = new Formatter(s);
    return formatter.sprintf(arguments, 0);
  }

}