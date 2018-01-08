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
package org.renjin.primitives;

import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;

/**
 * Bitwise operators
 */
public class Bitwise {


  @Internal
  @DataParallel
  public static int bitwiseNot(int a) {
    return ~a;
  }


  @Internal
  @DataParallel
  public static int bitwiseAnd(int a, int b) {
    return a & b;
  }

  @Internal
  @DataParallel
  public static int bitwiseOr(int a, int b) {
    return a | b;
  }


  @Internal
  @DataParallel
  public static int bitwiseXor(int a, int b) {
    return a ^ b;
  }

  @Internal
  @DataParallel
  public static int bitwiseShiftL(int x, int bits) {
    if(bits < 0 || bits > 31) {
      return IntVector.NA;
    }
    return x << bits;
  }

  @Internal
  @DataParallel
  public static int bitwiseShiftR(int x, int bits) {
    if(bits < 0 || bits > 31) {
      return IntVector.NA;
    }
    // Use *UNSIGNED* right shift operator
    return x >>> bits;
  }

}
