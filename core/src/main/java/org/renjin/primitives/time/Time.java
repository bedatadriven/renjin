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
package org.renjin.primitives.time;

import org.joda.time.*;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimeParserBucket;
import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.util.List;
import java.util.Locale;

/**
 * Implementation of date time-related functions.
 * 
 * <p>
 * R has three representations of date/times:
 * 
 * <ul>
 * <li>POSIXct - which is a stored as DoubleVector with the number seconds since Jan 1st 1970, with 
 * classes "POSIXct" and "POSIXt"</li>
 * <li>POSIXlt - which is stored as ListVector containing calendar elements sec, min, mon, etc</li>
 * <li>Date - which is stored as a DoubleVector with the number of days since Jan 1st 1970, where
 * Jan 1st, 1970 is day zero.</li>
 * </ul>
 */
public class Time {

  public static LocalDate EPOCH = new LocalDate(1970, 1, 1);

  /**
   * Parses a string value into a date time value.
   * @return
   */
  @Internal
  public static SEXP strptime(StringVector x, StringVector formats, String tz) {
    
    if(x.length() == 0 || formats.length() == 0) {
      return StringVector.EMPTY;
    }
    
    DateTimeZone timeZone = timeZoneFromRSpecification(tz);

    List<DateTimeFormatter> formatters = DateTimeFormat.forPatterns(formats, timeZone, false);
    
    PosixLtVector.Builder result = new PosixLtVector.Builder();
    if(!Strings.isNullOrEmpty(tz)) {
      result.withTimeZone(timeZone, tz);
    }
    
    int resultLength = Math.max(x.length(), formats.length());
    for(int i=0;i!=resultLength;++i) {
      DateTimeFormatter formatter = formatters.get(i % formatters.size());
      String string = x.getElementAsString(i % x.length());
      try {
        result.add(parseIgnoreTrailingCharacters(formatter, timeZone, string));
      } catch(IllegalArgumentException e) {
        result.addNA();
      }
    }   

    return result.buildListVector();
  }

  private static DateTime parseIgnoreTrailingCharacters(
      DateTimeFormatter formatter, 
      DateTimeZone defaultTimeZone, 
      String text) {
    // this is a modified version of DateTimeFormatter.parseDateTime() that does not
    // throw an exception on trailing characters
    
    Chronology chronology = ISOChronology.getInstance().withZone(defaultTimeZone);
    DateTimeParser parser = formatter.getParser();
    
    Locale locale = null;
    Integer pivotYear = null;
    int defaultYear = 2000;
    DateTimeZone timeZone = null;
    
    DateTimeParserBucket bucket = new DateTimeParserBucket(0, chronology, locale, pivotYear, defaultYear);
    int newPos = parser.parseInto(bucket, text, 0);
    if (newPos >= 0) {
      long millis = bucket.computeMillis(true, text);
      if (formatter.isOffsetParsed() && bucket.getOffsetInteger() != null) {
        int parsedOffset = bucket.getOffsetInteger();
        DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
        chronology = chronology.withZone(parsedZone);
      } else if (bucket.getZone() != null) {
        chronology = chronology.withZone(bucket.getZone());
      } 
      return new DateTime(millis, chronology);
    }
    throw new IllegalArgumentException();
  }


  /**
   * Converts a calendar-based representation of time (POSIXlt: see above) to 
   * a unix time value.
   * 
   * @param x a ListVector containing the fields above
   */
  @Internal("as.POSIXct")
  public static DoubleVector asPOSIXct(ListVector x, String tz) {
    
    SEXP timeZoneAttribute;
    if(Strings.isNullOrEmpty(tz)) {
      timeZoneAttribute = x.getAttribute(Symbols.TZONE);
    } else {
      timeZoneAttribute = StringArrayVector.valueOf(tz);
    }
    
    return new PosixCtVector.Builder()
        .setTimeZone(timeZoneAttribute)
        .addAll(new PosixLtVector(x))
        .buildDoubleVector();
  }
  
  @Internal("as.POSIXlt")
  public static ListVector asPOSIXlt(DoubleVector x, String tz) {
    SEXP timeZoneAttribute;
    if(Strings.isNullOrEmpty(tz)) {
      timeZoneAttribute = x.getAttribute(Symbols.TZONE);
    } else {
      timeZoneAttribute = StringArrayVector.valueOf(tz);
    }
    
    return new PosixLtVector.Builder()
      .withTimeZone(timeZoneAttribute)
      .addAll(new PosixCtVector(x))
      .buildListVector();
  }


