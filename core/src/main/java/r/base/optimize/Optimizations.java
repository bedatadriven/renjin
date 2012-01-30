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
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;
import org.apache.commons.math.optimization.univariate.BrentOptimizer;
import r.base.Types;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

public class Optimizations {

  /**
   * @param context
   * @param fn
   * @param p            inital parameter value
   * @param want_hessian Hessian required?
   * @param typesize     typical size of parameter elements
   * @param fscale       expected function size
   * @param msg          message bit pattern
   * @param ndigit
   * @param gradtl
   * @param stepmx
   * @param steptol
   * @param itnlim
   * @return
   */
  public static SEXP nlm(@Current Context context, @Current Environment rho,
                         Function fn, DoubleVector p, boolean want_hessian,
                         DoubleVector typesize, double fscale, int msg, int ndigit,
                         double gradtl, double stepmx, double steptol, int itnlim) {


    SEXP value, names, v;
    Symbol R_gradientSymbol, R_hessianSymbol;

    double x[], typsiz[],
        xpls[], gpls[], a[][], wrk[], dlt;

    double fpls[] = new double[]{-1,-1};
    int i, j, k, omsg,
        n, itncnt = -1;

    int code[] = new int[]{-1, -1};
    boolean iagflg, iahflg;

/* .Internal(
 *	nlm(function(x) f(x, ...), p, hessian, typsize, fscale,
 *	    msg, ndigit, gradtol, stepmax, steptol, iterlim)
 */

    //PrintDefaults(rho);

    RUncminFunction state = new RUncminFunction(context, rho, fn);


    n = 0;
    x = fixparam(p, n);
    n = p.length();


    typsiz = fixparam(typesize, n);

    assertNotNA(fscale);

    /* `msg' (bit pattern) */
    omsg = msg;
    assertNotNA(msg);
    assertNotNA(ndigit);
    assertNotNA(gradtl);
    assertNotNA(stepmx);
    assertNotNA(steptol);
    assertNotNA(itnlim);

    /* force one evaluation to check for the gradient and hessian */
    iagflg = false;      /* No analytic gradient */
    iahflg = false;      /* No analytic hessian */
    state.setHaveGradient(false);
    state.setHaveHessian(false);


    value = state.doApply(x);


    v = value.getAttribute(RUncminFunction.GRADIENT);
    if (v != Null.INSTANCE) {
      if (v.length() == n && (v instanceof DoubleVector || v instanceof IntVector)) {
        iagflg = true;
        state.setHaveGradient(true);
        v = value.getAttribute(RUncminFunction.HESSIAN);

        if (v != Null.INSTANCE) {
          if (v.length() == (n * n) && (v instanceof DoubleVector || v instanceof IntVector)) {
            iahflg = true;
            state.setHaveHessian(true);
          } else {
            //warning(_("hessian supplied is of the wrong length or mode, so ignored"));
          }
        }
      } else {
        //warning(_("gradient supplied is of the wrong length or mode, so ignored"));
      }
    }
    if (((msg / 4) % 2) != 0 && !iahflg) { /* skip check of analytic Hessian */
      msg -= 4;
    }
    if (((msg / 2) % 2) != 0 && !iagflg) { /* skip check of analytic gradient */
      msg -= 2;
    }
    //   FT_init(n, FT_SIZE, state);
    /* Plug in the call to the optimizer here */

    Uncmin.Method method = Uncmin.Method.LINE_SEARCH;  /* Line Search */
    boolean iexp = !iahflg; /* Function calls are expensive */
    dlt = 1.0;

    xpls = Uncmin.f77_array(n);
    gpls = Uncmin.f77_array(n);
    a = Uncmin.f77_array(n, n);
    wrk = new double[8 * n];

    // the fortran-to-java manual translation unfortunately
    // retained the 1-based indexing... to fix

    Uncmin.optif9_f77(
        n,
        Uncmin.to_f77(x),
        state,
        Uncmin.to_f77(typsiz),
        fscale,
        method,
        iexp,
        new int[]{0, msg},
        new int[]{0, ndigit},
        new int[]{0, itnlim},
        new int[]{0, iagflg ? 1 : 0},
        new int[]{0, iahflg ? 1 : 0},
        new double[]{0, dlt},
        new double[]{0, gradtl},
        new double[]{0, stepmx},
        new double[]{0, steptol},
        xpls,
        fpls,
        gpls,
        code,
        a,
        wrk
        /* new int[] { itncnt } */);

    if (msg < 0) {
      opterror(msg);
    }
    if (code[0] != 0 && (omsg & 8) == 0) {
      optcode(code[0]);
    }

    ListVector.NamedBuilder result = new ListVector.NamedBuilder();

    if (want_hessian) {
//	fdhess(n, xpls, fpls, (fcn_p) fcn, state, a, n, &wrk[0], &wrk[n],
//	       ndigit, typsiz);
      for (i = 0; i < n; i++)
        for (j = 0; j < i; j++)
          a[i + j * n] = a[j + i * n];
    }

    result.add("minimum", new DoubleVector(fpls[1]));
    result.add("estimate", new DoubleVector(Uncmin.from_f77(xpls)));
    result.add("gradient", new DoubleVector(Uncmin.from_f77(gpls)));
    if (want_hessian) {
//
//	SET_STRING_ELT(names, k, mkChar("hessian"));
//	SET_VECTOR_ELT(value, k, allocMatrix(REALSXP, n, n));
//	for (i = 0; i < n * n; i++)
//	    REAL(VECTOR_ELT(value, k))[i] = a[i];
    }
    result.add("code", new IntVector(code[1]));
    result.add("iterations", new IntVector(itncnt));

    return result.build();
  }

