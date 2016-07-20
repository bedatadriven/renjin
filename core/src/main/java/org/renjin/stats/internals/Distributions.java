package org.renjin.stats.internals;

import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Recycle;
public class Distributions {
  public static int toInt(boolean x) { return x ? 1 : 0; }

  @DataParallel @Internal
  public static double pbeta(double q, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pbeta.Rf_pbeta(q, shape1, shape2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qbeta(double p, @Recycle double shape1, @Recycle double shape2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qbeta.Rf_qbeta(p, shape1, shape2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dbeta(double x, @Recycle double shape1, @Recycle double shape2, boolean log) {
    return org.renjin.nmath.dbeta.Rf_dbeta(x, shape1, shape2, toInt(log));
  }
  @DataParallel @Internal
  public static double pnbeta(double q, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnbeta.Rf_pnbeta(q, shape1, shape2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnbeta(double p, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnbeta.Rf_qnbeta(p, shape1, shape2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnbeta(double x, @Recycle double shape1, @Recycle double shape2, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnbeta.Rf_dnbeta(x, shape1, shape2, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pbinom(double q, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pbinom.Rf_pbinom(q, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qbinom(double p, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qbinom.Rf_qbinom(p, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dbinom(double x, @Recycle double size, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dbinom.Rf_dbinom(x, size, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double pnbinom(double q, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnbinom.Rf_pnbinom(q, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnbinom(double p, @Recycle double size, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnbinom.Rf_qnbinom(p, size, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnbinom(double x, @Recycle double size, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dnbinom.Rf_dnbinom(x, size, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double pcauchy(double q, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pcauchy.Rf_pcauchy(q, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qcauchy(double p, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qcauchy.Rf_qcauchy(p, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dcauchy(double x, @Recycle double location, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dcauchy.Rf_dcauchy(x, location, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pchisq(double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pchisq.Rf_pchisq(q, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qchisq(double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qchisq.Rf_qchisq(p, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dchisq(double x, @Recycle double df, boolean log) {
    return org.renjin.nmath.dchisq.Rf_dchisq(x, df, toInt(log));
  }
  @DataParallel @Internal
  public static double pnchisq(double q, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnchisq.Rf_pnchisq(q, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnchisq(double p, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnchisq.Rf_qnchisq(p, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnchisq(double x, @Recycle double df, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnchisq.Rf_dnchisq(x, df, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pexp(double q, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pexp.Rf_pexp(q, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qexp(double p, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qexp.Rf_qexp(p, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dexp(double x, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dexp.Rf_dexp(x, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pf(double q, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pf.Rf_pf(q, df1, df2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qf(double p, @Recycle double df1, @Recycle double df2, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qf.Rf_qf(p, df1, df2, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double df(double x, @Recycle double df1, @Recycle double df2, boolean log) {
    return org.renjin.nmath.df.Rf_df(x, df1, df2, toInt(log));
  }
  @DataParallel @Internal
  public static double pnf(double q, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnf.Rf_pnf(q, df1, df2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnf(double p, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnf.Rf_qnf(p, df1, df2, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnf(double x, @Recycle double df1, @Recycle double df2, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnf.Rf_dnf(x, df1, df2, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double pgamma(double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pgamma.Rf_pgamma(q, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qgamma(double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qgamma.Rf_qgamma(p, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dgamma(double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dgamma.Rf_dgamma(x, shape, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pgeom(double q, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pgeom.Rf_pgeom(q, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qgeom(double p, @Recycle double prob, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qgeom.Rf_qgeom(p, prob, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dgeom(double x, @Recycle double prob, boolean log) {
    return org.renjin.nmath.dgeom.Rf_dgeom(x, prob, toInt(log));
  }
  @DataParallel @Internal
  public static double phyper(double q, @Recycle double m, @Recycle double n, @Recycle double k, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.phyper.Rf_phyper(q, m, n, k, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qhyper(double p, @Recycle double m, @Recycle double n, @Recycle double k, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qhyper.Rf_qhyper(p, m, n, k, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dhyper(double x, @Recycle double m, @Recycle double n, @Recycle double k, boolean log) {
    return org.renjin.nmath.dhyper.Rf_dhyper(x, m, n, k, toInt(log));
  }
  @DataParallel @Internal
  public static double plnorm(double q, @Recycle double meanlog, @Recycle double sdlog, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.plnorm.Rf_plnorm(q, meanlog, sdlog, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qlnorm(double p, @Recycle double meanlog, @Recycle double sdlog, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qlnorm.Rf_qlnorm(p, meanlog, sdlog, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dlnorm(double x, @Recycle double meanlog, @Recycle double sdlog, boolean log) {
    return org.renjin.nmath.dlnorm.Rf_dlnorm(x, meanlog, sdlog, toInt(log));
  }
  @DataParallel @Internal
  public static double plogis(double q, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.plogis.Rf_plogis(q, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qlogis(double p, @Recycle double location, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qlogis.Rf_qlogis(p, location, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dlogis(double x, @Recycle double location, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dlogis.Rf_dlogis(x, location, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pnorm(double q, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnorm.Rf_pnorm5(q, mean, sd, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnorm(double p, @Recycle double mean, @Recycle double sd, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnorm.Rf_qnorm5(p, mean, sd, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnorm(double x, @Recycle double mean, @Recycle double sd, boolean log) {
    return org.renjin.nmath.dnorm.Rf_dnorm4(x, mean, sd, toInt(log));
  }
  @DataParallel @Internal
  public static double ppois(double q, @Recycle double lambda, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.ppois.Rf_ppois(q, lambda, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qpois(double p, @Recycle double lambda, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qpois.Rf_qpois(p, lambda, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dpois(double x, @Recycle double lambda, boolean log) {
    return org.renjin.nmath.dpois.Rf_dpois(x, lambda, toInt(log));
  }
  @DataParallel @Internal
  public static double psignrank(double q, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.signrank.Rf_psignrank(q, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qsignrank(double p, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.signrank.Rf_qsignrank(p, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dsignrank(double x, @Recycle double n, boolean log) {
    return org.renjin.nmath.signrank.Rf_dsignrank(x, n, toInt(log));
  }
  @DataParallel @Internal
  public static double pt(double q, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pt.Rf_pt(q, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qt(double p, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qt.Rf_qt(p, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dt(double x, @Recycle double df, boolean log) {
    return org.renjin.nmath.dt.Rf_dt(x, df, toInt(log));
  }
  @DataParallel @Internal
  public static double ptukey(double q, @Recycle double nranges, @Recycle double nmeans, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.ptukey.Rf_ptukey(q, nranges, nmeans, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qtukey(double p, @Recycle double nranges, @Recycle double nmeans, @Recycle double df, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qtukey.Rf_qtukey(p, nranges, nmeans, df, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double pnt(double q, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pnt.Rf_pnt(q, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qnt(double p, @Recycle double df, @Recycle double ncp, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qnt.Rf_qnt(p, df, ncp, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dnt(double x, @Recycle double df, @Recycle double ncp, boolean log) {
    return org.renjin.nmath.dnt.Rf_dnt(x, df, ncp, toInt(log));
  }
  @DataParallel @Internal
  public static double punif(double q, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.punif.Rf_punif(q, min, max, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qunif(double p, @Recycle double min, @Recycle double max, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qunif.Rf_qunif(p, min, max, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dunif(double x, @Recycle double min, @Recycle double max, boolean log) {
    return org.renjin.nmath.dunif.Rf_dunif(x, min, max, toInt(log));
  }
  @DataParallel @Internal
  public static double pweibull(double q, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.pweibull.Rf_pweibull(q, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qweibull(double p, @Recycle double shape, @Recycle double scale, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.qweibull.Rf_qweibull(p, shape, scale, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dweibull(double x, @Recycle double shape, @Recycle double scale, boolean log) {
    return org.renjin.nmath.dweibull.Rf_dweibull(x, shape, scale, toInt(log));
  }
  @DataParallel @Internal
  public static double pwilcox(double q, @Recycle double m, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.wilcox.Rf_pwilcox(q, m, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double qwilcox(double p, @Recycle double m, @Recycle double n, boolean lowerTail, boolean logP) {
    return org.renjin.nmath.wilcox.Rf_qwilcox(p, m, n, toInt(lowerTail), toInt(logP));
  }
  @DataParallel @Internal
  public static double dwilcox(double x, @Recycle double m, @Recycle double n, boolean log) {
    return org.renjin.nmath.wilcox.Rf_dwilcox(x, m, n, toInt(log));
  }
}