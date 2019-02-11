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


import java.time.ZonedDateTime;
import java.util.Iterator;

public abstract class TimeVector implements Iterable<ZonedDateTime> {

  public abstract int length();

  public abstract ZonedDateTime getElementAsDateTime(int index);

  @Override
  public Iterator<ZonedDateTime> iterator() {
    return new ElementIterator();
  }

  private class ElementIterator implements Iterator<ZonedDateTime> {
    private int i = 0;

    @Override
    public boolean hasNext() {
      return i < length();
    }

    @Override
    public ZonedDateTime next() {
      return getElementAsDateTime(i++);
    }  
  }
}