  /**
   * Converts a POSIXlt object (in calendar form) to a Date object,
   * which stores dates as an offset from Jan 1, 1970.
   */
  @Internal
  public static DoubleVector POSIXlt2Date(ListVector x) {
    PosixLtVector ltVector = new PosixLtVector(x);
    DoubleArrayVector.Builder dateVector = DoubleArrayVector.Builder.withInitialCapacity(ltVector.length());
    for(int i=0;i!=ltVector.length();++i) {
      DateTime date = ltVector.getElementAsDateTime(i);
      if(date == null) {
        dateVector.addNA();
      } else {
        dateVector.add(Days.daysBetween(EPOCH, date.toLocalDate()).getDays());
      }
    }
    dateVector.setAttribute(Symbols.CLASS, StringVector.valueOf("Date"));
    return dateVector.build();
  }

  /**
   * Converts a {@code Date} object to a POSIXlt date time, always in the UTC timezone.
   */
  @Internal
  public static ListVector Date2POSIXlt(DoubleVector x) {
    PosixLtVector.Builder ltVector = new PosixLtVector.Builder();
    for(int i=0;i!=x.length();++i) {
      int daysSinceEpoch = x.getElementAsInt(i);
      if(IntVector.isNA(daysSinceEpoch)) {
        ltVector.addNA();
      } else {
        ltVector.add(EPOCH.plusDays(daysSinceEpoch).toDateTimeAtStartOfDay(DateTimeZone.UTC));
      }
    }
    return ltVector.buildListVector();
  }

  @Internal("Sys.time")
  public static DoubleVector sysTime() {
    return new PosixCtVector.Builder()
      .add(new DateTime())
      .buildDoubleVector();
  }
  
  /**
   * Formats a Posix-lt time as a string.
   */
  @Internal("format.POSIXlt")
  public static StringVector formatPOSIXlt(ListVector x, StringVector patterns, boolean useTz) {

    PosixLtVector dateTimes = new PosixLtVector(x);
    List<DateTimeFormatter> formatters = DateTimeFormat.forPatterns(patterns, dateTimes.getTimeZone(), useTz);
    
    StringVector.Builder result = new StringVector.Builder();
    int resultLength = Math.max(dateTimes.length(), patterns.length());

    for(int i=0;i!=resultLength;++i) {
      DateTimeFormatter formatter = formatters.get(i % formatters.size());
      DateTime dateTime = dateTimes.getElementAsDateTime(i % dateTimes.length());
      if (dateTime == null) {
        result.addNA();
      } else {
        result.add(formatter.print(dateTime));
      }
    }
    
    return result.build();
  }
  
  /**
   * Creates a Joda {@link DateTimeZone} instance from an R timezone string.
   * 
   * <p>The <a href="https://stat.ethz.ch/R-manual/R-devel/library/base/html/timezones.html">R documentation</a>
   * states that timezones are platform-dependent, but that "GMT" and "UTC" are guaranteed to be accepted on all 
   * platforms.</p>
   * 
   * <p>We rely on Joda to map IDs to the correct timezone, and JOda in turn relies on the Olson or "tz" 
   * database, which is commonly the source of timezones on platforms running R.</p>
   * 
   */
  public static DateTimeZone timeZoneFromRSpecification(String tz) {
    if(Strings.isNullOrEmpty(tz)) {
      return DateTimeZone.getDefault();
    } else if("GMT".equals(tz)) {
      return DateTimeZone.UTC;
    } else {
      return DateTimeZone.forID(tz);
    }
  }
  
  /**
   * Creates a Joda {@link DateTimeZone} instance from the {@code tzone}
   * attribute of an R Posix object. Returns the current timezone if
   * there is no {@code tzone} attribute;
   */
  public static DateTimeZone timeZoneFromPosixObject(SEXP lt) {
    SEXP attribute = lt.getAttribute(Symbols.TZONE);
    return timeZoneFromTzoneAttribute(attribute);
  }

  public static DateTimeZone timeZoneFromTzoneAttribute(SEXP attribute) {
    if(attribute instanceof StringVector) {
      return timeZoneFromRSpecification(((StringVector) attribute).getElementAsString(0));
    } else {
      return DateTimeZone.getDefault();
    }
  }
}
