package org.renjin.primitives.random.distributions;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.AbstractContinuousDistribution;
import org.apache.commons.math.distribution.ContinuousDistribution;

public class LogisticDistribution extends AbstractContinuousDistribution implements ContinuousDistribution {

  double m, s;

  public LogisticDistribution(double m, double s) {
    this.m = m;
    this.s = s;
  }

  @Override
  public double inverseCumulativeProbability(double p) throws MathException {
    return (Math.log(p / (1 - p)));
  }

  @Override
  public double cumulativeProbability(double x) throws MathException {
    return (1 / (1 + Math.exp((m - x) / s)));
  }

  @Override
  public double cumulativeProbability(double x1, double x2) throws MathException {
    return(cumulativeProbability(x2) - cumulativeProbability(x1));
  }

  @Override
  protected double getInitialDomain(double d) {
    /*
     * What is initial domain?
     */
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected double getDomainLowerBound(double d) {
    return(Double.NEGATIVE_INFINITY);
  }

  @Override
  protected double getDomainUpperBound(double d) {
    return(Double.POSITIVE_INFINITY);
  }

  @Override
  public double density(double x) throws MathRuntimeException {
    return Math.exp((m-x)/s) / (s * Math.pow(1 + Math.exp((m-x)/s),2.0));
  }
  
  
}
