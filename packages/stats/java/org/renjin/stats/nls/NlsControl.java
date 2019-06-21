package org.renjin.stats.nls;

import org.renjin.sexp.ListVector;

/**
 * Wraps an nlsControl object
 */
class NlsControl {
  private final int maxIter;
  private final double tolerance;
  private final double minFac;
  private final boolean warnOnly;
  private final boolean printEval;

  NlsControl(ListVector exp) {
    maxIter = exp.getElementAsInt("maxiter");
    tolerance = exp.getElementAsDouble("tol");
    minFac = exp.getElementAsDouble("minFactor");
    warnOnly = (exp.getElementAsInt("warnOnly") == 1);
    printEval = (exp.getElementAsInt("printEval") == 1);
  }

  public boolean isPrintEval() {
    return printEval;
  }

  public double getTolerance() {
    return tolerance;
  }
  public int getMaxIterations() {
    return maxIter;
  }

  public double getMinFactor() {
    return minFac;
  }

  public boolean isWarnOnly() {
    return warnOnly;
  }
}
