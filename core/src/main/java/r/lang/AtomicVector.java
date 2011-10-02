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
 * Root interface for "atomic" R vectors.
 *
 * {@code AtomicVector}s, in contrast to the {@code ListVector},
 * are collections of primitive elements of the same type.
 * 
 */
public interface AtomicVector extends Vector {

  /**
   *
   * @return true if the vector contains any {@code NA} values
   */
  boolean containsNA();


  /**
   *
   * @param vector an {@code AtomicVector}
   * @param vectorIndex an index of {@code vector}
   * @param startIndex
   * @return the index of the first element in this vector that equals
   * the element at {@code vectorIndex} in {@code vector}, or -1 if no such element
   * can be found
   */
  int indexOf(AtomicVector vector, int vectorIndex, int startIndex);

  /**
   * @param vector an {@code AtomicVector }
   * @param vectorIndex an index of {@code vector}
   * @return true if this vector contains an element equal to the
   * the element at {@code vectorIndex} in {@code vector}
   */
  boolean contains(AtomicVector vector, int vectorIndex);

  /***
   * @return the index of the first NA element.
   */
  int indexOfNA();

  /**
   * Compares two of the vector's elements.
   * @param index1 the index of the first element
   * @param index2 the index of the second element
   * @return a negative value if element at index1 < element at index 2;
   * a positive value if element at index1 > element at index2;
   * zero if the element at index1 == the element at index 2
   *
   */
  int compare(int index1, int index2);

  @Override
  Builder newBuilder(int initialSize);

  interface AtomicVectorBuilder {

  }

}
