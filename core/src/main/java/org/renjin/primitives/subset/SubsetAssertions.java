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
package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

/**
 * Common assertion checks for subset operations. 
 *
 */
final class SubsetAssertions {

  private SubsetAssertions() {}
 
  /**
   * Checks that count == 1
   * 
   * @throws EvalException if {@code count != 1}
   */
  public static void checkUniqueCount(int count) {
    if(count < 1) {
      throw new EvalException("attempt to select less than one element");
    }
    if(count > 1) {
      throw new EvalException("attempt to select more than one element");
    }
  }

  /**
   * Checks that index is not {@code NA} and a valid index in {@code source}
   * 
   * @throws EvalException if {@code index} is out of bounds.
   */
  public static void checkBounds(SEXP source, int index) {
    if(IntVector.isNA(index) ||  index >= source.length()) {
      throw outOfBounds();
    }
  }

  /**
   * Checks that the given {@code sexp} has a length of exactly 1.
   * 
   * @throws EvalException if the length of {@code sexp} is not equal to one.
   */
  public static void checkUnitLength(SEXP sexp) {
    checkUniqueCount(sexp.length());
  }

  public static EvalException outOfBounds() {
    return new EvalException("subscript out of bounds");
  }
}
