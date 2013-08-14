package org.renjin.stats;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.Types;
import org.renjin.primitives.matrix.DoubleMatrixBuilder;
import org.renjin.sexp.*;

public class NonlinearLeastSquares {

  @Internal
  public static SEXP nls_iter(@Current Context context, ListVector m, ListVector control, Logical doTraceArg) {
    double fac, newDev, convNew = -1./*-Wall*/;
    int i, j, nPars, evaltotCnt = -1;

    boolean hasConverged, doTrace = (doTraceArg == Logical.TRUE);

    int maxIter = control.getElementAsInt("maxiter");
    double tolerance = control.getElementAsDouble("tolerance");
    double minFac = control.getElementAsDouble("minFactor");
    boolean warnOnly = (control.getElementAsInt("warnOnly") == 1);
    boolean printEval = (control.getElementAsInt("printEval") == 1);

    SEXP conv = m.get("conv");
    if (conv == Null.INSTANCE || !Types.isFunction(conv)) {
      throw new EvalException("'%s' absent", "m$conv()");
    }
    conv = FunctionCall.newCall(conv);

    SEXP incr = m.get("incr");
    if (incr == Null.INSTANCE || !Types.isFunction(incr)) {
      throw new EvalException("'%s' absent", "m$incr()");
    }
    incr = FunctionCall.newCall(incr);

    SEXP deviance = m.get("deviance");
    if (deviance == Null.INSTANCE || !Types.isFunction(deviance)) {
      throw new EvalException("'%s' absent", "m$deviance()");
    }
    deviance = FunctionCall.newCall(deviance);

    SEXP trace = m.get("trace");
    if (trace == Null.INSTANCE || !Types.isFunction(trace)) {
      throw new EvalException("'%s' absent", "m$trace()");
    }
    trace = FunctionCall.newCall(trace);

    SEXP setPars = m.get("setPars");
    if (setPars == Null.INSTANCE || !Types.isFunction(setPars)) {
      throw new EvalException("'%s' absent", "m$setPars()");
    }
    setPars = FunctionCall.newCall(setPars);

    SEXP getPars = m.get("getPars");
    if (getPars == Null.INSTANCE || !Types.isFunction(getPars)) {
      throw new EvalException("'%s' absent", "m$getPars()");
    }
    getPars = FunctionCall.newCall(getPars);

    SEXP pars = setPars;
    nPars = pars.length();

    double dev = context.evaluate(deviance, context.getEnvironment()).asReal();
    if (doTrace) {
      context.evaluate(trace, context.getEnvironment());
    }

    fac = 1.0;
    hasConverged = false;

    DoubleArrayVector.Builder newPars = DoubleArrayVector.Builder.withInitialCapacity(nPars);
    if (printEval) {
      evaltotCnt = 1;
    }

    for (i = 0; i < maxIter; i++) {
      SEXP newIncr;
      int evalCnt = -1;
      if ((convNew = context.evaluate(conv, context.getEnvironment()).asReal()) < tolerance) {
        hasConverged = true;
        break;
      }
      newIncr = context.evaluate(incr, context.getEnvironment());

      if (printEval) {
        evalCnt = 1;
      }

      while (fac >= minFac) {
        if (printEval) {
          // ???: is this a good replacement for Rprintf
          System.out.printf("  It. %3d, fac= %11.6f, eval (no.,total): (%2d,%3d):",
                  i + 1, fac, evalCnt, evaltotCnt);
          evalCnt++;
          evaltotCnt++;
        }

        for (j = 0; j < nPars; j++) {
          newPars.set(j, pars.getElementAsSEXP(j).asReal() + fac * newIncr.getElementAsSEXP(j).asReal());
        }

        FunctionCall setParsFunction = FunctionCall.newCall(setPars, newPars.build());
        if (context.evaluate(setParsFunction, context.getEnvironment()).asLogical() == Logical.TRUE) { /* singular gradient */

          if (warnOnly) {
            // warning("singular gradient");
            return convInfoMsg("singular gradient", i, 1, fac, minFac, maxIter, convNew);
          } else {
            throw new EvalException("singular gradient");
          }
        }

        newDev = context.evaluate(deviance, context.getEnvironment()).asReal();
        if (printEval) {
          // ???: is this a good replacement for Rprintf
          System.out.printf(" new dev = %f\n", newDev);
        }

        SEXP tmp;
        if (newDev <= dev) {
          dev = newDev;
          fac = Math.min(2 * fac, 1);
          tmp = newPars;
          newPars = pars;
          pars = tmp;
          break;
        }
        fac /= 2.;
      }

      if (fac < minFac) {
        if (warnOnly) {
          // warning("step factor %f reduced below 'minFactor' of %f", fac, minFac);
          return convInfoMsg(String.format("step factor %f reduced below 'minFactor' of %f", fac, minFac), i, 2, fac, minFac, maxIter, convNew);
        } else {
          throw new EvalException("step factor %f reduced below 'minFactor' of %f", fac, minFac);
        }
      }
      if (doTrace) {
        context.evaluate(trace, context.getEnvironment());
      }
    }

    if (!hasConverged) {
      if (warnOnly) {
        // warning("number of iterations exceeded maximum of %d", maxIter);
        return convInfoMsg(String.format("number of iterations exceeded maximum of %d", maxIter), i, 3, fac, minFac, maxIter, convNew);
      } else {
        throw new EvalException("number of iterations exceeded maximum of %d", maxIter);
      }
    }
    /* else */

    return convInfoMsg("converged", i, 0, fac, minFac, maxIter, convNew);
  }

