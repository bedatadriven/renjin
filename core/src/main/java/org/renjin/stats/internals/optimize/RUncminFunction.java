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

package org.renjin.stats.internals.optimize;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

class RUncminFunction implements UncminFunction {

  public static final Symbol GRADIENT = Symbol.get("gradient");
  public static final Symbol HESSIAN = Symbol.get("hessian");

  private final Context context;
  /**
   * The environment in which the function should be evaluated
   */
  private Environment rho;

  private Function function;

  private boolean have_gradient;

  private boolean have_hessian;

  public RUncminFunction(Context context, Environment rho, Function fn) {
    this.context = context;
    this.rho = rho;
    this.function = fn;
  }


  public void setHaveGradient(boolean b) {
    this.have_gradient = b;
  }

  public void setHaveHessian(boolean b) {
    this.have_gradient = b;
  }

  public SEXP doApply(double x[]) {
    FunctionCall call = FunctionCall.newCall(function, new DoubleArrayVector(x));
    return context.evaluate(call, rho);
  }

  @Override
  public double apply(double[] x) {
    AtomicVector y = (AtomicVector) doApply(Uncmin.from_f77(x));
    return y.getElementAsDouble(0);
  }

  @Override
  public void applyGradient(double[] x, double[] g) {

    SEXP y = doApply(Uncmin.from_f77(x));
    Vector gradient = (Vector) y.getAttribute(GRADIENT);
    for(int i=0;i!=gradient.length();++i) {
      g[i+1] = gradient.getElementAsDouble(i);
    }
  }

  @Override
  public void applyHessian(double[] x, double[][] h) {
    throw new UnsupportedOperationException("hessian functions not yet implemented");

  }
}
