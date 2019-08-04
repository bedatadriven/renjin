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
package org.renjin.sexp;

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
   * @param vector an {@code AtomicVector }
   * @param vectorIndex an index of {@code vector}
   * @return true if this vector contains an element equal to the
   * the element at {@code vectorIndex} in {@code vector}
   */
  boolean contains(AtomicVector vector, int vectorIndex);

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



  /***
   * @return the index of the first NA element.
   */
  int indexOfNA();

  /**
   * Compares two of the vector's elements.
   * @param index1 the index of the first element
   * @param index2 the index of the second element
   * @return a negative value if element at index1 &lt; element at index 2;
   * a positive value if element at index1 &gt; element at index2;
   * zero if the element at index1 == the element at index 2
   *
   */
  int compare(int index1, int index2);


  /**
   * @return a copy of the vector as an array of doubles
   */
  public double[] toDoubleArray();

  /**
   * @return a copy of the vector as an array of integers
   */
  int[] toIntArray();

  @Override
  Builder newBuilderWithInitialSize(int initialSize);

  

}
