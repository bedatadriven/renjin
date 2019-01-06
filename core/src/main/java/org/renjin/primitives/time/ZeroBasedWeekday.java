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

import java.time.temporal.*;

/**
 * A temporal field with the value of the weekday, where Sunday is 0.
 *
 * <p>This is the format used by R's date time functions.</p>
 */
class ZeroBasedWeekday implements TemporalField {

  public static final ZeroBasedWeekday INSTANCE = new ZeroBasedWeekday();

  private ZeroBasedWeekday() {
  }

  @Override
  public TemporalUnit getBaseUnit() {
    return ChronoField.DAY_OF_WEEK.getBaseUnit();
  }

  @Override
  public TemporalUnit getRangeUnit() {
    return ChronoField.DAY_OF_WEEK.getRangeUnit();
  }

  @Override
  public ValueRange range() {
    return ValueRange.of(0, 6);
  }

  @Override
  public boolean isDateBased() {
    return ChronoField.DAY_OF_WEEK.isDateBased();
  }

  @Override
  public boolean isTimeBased() {
    return ChronoField.DAY_OF_WEEK.isTimeBased();
  }

  @Override
  public boolean isSupportedBy(TemporalAccessor temporal) {
    return temporal.isSupported(ChronoField.DAY_OF_WEEK);
  }

  @Override
  public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
    return range();
  }

  @Override
  public long getFrom(TemporalAccessor temporal) {
    int dayOfWeek = temporal.get(ChronoField.DAY_OF_WEEK);
    if(dayOfWeek == 7) {
      return 0;
    } else {
      return dayOfWeek;
    }
  }

  @Override
  public <R extends Temporal> R adjustInto(R temporal, long newValue) {
    if(newValue == 0) {
      newValue = 7;
    }
    return ChronoField.DAY_OF_WEEK.adjustInto(temporal, newValue);
  }
}
