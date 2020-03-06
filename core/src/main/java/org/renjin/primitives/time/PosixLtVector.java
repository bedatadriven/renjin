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
import org.renjin.sexp.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.renjin.sexp.IntVector.isNA;


/**
 * Wraps a ListVector that contains instants-in-time stored as a moment
 * in the ISO? Gregorian? calendar?
 */
public class PosixLtVector extends TimeVector {
  public static final String DST_FIELD = "isdst";
  public static final String DAY_OF_YEAR_FIELD = "yday";
  public static final String WEEKDAY_FIELD = "wday";
  public static final String YEAR_FIELD = "year";
  public static final String MONTH_FIELD = "mon";
  public static final String DAY_OF_MONTH_FIELD = "mday";
  public static final String HOUR_FIELD = "hour";
  public static final String MINUTE_FIELD = "min";
  public static final String SECOND_FIELD = "sec";
  public static final String GMT_OFFSET_FIELD = "gmtoff";
  public static final String ZONE_FIELD = "zone";


  private final ListVector vector;
  private final AtomicVector seconds;
  private final AtomicVector minutes;
  private final AtomicVector hours;
  private final AtomicVector daysOfMonth;
  private final AtomicVector monthsOfYear;
  private final AtomicVector years;

  private final ZoneId timeZone;

  private int length;

  public PosixLtVector(ListVector x) {
    this.vector = x;
    seconds = getElementAsVector(x, SECOND_FIELD);
    minutes = getElementAsVector(x, MINUTE_FIELD);
    hours = getElementAsVector(x, HOUR_FIELD);
    monthsOfYear = getElementAsVector(x, MONTH_FIELD);
    daysOfMonth = getElementAsVector(x, DAY_OF_MONTH_FIELD);
    years = getElementAsVector(x, YEAR_FIELD);

    Vector tzoneAttribute = (Vector) vector.getAttribute(Symbols.TZONE);
    if(tzoneAttribute.length() >= 1) {
      timeZone = Time.timeZoneFromRSpecification(tzoneAttribute.getElementAsString(0));
    } else {
      timeZone = ZoneId.systemDefault();
    }

    length = maxLength(seconds, minutes, hours, monthsOfYear, daysOfMonth, years);
  }

  public ZoneId getTimeZone() {
    return timeZone;
  }

  private int maxLength(AtomicVector... components) {
    int maxLength = 0;
    for (AtomicVector component : components) {
      if(component.length() > maxLength) {
        maxLength = component.length();
      }
    }
    return maxLength;
  }

