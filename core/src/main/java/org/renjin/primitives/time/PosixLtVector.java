package org.renjin.primitives.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


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
  
  private final ListVector vector;
  private final AtomicVector seconds;
  private final AtomicVector minutes;
  private final AtomicVector hours;
  private final AtomicVector daysOfMonth;
  private final AtomicVector monthsOfYear;
  private final AtomicVector years;
  
  private final DateTimeZone timeZone;
  
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
      timeZone = DateTimeZone.getDefault();
    }
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
    return seconds.length();
  }

  @Override
  public DateTime getElementAsDateTime(int index) {
    return new DateTime(
        years.getElementAsInt(index)+1900,
        monthsOfYear.getElementAsInt(index)+1,
        daysOfMonth.getElementAsInt(index),
        hours.getElementAsInt(index),
        minutes.getElementAsInt(index),
        seconds.getElementAsInt(index),
        timeZone);
  }

  public static class Builder {
    private ListVector.NamedBuilder list = new ListVector.NamedBuilder(9);
    private IntArrayVector.Builder second = new IntArrayVector.Builder();
    private IntArrayVector.Builder minute = new IntArrayVector.Builder();
    private IntArrayVector.Builder hour = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfMonth = new IntArrayVector.Builder();
    private IntArrayVector.Builder month = new IntArrayVector.Builder();
    private IntArrayVector.Builder year = new IntArrayVector.Builder();
    private IntArrayVector.Builder weekday = new IntArrayVector.Builder();
    private IntArrayVector.Builder dayOfYear = new IntArrayVector.Builder();
    private IntArrayVector.Builder dst = new IntArrayVector.Builder();
    
    public Builder add(DateTime time) {
      second.add(time.getSecondOfMinute());
      minute.add(time.getMinuteOfHour());
      hour.add(time.getHourOfDay());
      dayOfMonth.add(time.getDayOfMonth());
      month.add(time.getMonthOfYear()-1);
      year.add(time.getYear()-1900);
      weekday.add(getRDayOfWeek(time));
      dayOfYear.add(time.getDayOfYear()-1);
      dst.add(getDstFlag(time));
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
      return this;
    }
    
    public Builder withTimeZone(DateTimeZone tz) {
      list.setAttribute(Symbols.TZONE, StringVector.valueOf(tz.getID()));
      return this;
    }
  
    private static int getDstFlag(DateTime time) {
      if( time == null ) {
        return -1;
      } else {
        return time.getZone().isStandardOffset(time.getMillis()) ? 0 : 1;
      }
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
      list.setAttribute(Symbols.CLASS, new StringArrayVector("POSIXlt", "POSIXt"));
      return list.build();
    }
  }
}
