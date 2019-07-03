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


import org.renjin.invoke.annotations.Internal;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

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

  public static LocalDate EPOCH = LocalDate.of(1970, 1, 1);


  /**
   * Parses a string value into a date time value.
   * @return
   */
  @Internal
  public static SEXP strptime(StringVector x, StringVector formats, String tz) {
    
    if(x.length() == 0 || formats.length() == 0) {
      return StringVector.EMPTY;
    }
    
    ZoneId timeZone = timeZoneFromRSpecification(tz);

    List<RDateTimeFormatter> formatters = RDateTimeFormats.forPatterns(formats, false, timeZone);
    
    PosixLtVector.Builder result = new PosixLtVector.Builder();
    if(!Strings.isNullOrEmpty(tz)) {
      result.withTimeZone(timeZone, tz);
    }
    
    int resultLength = Math.max(x.length(), formats.length());
    for(int i=0;i!=resultLength;++i) {
      RDateTimeFormatter formatter = formatters.get(i % formatters.size());
      String string = x.getElementAsString(i % x.length());
      if(StringVector.isNA(string)) {
        result.addNA();
      } else {
        try {
          result.add(formatter.parse(string));
        } catch (DateTimeParseException e) {
          result.addNA();
        }
      }
    }

    return result.buildListVector();
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
  public static ListVector asPOSIXlt(AtomicVector x, String tz) {
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
      ZonedDateTime date = ltVector.getElementAsDateTime(i);
      if(date == null) {
        dateVector.addNA();
      } else {
        dateVector.add(EPOCH.until(date, ChronoUnit.DAYS));
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
        ltVector.add(EPOCH.plusDays(daysSinceEpoch));
      }
    }
    return ltVector.buildListVector();
  }

  @Internal("Sys.time")
  public static DoubleVector sysTime() {
    return new PosixCtVector.Builder()
      .add(ZonedDateTime.now())
      .buildDoubleVector();
  }
  
  /**
   * Formats a Posix-lt time as a string.
   */
  @Internal("format.POSIXlt")
  public static StringVector formatPOSIXlt(ListVector x, StringVector patterns, boolean useTz) {

    PosixLtVector dateTimes = new PosixLtVector(x);
    List<RDateTimeFormatter> formatters = RDateTimeFormats.forPatterns(patterns, useTz, dateTimes.getTimeZone());
    
    StringVector.Builder result = new StringVector.Builder();
    int resultLength = Math.max(dateTimes.length(), patterns.length());

    for(int i=0;i!=resultLength;++i) {
      RDateTimeFormatter formatter = formatters.get(i % formatters.size());
      ZonedDateTime dateTime = dateTimes.getElementAsDateTime(i % dateTimes.length());
      if (dateTime == null) {
        result.addNA();
      } else {
        result.add(formatter.format(dateTime));
      }
    }
    
    return result.build();
  }

  @Internal("OlsonNames")
  public static StringVector OlsonNames() {
    Set<String> zoneIds= ZoneId.getAvailableZoneIds();

    StringVector.Builder result = new StringVector.Builder();

    for (String zone : zoneIds) {
      result.add(zone);
    }

    return result.build();
  }

  /**
   * Creates a {@link ZoneId} instance from an R timezone string.
   * 
   * <p>The <a href="https://stat.ethz.ch/R-manual/R-devel/library/base/html/timezones.html">R documentation</a>
   * states that timezones are platform-dependent, but that "GMT" and "UTC" are guaranteed to be accepted on all 
   * platforms.</p>
   * 
   *
   */
  public static ZoneId timeZoneFromRSpecification(String tz) {
    if(Strings.isNullOrEmpty(tz)) {
      return ZoneId.systemDefault();
    } else if("GMT".equals(tz)) {
      return ZoneId.of("UTC");
    } else if("Coordinated Universal Time".equals(tz)) {
      return ZoneId.of("UTC");
    } else {
      return ZoneId.of(tz, ZoneId.SHORT_IDS);
    }
  }
  
  /**
   * Creates a {@link ZoneId} instance from the {@code tzone}
   * attribute of an R Posix object. Returns the current timezone if
   * there is no {@code tzone} attribute;
   */
  public static ZoneId timeZoneFromPosixObject(SEXP lt) {
    SEXP attribute = lt.getAttribute(Symbols.TZONE);
    return timeZoneFromTzoneAttribute(attribute);
  }

  public static ZoneId timeZoneFromTzoneAttribute(SEXP attribute) {
    if(attribute instanceof StringVector) {
      return timeZoneFromRSpecification(((StringVector) attribute).getElementAsString(0));
    } else {
      return ZoneId.systemDefault();
    }
  }
}
