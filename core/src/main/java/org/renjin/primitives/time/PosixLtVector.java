package org.renjin.primitives.time;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.concurrent.TimeUnit;

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
  private final AtomicVector gmtOffset;

  private final DateTimeZone timeZone;

  private int length;

  public PosixLtVector(ListVector x) {
    this.vector = x;
    seconds = getElementAsVector(x, SECOND_FIELD);
    minutes = getElementAsVector(x, MINUTE_FIELD);
    hours = getElementAsVector(x, HOUR_FIELD);
    monthsOfYear = getElementAsVector(x, MONTH_FIELD);
    daysOfMonth = getElementAsVector(x, DAY_OF_MONTH_FIELD);
    years = getElementAsVector(x, YEAR_FIELD);
    gmtOffset = getElementAsVector(x, GMT_OFFSET_FIELD);

    Vector tzoneAttribute = (Vector) vector.getAttribute(Symbols.TZONE);
    if(tzoneAttribute.length() >= 1) {
      timeZone = Time.timeZoneFromRSpecification(tzoneAttribute.getElementAsString(0));
    } else {
      timeZone = DateTimeZone.getDefault();
    }

    length = maxLength(seconds, minutes, hours, monthsOfYear, daysOfMonth, years);
  }

  public DateTimeZone getTimeZone() {
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
  public DateTime getElementAsDateTime(int index) {


    // Start with the years field, which is stored relative 
    // to 1900
    int year = elementAt(years, index);
    if(isNA(year)) {
      return null;
    }
    
    // Since all fields following the year field can actually be provided as offsets
    // (for example, mday = -15, etc)
    // we'll use the year field to establish the base date (2011-01-01 for example)
    // to which all other fields
    // are add/subtracted.
    // 
    // _time_ is the number of milliseconds since 1970-01-01 UTC, so to respect
    // the time zone parameter if provided, we will use a ZonedChronology
    Chronology chronology = ISOChronology.getInstance().withZone(timeZone);
    
    // Computes the milliseconds since UTC epoch _at the start of the day in the given
    // timezone_
    long time = chronology.getDateTimeMillis(1900 + year, 1, 1, 0);
    
    // add months, which is normally in the range [0-11],
    // but can also be NA, less than 0 or greater than 11
    int month = elementAt(monthsOfYear, index);
    if(isNA(month)) {
      return null;
    }
    
    time = chronology.monthOfYear().add(time, month);

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
    
    time = chronology.dayOfMonth().add(time, daysToAdd);
    
    // Add time fields
    // These are all zero-based, so we can treat as an offset of the result so far
    int hourOfDay = elementAt(hours, index);
    int minuteOfHour = elementAt(minutes, index);
    int secondOfMinute = elementAt(seconds, index);

    if(isNA(hourOfDay) || isNA(minuteOfHour) || isNA(secondOfMinute)) {
      return null;
    }
    
    time = chronology.hourOfDay().add(time, hourOfDay);
    time = chronology.minuteOfHour().add(time, minuteOfHour);
    time = chronology.secondOfMinute().add(time, secondOfMinute);
    
    return new DateTime(time);

  }

  private int elementAt(AtomicVector component, int index) {
    return component.getElementAsInt(index % component.length());
  }

  public static class Builder {
    private ListVector.NamedBuilder list = new ListVector.NamedBuilder(0, 9);
    private IntArrayVector.Builder second = new IntArrayVector.Builder();
    private IntArrayVector.Builder minute = new IntArrayVector.Builder();
    private IntArrayVector.Builder hour = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfMonth = new IntArrayVector.Builder();
    private IntArrayVector.Builder month = new IntArrayVector.Builder();
    private IntArrayVector.Builder year = new IntArrayVector.Builder();
    private IntArrayVector.Builder weekday = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfYear = new IntArrayVector.Builder();
    private IntArrayVector.Builder dst = new IntArrayVector.Builder();
    private IntArrayVector.Builder gmtOffset = new IntArrayVector.Builder();
    private DateTimeZone zone;

    public Builder add(DateTime time) {
      second.add(time.getSecondOfMinute());
      minute.add(time.getMinuteOfHour());
      hour.add(time.getHourOfDay());
      dayOfMonth.add(time.getDayOfMonth());
      month.add(time.getMonthOfYear()-1);
      year.add(time.getYear()-1900);
      weekday.add(getRDayOfWeek(time));
      dayOfYear.add(time.getDayOfYear()-1);
      dst.add(computeDaylightSavingsFlag(time));
      gmtOffset.add(computeGmtOffset(time));
      return this;
    }



    public Builder addAll(Iterable<DateTime> dateTimes) {
      for(DateTime dateTime : dateTimes) {
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

    public Builder withTimeZone(DateTimeZone tz) {
      zone = tz;
      return this;
    }


    public Builder withTimeZone(SEXP timeZoneAttribute) {
      zone = Time.timeZoneFromTzoneAttribute(timeZoneAttribute);
      return this;
    }

    private static int computeDaylightSavingsFlag(DateTime time) {
      if( time == null ) {
        return -1;
      } else {
        return time.getZone().isStandardOffset(time.getMillis()) ? 0 : 1;
      }
    }

    /**
     * Computes the offset of this timezone from GMT in seconds, or NA if not known
     */
    private int computeGmtOffset(DateTime time) {
      return (int)TimeUnit.MILLISECONDS.toSeconds(time.getZone().getOffset(time));
    }

    private static int getRDayOfWeek(DateTime time) {
      if(time.getDayOfWeek()==7) {
        return 0;
      } else {
        return time.getDayOfWeek();
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
      list.add(GMT_OFFSET_FIELD, gmtOffset);
      if(zone != null) {
        list.setAttribute(Symbols.TZONE, StringArrayVector.valueOf(zone.getID()));
      }
      list.setAttribute(Symbols.CLASS, new StringArrayVector("POSIXlt", "POSIXt"));
      return list.build();
    }

  }
}
