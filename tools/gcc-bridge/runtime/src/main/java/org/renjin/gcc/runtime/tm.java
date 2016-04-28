package org.renjin.gcc.runtime;

import java.util.Calendar;
import java.util.Date;

/**
 * Structure containing a calendar date and time broken down into its components.
 */
public class tm {

  public tm(long instant) {

    Calendar instance = Calendar.getInstance();
    instance.setTimeInMillis(instant);

    tm_sec = instance.get(Calendar.SECOND);
    tm_min = instance.get(Calendar.MINUTE);
    tm_hour = instance.get(Calendar.HOUR);
    tm_mday = instance.get(Calendar.DAY_OF_MONTH);
    tm_mon = instance.get(Calendar.MONTH);
    tm_year = instance.get(Calendar.YEAR);
    tm_wday = instance.get(Calendar.DAY_OF_WEEK);
    tm_yday = instance.get(Calendar.DAY_OF_YEAR);
    tm_isdst = instance.getTimeZone().inDaylightTime(new Date(instant)) ? 1 : 0;
  }
  
  /**
   * seconds,  range 0 to 59 
   */
  public int tm_sec;

  /**
   *  minutes, range 0 to 59     
   */
  public int tm_min;

  /**
   * hours, range 0 to 23   
   */
  public int tm_hour;

  /**
   * day of the month, range 1 to 31
   */
  public int tm_mday;


  /**
   * month, range 0 to 11   
   */
  public int tm_mon;


  /**
   * The number of years since 1900  
   */
  public int tm_year;

  /**
   * day of the week, range 0 to 6  
   */
  public int tm_wday;

  /**
   * day in the year, range 0 to 365
   */
  public int tm_yday;

  /**
   * daylight saving time
   */
  public int tm_isdst;
  
}