  private static void optcode(int code) {
    // TODO
  }

  private static void opterror(int msg) {
    // TODO
  }



  static double[] fixparam(AtomicVector p, int n) {
    if (!Types.isNumeric(p))
      throw new EvalException("numeric parameter expected");

    if (n > 0) {
      if (p.length() != n) {
        throw new EvalException("conflicting parameter lengths");
      }
    } else {
      if (p.length() <= 0)
        throw new EvalException("invalid parameter length");
//	*n = LENGTH(p);
    }

    if (p.containsNA()) {
      throw new EvalException("missing value in parameter");
    }
    return p.toDoubleArray();
  }

  private static void assertNotNA(int x) {
    if (IntVector.isNA(x)) {
      throw new EvalException("invalid NA in parameter");
    }
  }

  private static void assertNotNA(double x) {
    if(DoubleVector.isNA(x)) {
      throw new EvalException("invalid NA parameter");
    }
  }

  /**
   * Searches the interval from lower to upper for a minimum or maximum of the
   * function f with respect to its first argument.
   *
   * <p>This implementation uses the BrentOptimizer from Apache Commons Math, which
   * is the same reference used by the original R:
   *
   * <p>
   * Brent, R. (1973) Algorithms for Minimization without Derivatives. Englewood Cliffs N.J.: Prentice-Hall.
   */
  public static double fmin(@Current Context context, @Current Environment rho,
                          Function fn, double lower, double upper, double tol) {

    BrentOptimizer optimizer = new BrentOptimizer();
    optimizer.setAbsoluteAccuracy(tol);
    try {
      return optimizer.optimize(new UnivariateRealClosure(context, rho, fn), GoalType.MINIMIZE, lower, upper);
    } catch (MaxIterationsExceededException e) {
      throw new EvalException("maximum iterations reached", e);
    } catch (FunctionEvaluationException e) {
      throw new EvalException(e);
    }
  }

  /**
   * General-purpose optimization based on Nelder–Mead, quasi-Newton and conjugate-gradient algorithms.
   * It includes an option for box-constrained
   * optimization and simulated annealing.
   *
   * @param par initial parameters
   * @param fn
   * @param gradientFunction
   * @param method
   * @param controlParameters
   * @param lower
   * @param upper
   * @return
   */
  @Primitive
  public static ListVector optim(@Current Context context,
                             @Current Environment rho,
                             DoubleVector par,
                             Function fn,
                             SEXP gradientFunction,
                             String method,
                             ListVector controlParameters,
                             DoubleVector lower,
                             DoubleVector upper) {

    MultivariateRealClosure g = new MultivariateRealClosure(context, rho, fn) ;

    if(method.equals("Nelder-Mead")) {

      NelderMead optimizer = new NelderMead();
      try {
        RealPointValuePair res = optimizer.optimize(g, GoalType.MINIMIZE, par.toDoubleArray());
        ListVector.Builder result = new ListVector.Builder();
        result.add(new DoubleVector(res.getPoint()));
        result.add(new DoubleVector(res.getValue()));
        result.add(new IntVector(IntVector.NA, IntVector.NA));
        result.add(new IntVector(0));
        result.add(Null.INSTANCE);
        return result.build();

      } catch (FunctionEvaluationException e) {
        throw new EvalException(e);
      } catch (OptimizationException e) {
        throw new EvalException(e);
      }
    } else {
      throw new EvalException("method '" + method + "' not implemented.");
    }
  }

}