  private AtomicVector getElementAsVector(ListVector x, String fieldName) {
    SEXP sexp = vector.getElementAsSEXP(x.getIndexByName(fieldName));
    if(!(sexp instanceof IntVector) && !(sexp instanceof DoubleVector)) {
      throw new EvalException("Expected element '%s' to be of type integer or numeric, was %s",
          fieldName, sexp.getTypeName());
    }
    return (AtomicVector) sexp;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public ZonedDateTime getElementAsDateTime(int index) {


    // Start with the years field, which is stored relative 
    // to 1900
    int year = elementAt(years, index);
    if(isNA(year)) {
      return null;
    }
    
    // The month and day are not guaranteed to be in valid ranges, so we need to add them
    // in sequence

    LocalDate localDate = LocalDate.of(1900 + year, 1, 1);

    // add months, which is normally in the range [0-11],
    // but can also be NA, less than 0 or greater than 11
    int month = elementAt(monthsOfYear, index);
    if(isNA(month)) {
      return null;
    }

    localDate = localDate.plusMonths(month);
    
    // normalize day of month
    
    // This value is actually treated as an offset: POSIXlt vectors
    // may have values like 4, 5, indicating the 4th and 5th day 
    // of the month, or values like 0, -15, 3000, or -233.
    int dayOfMonth = elementAt(daysOfMonth, index);
    if(isNA(dayOfMonth)) {
      return null;
    }

    // Since result is initialized to the first day of the month, we'll
    // treat this field as an offset relative to the first day of the month,
    // which means we have to subtract 1, so...
    // 1 -> 0 (already first day of the month, nothing to do)
    // 0 -> -1 (refers to one day before the first day, or last day of the previous month)
    int daysToAdd = dayOfMonth - 1;

    localDate = localDate.plusDays(daysToAdd);

    // Add time fields
    // These are all zero-based, so we can treat as an offset of the result so far
    int hourOfDay = elementAt(hours, index);
    int minuteOfHour = elementAt(minutes, index);
    double fractionalSecondOfMinute = doubleElementAt(seconds, index);

    if(isNA(hourOfDay) || isNA(minuteOfHour) || DoubleVector.isNA(fractionalSecondOfMinute)) {
      return null;
    }

    int secondOfMinute = (int) Math.floor(fractionalSecondOfMinute);
    int nanoOfSecond = (int)((fractionalSecondOfMinute - secondOfMinute) * 1e9);

    return localDate.atTime(hourOfDay, minuteOfHour, secondOfMinute, nanoOfSecond).atZone(timeZone);
  }

  private int elementAt(AtomicVector component, int index) {
    return component.getElementAsInt(index % component.length());
  }
  private double doubleElementAt(AtomicVector component, int index) {
    return component.getElementAsDouble(index % component.length());
  }

  public static class Builder {
    private ListVector.NamedBuilder list = new ListVector.NamedBuilder(0, 9);
    private DoubleArrayVector.Builder second = new DoubleArrayVector.Builder();
    private IntArrayVector.Builder minute = new IntArrayVector.Builder();
    private IntArrayVector.Builder hour = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfMonth = new IntArrayVector.Builder();
    private IntArrayVector.Builder month = new IntArrayVector.Builder();
    private IntArrayVector.Builder year = new IntArrayVector.Builder();
    private IntArrayVector.Builder weekday = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfYear = new IntArrayVector.Builder();
    private IntArrayVector.Builder dst = new IntArrayVector.Builder();
    private IntArrayVector.Builder gmtOffset = new IntArrayVector.Builder();
    private ZoneId zone;
    private StringVector zoneName;

    public Builder add(ZonedDateTime parsedTime) {

      // Regardless of what time zone was parsed, update to match
      // the timezone of this vector, or the system default if this vector does not have one
      ZonedDateTime time;
      if(zone == null) {
        time = parsedTime.withZoneSameInstant(ZoneId.systemDefault());
      } else {
        time = parsedTime.withZoneSameInstant(zone);
      }

      second.add(time.getSecond() + fractionalSeconds(time));
      minute.add(time.getMinute());
      hour.add(time.getHour());
      dayOfMonth.add(time.getDayOfMonth());
      month.add(time.getMonthValue()-1);
      year.add(time.getYear()-1900);
      weekday.add(time.get(ZeroBasedWeekday.INSTANCE));
      dayOfYear.add(time.getDayOfYear()-1);
      dst.add(computeDaylightSavingsFlag(time));

      // Exceptionally, we retain the offset of the parsed time.
      gmtOffset.add(parsedTime.getOffset().getTotalSeconds());

      return this;
    }

    private double fractionalSeconds(ZonedDateTime time) {
      double nanos = time.getNano();
      return nanos * 1e-9;
    }

    public void add(LocalDate localDate) {
      second.add(0);
      minute.add(0);
      hour.add(0);
      dayOfMonth.add(localDate.getDayOfMonth());
      month.add(localDate.getMonthValue()-1);
      year.add(localDate.getYear()-1900);
      weekday.add(localDate.get(ZeroBasedWeekday.INSTANCE));
      dayOfYear.add(localDate.getDayOfYear()-1);
      dst.add(0);
      gmtOffset.add(0);
    }

    public Builder addAll(Iterable<ZonedDateTime> dateTimes) {
      for(ZonedDateTime dateTime : dateTimes) {
        add(dateTime);
      }
      return this;
    }

    public Builder addNA() {
      second.addNA();
      minute.addNA();
      hour.addNA();
      dayOfMonth.addNA();
      month.addNA();
      year.addNA();
      weekday.addNA();
      dayOfYear.addNA();
      dst.add(-1);
      gmtOffset.addNA();
      return this;
    }

    public void withTimeZone(ZoneId zone, String zoneName) {
      this.zone = zone;
      this.zoneName = StringVector.valueOf(zoneName);
    }

    public Builder withTimeZone(SEXP timeZoneAttribute) {
      zone = Time.timeZoneFromTzoneAttribute(timeZoneAttribute);
      
      if(timeZoneAttribute instanceof StringVector) {
        zoneName = (StringVector) timeZoneAttribute;
      } else {
        zoneName = StringVector.valueOf(zone.getId());
      }
      return this;
    }

    private static int computeDaylightSavingsFlag(ZonedDateTime time) {
      if( time == null ) {
        return -1;
      } else {
        return time.getZone().getRules().isDaylightSavings(time.toInstant()) ? 1 : 0;
      }
    }

    public ListVector buildListVector() {
      list.add(SECOND_FIELD, second);
      list.add(MINUTE_FIELD, minute);
      list.add(HOUR_FIELD, hour);
      list.add(DAY_OF_MONTH_FIELD, dayOfMonth);
      list.add(MONTH_FIELD, month);
      list.add(YEAR_FIELD, year);
      list.add(WEEKDAY_FIELD, weekday);
      list.add(DAY_OF_YEAR_FIELD, dayOfYear);
      list.add(DST_FIELD, dst);

      if(zone == null || !zone.getId().equals("UTC")) {
        list.add(GMT_OFFSET_FIELD, gmtOffset);
      }

      // Expected Order of attributes: names, class, tzone
      // Add placeholder for names
      list.setAttribute(Symbols.NAMES, StringVector.EMPTY); // (placeholder to establish order)
      list.setAttribute(Symbols.CLASS, new StringArrayVector("POSIXlt", "POSIXt"));

      if(zone != null) {
          list.setAttribute(Symbols.TZONE, zoneName);
      }
      return list.build();
    }
  }
}
