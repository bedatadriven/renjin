/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.format;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.format.Formatter.ArgumentType;
import static org.renjin.gcc.format.Formatter.sprintf;

public class FormatterTest {

  @Test
  public void helloWorld() {
    Formatter formatter = new Formatter("Hello %s");
    assertThat(formatter.getArgumentTypes(), Matchers.hasItems(ArgumentType.STRING));
    assertThat(formatter.sprintf("World"), equalTo("Hello World"));
  }

  @Test
  public void positionalArguments() {
    Formatter formatter = new Formatter("%2$d %1$s");
    assertThat(formatter.getArgumentTypes(), Matchers.hasItems(ArgumentType.STRING, ArgumentType.INTEGER));
    assertThat(formatter.sprintf("Cookies", 2), equalTo("2 Cookies"));
  }

  @Test
  public void integerFieldWidth() {
    Formatter formatter = new Formatter("%05d");
    assertThat(formatter.sprintf(42), equalTo("00042"));
  }

  @Test
  public void widthAndPrecision() {
    Formatter formatter = new Formatter("%015.2f");
    assertThat(formatter.sprintf(42.1234), equalTo("000000000042.12"));
  }

  @Test
  public void hex() {
    Formatter formatter = new Formatter("%20.x");
    assertThat(formatter.sprintf(305441741), equalTo("            1234abcd"));
  }

  @Test
  public void misc() {
    assertThat(sprintf("%u%u%ctest%d %s", 5, 3000, 'a', -20, "bit"), equalTo("53000atest-20 bit"));
  }

  @Test
  public void variablePrecision() {
    assertThat(sprintf("%.*f", 2, 0.33333333), equalTo("0.33"));
  }

  @Test
  public void variableWidth() {
    assertThat(sprintf("%*sx", -3, "hi"), equalTo("hi x"));
  }

  @Test
  public void plusPrefixIgnoredForUnsigned() {
    assertThat(sprintf("%+u", 1024), equalTo("1024"));
  }

  @Test
  public void longUnsigned() {
    assertThat(sprintf("%lu", 0xFFFFFFFFL), equalTo("4294967295"));
  }


  @Test
  public void floatingPoint() {
    assertThat(sprintf("%f", Math.PI), Matchers.equalTo("3.141593"));
    assertThat(sprintf("%.3f", Math.PI), Matchers.equalTo("3.142"));
    assertThat(sprintf("%1.0f", Math.PI), Matchers.equalTo("3"));
    assertThat(sprintf("%5.1f", Math.PI), Matchers.equalTo("  3.1"));

    assertThat(sprintf("%05.1f", Math.PI), Matchers.equalTo("003.1"));
    assertThat(sprintf("%+f", Math.PI), Matchers.equalTo("+3.141593"));
    assertThat(sprintf("% f", Math.PI), Matchers.equalTo(" 3.141593"));
    assertThat(sprintf("%-10f", Math.PI), Matchers.equalTo("3.141593  "));
    assertThat(sprintf("%e", Math.PI), Matchers.equalTo("3.141593e+00"));
    assertThat(sprintf("%E", Math.PI), Matchers.equalTo("3.141593E+00"));
  }

  @Test
  @Ignore
  public void formatG() {
    // In R, the precision is the number of significant digits
    // where as the implementation we're using interprets the
    // precision as the number of digits after the radix.
    assertThat(sprintf("%g", Math.PI), Matchers.equalTo("3.14159"));
    assertThat(sprintf("%7.6g", Math.PI), Matchers.equalTo("3.14159"));
    assertThat(sprintf("%g",   1e6 * Math.PI), Matchers.equalTo("3.14159e+06"));
    assertThat(sprintf("%.9g", 1e6 * Math.PI), Matchers.equalTo("3141592.65"));
    assertThat(sprintf("%G", 1e-6 * Math.PI), Matchers.equalTo("3.14159E-06"));
  }

  @Test
  public void formatA() {
    assertThat(sprintf("%a", 12), Matchers.equalTo("0x1.8p+3"));

    assertThat(sprintf("%a", 1000), Matchers.equalTo("0x1.f4p+9"));
    assertThat(sprintf("%A", 1000), Matchers.equalTo("0X1.F4P+9"));

  }

  @Test
  public void formatA15() {
    assertThat(sprintf("%a", Math.pow(Math.sqrt(2.0), 2.0)), Matchers.equalTo("0x1.0000000000001p+1"));
  }


}