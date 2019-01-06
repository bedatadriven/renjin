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
package org.renjin.primitives.time;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RDateTimeFormatsTest {

  @Test
  public void formatBuilder() {
    verifyFormat("2009-07-01 00:00:00", "%Y-%m-%d %H:%M:%OS", LocalDate.of(2009,7,1).atStartOfDay());
  }

  @Test
  public void newlines() {
    verifyFormat("Aug 21\n2018", "%b %d%n%Y", LocalDate.of(2018, 8, 21));
  }

  @Test
  public void timeZoneParsing() {
    RDateTimeFormatter formatter = RDateTimeFormats.forPattern("%d/%b/%Y:%H:%M:%S %z");
    ZonedDateTime result = formatter.parse("24/Aug/2014:17:57:26 +0200");

    assertThat(result.getOffset().getTotalSeconds(), equalTo(2 * 60 * 60));
  }

  @Test
  public void zeroBasedDayOfWeek() {
    RDateTimeFormatter formatter = RDateTimeFormats.forPattern("%w");
    assertThat(formatter.format(LocalDate.of(2018,1,7)), equalTo("0"));
    assertThat(formatter.format(LocalDate.of(2018,1,8)), equalTo("1"));
    assertThat(formatter.format(LocalDate.of(2018,1,9)), equalTo("2"));
    assertThat(formatter.format(LocalDate.of(2018,1,10)), equalTo("3"));
    assertThat(formatter.format(LocalDate.of(2018,1,11)), equalTo("4"));
    assertThat(formatter.format(LocalDate.of(2018,1,12)), equalTo("5"));
    assertThat(formatter.format(LocalDate.of(2018,1,13)), equalTo("6"));
  }


  private void verifyFormat(String x, String format, LocalDateTime dateTime) {
    RDateTimeFormatter formatter = RDateTimeFormats.forPattern(format);
    assertThat(formatter.format(dateTime), equalTo(x));
    assertThat(LocalDateTime.from(formatter.parse(x)), equalTo(dateTime));
  }


  private void verifyFormat(String x, String format, LocalDate dateTime) {
    verifyFormat(x, format, dateTime.atStartOfDay());
  }
  
}
