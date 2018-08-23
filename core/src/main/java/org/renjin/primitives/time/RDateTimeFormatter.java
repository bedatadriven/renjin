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
package org.renjin.primitives.time;

import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class RDateTimeFormatter {


  private final String formatString;
  private final DateTimeFormatter formatter;
  private final boolean hasTime;
  private final boolean hasZone;
  private final ZoneId zoneId;

  RDateTimeFormatter(String formatString, DateTimeFormatter formatter, boolean hasTime, boolean hasZone, ZoneId zoneId) {
    this.formatString = formatString;
    this.formatter = formatter;
    this.hasTime = hasTime;
    this.hasZone = hasZone;
    this.zoneId = zoneId;
  }

  public ZonedDateTime parse(String string) {
    ParsePosition position = new ParsePosition(0);
    TemporalAccessor result = formatter.parse(string, position);

    if (hasTime) {
      if(hasZone) {
        return ZonedDateTime.from(result);
      } else {
        return LocalDateTime.from(result).atZone(zoneId);
      }
    } else {
      return LocalDate.from(result).atStartOfDay(zoneId);
    }
  }

  public String format(TemporalAccessor dateTime) {
    return formatter.format(dateTime);
  }
}
