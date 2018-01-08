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
// Initial template generated from Arith.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;

/**
 * GNU R API methods defined in the "ext/Arith.h" header file
 */
@SuppressWarnings("unused")
public final class Arith {

  private Arith() { }

  public static final double R_NaN = Double.NaN;		/* IEEE NaN */
  public static final double R_PosInf = Double.POSITIVE_INFINITY;	/* IEEE Inf */
  public static final double R_NegInf = Double.NEGATIVE_INFINITY;	/* IEEE -Inf */
  public static final double R_NaReal = DoubleVector.NA;	/* NA_REAL: IEEE */
  public static final int	 R_NaInt = IntVector.NA;	/* NA_INTEGER:= INT_MIN currently */

  public static int R_IsNA(double p0) {
    return DoubleVector.isNA(p0) ? 1 : 0;
  }

  public static int R_IsNaN(double p0) {
    return DoubleVector.isNaN(p0) ? 1 : 0;
  }

  public static int R_finite(double p0) {
    return DoubleVector.isFinite(p0) ? 1 : 0;
  }
}
