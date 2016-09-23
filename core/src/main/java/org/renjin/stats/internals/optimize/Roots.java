/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;


public class Roots {

  /**
   * Searches the interval from lower to upper for a root (i.e., zero) of the function f with
   * respect to its first argument.
   *
   * <p>
   * This internal primitive is used by the uniroot() function in the stats package.
   *
   * <p>
   * This implementation wraps the Commons Math {@link BrentSolver}; there are however
   *
   */
  @Internal
  public static DoubleVector zeroin2(@Current Context context, @Current Environment rho, Function fn,
                             double lower, double upper, double fLower, double fUpper,
			     double tol, int maximumIterations) {

    BrentSolver solver = new BrentSolver(maximumIterations, tol);

    double root;
    int iterations;
    double estimatedPrecision = DoubleVector.EPSILON; // not exposed by commons math impl

    try {
      root = solver.solve(maximumIterations, new UnivariateRealClosure(context, rho, fn), lower, upper);
      iterations = 1; // the Commons math impl doesn't expose this
    } catch (MaxIterationsExceededException e) {
      root = DoubleVector.NA;
      iterations = -1;
    } catch (FunctionEvaluationException e) {
      throw new EvalException(e);
    }

    return new DoubleArrayVector(root, iterations, estimatedPrecision);
  }
}
