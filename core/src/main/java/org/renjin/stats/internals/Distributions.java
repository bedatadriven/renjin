package org.renjin.stats.internals;

import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Recycle;
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
}