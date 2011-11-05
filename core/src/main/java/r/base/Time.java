/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import r.jvmi.annotations.AllowNA;
import r.jvmi.annotations.Primitive;
import r.jvmi.annotations.Recycle;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Symbols;
import r.lang.exception.EvalException;

import com.google.common.base.Strings;

/**
 * Implementation of date time-related functions.
 */
public class Time {

  private static final String DST_FIELD = "isdst";
  private static final String DAY_OF_YEAR_FIELD = "yday";
  private static final String WEEKDAY_FIELD = "wday";
  private static final String YEAR_FIELD = "year";
  private static final String MONTH_FIELD = "mon";
  private static final String DAY_OF_MONTH_FIELD = "mday";
  private static final String HOUR_FIELD = "hour";
  private static final String MINUTE_FIELD = "min";
  private static final String SECOND_FIELD = "sec";
  
  /**
   * Parses a string value into a date time value. 
   * @param x
   * @param format
   * @param tz
   * @return
   */
  @AllowNA
  public static ListVector strptime(@Recycle String x, @Recycle String format, String tz) {
    DateTimeFormatter formatter = formatterFromRSpecification(format);
    DateTimeZone timeZone = timeZoneFromRSpecification(tz);
    ListVector.Builder result = new ListVector.Builder();
    
    try {
      DateTime time = formatter.withZone(timeZone).parseDateTime(x);
      
      result.add(SECOND_FIELD, time.getSecondOfMinute());
      result.add(MINUTE_FIELD, time.getMinuteOfHour());
      result.add(HOUR_FIELD, time.getHourOfDay());
      result.add(DAY_OF_MONTH_FIELD, time.getDayOfMonth());
      result.add(MONTH_FIELD, time.getMonthOfYear()-1);
      result.add(YEAR_FIELD, time.getYear()-1900);
      result.add(WEEKDAY_FIELD,  getRDayOfWeek(time));
      result.add(DAY_OF_YEAR_FIELD, time.getDayOfYear()-1);
      result.add(DST_FIELD, timeZone.isStandardOffset(time.getMillis()) ? 0 : 1);
    
    } catch(IllegalArgumentException e) {
      result.add(SECOND_FIELD, IntVector.NA);
      result.add(MINUTE_FIELD, IntVector.NA);
      result.add(HOUR_FIELD, IntVector.NA);
      result.add(DAY_OF_MONTH_FIELD, IntVector.NA);
      result.add(MONTH_FIELD, IntVector.NA);
      result.add(YEAR_FIELD, IntVector.NA);
      result.add(WEEKDAY_FIELD,  IntVector.NA);
      result.add(DAY_OF_YEAR_FIELD, IntVector.NA);
      result.add(DST_FIELD, -1);      
    }
      
    result.setAttribute(Symbols.CLASS, new StringVector("POSIXlt", "POSIXt"));
    return result.build();
  }
  
  /**
   * Converts a calendar-based representation of time (POSIXlt: see above) to 
   * a unix time value.
   * 
   * @param x a ListVector containing the fields above
   */
  @Primitive("as.POSIXct")
  public static DoubleVector asPOSIXct(ListVector x, String tz) {
    
    int second = x.getElementAsInt(x.getIndexByName(SECOND_FIELD));
    int minute = x.getElementAsInt(x.getIndexByName(MINUTE_FIELD));
    int hour = x.getElementAsInt(x.getIndexByName(HOUR_FIELD));
    int dayOfMonth = x.getElementAsInt(x.getIndexByName(DAY_OF_MONTH_FIELD));
    int monthOfYear = x.getElementAsInt(x.getIndexByName(MONTH_FIELD));
    int year = x.getElementAsInt(x.getIndexByName(YEAR_FIELD));

    DateTimeZone timeZone = timeZoneFromRSpecification(tz);
    
    DateTime time = new DateTime(year+1900, monthOfYear+1, dayOfMonth, hour, minute, second, timeZone);
    
    DoubleVector.Builder result = new DoubleVector.Builder();
    result.add(time.getMillis() / 1000);
    result.setAttribute(Symbols.CLASS, new StringVector("POSIXct", "POSIXt"));
    result.setAttribute(Symbol.get("tzone"), new StringVector(tz));
    
    return result.build();
  }
  
  
  private static int getRDayOfWeek(org.joda.time.DateTime time) {
    if(time.getDayOfWeek()==7) {
      return 0;
    } else {
      return time.getDayOfWeek();
    }
  }
  
