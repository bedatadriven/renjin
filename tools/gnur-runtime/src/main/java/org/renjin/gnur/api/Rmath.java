/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Rmath.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.nmath.*;
import org.renjin.stats.internals.Distributions;

@SuppressWarnings("unused")
public final class Rmath {

  private Rmath() { }


  public static double R_pow(double x, double y) {
    return mlutils.R_pow(x, y);
  }

  public static double R_pow_di(double x, int n) {
    return mlutils.R_pow_di(x, n);
  }


  public static double norm_rand() {
    throw new UnimplementedGnuApiMethod("norm_rand");
  }

  @Deprecated
  public static double unif_rand() {
    return Random.unif_rand();
  }

  public static double exp_rand() {
    throw new UnimplementedGnuApiMethod("exp_rand");
  }

  public static double Rf_dnorm4(double x, double mean, double sd, int log) {
    return Distributions.dnorm(x, mean, sd, log != 0);
  }

  public static double Rf_pnorm5(double q, double mean, double sd, int lowerTail, int logP) {
    return Distributions.pnorm(q, mean, sd, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnorm5(double q, double mean, double sd, int lowerTail, int logP) {
    return Distributions.qnorm(q, mean, sd, lowerTail != 0, logP != 0);
  }

  public static double Rf_rnorm(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rnorm");
  }

  public static void Rf_pnorm_both(double p0, DoublePtr p1, DoublePtr p2, int p3, int p4) {
    throw new UnimplementedGnuApiMethod("Rf_pnorm_both");
  }

  public static double Rf_dunif(double x, double min, double max, int log) {
    return Distributions.dunif(x, min, max, log != 0);
  }

  public static double Rf_punif(double q, double min, double max, int lowerTail, int logP) {
    return Distributions.punif(q, min, max, lowerTail != 0, logP != 0);
  }

  public static double Rf_qunif(double p, double min, double max, int lowerTail, int logP) {
    return Distributions.qunif(p, min, max, lowerTail != 0, logP != 0);
  }

  public static double Rf_runif(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_runif");
  }

  public static double Rf_dgamma(double x, double shape, double scale, int log) {
    return Distributions.dgamma(x, shape, scale, log != 0);
  }

  public static double Rf_pgamma(double q, double shape, double scale, int lowerTail, int logP) {
    return Distributions.pgamma(q, shape, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_qgamma(double p, double shape, double scale, int lowerTail, int logP) {
    return Distributions.qgamma(p, shape, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_rgamma(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rgamma");
  }

  public static double Rf_log1pmx(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_log1pmx");
  }

  public static double log1pexp(double p0) {
    throw new UnimplementedGnuApiMethod("log1pexp");
  }

  public static double Rf_lgamma1p(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_lgamma1p");
  }

  public static double Rf_logspace_add(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_logspace_add");
  }

  public static double Rf_logspace_sub(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_logspace_sub");
  }

  public static double logspace_sum(DoublePtr p0, int p1) {
    throw new UnimplementedGnuApiMethod("logspace_sum");
  }

  public static double Rf_dbeta(double x, double shape1, double shape2, int log) {
    return Distributions.dbeta(x, shape1, shape2, log != 0);
  }

  public static double Rf_pbeta(double q, double shape1, double shape2, int lowerTail, int logP) {
    return Distributions.pbeta(q, shape1, shape2, lowerTail != 0, logP != 0);
  }

  public static double Rf_qbeta(double p, double shape1, double shape2, int lowerTail, int logP) {
    return Distributions.qbeta(p, shape1, shape2, lowerTail != 0, logP != 0);
  }

  public static double Rf_rbeta(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rbeta");
  }

  public static double Rf_dlnorm(double x, double meanlog, double sdlog, int log) {
    return Distributions.dlnorm(x, meanlog, sdlog, log != 0);
  }

  public static double Rf_plnorm(double q, double meanlog, double sdlog, int lowerTail, int logP) {
    return Distributions.plnorm(q, meanlog, sdlog, lowerTail != 0, logP != 0);
  }

  public static double Rf_qlnorm(double p, double meanlog, double sdlog, int lowerTail, int logP) {
    return Distributions.qlnorm(p, meanlog, sdlog,lowerTail != 0, logP != 0);
  }

  public static double Rf_rlnorm(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rlnorm");
  }

  public static double Rf_dchisq(double x, double df, int log) {
    return Distributions.dchisq(x, df, log != 0);
  }

  public static double Rf_pchisq(double q, double df, int lowerTail, int logP) {
    return Distributions.pchisq(q, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_qchisq(double p, double df, int lowerTail, int logP) {
    return Distributions.qchisq(p, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_rchisq(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_rchisq");
  }

  public static double Rf_dnchisq(double x, double df, double ncp, int log) {
    return Distributions.dnchisq(x, df, ncp, log != 0);
  }

  public static double Rf_pnchisq(double q, double df, double ncp, int lowerTail, int logP) {
    return Distributions.pnchisq(q, df, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnchisq(double p, double df, double ncp, int lowerTail, int logP) {
    return Distributions.qnchisq(p, df, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_rnchisq(double df, double ncp) {
    throw new UnimplementedGnuApiMethod("Rf_rnchisq");
  }

  public static double Rf_df(double x, double df1, double df2, int log) {
    return Distributions.df(x, df1, df2, log != 0);
  }

  public static double Rf_pf(double q, double df1, double df2, int lowerTail, int logP) {
    return Distributions.pf(q, df1, df2, lowerTail != 0, logP != 0);
  }

  public static double Rf_qf(double p, double df1, double df2, int lowerTail, int logP) {
    return Distributions.qf(p, df1, df2, lowerTail != 0, logP != 0);
  }

  public static double Rf_rf(double d1, double d2) {
    throw new UnimplementedGnuApiMethod("Rf_rf");
  }

  public static double Rf_dt(double x, double df, int log) {
    return Distributions.dt(x, df, log != 0);
  }

  public static double Rf_pt(double q, double df, int lowerTail, int logP) {
    return Distributions.pt(q, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_qt(double p, double df, int lowerTail, int logP) {
    return Distributions.qt(p, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_rt(double df) {
    throw new UnimplementedGnuApiMethod("Rf_rt");
  }

  public static double Rf_dbinom_raw(double x, double n, double p, double q, int give_log) {
    throw new UnimplementedGnuApiMethod("Rf_dbinom_raw");
  }

  public static double Rf_dbinom(double x, double size, double prob, int log) {
    return Distributions.dbinom(x, size, prob, log != 0);
  }

  public static double Rf_pbinom(double q, double size, double prob, int lowerTail, int logP) {
    return Distributions.pbinom(q, size, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_qbinom(double p, double size, double prob, int lowerTail, int logP) {
    return Distributions.qbinom(p, size, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_rbinom(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rbinom");
  }

  public static void Rf_rmultinom(int p0, DoublePtr p1, int p2, IntPtr p3) {
    throw new UnimplementedGnuApiMethod("Rf_rmultinom");
  }

  public static double Rf_dcauchy(double x, double location, double scale, int log) {
    return Distributions.dcauchy(x, location, scale, log != 0);
  }

  public static double Rf_pcauchy(double q, double location, double scale, int lowerTail, int logP) {
    return Distributions.pcauchy(q, location, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_qcauchy(double p, double location, double scale, int lowerTail, int logP) {
    return Distributions.qcauchy(p, location, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_rcauchy(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rcauchy");
  }

  public static double Rf_dexp(double x, double scale, int log) {
    return Distributions.dexp(x, scale, log != 0);
  }

  public static double Rf_pexp(double q, double scale, int lowerTail, int logP) {
    return Distributions.pexp(q, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_qexp(double p, double scale, int lowerTail, int logP) {
    return Distributions.qexp(p, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_rexp(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_rexp");
  }

  public static double Rf_dgeom(double x, double prob, int log) {
    return Distributions.dgeom(x, prob, log != 0);
  }

  public static double Rf_pgeom(double q, double prob, int lowerTail, int logP) {
    return Distributions.pgeom(q, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_qgeom(double p, double prob, int lowerTail, int logP) {
    return Distributions.qgeom(p, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_rgeom(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_rgeom");
  }

  public static double Rf_dhyper(double x, double m, double n, double k, int log) {
    return Distributions.dhyper(x, m, n, k, log != 0);
  }

  public static double Rf_phyper(double q, double m, double n, double k, int lowerTail, int logP) {
    return Distributions.phyper(q, m, n, k, lowerTail != 0, logP != 0);
  }

  public static double Rf_qhyper(double p, double m, double n, double k, int lowerTail, int logP) {
    return Distributions.qhyper(p, m, n, k, lowerTail != 0, logP != 0);
  }

  public static double Rf_rhyper(double p0, double p1, double p2) {
    throw new UnimplementedGnuApiMethod("Rf_rhyper");
  }

  public static double Rf_dnbinom(double x, double size, double prob, int log) {
    return Distributions.dnbinom(x, size, prob, log != 0);
  }

  public static double Rf_pnbinom(double q, double size, double prob, int lowerTail, int logP) {
    return Distributions.pnbinom(q, size, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnbinom(double p, double size, double prob, int lowerTail, int logP) {
    return Distributions.qnbinom(p, size, prob, lowerTail != 0, logP != 0);
  }

  public static double Rf_rnbinom(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rnbinom");
  }

  public static double Rf_dnbinom_mu(double x, double size, double mu, int log) {
    return Distributions.dnbinom_mu(x, size, mu, log != 0);
  }

  public static double Rf_pnbinom_mu(double q, double size, double mu, int lowerTail, int logP) {
    return Distributions.pnbinom_mu(q, size, mu, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnbinom_mu(double p, double size, double mu, int lowerTail, int logP) {
    return Distributions.qnbinom_mu(p, size, mu, lowerTail != 0, logP != 0);
  }

  public static double Rf_rnbinom_mu(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rnbinom_mu");
  }

  public static double Rf_dpois_raw(double x, double lambda, int log) {
    throw new UnimplementedGnuApiMethod("Rf_dpois_raw");
  }

  public static double Rf_dpois(double x, double lambda, int log) {
    return Distributions.dpois(x, lambda, log != 0);
  }

  public static double Rf_ppois(double q, double lambda, int lowerTail, int logP) {
    return Distributions.ppois(q, lambda, lowerTail != 0, logP != 0);
  }

  public static double Rf_qpois(double p, double lambda, int lowerTail, int logP) {
    return Distributions.qpois(p, lambda, lowerTail != 0, logP != 0);
  }

  public static double Rf_rpois(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_rpois");
  }

  public static double Rf_dweibull(double x, double shape, double scale, int log) {
    return Distributions.dweibull(x, shape, scale, log != 0);
  }

  public static double Rf_pweibull(double q, double shape, double scale, int lowerTail, int logP) {
    return Distributions.pweibull(q, shape, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_qweibull(double p, double shape, double scale, int lowerTail, int logP) {
    return Distributions.qweibull(p, shape, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_rweibull(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rweibull");
  }

  public static double Rf_dlogis(double x, double location, double scale, int log) {
    return Distributions.dlogis(x, location, scale, log != 0);
  }

  public static double Rf_plogis(double q, double location, double scale, int lowerTail, int logP) {
    return Distributions.plogis(q, location, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_qlogis(double p, double location, double scale, int lowerTail, int logP) {
    return Distributions.qlogis(p, location, scale, lowerTail != 0, logP != 0);
  }

  public static double Rf_rlogis(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rlogis");
  }

  public static double Rf_dnbeta(double x, double shape1, double shape2, double ncp, int log) {
    return Distributions.dnbeta(x, shape1, shape2, ncp, log != 0);
  }

  public static double Rf_pnbeta(double q, double shape1, double shape2, double ncp, int lowerTail, int logP) {
    return Distributions.pnbeta(q, shape1, shape2, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnbeta(double p, double shape1, double shape2, double ncp, int lowerTail, int logP) {
    return Distributions.qnbeta(p, shape1, shape2, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_rnbeta(double p0, double p1, double p2) {
    throw new UnimplementedGnuApiMethod("Rf_rnbeta");
  }

  public static double Rf_dnf(double x, double df1, double df2, double ncp, int log) {
    return Distributions.dnf(x, df1, df2, ncp, log != 0);
  }

  public static double Rf_pnf(double q, double df1, double df2, double ncp, int lowerTail, int logP) {
    return Distributions.pnf(q, df1, df2, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnf(double p, double df1, double df2, double ncp, int lowerTail, int logP) {
    return Distributions.qnf(p, df1, df2, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_dnt(double x, double df, double ncp, int log) {
    return Distributions.dnt(x, df, ncp, log != 0);
  }

  public static double Rf_pnt(double q, double df, double ncp, int lowerTail, int logP) {
    return Distributions.pnt(q, df, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_qnt(double p, double df, double ncp, int lowerTail, int logP) {
    return Distributions.qnt(p, df, ncp, lowerTail != 0, logP != 0);
  }

  public static double Rf_ptukey(double q, double nranges, double nmeans, double df, int lowerTail, int logP) {
    return Distributions.ptukey(q, nranges, nmeans, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_qtukey(double p, double nranges, double nmeans, double df, int lowerTail, int logP) {
    return Distributions.qtukey(p, nranges, nmeans, df, lowerTail != 0, logP != 0);
  }

  public static double Rf_dwilcox(double x, double m, double n, int log) {
    return Distributions.dwilcox(x, m, n, log != 0);
  }

  public static double Rf_pwilcox(double q, double m, double n, int lowerTail, int logP) {
    return Distributions.pwilcox(q, m, n, lowerTail != 0, logP != 0);
  }

  public static double Rf_qwilcox(double p, double m, double n, int lowerTail, int logP) {
    return Distributions.qwilcox(p, m, n, lowerTail != 0, logP != 0);
  }

  public static double Rf_rwilcox(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_rwilcox");
  }

  public static double Rf_dsignrank(double x, double n, int log) {
    return Distributions.dsignrank(x, n, log != 0);
  }

  public static double Rf_psignrank(double q, double n, int lowerTail, int logP) {
    return Distributions.psignrank(q, n, lowerTail != 0, logP != 0);
  }

  public static double Rf_qsignrank(double p, double n, int lowerTail, int logP) {
    return Distributions.qsignrank(p, n, lowerTail != 0, logP != 0);
  }

  public static double Rf_rsignrank(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_rsignrank");
  }

  public static double Rf_gammafn(double p0) {
    return gamma.gammafn(p0);
  }

  public static double Rf_lgammafn(double p0) {
    return lgamma.lgammafn(p0);
  }

  public static double Rf_lgammafn_sign(double p0, IntPtr p1) {
    return lgamma.lgammafn_sign(p0, p1);
  }

  public static void Rf_dpsifn(double p0, int p1, int p2, int p3, DoublePtr p4, IntPtr p5, IntPtr p6) {
    throw new UnimplementedGnuApiMethod("Rf_dpsifn");
  }

  public static double Rf_psigamma(double p0, double p1) {
    return polygamma.psigamma(p0, p1);
  }

  public static double Rf_digamma(double p0) {
    return polygamma.digamma(p0);
  }

  public static double Rf_trigamma(double p0) {
    return polygamma.trigamma(p0);
  }

  public static double Rf_tetragamma(double p0) {
    return polygamma.tetragamma(p0);
  }

  public static double Rf_pentagamma(double p0) {
    return polygamma.pentagamma(p0);
  }

  public static double Rf_beta(double p0, double p1) {
    return beta.beta(p0, p1);
  }

  public static double Rf_lbeta(double p0, double p1) {
    return lbeta.lbeta(p0, p1);
  }

  public static double Rf_choose(double p0, double p1) {
    return choose.choose(p0, p1);
  }

  public static double Rf_lchoose(double p0, double p1) {
    return choose.lchoose(p0, p1);
  }

  public static double Rf_bessel_i(double p0, double p1, double p2) {
    return bessel_i.bessel_i(p0, p1, p2);
  }

  public static double Rf_bessel_j(double p0, double p1) {
    return bessel_j.bessel_j(p0, p1);
  }

  public static double Rf_bessel_k(double p0, double p1, double p2) {
    return bessel_k.bessel_k(p0, p1, p2);
  }

  public static double Rf_bessel_y(double p0, double p1) {
    return bessel_y.bessel_y(p0, p1);
  }

  public static double Rf_bessel_i_ex(double p0, double p1, double p2, DoublePtr p3) {
    return bessel_i.bessel_i_ex(p0, p1, p2, p3);
  }

  public static double Rf_bessel_j_ex(double p0, double p1, DoublePtr p2) {
    return bessel_j.bessel_j_ex(p0, p1, p2);
  }

  public static double Rf_bessel_k_ex(double p0, double p1, double p2, DoublePtr p3) {
    return bessel_k.bessel_k_ex(p0, p1, p2, p3);
  }

  public static double Rf_bessel_y_ex(double p0, double p1, DoublePtr p2) {
    return bessel_y.bessel_y_ex(p0, p1, p2);
  }

  public static double Rf_pythag(double p0, double p1) {
    throw new UnimplementedGnuApiMethod("Rf_pythag");
  }

  public static int Rf_imax2(int x, int y) {
    return imax2.imax2(x, y);
  }

  public static int Rf_imin2(int x, int y) {
    return imin2.imin2(x, y);
  }

  /**
   * @return the maximum of {@code x} and {@code y}, or Nan if either x or y is NaN.
   */
  public static double Rf_fmax2(double x, double y) {
    return fmax2.fmax2(x, y);
  }

  /**
   * @return the minimum of {@code x} and {@code y}, or Nan if either x or y is NaN.
   */
  public static double Rf_fmin2(double x, double y) {
    return fmin2.fmin2(x, y);
  }

  public static double Rf_sign(double p0) {
    return sign.sign(p0);
  }

  public static double Rf_fprec(double p0, double p1) {
    return fprec.fprec(p0, p1);
  }

  public static double Rf_fround(double p0, double p1) {
    return fround.fround(p0, p1);
  }

  public static double Rf_fsign(double x, double y) {
    return fsign.fsign(x, y);
  }

  public static double Rf_ftrunc(double p0) {
    throw new UnimplementedGnuApiMethod("Rf_ftrunc");
  }

  public static double cospi(double p0) {
    return org.renjin.nmath.cospi.cospi(p0);
  }

  public static double sinpi(double p0) {
    return org.renjin.nmath.cospi.sinpi(p0);
  }

  public static double tanpi(double p0) {
    return org.renjin.nmath.cospi.tanpi(p0);
  }
}
