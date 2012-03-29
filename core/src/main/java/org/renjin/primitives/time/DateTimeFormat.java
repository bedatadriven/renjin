package org.renjin.primitives.time;

import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.renjin.eval.EvalException;
import org.renjin.sexp.StringVector;


import com.google.common.collect.Lists;

/**
 * Factory that creates instances of DateTimeFormatter from 
 * R-style date time format strings.
 */
public class DateTimeFormat  {
  
  private DateTimeFormat() { }

  public static DateTimeFormatter forPattern(String patterns, boolean useTz) {
   DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
    
    for(int i=0;i<patterns.length();++i) {
      if(patterns.charAt(i)=='%' && i+1 < patterns.length()) {
        char specifier = patterns.charAt(++i);
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
          if(i+1>=patterns.length()) {
            builder.appendLiteral("%O");
          } else {
            switch(patterns.charAt(++i)) {
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
        case 'S':
          // Second as decimal number (00-61), allowing for up to two
          // leap-seconds (but POSIX-complaint implementations will ignore
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
        builder.appendLiteral(patterns.substring(i,i+1));
      }
    }
    if(useTz) {
      builder.appendLiteral(" ");
      builder.appendTimeZoneShortName();
    }
    return builder.toFormatter(); 
  }
  

  public static DateTimeFormatter forPattern(String pattern) {
    return forPattern(pattern, false);
  }
  
  /**
   * Creates a {@code List} of {@code DateTimeFormatter}s from a {@code StringVector}
   * 
   * @param patterns R-Style date formatters
   * @param timeZone the time zone in which to format the date
   * @param useTz true if the timezone name should be appended to the string
   */
  public static List<DateTimeFormatter> forPatterns(StringVector patterns, DateTimeZone timeZone, boolean useTz) {
    List<DateTimeFormatter> formatters = Lists.newArrayListWithCapacity(patterns.length());
    for(String format : patterns) {
      formatters.add(forPattern(format, useTz).withZone(timeZone));
    }
    return formatters;
  }

  /**
   * Creates a {@code List} of {@code DateTimeFormatter}s from a {@code StringVector}
   * 
   * @param patterns R-Style date formatters
   * @param timeZone the time zone in which to format the date
   */
  public static List<DateTimeFormatter> forPatterns(StringVector patterns, DateTimeZone timeZone) {
    return forPatterns(patterns, timeZone, false);
  }
}
