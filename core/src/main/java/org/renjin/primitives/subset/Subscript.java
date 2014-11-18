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

package org.renjin.primitives.subset;

/**
 * Base classes for R's rich set of different type subscripts.
 */
public abstract class Subscript {

  /**
   *
   * @return  the number of elements selected by this subscript
   */
  public int getCount() {
    throw new UnsupportedOperationException();
  }

  /**
   * Looks up the source index of the {@code i}-th element
   * selected by this {@code Subscript}
   *
   * @param i the index of the selected element
   * @return the source index
   */
  public abstract int getAt(int i);

  /**
   *
   * @return true if this subscript <i>definitely</i> selects all elements, or
   * false if it is not known
   */
  public boolean definitelySelectsAllElements() {
    return false;
  }
}
