/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.io.connections;

/**
 * Interface for processing large amounts of character data efficiently.
 */
public interface CharProcessor {

  /**
   *
   * The implementation of this method should consume as much of the buffer as possible, and return
   * the number of characters it has consumed. The difference between the buffer's limit and the number
   * of characters will be copied to the beginning of the buffer provided to the next call of process().
   */
  void process(char[] buffer, int bufferLength, boolean endOfInput);

}
