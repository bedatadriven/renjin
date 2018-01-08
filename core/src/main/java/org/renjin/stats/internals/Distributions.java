/*
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
package org.renjin.stats.internals;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Recycle;
import org.renjin.nmath.*;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;

public class Distributions {
  public static int toInt(boolean x) { return x ? 1 : 0; }

  @DataParallel @Internal
  public static double pbeta(double q, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pbeta.pbeta(q, shape1, shape2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qbeta(double p, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qbeta.qbeta(p, shape1, shape2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dbeta(double x, @Recycle double shape1, @Recycle double shape2, boolean log) {
    return org.renjin.nmath.dbeta.dbeta(x, shape1, shape2, toInt(log));
  }
  @DataParallel @Internal
  public static double pnbeta(double q, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnbeta.pnbeta(q, shape1, shape2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnbeta(double p, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnbeta.qnbeta(p, shape1, shape2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnbeta(double x, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnbeta.dnbeta(x, shape1, shape2, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pbinom(double q, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pbinom.pbinom(q, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qbinom(double p, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qbinom.qbinom(p, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dbinom(double x, @Recycle double size, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dbinom.dbinom(x, size, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double pnbinom(double q, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnbinom.pnbinom(q, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnbinom(double p, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnbinom.qnbinom(p, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnbinom(double x, @Recycle double size, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dnbinom.dnbinom(x, size, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double pnbinom_mu(double q, @Recycle double size, @Recycle double mu, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnbinom.pnbinom_mu(q, size, mu, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnbinom_mu(double p, @Recycle double size, @Recycle double mu, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnbinom.qnbinom_mu(p, size, mu, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnbinom_mu(double x, @Recycle double size, @Recycle double mu, boolean log) {
    return org.renjin.nmath.dnbinom.dnbinom_mu(x, size, mu, toInt(log));
  }
  @DataParallel @Internal
  public static double pcauchy(double q, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pcauchy.pcauchy(q, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qcauchy(double p, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qcauchy.qcauchy(p, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dcauchy(double x, @Recycle double location, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dcauchy.dcauchy(x, location, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pchisq(double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pchisq.pchisq(q, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qchisq(double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qchisq.qchisq(p, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dchisq(double x, @Recycle double df, boolean log) {
    return org.renjin.nmath.dchisq.dchisq(x, df, toInt(log));
  }
  @DataParallel @Internal
  public static double pnchisq(double q, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnchisq.pnchisq(q, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnchisq(double p, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnchisq.qnchisq(p, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnchisq(double x, @Recycle double df, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnchisq.dnchisq(x, df, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pexp(double q, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pexp.pexp(q, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qexp(double p, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qexp.qexp(p, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dexp(double x, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dexp.dexp(x, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pf(double q, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pf.pf(q, df1, df2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qf(double p, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qf.qf(p, df1, df2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double df(double x, @Recycle double df1, @Recycle double df2, boolean log) {
    return org.renjin.nmath.df.df(x, df1, df2, toInt(log));
  }
  @DataParallel @Internal
  public static double pnf(double q, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnf.pnf(q, df1, df2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnf(double p, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnf.qnf(p, df1, df2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnf(double x, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnf.dnf(x, df1, df2, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pgamma(double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pgamma.pgamma(q, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qgamma(double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qgamma.qgamma(p, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dgamma(double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dgamma.dgamma(x, shape, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pgeom(double q, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pgeom.pgeom(q, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qgeom(double p, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qgeom.qgeom(p, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dgeom(double x, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dgeom.dgeom(x, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double phyper(double q, @Recycle double m, @Recycle double n, @Recycle double k, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.phyper.phyper(q, m, n, k, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qhyper(double p, @Recycle double m, @Recycle double n, @Recycle double k, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qhyper.qhyper(p, m, n, k, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dhyper(double x, @Recycle double m, @Recycle double n, @Recycle double k, boolean log) {
    return org.renjin.nmath.dhyper.dhyper(x, m, n, k, toInt(log));
  }
  @DataParallel @Internal
  public static double plnorm(double q, @Recycle double meanlog, @Recycle double sdlog, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.plnorm.plnorm(q, meanlog, sdlog, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qlnorm(double p, @Recycle double meanlog, @Recycle double sdlog, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qlnorm.qlnorm(p, meanlog, sdlog, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dlnorm(double x, @Recycle double meanlog, @Recycle double sdlog, boolean log) {
    return org.renjin.nmath.dlnorm.dlnorm(x, meanlog, sdlog, toInt(log));
  }
  @DataParallel @Internal
  public static double plogis(double q, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.plogis.plogis(q, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qlogis(double p, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qlogis.qlogis(p, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dlogis(double x, @Recycle double location, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dlogis.dlogis(x, location, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pnorm(double q, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnorm.pnorm5(q, mean, sd, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnorm(double p, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnorm.qnorm5(p, mean, sd, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnorm(double x, @Recycle double mean, @Recycle double sd, boolean log) {
    return org.renjin.nmath.dnorm.dnorm4(x, mean, sd, toInt(log));
  }
  @DataParallel @Internal
  public static double ppois(double q, @Recycle double lambda, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.ppois.ppois(q, lambda, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qpois(double p, @Recycle double lambda, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qpois.qpois(p, lambda, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dpois(double x, @Recycle double lambda, boolean log) {
    return org.renjin.nmath.dpois.dpois(x, lambda, toInt(log));
  }
  @DataParallel @Internal
  public static double psignrank(double q, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.signrank.psignrank(q, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qsignrank(double p, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.signrank.qsignrank(p, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dsignrank(double x, @Recycle double n, boolean log) {
    return org.renjin.nmath.signrank.dsignrank(x, n, toInt(log));
  }
  @DataParallel @Internal
  public static double pt(double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pt.pt(q, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qt(double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qt.qt(p, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dt(double x, @Recycle double df, boolean log) {
    return org.renjin.nmath.dt.dt(x, df, toInt(log));
  }
  @DataParallel @Internal
  public static double ptukey(double q, @Recycle double nranges, @Recycle double nmeans, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.ptukey.ptukey(q, nranges, nmeans, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qtukey(double p, @Recycle double nranges, @Recycle double nmeans, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qtukey.qtukey(p, nranges, nmeans, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double pnt(double q, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnt.pnt(q, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnt(double p, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnt.qnt(p, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnt(double x, @Recycle double df, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnt.dnt(x, df, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double punif(double q, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.punif.punif(q, min, max, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qunif(double p, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qunif.qunif(p, min, max, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dunif(double x, @Recycle double min, @Recycle double max, boolean log) {
    return org.renjin.nmath.dunif.dunif(x, min, max, toInt(log));
  }
  @DataParallel @Internal
  public static double pweibull(double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pweibull.pweibull(q, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qweibull(double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qweibull.qweibull(p, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dweibull(double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dweibull.dweibull(x, shape, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pwilcox(double q, @Recycle double m, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.wilcox.pwilcox(q, m, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qwilcox(double p, @Recycle double m, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.wilcox.qwilcox(p, m, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dwilcox(double x, @Recycle double m, @Recycle double n, boolean log) {
    return org.renjin.nmath.wilcox.dwilcox(x, m, n, toInt(log));
  }

  @Internal
  public static DoubleVector runif(@Current Context context, Vector nVector, AtomicVector min, AtomicVector max) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int minLength = min.length();
    int maxLength = min.length();
    if (minLength == 0 || maxLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runifMethod = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(runif.runif(runifMethod, min.getElementAsDouble(j), max.getElementAsDouble(k)));
      j++;
      k++;
      if (j == minLength) {
        j = 0;
      }
      if (k == maxLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnorm(@Current Context context, Vector nVector, AtomicVector mean, AtomicVector sd) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int meanLength  = mean.length();
    int sdLength = sd.length();
    if (meanLength == 0 || sdLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rnorm.rnorm(runif, mean.getElementAsDouble(j), sd.getElementAsDouble(k)));
      j++;
      k++;
      if (j == meanLength) {
        j = 0;
      }
      if (k == sdLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rgamma(@Current Context context, Vector nVector, AtomicVector shape, AtomicVector scale) {
    int n  = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int shapeLength = shape.length();
    int scaleLength = scale.length();
    if (shapeLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rgamma.rgamma(runif, shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shapeLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rchisq(@Current Context context, Vector nVector, AtomicVector df) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int dfLength = df.length();
    boolean hasNA = df.containsNA();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;

    if (hasNA) {
      for (int i = 0; i < n; i++) {
        double dfElement = df.getElementAsDouble(j);
        if (DoubleVector.isNA(dfElement)) {
          vb.add(DoubleVector.NaN);
        } else {
          vb.add(rchisq.rchisq(runif, df.getElementAsDouble(j)));
        }
        j++;
        if (j == dfLength) {
          j = 0;
        }
      }
    } else {
      for (int i = 0; i < n; i++) {
        vb.add(rchisq.rchisq(runif, df.getElementAsDouble(j)));
        j++;
        if (j == dfLength) {
          j = 0;
        }
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnchisq(@Current Context context, Vector nVector, AtomicVector df, double ncp) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int dfLength = df.length();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rnchisq.rnchisq(runif, df.getElementAsDouble(j), ncp));
      j++;
      if (j == dfLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rexp(@Current Context context, Vector nVector, AtomicVector invrate) {
    int n = defineSize(nVector);
    if (n == 0) {
      // replace this with error!
      return DoubleVector.EMPTY;
    }
    int invrateLength = invrate.length();
    if (invrateLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rexp.rexp(runif, invrate.getElementAsDouble(j)));
      j++;
      if (j == invrateLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static IntVector rpois(@Current Context context, Vector nVector, AtomicVector mu) {
    int n = defineSize(nVector);
    if (n == 0) {
      return IntVector.EMPTY;
    }
    int muLength = mu.length();
    if (muLength == 0) {
      return (IntArrayVector.Builder.withInitialSize(n).build());
    }
    IntArrayVector.Builder vb = IntArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rpois.rpois(runif, mu.getElementAsDouble(j)));
      j++;
      if (j == muLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rsignrank(@Current Context context, Vector nnVector, AtomicVector n) {
    int nn = defineSize(nnVector);
    if (nn == 0) {
      return DoubleVector.EMPTY;
    }
    int nLength = n.length();
    if (nLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(signrank.rsignrank(runif, n.getElementAsDouble(j)));
      j++;
      if (j == nLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rwilcox(@Current Context context, Vector nnVector, AtomicVector m, AtomicVector n) {
    int nn = defineSize(nnVector);
    if (nn == 0) {
      return DoubleVector.EMPTY;
    }
    int mLength = m.length();
    int nLength = n.length();
    if (mLength == 0 || nLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(wilcox.rwilcox(runif, m.getElementAsDouble(j), n.getElementAsDouble(k)));
      j++;
      k++;
      if (j == mLength) {
        j = 0;
      }
      if (k == nLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static IntVector rgeom(@Current Context context, Vector nVector, AtomicVector p) {
    int n = defineSize(nVector);
    if (n == 0) {
      return IntVector.EMPTY;
    }
    int pLength = p.length();
    if (pLength == 0) {
      return (IntArrayVector.Builder.withInitialSize(n).build());
    }
    IntArrayVector.Builder vb = IntArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rgeom.rgeom(runif, p.getElementAsDouble(j)));
      j++;
      if (j == pLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rt(@Current Context context, Vector nVector, AtomicVector df) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int dfLength = df.length();
    if (dfLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rt.rt(runif, df.getElementAsDouble(j)));
      j++;
      if (j == dfLength) {
        j = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rcauchy(@Current Context context, Vector nVector, AtomicVector location, AtomicVector scale) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int locationLength = location.length();
    int scaleLength = scale.length();
    if (locationLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rcauchy.rcauchy(runif, location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == locationLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlnorm(@Current Context context, Vector nVector, AtomicVector meanlog, AtomicVector sdlog) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int meanlogLenght = meanlog.length();
    int sdlogLength = sdlog.length();
    if (meanlogLenght == 0 || sdlogLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rlnorm.rlnorm(runif, meanlog.getElementAsDouble(j), sdlog.getElementAsDouble(k)));
      j++;
      k++;
      if (j == meanlogLenght) {
        j = 0;
      }
      if (k == sdlogLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rlogis(@Current Context context, Vector nVector, AtomicVector location, AtomicVector scale) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int locationLength = location.length();
    int scaleLength = scale.length();
    if (locationLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rlogis.rlogis(runif, location.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == locationLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rweibull(@Current Context context, Vector nVector, AtomicVector shape, AtomicVector scale) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int shapeLength = shape.length();
    int scaleLength = scale.length();
    if (shapeLength == 0 || scaleLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rweibull.rweibull(runif, shape.getElementAsDouble(j), scale.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shapeLength) {
        j = 0;
      }
      if (k == scaleLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static IntVector rnbinom(@Current Context context, Vector nVector, AtomicVector size, AtomicVector prob) {
    int n = defineSize(nVector);
    if (n == 0) {
      return IntVector.EMPTY;
    }
    int sizeLength = size.length();
    int probLength = prob.length();
    if (sizeLength == 0 || probLength == 0) {
      return (IntArrayVector.Builder.withInitialSize(n).build());
    }
    IntArrayVector.Builder vb = IntArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rnbinom.rnbinom(runif, size.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == probLength) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rnbinom_mu(@Current Context context, Vector nVector, AtomicVector size, AtomicVector mu) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int sizeLength = size.length();
    int muLenght = mu.length();
    if (sizeLength == 0 || muLenght == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rnbinom.rnbinom_mu(runif, size.getElementAsDouble(j), mu.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == muLenght) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static IntVector rbinom(@Current Context context, Vector nVector, AtomicVector size, AtomicVector prob) {
    int n = defineSize(nVector);
    if (n == 0) {
      return IntVector.EMPTY;
    }
    int sizeLength = size.length();
    int probLength = prob.length();
    if (sizeLength == 0 || probLength == 0) {
      return (IntArrayVector.Builder.withInitialSize(n).build());
    }
    IntArrayVector.Builder vb = IntArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rbinom.rbinom(runif, size.getElementAsDouble(j), prob.getElementAsDouble(k)));
      j++;
      k++;
      if (j == sizeLength) {
        j = 0;
      }
      if (k == probLength) {
        k = 0;
      }
    }
    return (vb.build());
  }


  @Internal
  public static DoubleVector rf(@Current Context context, Vector nVector, AtomicVector df1, AtomicVector df2) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int df1Length = df1.length();
    int df2Length = df2.length();
    if (df1Length == 0 || df2.length() == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rf.rf(runif, df1.getElementAsDouble(j), df2.getElementAsDouble(k)));
      j++;
      k++;
      if (j == df1Length) {
        j = 0;
      }
      if (k == df2Length) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rbeta(@Current Context context, Vector nVector, AtomicVector shape1, AtomicVector shape2) {
    int n = defineSize(nVector);
    if (n == 0) {
      return DoubleVector.EMPTY;
    }
    int shape1Length = shape1.length();
    int shape2Length = shape2.length();
    if (shape1Length == 0 || shape2Length == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(n).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(n);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      vb.add(rbeta.rbeta(runif, shape1.getElementAsDouble(j), shape2.getElementAsDouble(k)));
      j++;
      k++;
      if (j == shape1Length) {
        j = 0;
      }
      if (k == shape2Length) {
        k = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static DoubleVector rhyper(@Current Context context, Vector nnVector, AtomicVector m, AtomicVector n, AtomicVector k) {
    int nn = defineSize(nnVector);
    if (nn == 0) {
      return DoubleVector.EMPTY;
    }
    int mLength = m.length();
    int nLength = n.length();
    int kLength = k.length();
    if (mLength == 0 || nLength == 0 || kLength == 0) {
      return (DoubleArrayVector.Builder.withInitialSize(nn).build());
    }
    DoubleArrayVector.Builder vb = DoubleArrayVector.Builder.withInitialCapacity(nn);
    MethodHandle runif = context.getSession().getRngMethod();
    int j = 0, p = 0, q = 0;
    for (int i = 0; i < nn; i++) {
      vb.add(rhyper.rhyper(
              runif, m.getElementAsDouble(j), n.getElementAsDouble(p), k.getElementAsDouble(q)));
      j++;
      p++;
      q++;
      if (j == mLength) {
        j = 0;
      }
      if (p == nLength) {
        p = 0;
      }
      if (q == kLength) {
        q = 0;
      }
    }
    return (vb.build());
  }

  @Internal
  public static IntVector rmultinom(@Current Context context, int n, int size, AtomicVector probVector) {

    if (IntVector.isNA(size) || size < 0) {
      throw new EvalException("invalid second argument 'size'");
    }

    double[] prob = normalizeProbabilities(probVector);
    DoublePtr probPtr = new DoublePtr(prob);

    int k = prob.length;  // number of components or classes, = X-vector length

    MethodHandle runif = context.getSession().getRngMethod();

    int[] ans = new int[n * k];

    int i;
    int ik;
    for(i = ik = 0; i < n; i++, ik += k) {
      rmultinom.rmultinom(runif, size, probPtr, k, new IntPtr(ans, ik));
    }

    return IntArrayVector.unsafe(ans, AttributeMap.builder().setDim(prob.length, n).build());
  }

  private static double[] normalizeProbabilities(AtomicVector vector) {
    double p[] = vector.toDoubleArray();
    int n = p.length;
    double sum = 0.0;
    int npos = 0;

    for (int i = 0; i < n; i++) {
      if (!DoubleVector.isFinite(p[i])) {
        throw new EvalException("NA in probability vector");
      }
      if (p[i] < 0.0) {
        throw new EvalException("negative probability");
      }
      if (p[i] > 0.0) {
        npos++;
        sum += p[i];
      }
    }
    if (npos == 0) {
      throw new EvalException("no positive probabilities");
    }
    for (int i = 0; i < n; i++) {
      p[i] /= sum;
    }

    return p;
  }


  public static int defineSize(Vector input) {
    int inputLength = (input.length() == 1) ? input.getElementAsInt(0) : input.length();
    if (input.length() == 1 && (input.isElementNA(0) || input.isElementNaN(0))) {
      throw new EvalException("invalid arguments.");
    }
    return inputLength;
  }
}