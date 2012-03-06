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
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.renjin.primitives.annotations.Current;

import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.Function;
import r.lang.exception.EvalException;

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
  public static DoubleVector zeroin2(@Current Context context, @Current Environment rho, Function fn,
                             double lower, double upper, double fLower, double fUpper,
			     double tol, int maximumIterations) {

    BrentSolver solver = new BrentSolver(maximumIterations, tol);

    double root;
    int iterations;
    double estimatedPrecision = DoubleVector.EPSILON; // not exposed by commons math impl

    try {
      root = solver.solve(new UnivariateRealClosure(context, rho, fn), lower, upper);
      iterations = 1; // the Commons math impl doesn't expose this
    } catch (MaxIterationsExceededException e) {
      root = DoubleVector.NA;
      iterations = -1;
    } catch (FunctionEvaluationException e) {
      throw new EvalException(e);
    }

    return new DoubleVector(root, iterations, estimatedPrecision);
  }
}
