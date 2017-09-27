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

/**
 * Iterator over a sequence of integer indices.
 */
public interface IndexIterator {
  
  int EOF = -1;

  /**
   * 
   * Returns the next index in the sequence, and advances the iterator to next element. 
   * 
   * @return the next zero-based index in this sequence or {@code EOF} if there are no more elements.
   */
  int next();

  /**
   * Resets the position in the sequence to the beginning. Any subsequent call to {@link #next()} will return 
   * the first index in the sequence.
   */
  void restart();
}
