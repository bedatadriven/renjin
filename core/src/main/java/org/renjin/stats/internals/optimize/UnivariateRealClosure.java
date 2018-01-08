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
package org.renjin.stats.internals.optimize;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.renjin.eval.Context;
import org.renjin.sexp.*;

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
    FunctionCall call = FunctionCall.newCall(fn, new DoubleArrayVector(x));
    Vector y = (Vector)context.evaluate(call, rho);
    return y.getElementAsDouble(0);
  }
}
