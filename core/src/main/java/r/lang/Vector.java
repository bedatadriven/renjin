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

package r.lang;

/**
 * Provides a common interface to {@code ListExp}, all {@code AtomicExp}s, and
 * {@code PairList}s
 */
public interface Vector extends SEXP {

  int getIndexByName(String name);

  /**
   * @param index zero-based index of the element
   * @return the element at {@code index} as a new {@code SEXP}
   */
  SEXP getElementAsSEXP(int index);

  /**
   * Returns a builder for this type, initially empty.
   * @param initialSize
   * @return
   */
  Builder newBuilder(int initialSize);

  /**
   *
   * @param vector
   * @return true if this vector can be widened to the given
   * vector
   */
  boolean isWiderThan(Object vector);

  /**
   *         
   * @return a builder initialized with a copy of this set of elements.
   */
  Builder newCopyBuilder();

  public static interface Builder<S extends SEXP, E extends Vector> {

    /**
     * Sets the element at index {@code index} to NA
     */
    Builder setNA(int index);

    public Builder setFrom(int destinationIndex, E source, int sourceIndex );

    public S build();
  }
}
