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

package r.base;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.*;
import r.base.distributions.UniformDistribution;

/**
 * Density, mass, cumulative and inverse cumulative distribution functions.
 *
 * <p>The methods defined here serve as an adapter between the R function conventions and the
 * Apache Commons Math Library. (See {@link Distribution}
 */
public class Distributions {

  private Distributions() {}

  // TODO: there are several distributions for which the inverse is not provided
  // by commons math, and most non-central distributions are not present.
  // these should be implemented in the distributions package but using the Commons Math API

  /**
   * Calculates the value of the density function at {@code x}
   * for the given continuous distribution
   *
   * @param dist the distribution of the random variable
   * @param x the value
   * @param log whether to return the natural logarithm of the function's value
   * @return the (natural logarithm) of the relative likelihood for the random
   * variable to take the value {@code x}
   */
  private static double d(AbstractContinuousDistribution dist, double x, boolean log) {
    double d = dist.density(x);
    if(log) {
      d = Math.log(d);
    }
    return d;
  }

  /**
   * Calculates the value of the probability mass function at {@code x}
   * for the given discrete distribution
   *
   * @param dist the discrete distribution
   * @param x the value
   * @param log whether to return the natural logarithm of the probability
   * @return  the (natural logarithm) of the probability for the  random variable
   *  to take the value {@code x}
   */
  private static double d(IntegerDistribution dist, double x, boolean log)  {
    double d = dist.probability(x);
    if(log) {
      d = Math.log(d);
    }
    return d;
  }

  /**
   *
   * Calculates the value of the cumulative distribution function
   *
   * @param dist the distribution
   * @param q the value
   * @param lowerTail if true, return the value P(x < q), otherwise P(x > q)
   * @param logP  if true, return the natural logarithm of the probability
   * @return  the probability that the random variable will take the value less than (greater than)
   * {@code q}
   */
  private static double p(Distribution dist, double q, boolean lowerTail, boolean logP) {
    double p;
    try {
      p = dist.cumulativeProbability(q);
    } catch (MathException e) {
      return Double.NaN;
    } catch (MathRuntimeException e) {
      return Double.NaN;
    }
    if(!lowerTail) {
      p = 1.0 - p;
    }
    if(logP) {
      p = Math.log(p);
    }

    return p;
  }

  /**
   * Calculates the value of the inverse cumulative probability function according to standard R arguments.
   *
   * @param dist the continuous distribution
   * @param p the probability
   * @param lowerTail
   * @param logP if true, interpret {@code p} as the natural logarithm of the probability
   * @return the value fo
   */
  private static double q(ContinuousDistribution dist, double p, boolean lowerTail, boolean logP)  {
    if(logP) {
      p = Math.exp(p);
    }
    double q = 0;
    try {
      q = dist.inverseCumulativeProbability(p);
    } catch (IllegalArgumentException e) {
      return Double.NaN;
    } catch (MathException e) {
      return Double.NaN;
    } catch (MathRuntimeException e) {
      return Double.NaN;
    }
    if(!lowerTail) {
      q = -q;
    }
    return q;
  }


  public static double dnorm(double x, double mean, double sd, boolean log)  {
    return d(new NormalDistributionImpl(mean, sd), x, log);
  }

  public static double pnorm(double q, double mean, double sd, boolean lowerTail, boolean logP)  {
    return p(new NormalDistributionImpl(mean, sd), q, lowerTail, logP);
  }

  public static double qnorm(double p, double mean, double sd, boolean lowerTail, boolean logP)  {
    return q(new NormalDistributionImpl(mean, sd), p, lowerTail, logP);
  }

  public static double dbeta(double x, double shape1, double shape2, boolean log)  {
    return d(new BetaDistributionImpl(shape1, shape2), x, log);
  }

  public static double pbeta(double q, double shape1, double shape2, boolean lowerTail, boolean logP)  {
    return p(new BetaDistributionImpl(shape1, shape2), q, lowerTail, logP);
  }

  public static double qbeta(double p, double shape1, double shape2, boolean lowerTail, boolean logP)  {
    return q(new BetaDistributionImpl(shape1, shape2), p, lowerTail, logP);
  }

  public static double dchisq(double x, double df, boolean log)  {
    return d(new ChiSquaredDistributionImpl(df), x, log);
  }

  public static double pchisq(double q, double df, boolean lowerTail, boolean logP)  {
    return p(new ChiSquaredDistributionImpl(df), q, lowerTail, logP);
  }

  public static double qchisq(double p, double df, boolean lowerTail, boolean logP)  {
    return q(new ChiSquaredDistributionImpl(df), p, lowerTail, logP);
  }

  public static double dexp(double x, double mean, boolean log)  {
    return d(new ExponentialDistributionImpl(mean), x, log);
  }

  public static double pexp(double q, double mean, boolean lowerTail, boolean logP)  {
    return p(new ExponentialDistributionImpl(mean), q, lowerTail, logP);
  }

