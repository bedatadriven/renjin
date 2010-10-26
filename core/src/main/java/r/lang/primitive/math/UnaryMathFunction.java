/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

import r.lang.EvalResult;
import r.lang.NumericExp;
import r.lang.RealExp;
import r.lang.SEXP;
import r.lang.exception.EvalException;
import r.lang.primitive.PureUnaryFunction;

public abstract class UnaryMathFunction extends PureUnaryFunction {

  @Override
  public final EvalResult apply(SEXP argument) {

    if (!(argument instanceof NumericExp)) {
      throw new EvalException("Non-numeric argument to mathematical function");
    }

    double x[] = ((NumericExp)argument).asDoubleArray();

    return new EvalResult(applyReal(x));
  }

  private RealExp applyReal(double sa[]) {
    RealExp sy = RealExp.ofLength(sa.length);

    boolean naflag = false;
    int i;

    for (i = 0; i < sa.length; i++) {
      if (Double.isNaN(sa[i])) {
        sy.set(i, sa[i]);
      } else {
        sy.set(i, apply(sa[i]));
        if (Double.isNaN(sy.get(i))) {
          naflag = true;
        }
      }
    }
    if (naflag) {
      //warningcall(lcall, R_MSG_NA);
    }

    // todo: attribs DUPLICATE_ATTRIB(sy, sa);
    return sy;
  }

  public abstract double apply(double value);


}
