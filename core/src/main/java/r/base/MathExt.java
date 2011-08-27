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

package r.base;

import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.MathUtils;
import r.jvmi.annotations.Primitive;
import r.lang.IntVector;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;

/**
 * Math functions not found in java.Math or apache commons math
 */
public class MathExt {

  private MathExt() {}

  public static double gamma(double x) {
    return Math.exp(Gamma.logGamma(x));
  }

  public static double log(double x, double base) {

      //Method cannot be called directly as R and Apache Commons Math argument order
      // are reversed
      return MathUtils.log(base, x);
  }

  public static double log(double d) {
      return Math.log(d);
  }

  public static double log2(double d) {
      return MathUtils.log(2, d);
  }
  
  /*
   * Attention:
   * Colnames and rownames do not copied into the new vector.
   * This must be corrected. Empty or Null vector control is not implemented.
   */
  @Primitive("t.default")
  public static Vector transpose (Vector x){
    Vector.Builder builder = x.newBuilder(x.length());
    Vector result = builder.build();
    int nrows = (int)x.getAttribute(Symbol.DIM).getElementAsSEXP(0).asReal();
    int ncols = (int)x.getAttribute(Symbol.DIM).getElementAsSEXP(1).asReal();
    for (int i=0;i<nrows;i++){
      for (int j=0;j<ncols;j++){
        builder.setFrom(j * nrows + i, x, i * ncols + j );
      }
    }
    builder.setAttribute(Symbol.ROW_NAMES, x.getAttribute(Symbol.DIMNAMES));
    builder.setAttribute(Symbol.DIMNAMES, x.getAttribute(Symbol.ROW_NAMES));
    builder.setAttribute(Symbol.DIM, new IntVector(ncols, nrows));
    result = builder.build();
    return(result);
  }

}