  @Internal
  public static SEXP numeric_deriv(@Current Context context, SEXP expr, StringVector theta, @Current Environment rho, SEXP dir) {
    DoubleVector ans;
    double eps = Math.sqrt(DoubleVector.EPSILON);
    int start, i, j, k, lengthTheta = 0;

    if (!Types.isReal(dir) || dir.length() != theta.length()) {
      throw new EvalException("'dir' is not a numeric vector of the correct length");
    }

    ListVector.Builder pars = new ListVector.Builder();

    if (expr instanceof Symbol) {
      ans = duplicate(context.evaluate(expr, rho));
    } else {
      ans = context.evaluate(expr, rho);
    }

    for (i = 0; i < ans.length(); i++) {
      if (!DoubleVector.isFinite(ans.get(i))) {
        throw new EvalException("Missing value or an infinity produced when evaluating the model");
      }
    }

    for (i = 0; i < theta.length(); i++) {
      String name = theta.getElementAsString(i);
      SEXP temp = rho.findVariable(Symbol.get(name));
      if (Types.isInteger(temp)) {
        throw new EvalException("variable '%s' is integer, not numeric", name);
      }

      if (!Types.isReal(temp)) {
        throw new EvalException("variable '%s' is not numeric", name);
      }

      pars.set(i, temp);
      lengthTheta += temp.length();
    }

    //
    DoubleMatrixBuilder gradient = new DoubleMatrixBuilder(ans.length(), lengthTheta);

    for (i = 0, start = 0; i < theta.length(); i++) {
      for (j = 0; j < pars.getElementAsSEXP(i).length(); j++, start += ans.length()) {
        SEXP ans_del;
        double origPar, xx, delta;

        origPar = pars.getElementAsSEXP(i))[j];
        xx = Math.abs(origPar);
        delta = (xx == 0) ? eps : xx * eps;
        REAL(pars.getElementAsSEXP(i))[j] += rDir[i] * delta;
        ans_del = context.evaluate(expr, rho);

        if (!Types.isReal(ans_del)) {
          ans_del = coerceVector(ans_del, REALSXP);
        }

        for (k = 0; k < ans.length(); k++) {
          if (!DoubleVector.isFinite(ans_del.getElementAsSEXP(k).asReal())) {
            throw new EvalException("Missing value or an infinity produced when evaluating the model");
          }
          REAL(gradient)[start + k] = rDir[i] * (REAL(ans_del)[k] - REAL(ans)[k]) / delta;
        }

        REAL(VECTOR_ELT(pars, i))[j] = origPar;
      }
    }

    ans.setAttribute(Symbol.get("gradient"), gradient);

    return ans;
  }

  private static SEXP convInfoMsg(String msg, int iter, int whystop, double fac,
                                  double minFac, int maxIter, double convNew) {
    ListVector.NamedBuilder ans = ListVector.newNamedBuilder();

    ans.add("isConv", Logical.valueOf(whystop == 0));
    ans.add("finIter", Integer.valueOf(iter));
    ans.add("finTol", Double.valueOf(convNew));
    ans.add("stopCode", Integer.valueOf(whystop));
    ans.add("stopMessage", msg);

    return ans.build();
  }

}
