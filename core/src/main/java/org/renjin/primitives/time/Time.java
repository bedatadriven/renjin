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

package org.renjin.primitives.time;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Implementation of date time-related functions.
 * 
 * <p>
 * R has two primary representations of date/times:
 * 
 * <ul>
 * <li>POSIXct - which is a stored as DoubleVector with the number seconds since Jan 1st 1970, with 
 * classes "POSIXct" and "POSIXt"</li>
 * <li>POSIXlt - which is stored as ListVector containing calendar elements sec, min, mon, etc</li>
 * </ul>
 */
public class Time {

  
  /**
   * Parses a string value into a date time value. 
   * @param x
   * @param format
   * @param tz
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
    int resultLength = Math.max(x.length(), formats.length());
    for(int i=0;i!=resultLength;++i) {
      DateTimeFormatter formatter = formatters.get(i % formatters.size());
      String string = x.getElementAsString(i % x.length());
      try {
        result.add(formatter.parseDateTime(string));
      } catch(IllegalArgumentException e) {
        result.addNA();
      }
    }   
    if(!Strings.isNullOrEmpty(tz)) {
      result.withTimeZone(timeZone);
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
    return new PosixCtVector.Builder()
        .addAll(new PosixLtVector(x))
        .buildDoubleVector();
  }
  
  @Internal("as.POSIXlt")
  public static ListVector asPOSIXlt(DoubleVector x, String tz) {
    return new PosixLtVector.Builder()
      .addAll(new PosixCtVector(x))
      .buildListVector();
  }
  
  @Internal("Sys.time")
  public static DoubleVector sysTime() {
    return new PosixCtVector.Builder()
      .add(new DateTime())
      .buildDoubleVector();
  }
  
  /**
   * Formats a Posix-lt time as a string.
   * @param x
   * @param format
   * @param useTz
   * @return
   */
  @Internal("format.POSIXlt")
  public static StringVector formatPOSIXlt(ListVector x, StringVector patterns, boolean useTz) {

    
    PosixLtVector dateTimes = new PosixLtVector(x);
    List<DateTimeFormatter> formatters = DateTimeFormat.forPatterns(patterns, DateTimeZone.getDefault(), useTz);
    
    StringVector.Builder result = new StringVector.Builder();
    int resultLength = Math.max(dateTimes.length(), patterns.length());

    for(int i=0;i!=resultLength;++i) {
      DateTimeFormatter formatter = formatters.get(i % formatters.size());
      DateTime dateTime = dateTimes.getElementAsDateTime(i % dateTimes.length());
      
      result.add(formatter.print(dateTime));
    }
    
    return result.build();
  }
  
  /**
   * Creates a Joda {@link DateTimeZone} instance from an R timezone string.
   */
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
  
  /**
   * Creates a Joda {@link DateTimeZone} instance from the {@code tzone}
   * attribute of an R Posix object. Returns the current timezone if
   * there is no {@code tzone} attribute;
   */
  public static DateTimeZone timeZoneFromPosixObject(SEXP lt) {
    SEXP attribute = lt.getAttribute(Symbols.TZONE);
    if(attribute instanceof StringVector) {
      return timeZoneFromRSpecification(((StringVector) attribute).getElementAsString(0));
    } else {
      return DateTimeZone.getDefault();
    }
  }
}