  /**
   * Builds a Joda DateTimeFormatter from an R-style date/time format string
   * 
   */
  public static DateTimeFormatter formatterFromRSpecification(String format) {
    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    
    for(int i=0;i<format.length();++i) {
      if(format.charAt(i)=='%' && i+1 < format.length()) {
        char specifier = format.charAt(++i);
        switch(specifier) {
        case '%':
          builder.appendLiteral("%");
          break;
        case 'a': 
          // Abbreviated weekday name in the current locale. (Also matches
          // full name on input.)
          builder.appendDayOfWeekShortText();
          break;
        case 'A': 
          // Full weekday name in the current locale.  (Also matches
          // abbreviated name on input.)
          builder.appendDayOfWeekText();
          break;
        case 'b': 
          // Abbreviated month name in the current locale. (Also matches        
          // full name on input.)
          builder.appendMonthOfYearShortText();
          break;
        case 'B': 
          // Full month name in the current locale.  (Also matches
          // abbreviated name on input.)
          builder.appendMonthOfYearText();
          break;
        case 'c':
          //  Date and time.  Locale-specific on output, ‘"%a %b %e
          // %H:%M:%S %Y"’ on input.
          throw new UnsupportedOperationException("%c not yet implemented");
        case 'd':
          // Day of the month as decimal number (01-31).
          builder.appendDayOfMonth(2);
          break;
        case 'H':
          // Hours as decimal number (00-23).
          builder.appendHourOfDay(2);
          break;
        case 'I':
          // Hours as decimal number (01-12).
          builder.appendHourOfHalfday(2);
          break;
        case 'j':
          // Day of year as decimal number (001-366).
          builder.appendDayOfYear(3);
          break;
        case 'm':
          // Month as decimal number (01-12).
          builder.appendMonthOfYear(2);
          break;
        case 'M':
          // Minute as decimal number (00-59).
          builder.appendMinuteOfHour(2);
          break;
        case 'p':
          // AM/PM indicator in the locale.  Used in conjunction with ‘%I’
          // and *not* with ‘%H’.  An empty string in some locales.
          builder.appendHalfdayOfDayText();
          break;
        case 'O':
          if(i+1>=format.length()) {
            builder.appendLiteral("%O");
          } else {
            switch(format.charAt(++i)) {
            case 'S':
              // Specific to R is ‘%OSn’, which for output gives the seconds to ‘0
              // <= n <= 6’ decimal places (and if ‘%OS’ is not followed by a
              // digit, it uses the setting of ‘getOption("digits.secs")’, or if
              // that is unset, ‘n = 3’).  Further, for ‘strptime’ ‘%OS’ will input
              // seconds including fractional seconds.  Note that ‘%S’ ignores (and
              // not rounds) fractional parts on output.
             
              // TODO: not sure how to handle fractional seconds here
              builder.appendSecondOfMinute(2);
              break;
            default:
              throw new EvalException("%O[dHImMUVwWy] not yet implemented");
              
            }
          }
          break;
        case 's':
          // Second as decimal number (00-61), allowing for up to two
          // leap-seconds (but POSIX-compliant implementations will ignore
          // leap seconds).
          // TODO: I have no idea what the docs are talking about in relation
          // to leap seconds
          builder.appendSecondOfDay(2);
          break;
          // case 'U':
          // Week of the year as decimal number (00-53) using Sunday as
          // the first day 1 of the week (and typically with the first
          //  Sunday of the year as day 1 of week 1).  The US convention.
          // case 'w':
          // Weekday as decimal number (0-6, Sunday is 0).

          // case 'W':
          // Week of the year as decimal number (00-53) using Monday as
          // the first day of week (and typically with the first Monday of
          // the year as day 1 of week 1). The UK convention.
         
          // ‘%x’ Date.  Locale-specific on output, ‘"%y/%m/%d"’ on input.

         
          //‘%X’ Time.  Locale-specific on output, ‘"%H:%M:%S"’ on input.

        case 'y':
          // Year without century (00-99). Values 00 to 68 are prefixed by
          // 20 and 69 to 99 by 19 - that is the behaviour specified by
          // the 2004 POSIX standard, but it does also say ‘it is expected
          // that in a future version the default century inferred from a
          // 2-digit year will change’.
          builder.appendTwoDigitYear(1968, true);
          break;
        case 'Y':
          // Year with century
          builder.appendYear(1,4);
          break;
        case 'z':
          // Signed offset in hours and minutes from UTC, so ‘-0800’ is 8
          // hours behind UTC.
          builder.appendTimeZoneOffset(null /* always show offset, even when zero */,
              true /* show seperators */, 
              1 /* min fields (hour, minute, etc) */,
              2 /* max fields */ );
          break;
        case 'Z':
          // (output only.) Time zone as a character string (empty if not
          // available).
          builder.appendTimeZoneName();
          break;
        default:
          throw new EvalException("%" + specifier + " not yet implemented. (Implement me!)");
        }
      } else {
        builder.appendLiteral(format.substring(i,i+1));
      }
    }
    return builder.toFormatter(); 
  }
  
  public static DateTimeZone timeZoneFromRSpecification(String tz) {
    if(Strings.isNullOrEmpty(tz)) {
      return DateTimeZone.getDefault();
    } else if("GMT".equals(tz)) {
      return DateTimeZone.UTC;
    } else {
      // TODO: this probably isn't right..
      return DateTimeZone.forID(tz);
    }
  }
}
