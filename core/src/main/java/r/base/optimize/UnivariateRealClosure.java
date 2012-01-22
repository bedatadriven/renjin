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

package r.base.optimize;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import r.lang.*;

public class UnivariateRealClosure implements UnivariateRealFunction {

  private final Context context;
  private final Environment rho;
  private final Function fn;

  public UnivariateRealClosure(Context context, Environment rho, Function fn) {
    this.context = context;
    this.rho = rho;
    this.fn = fn;
  }

  @Override
  public double value(double x) throws FunctionEvaluationException {
    FunctionCall call = FunctionCall.newCall(fn, new DoubleVector(x));
    Vector y = (Vector)context.evaluate(call, rho);
    return y.getElementAsDouble(0);
  }
}
