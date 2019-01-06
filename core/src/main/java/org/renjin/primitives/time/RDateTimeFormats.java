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

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.StringVector;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.List;

/**
 * Factory that creates instances of DateTimeFormatter from
 * R-style date time format strings.
 */
public class RDateTimeFormats {

  private RDateTimeFormats() { }

  public static RDateTimeFormatter forPattern(String pattern, boolean useTz, ZoneId timeZone) {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseLenient();

    boolean hasTime = false;
    boolean hasZone = false;

    for(int i=0;i<pattern.length();++i) {
      if(pattern.charAt(i)=='%' && i+1 < pattern.length()) {
        char specifier = pattern.charAt(++i);
        switch(specifier) {
          case '%':
            builder.appendLiteral("%");
            break;
          case 'a':
            // Abbreviated weekday name in the current locale. (Also matches
            // full name on input.)
            builder.appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT);
            break;
          case 'A':
            // Full weekday name in the current locale.  (Also matches
            // abbreviated name on input.)
            builder.appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL);
            break;
          case 'b':
            // Abbreviated month name in the current locale. (Also matches
            // full name on input.)
            builder.appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT);
            break;
          case 'B':
            // Full month name in the current locale.  (Also matches
            // abbreviated name on input.)
            builder.appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL);
            break;
          case 'c':
            //  Date and time.  Locale-specific on output, ‘"%a %b %e
            // %H:%M:%S %Y"’ on input.
            throw new UnsupportedOperationException("%c not yet implemented");
          case 'd':
            // Day of the month as decimal number (01-31).
            builder.appendValue(ChronoField.DAY_OF_MONTH, 2, 2, SignStyle.NEVER);
            break;
          case 'H':
            // Hours as decimal number (00-23).
            hasTime = true;
            builder.appendValue(ChronoField.HOUR_OF_DAY, 2, 2, SignStyle.NEVER);
            break;
          case 'I':
            // Hours as decimal number (01-12).
            hasTime = true;
            builder.appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 2, 2, SignStyle.NEVER);
            break;
          case 'j':
            // Day of year as decimal number (001-366).
            builder.appendValue(ChronoField.DAY_OF_YEAR, 3, 3, SignStyle.NEVER);
            break;
          case 'm':
            // Month as decimal number (01-12).
            builder.appendValue(ChronoField.MONTH_OF_YEAR, 2, 2, SignStyle.NEVER);
            break;
          case 'M':
            // Minute as decimal number (00-59).
            hasTime = true;
            builder.appendValue(ChronoField.MINUTE_OF_HOUR, 2, 2, SignStyle.NEVER);
            break;
          case 'n':
            // New line
            builder.appendLiteral('\n');
            break;
          case 'p':
            // AM/PM indicator in the locale.  Used in conjunction with ‘%I’
            // and *not* with ‘%H’.  An empty string in some locales.
            builder.appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT);
            break;
          case 'O':
            if(i+1>=pattern.length()) {
              builder.appendLiteral("%O");
            } else {
              switch(pattern.charAt(++i)) {
                case 'S':
                  // Specific to R is ‘%OSn’, which for output gives the seconds to ‘0
                  // <= n <= 6’ decimal places (and if ‘%OS’ is not followed by a
                  // digit, it uses the setting of ‘getOption("digits.secs")’, or if
                  // that is unset, ‘n = 3’).  Further, for ‘strptime’ ‘%OS’ will input
                  // seconds including fractional seconds.  Note that ‘%S’ ignores (and
                  // not rounds) fractional parts on output.

                  // TODO: not sure how to handle fractional seconds here
                  builder.appendValue(ChronoField.SECOND_OF_MINUTE, 2, 2, SignStyle.NEVER);
                  break;
                default:
                  throw new EvalException("%O[dHImMUVwWy] not yet implemented");

              }
            }
            break;
          case 'S':
            // Second as decimal number (00-61), allowing for up to two
            // leap-seconds (but POSIX-complaint implementations will ignore
            // leap seconds).
            // TODO: I have no idea what the docs are talking about in relation
            // to leap seconds
            builder.appendValue(ChronoField.SECOND_OF_MINUTE, 2, 2, SignStyle.NEVER);
            break;

          case 'U':
            // Week of the year as decimal number (00-53) using Sunday as
            // the first day 1 of the week (and typically with the first
            //  Sunday of the year as day 1 of week 1).  The US convention.
            builder.appendValue(WeekFields.of(DayOfWeek.SUNDAY, 7).weekOfYear(), 2, 2, SignStyle.NEVER);
            break;

          case 'w':
            // Weekday as decimal number (0-6, Sunday is 0).
            builder.appendValue(ZeroBasedWeekday.INSTANCE);
            break;

          case 'W':
            // Week of the year as decimal number (00-53) using Monday as
            // the first day of week (and typically with the first Monday of
            // the year as day 1 of week 1). The UK convention.
            builder.appendValue(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear(), 2, 2, SignStyle.NEVER);
            break;

          case 'x':
            // Date.  Locale-specific on output, ‘"%y/%m/%d"’ on input.
            builder.append(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
            break;

          case 'X':
            builder.append(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
            break;

          case 'y':
            // Year without century (00-99). Values 00 to 68 are prefixed by
            // 20 and 69 to 99 by 19 - that is the behaviour specified by
            // the 2004 POSIX standard, but it does also say ‘it is expected
            // that in a future version the default century inferred from a
            // 2-digit year will change’.
            builder.appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.of(1968, 1, 1));
            break;

          case 'Y':
            // Year with century
            builder.appendValue(ChronoField.YEAR, 4, 4, SignStyle.NEVER);
            break;

          case 'z':
            // Signed offset in hours and minutes from UTC, so ‘-0800’ is 8
            // hours behind UTC.
            hasZone = true;
            builder.appendOffset("+HHMM", "+0000");
            break;
          case 'Z':
            // (output only.) Time zone as a character string (empty if not
            // available).
            hasZone = true;
            builder.appendZoneText(TextStyle.SHORT);
            break;
          default:
            throw new EvalException("%" + specifier + " not yet implemented. (Implement me!)");
        }
      } else {
        builder.appendLiteral(pattern.substring(i,i+1));
      }
    }
    if(useTz) {
      hasZone = true;
      builder.appendLiteral(" ");
      builder.appendZoneText(TextStyle.SHORT);
    }
    return new RDateTimeFormatter(pattern, builder.toFormatter(), hasTime, hasZone, timeZone);
  }
  

  public static RDateTimeFormatter forPattern(String pattern) {
    return forPattern(pattern, false, ZoneId.systemDefault());
  }
  
  /**
   * Creates a {@code List} of {@code DateTimeFormatter}s from a {@code StringVector}
   *  @param patterns R-Style date formatters
   * @param useTz true if the timezone name should be appended to the string
   * @param timeZone the time zone in which to format the date
   */
  public static List<RDateTimeFormatter> forPatterns(StringVector patterns, boolean useTz, ZoneId timeZone) {
    List<RDateTimeFormatter> formatters = Lists.newArrayListWithCapacity(patterns.length());
    for(String format : patterns) {
      formatters.add(forPattern(format, useTz, timeZone));
    }
    return formatters;
  }

}
