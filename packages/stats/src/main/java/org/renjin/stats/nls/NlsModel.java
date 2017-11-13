package org.renjin.stats.nls;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

/**
 * Wraps a NLS Model Object stored in R as a
 * list expression with names.
 */
class NlsModel {
  private final FunctionCall conv;
  private final FunctionCall incr;
  private final FunctionCall deviance;
  private final FunctionCall trace;
  private final Function setPars;
  private final FunctionCall getPars;
  private Context context;

  NlsModel(Context context, ListVector exp) {
    this.context = context;
    conv = getElementAsFunctionCall(exp, "conv");
    incr = getElementAsFunctionCall(exp, "incr");
    deviance = getElementAsFunctionCall(exp, "deviance");
    trace = getElementAsFunctionCall(exp, "trace");
    setPars = getElementAsFunction(exp, "setPars");
    getPars = getElementAsFunctionCall(exp, "getPars");
  }

  private static Function getElementAsFunction(ListVector m, String name) {
    SEXP element = m.get(name);
    if (element == Null.INSTANCE || ! (element instanceof Function)) {
      throw new EvalException("'%s' absent", "m$" + name + "()");
    }
    return (Function) element;
  }

  private static FunctionCall getElementAsFunctionCall(ListVector m, String name) {
    SEXP element = getElementAsFunction(m, name);
    return PairList.Node.newCall(element);
  }

  /**
   * @return a set of values by which to increment the parameter values for
   * the next iteration.
   */
  public DoubleVector calculateIncrements() {
    return (DoubleVector) evaluateCall(incr);
  }

  /**
   * Updates the values of the parameters within the model
   * @param newParameters the values of the new parameters
   * @return true, if the update results in a singular gradient,
   * false otherwise
   */
  public boolean updateParameters(double[] newParameters) {
    SEXP result = context.evaluate(PairList.Node.newCall(setPars, new DoubleArrayVector(newParameters)),
            context.getGlobalEnvironment());
    if(!(result instanceof AtomicVector)) {
      throw new EvalException("Unexpected result from setPars");
    }
    return ((AtomicVector) result).getElementAsLogical(0) == Logical.TRUE;
  }

  /**
   *
   * @return the current parameter values
   */
  public AtomicVector getParameterValues() {
    SEXP exp = evaluateCall(getPars);
    return (AtomicVector)exp;
  }

  public double calculateDeviation() {
    return evaluateCallAsDouble(deviance);
  }
  public double getConvergence() {
    return evaluateCallAsDouble(conv);
  }

  public void trace() {
    context.evaluate(trace, context.getGlobalEnvironment());
  }

  private SEXP evaluateCall(FunctionCall functionCall) {
    return context.evaluate(functionCall, context.getGlobalEnvironment());
  }

  private double evaluateCallAsDouble(FunctionCall functionCall) {
    SEXP result = evaluateCall(functionCall);
    Vector vector = (Vector)result;
    return vector.getElementAsDouble(0);
  }
}
