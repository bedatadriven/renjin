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
package org.renjin.gcc.format;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.Stdlib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.gcc.format.Formatter.ArgumentType;
import static org.renjin.gcc.format.Formatter.format;

public class FormatterTest {

  @Test
  public void helloWorld() {
    Formatter formatter = new Formatter("Hello %s");
    assertThat(formatter.getArgumentTypes(), Matchers.hasItems(ArgumentType.STRING));
    assertThat(formatter.format(new FormatArrayInput("World")), equalTo("Hello World"));
  }

  @Test
  public void positionalArguments() {
    Formatter formatter = new Formatter("%2$d %1$s");
    assertThat(formatter.getArgumentTypes(), Matchers.hasItems(ArgumentType.STRING, ArgumentType.INTEGER));
    assertThat(formatter.format(new FormatArrayInput("Cookies", 2)), equalTo("2 Cookies"));
  }

  @Test
  public void positionalWidth() {
    assertThat(Formatter.format("Hello %2$*2$d", 3, 2), equalTo("Hello  2"));
  }

  @Test
  public void integerFieldWidth() {
    assertThat(Formatter.format("%05d", 42), equalTo("00042"));
  }

  @Test
  public void widthAndPrecision() {
    assertThat(Formatter.format("%015.2f", 42.1234), equalTo("000000000042.12"));
  }

  @Test
  public void hex() {
    assertThat(Formatter.format("%20.x", 305441741), equalTo("            1234abcd"));
  }

  @Test
  public void misc() {
    assertThat(format("%u%u%ctest%d %s", 5, 3000, 'a', -20, "bit"), equalTo("53000atest-20 bit"));
  }

  @Test
  public void variablePrecision() {
    assertThat(format("%.*f", 2, 0.33333333), equalTo("0.33"));
  }

  @Test
  public void variableWidth() {
    assertThat(format("%*sx", -3, "hi"), equalTo("hi x"));
  }

  @Test
  public void plusPrefixIgnoredForUnsigned() {
    assertThat(format("%+u", 1024), equalTo("1024"));
  }

  @Test
  public void longUnsigned() {
    assertThat(format("%lu", 0xFFFFFFFFL), equalTo("4294967295"));
  }


  @Test
  public void floatingPoint() {
    assertThat(format("%f", Math.PI), Matchers.equalTo("3.141593"));
    assertThat(format("%.3f", Math.PI), Matchers.equalTo("3.142"));
    assertThat(format("%1.0f", Math.PI), Matchers.equalTo("3"));
    assertThat(format("%5.1f", Math.PI), Matchers.equalTo("  3.1"));

    assertThat(format("%05.1f", Math.PI), Matchers.equalTo("003.1"));
    assertThat(format("%+f", Math.PI), Matchers.equalTo("+3.141593"));
    assertThat(format("% f", Math.PI), Matchers.equalTo(" 3.141593"));
    assertThat(format("%-10f", Math.PI), Matchers.equalTo("3.141593  "));
    assertThat(format("%e", Math.PI), Matchers.equalTo("3.141593e+00"));
    assertThat(format("%E", Math.PI), Matchers.equalTo("3.141593E+00"));
  }

  @Test
  @Ignore
  public void formatG() {
    // In R, the precision is the number of significant digits
    // where as the implementation we're using interprets the
    // precision as the number of digits after the radix.
    assertThat(format("%g", Math.PI), Matchers.equalTo("3.14159"));
    assertThat(format("%7.6g", Math.PI), Matchers.equalTo("3.14159"));
    assertThat(format("%g",   1e6 * Math.PI), Matchers.equalTo("3.14159e+06"));
    assertThat(format("%.9g", 1e6 * Math.PI), Matchers.equalTo("3141592.65"));
    assertThat(format("%G", 1e-6 * Math.PI), Matchers.equalTo("3.14159E-06"));
  }

  @Test
  public void formatA() {
    assertThat(format("%a", 12), Matchers.equalTo("0x1.8p+3"));

    assertThat(format("%a", 1000), Matchers.equalTo("0x1.f4p+9"));
    assertThat(format("%A", 1000), Matchers.equalTo("0X1.F4P+9"));
    assertThat(format("%a", -2042344), Matchers.equalTo("-0x1.f29e8p+20"));
  }

  @Test
  public void formatA15() {
    assertThat(format("%a", Math.pow(Math.sqrt(2.0), 2.0)), Matchers.equalTo("0x1.0000000000001p+1"));
  }

  @Test
  public void backslash() {
    assertThat(format("\\d"), equalTo("\\d"));
  }

  @Test
  public void scanf() {
    Formatter formatter = new Formatter("%[^\r\n]", Formatter.Mode.SCAN);
    BytePtr stringOutput = new BytePtr(new byte[1024]);
    Ptr[] output = new Ptr[1];
    output[0] = stringOutput;

    formatter.scan(new StringCharIterator("Helvetica\nHello world"), output);

    assertThat(Stdlib.nullTerminatedString(stringOutput), equalTo("Helvetica"));
  }

  @Test
  public void sscanfWhitespace() {
    Formatter formatter = new Formatter("%39s", Formatter.Mode.SCAN);

    BytePtr stringOutput = new BytePtr(new byte[1024]);
    Ptr[] output = new Ptr[1];
    output[0] = stringOutput;

    formatter.scan(new StringCharIterator(".notdef 34 3 39"), output);

    assertThat(Stdlib.nullTerminatedString(stringOutput), equalTo(".notdef"));


  }
}