/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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


  @Test
  public void formatA() {
    assertThat(sprintf("%a", 12), equalTo("0x1.8p+3"));

    assertThat(sprintf("%a", 1000), equalTo("0x1.f4p+9"));
    assertThat(sprintf("%A", 1000), equalTo("0X1.F4P+9"));

  }

  @Test
  public void formatA15() {
    assertThat(sprintf("%a", Math.pow(Math.sqrt(2.0), 2.0)), equalTo("0x1.0000000000001p+1"));
  }


  private String sprintf(String s, double x) {

    AtomicVector arguments[] = new AtomicVector[] {DoubleVector.valueOf(x) };

    Formatter formatter = new Formatter(s);
    return formatter.sprintf(arguments, 0);
  }

}