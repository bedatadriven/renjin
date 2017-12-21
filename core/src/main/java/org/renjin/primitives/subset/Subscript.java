/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.subset;


public interface Subscript {


  /**
   * Computes the single index selected by this subscript.
   * 
   * @throws org.renjin.eval.EvalException if this subscript selects less or more than one element.
   * @return zero-based index
   */
  int computeUniqueIndex();


  /**
   * Computes the sequence of indices to replace. The sequence will not
   * contain {@code NA}s
   * 
   * @return an iterator over the sequence.
   */
  IndexIterator computeIndexes();

  
  IndexPredicate computeIndexPredicate();

  /**
   * Computes the count of elements selected by this subscript.
   */
  int computeCount();
}
