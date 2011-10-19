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
import org.apache.commons.math.distribution.AbstractContinuousDistribution;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.CauchyDistributionImpl;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.Distribution;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.apache.commons.math.distribution.IntegerDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.distribution.PascalDistributionImpl;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.distribution.WeibullDistributionImpl;

import r.base.distributions.LogisticDistribution;
import r.base.distributions.UniformDistribution;
import r.base.random.Beta;
import r.base.random.Binom;
import r.base.random.ChiSquare;
import r.base.random.Geometric;
import r.base.random.LNorm;
import r.base.random.SignRank;
import r.base.random.Wilcox;
import r.jvmi.annotations.Recycle;

/**
 * Density, mass, cumulative and inverse cumulative distribution functions.
 *
 * <p>The methods defined here serve as an adapter between the R function conventions and the
 * Apache Commons Math Library. (See {@link Distribution}
 */
public class Distributions {

  private Distributions() {
  }

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
    if (log) {
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
  private static double d(IntegerDistribution dist, double x, boolean log) {
    double d = dist.probability(x);
    if (log) {
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
    if (!lowerTail) {
      p = 1.0 - p;
    }
    if (logP) {
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
  private static double q(ContinuousDistribution dist, double p, boolean lowerTail, boolean logP) {
    if (logP) {
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
    if (!lowerTail) {
      q = -q;
    }
    return q;
  }

   private static double q(IntegerDistribution dist, double p, boolean lowerTail, boolean logP) {
    if (logP) {
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
    if (!lowerTail) {
      q = -q;
    }
    return q;
  }

  public static double dnorm(@Recycle double x, @Recycle double mean, @Recycle double sd, boolean log) {
    return d(new NormalDistributionImpl(mean, sd), x, log);
  }

  public static double pnorm(@Recycle double q, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return p(new NormalDistributionImpl(mean, sd), q, lowerTail, logP);
  }

  public static double plnorm(@Recycle double q, @Recycle double logmean, @Recycle double logsd, boolean lowerTail, boolean logP) {
    return p(new NormalDistributionImpl(logmean, logsd), Math.log(q), lowerTail, logP);
  }

  public static double qnorm(@Recycle double p, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return q(new NormalDistributionImpl(mean, sd), p, lowerTail, logP);
  }

  public static double qlnorm(@Recycle double p, @Recycle double meanlog, @Recycle double sdlog, boolean lowerTail, boolean logP) {
    return Math.exp(q(new NormalDistributionImpl(meanlog, sdlog), p, lowerTail, logP));
  }
  
  public static double dlnorm(@Recycle double x, @Recycle double meanlog, @Recycle double sdlog, boolean logP) {
    return LNorm.dlnorm(x, meanlog, sdlog, logP);
  }

  public static double dbeta(@Recycle double x, @Recycle double shape1, @Recycle double shape2, boolean log) {
    return d(new BetaDistributionImpl(shape1, shape2), x, log);
  }
  
  public static double dnbeta(@Recycle double x, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean log) {
    return Beta.dnbeta(x, shape1, shape2, ncp, log);
  }
  

  public static double pbeta(@Recycle double q, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return p(new BetaDistributionImpl(shape1, shape2), q, lowerTail, logP);
  }

  public static double qbeta(@Recycle double p, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return q(new BetaDistributionImpl(shape1, shape2), p, lowerTail, logP);
  }

  public static double dchisq(@Recycle double x, @Recycle double df, boolean log) {
    return d(new ChiSquaredDistributionImpl(df), x, log);
  }

  public static double pchisq(@Recycle double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return p(new ChiSquaredDistributionImpl(df), q, lowerTail, logP);
  }
  
  public static double pnchisq(@Recycle double q, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return ChiSquare.pnchisq(q, df, ncp, lowerTail, logP);
  }

  public static double qchisq(@Recycle double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return q(new ChiSquaredDistributionImpl(df), p, lowerTail, logP);
  }
  
  public static double qnchisq(@Recycle double p, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return ChiSquare.qnchisq(p, df, ncp, lowerTail, logP);
  }

  public static double dexp(@Recycle double x, @Recycle double mean, boolean log) {
    return d(new ExponentialDistributionImpl(mean), x, log);
  }

  public static double pexp(@Recycle double q, @Recycle double mean, boolean lowerTail, boolean logP) {
    return p(new ExponentialDistributionImpl(mean), q, lowerTail, logP);
  }

  public static double qexp(@Recycle double p, @Recycle double mean, boolean lowerTail, boolean logP) {
    return q(new ExponentialDistributionImpl(mean), p, lowerTail, logP);
  }

  /*
   * non-centarity parameters is not implemented yet. Correct this.
   */ 
  public static double dt(@Recycle double x, @Recycle double df, boolean log) {
    return d(new TDistributionImpl(df), x, log);
  }

  /*
   * non-centarity parameters is not implemented yet. Correct this.
   */ 
  public static double pt(@Recycle double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return p(new TDistributionImpl(df), q, lowerTail, logP);
  }

  /*
   * non-centarity parameters is not implemented yet. Correct this.
   */ 
  public static double qt(@Recycle double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return q(new TDistributionImpl(df), p, lowerTail, logP);
  }

  public static double dpois(@Recycle double x, @Recycle double lambda, boolean log) {
    return d(new PoissonDistributionImpl(lambda), x, log);
  }

  public static double ppois(@Recycle double q, @Recycle double lambda, boolean lowerTail, boolean logP) {
    return p(new PoissonDistributionImpl(lambda), q, lowerTail, logP);
  }

//  public static double qpois(double p, double lambda, boolean lowerTail, boolean logP)  {
//    return q(new PoissonDistributionImpl(lambda), p, lowerTail, logP);
//  }
  public static double dbinom(@Recycle double x, @Recycle int size, @Recycle double prob, boolean log) {
    return d(new BinomialDistributionImpl(size, prob), x, log);
  }

  public static double dnbinom(@Recycle double x, @Recycle int size, @Recycle double prob, boolean log) {
    return d(new PascalDistributionImpl(size, prob), x, log);
  }

  public static double pbinom(@Recycle double x, @Recycle int size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return p(new BinomialDistributionImpl(size, prob), x, lowerTail, logP);
  }

  public static double pnbinom(@Recycle double x, @Recycle int size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return p(new PascalDistributionImpl(size, prob), x, lowerTail, logP);
  }

 public static double qbinom(@Recycle double p, @Recycle int size, @Recycle double prob, boolean lowerTail, boolean logP)  {
    return q(new BinomialDistributionImpl(size, prob), p, lowerTail, logP) + 1;
  }
 
 public static double qnbinom(@Recycle double p, @Recycle double size, @Recycle double prob, boolean lower_tail, boolean log_p){
   return Binom.qnbinom(p, size, prob, lower_tail, log_p);
 }

 
  public static double dcauchy(@Recycle double x, @Recycle double location, @Recycle double scale, boolean log) {
    return d(new CauchyDistributionImpl(location, scale), x, log);
  }

  public static double pcauchy(@Recycle double q, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return p(new CauchyDistributionImpl(location, scale), q, lowerTail, logP);
  }

  public static double qcauchy(@Recycle double p, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return q(new CauchyDistributionImpl(location, scale), p, lowerTail, logP);
  }

  public static double df(@Recycle double x, @Recycle double df1, @Recycle double df2, boolean log) {
    return d(new FDistributionImpl(df1, df2), x, log);
  }

  public static double pf(@Recycle double q, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return p(new FDistributionImpl(df1, df2), q, lowerTail, logP);
  }

  public static double qf(@Recycle double p, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return q(new FDistributionImpl(df1, df2), p, lowerTail, logP);
  }

  public static double dgamma(@Recycle double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return d(new GammaDistributionImpl(shape, scale), x, log);
  }

  public static double pgamma(@Recycle double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return p(new GammaDistributionImpl(shape, scale), q, lowerTail, logP);
  }

  public static double qgamma(@Recycle double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return q(new GammaDistributionImpl(shape, scale), p, lowerTail, logP);
  }

  public static double dunif(@Recycle double x, @Recycle double min, @Recycle double max, boolean log) {
    double d = new UniformDistribution(min, max).density(x);
    if (log) {
      d = Math.log(d);
    }
    return d;
  }

  public static double punif(@Recycle double q, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return p(new UniformDistribution(min, max), q, lowerTail, logP);
  }

  public static double qunif(@Recycle double p, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return q(new UniformDistribution(min, max), p, lowerTail, logP);
  }

  public static double dweibull(@Recycle double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return d(new WeibullDistributionImpl(shape, scale), x, log);
  }

  public static double pweibull(@Recycle double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return p(new WeibullDistributionImpl(shape, scale), q, lowerTail, logP);
  }

  public static double qweibull(@Recycle double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return q(new WeibullDistributionImpl(shape, scale), p, lowerTail, logP);
  }

  public static double dhyper(@Recycle double x, @Recycle double whiteBalls, @Recycle double blackBalls, @Recycle double sampleSize, boolean log) {
    return d(new HypergeometricDistributionImpl((int) (whiteBalls + blackBalls), (int) whiteBalls, (int) sampleSize), x, log);
  }

  public static double phyper(@Recycle double q, @Recycle double x, @Recycle double whiteBalls, @Recycle double blackBalls, @Recycle double sampleSize, boolean lowerTail, boolean logP) {
    return p(new HypergeometricDistributionImpl((int) (whiteBalls + blackBalls), (int) whiteBalls, (int) sampleSize), q, lowerTail, logP);
  }

//  public static double qhyper(double p,double m, double n, double k, boolean lowerTail, boolean logP)  {
//    return q(new HypergeometricDistributionImpl((int)m, (int)n, (int)k), p, lowerTail, logP);
//  }
  
  
  /*
  public static double dgeom(@Recycle int x, @Recycle double p, @Recycle boolean log) {
    if (log) {
      return (Math.log(p * Math.pow(1 - p, x)));
    } else {
      return (p * Math.pow(1 - p, x));
    }
  }
   */
  
  public static double pgeom(@Recycle double q, @Recycle double prob, boolean lowerTail, boolean log){
    return (Geometric.pgeom(q, prob, lowerTail, log));
  }
  
  public static double dgeom(@Recycle double x, @Recycle double prob, boolean log){
    return Geometric.dgeom(x, prob, log);
  }
  
  public static double qgeom(@Recycle double p, @Recycle double prob, boolean lowerTail, boolean log){
    return Geometric.qgeom(p, prob, lowerTail, log);
  }

  public static double plogis(@Recycle double p, @Recycle double m, @Recycle double s, boolean lowerTail, boolean logP) {
    return p(new LogisticDistribution(m, s), p, lowerTail, logP);
  }

  public static double dlogis(@Recycle double x, @Recycle double location, @Recycle double scale, boolean log) {
    return d(new LogisticDistribution(location, scale), x, log);
  }

  public static double qlogis(@Recycle double p, @Recycle double m, @Recycle double s, boolean lowerTail, boolean logP) {
    return q(new LogisticDistribution(m, s), p, lowerTail, logP);
  }
  
  public static double qsignrank(double p, double n, boolean lowerTail, boolean logP)  {
    return SignRank.qsignrank(p, n, lowerTail, logP);
  }
  
  public static double psignrank(double p, double n, boolean lowerTail, boolean logP)  {
    return SignRank.psignrank(p, n, lowerTail, logP);
  }
  
  public static double dsignrank(double x, double n, boolean logP)  {
    return SignRank.dsignrank(x, n, logP);
  }
  
  public static double dwilcox(double x, double m, double n, boolean logP)  {
    return Wilcox.dwilcox(x, m, n, logP);
  }
  
  public static double pwilcox(double q, double m, double n, boolean lowerTail, boolean logP)  {
    return Wilcox.pwilcox(q, m, n, lowerTail, logP);
  }
  
  public static double qwilcox(double p, double m, double n, boolean lowerTail, boolean logP)  {
    return Wilcox.qwilcox(p, m, n, lowerTail, logP);
  }
  
}
