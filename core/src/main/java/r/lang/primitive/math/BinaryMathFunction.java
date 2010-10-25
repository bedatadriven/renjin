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

package r.lang.primitive.math;

import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.BinaryFunction;

public abstract class BinaryMathFunction extends BinaryFunction {

  @Override
  public EvalResult apply(LangExp call, EnvExp rho, SEXP arg0, SEXP arg1) {
    double x[] = checkedCastToNumeric(arg0);
    double y[] = checkedCastToNumeric(arg1);
    int xlen = x.length;
    int ylen = y.length;
    int maxlen = Math.max(xlen, ylen);
    int minlen = Math.min(xlen, ylen);
    
    if( maxlen % minlen != 0) {
      throw new EvalException("longer object length is not a multiple of shorter object length");
    }

    RealExp result = RealExp.ofLength(Math.max(xlen, ylen));
    for(int i=0; i!=result.length(); i++) {

      double xi = x[ i % xlen ];
      double yi = y[ i % ylen ];

      result.set(i, apply(xi, yi));
    }

    return new EvalResult(result);
  }

  protected abstract double apply(double x, double y);

  private double[] checkedCastToNumeric(SEXP x) {
    if(! (x instanceof NumericExp)) {
      throw new EvalException("non-numeric argument to binary operator");
    }
    return ((NumericExp)x).asDoubleArray();
  }
}
