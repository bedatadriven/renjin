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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Beta;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.MathUtils;

import r.jvmi.annotations.Primitive;
import r.jvmi.annotations.Recycle;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Symbol;
import r.lang.Vector;

/**
 * Math functions not found in java.Math or apache commons math
 */
public class MathExt {

  private MathExt() {
  }

  @Recycle
  public static double gamma(double x) {
    return Math.exp(Gamma.logGamma(x));
  }

  @Recycle
  public static double log(double x, double base) {

    //Method cannot be called directly as R and Apache Commons Math argument order
    // are reversed
    return MathUtils.log(base, x);
  }

  @Recycle
  public static double log(double d) {
    return Math.log(d);
  }

  @Recycle
  public static double log2(double d) {
    return MathUtils.log(2, d);
  }

  @Recycle
  public static double abs(double x) {
    return Math.abs(x);
  }

  @Primitive("asinh")
  public static double asinh(double val) {
    return (Math.log(val + Math.sqrt(val * val + 1)));
  }

  @Primitive("acosh")
  public static double acosh(double val) {
    return (Math.log(val + Math.sqrt(val + 1) * Math.sqrt(val - 1)));
  }

  @Primitive("atanh")
  public static double atanh(double val) {
    return (0.5 * Math.log((1 + val) / (1 - val)));
  }

  @Primitive("atan2")
  public static double atan2(double y, double x) {
    return (Math.atan2(y, x));
  }

  @Primitive("signif")
  public static double signif(@Recycle double x, @Recycle int digits) {
    return new BigDecimal(x).round(new MathContext(digits, RoundingMode.HALF_UP)).doubleValue();
  }

  @Primitive("expm1")
  public static double expm1(@Recycle double x) {
    return (Math.exp(x) - 1);
  }

  @Primitive("log1p")
  public static double log1p(@Recycle double x) {
    return (Math.log(1 + x));
  }

  @Primitive("beta")
  public static double beta(@Recycle double a, @Recycle double b) {
    return (Math.exp(Beta.logBeta(a, b)));
  }

  @Primitive("lbeta")
  public static double lbeta(@Recycle double a, @Recycle double b) {
    return (Beta.logBeta(a, b));
  }

  @Primitive("choose")
  public static double choose(@Recycle double n, @Recycle int k) {
    /*
     * Because gamma(a+1) = factorial(a)
     * we use gamma(n+1) /(gamma(n-k+1) * gamma(k+1)) instead of
     * Binomial(n,k) = n! / ((n-k)! * k!) for non-integer n values.
     * 
     */
    if (k < 0) {
      return (0);
    } else if (k == 0) {
      return (1);
    } else if ((int) n == n) {
      return (MathUtils.binomialCoefficientDouble((int) n, k));
    } else {
      return (MathExt.gamma(n + 1) / (MathExt.gamma(n - k + 1) * MathExt.gamma(k + 1)));
    }
  }

  @Primitive("lchoose")
  public static double lchoose(@Recycle double n, @Recycle int k) {
    return (Math.log(choose(n, k)));
  }
  
}
