/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.runtime;

import java.util.Calendar;
import java.util.Date;

/**
 * Structure containing a calendar date and time broken down into its components.
 */
@Deprecated
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
