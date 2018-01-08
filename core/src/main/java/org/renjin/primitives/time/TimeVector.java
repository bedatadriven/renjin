/**
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
package org.renjin.primitives.time;

import org.joda.time.DateTime;
import org.renjin.repackaged.guava.collect.UnmodifiableIterator;

import java.util.Iterator;

public abstract class TimeVector implements Iterable<DateTime> {

  public abstract int length();

  public abstract DateTime getElementAsDateTime(int index);

  @Override
  public Iterator<DateTime> iterator() {
    return new ElementIterator();
  }

  private class ElementIterator extends UnmodifiableIterator<DateTime> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < length();
    }

    @Override
    public DateTime next() {
      return getElementAsDateTime(i++);
    }  
  }
}