  public static double qexp(double p, double mean, boolean lowerTail, boolean logP)  {
    return q(new ExponentialDistributionImpl(mean), p, lowerTail, logP);
  }

  public static double dt(double x, double df, boolean log)  {
    return d(new TDistributionImpl(df), x, log);
  }

  public static double pt(double q, double df, boolean lowerTail, boolean logP)  {
    return p(new TDistributionImpl(df), q, lowerTail, logP);
  }

  public static double qt(double p, double df, boolean lowerTail, boolean logP)  {
    return q(new TDistributionImpl(df), p, lowerTail, logP);
  }

  public static double dpois(double x, double lambda, boolean log)  {
    return d(new PoissonDistributionImpl(lambda), x, log);
  }

  public static double ppois(double q, double lambda, boolean lowerTail, boolean logP)  {
    return p(new PoissonDistributionImpl(lambda), q, lowerTail, logP);
  }

//  public static double qpois(double p, double lambda, boolean lowerTail, boolean logP)  {
//    return q(new PoissonDistributionImpl(lambda), p, lowerTail, logP);
//  }

  public static double dbinom(double x, int size, double prob, boolean log)  {
    return d(new BinomialDistributionImpl(size, prob), x, log);
  }

  public static double pbinom(double x, int size, double prob, boolean lowerTail, boolean logP)  {
    return p(new BinomialDistributionImpl(size, prob), x, lowerTail, logP);
  }

//  public static double qbinom(double p, int size, double prob, boolean lowerTail, boolean logP)  {
//    return q(new BinomialDistributionImpl(size, prob), p, lowerTail, logP);
//  }

  public static double dcauchy(double x, double location, double scale, boolean log)  {
    return d(new CauchyDistributionImpl(location, scale), x, log);
  }

  public static double pcauchy(double q, double location, double scale, boolean lowerTail, boolean logP)  {
    return p(new CauchyDistributionImpl(location, scale), q, lowerTail, logP);
  }

  public static double qcauchy(double p, double location, double scale, boolean lowerTail, boolean logP)  {
    return q(new CauchyDistributionImpl(location, scale), p, lowerTail, logP);
  }

  public static double df(double x, double df1, double df2, boolean log)  {
    return d(new FDistributionImpl(df1, df2), x, log);
  }

  public static double pf(double q, double df1, double df2, boolean lowerTail, boolean logP)  {
    return p(new FDistributionImpl(df1, df2), q, lowerTail, logP);
  }

  public static double qf(double p, double df1, double df2, boolean lowerTail, boolean logP)  {
    return q(new FDistributionImpl(df1, df2), p, lowerTail, logP);
  }

  public static double dgamma(double x, double shape, double scale, boolean log)  {
    return d(new GammaDistributionImpl(shape, scale), x, log);
  }

  public static double pgamma(double q, double shape, double scale, boolean lowerTail, boolean logP)  {
    return p(new GammaDistributionImpl(shape, scale), q, lowerTail, logP);
  }

  public static double qgamma(double p, double shape, double scale, boolean lowerTail, boolean logP)  {
    return q(new GammaDistributionImpl(shape, scale), p, lowerTail, logP);
  }

  public static double dunif(double x, double min, double max, boolean log) {
    double d = new UniformDistribution(min,max).density(x);
    if(log) {
      d = Math.log(d);
    }
    return d;
  }

  public static double punif(double q, double min, double max, boolean lowerTail, boolean logP)  {
    return p(new UniformDistribution(min, max), q, lowerTail, logP);
  }

  public static double qunif(double p, double min, double max, boolean lowerTail, boolean logP)  {
    return q(new UniformDistribution(min, max), p, lowerTail, logP);
  }

  public static double dweibull(double x, double shape, double scale, boolean log)  {
    return d(new WeibullDistributionImpl(shape, scale), x, log);
  }

  public static double pweibull(double q, double shape, double scale, boolean lowerTail, boolean logP)  {
    return p(new WeibullDistributionImpl(shape, scale), q, lowerTail, logP);
  }

  public static double qweibull(double p, double shape, double scale, boolean lowerTail, boolean logP)  {
    return q(new WeibullDistributionImpl(shape, scale), p, lowerTail, logP);
  }


  public static double dhyper(double x, double whiteBalls, double blackBalls, double sampleSize, boolean log)  {
    return d(new HypergeometricDistributionImpl((int)(whiteBalls+blackBalls), (int)whiteBalls, (int)sampleSize), x, log);
  }

  public static double phyper(double q, double x,  double whiteBalls, double blackBalls, double sampleSize, boolean lowerTail, boolean logP)  {
    return p(new HypergeometricDistributionImpl((int)(whiteBalls+blackBalls), (int)whiteBalls, (int)sampleSize), q, lowerTail, logP);
  }

//  public static double qhyper(double p,double m, double n, double k, boolean lowerTail, boolean logP)  {
//    return q(new HypergeometricDistributionImpl((int)m, (int)n, (int)k), p, lowerTail, logP);
//  }
}
