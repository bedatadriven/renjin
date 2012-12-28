package org.renjin.primitives.random;

import org.renjin.eval.Session;
import org.renjin.primitives.MathExt;
import org.renjin.sexp.DoubleVector;


public class Binom {

  public static double rbinom(Session context, double nin, double pp) {

    /* static */
    double c = 0, fm = 0, npq = 0, p1 = 0, p2 = 0, p3 = 0, p4 = 0, qn = 0;
    double xl = 0, xll = 0, xlr = 0, xm = 0, xr = 0;
    double psave = -1.0;
    int nsave = -1;
    int m = 0;
    /* end of static */

    double f, f1, f2, u, v, w, w2, x, x1, x2, z, z2;
    double p, q, np, g, r, al, alv, amaxp, ffm, ynorm;
    int i, ix, k, n;

    if (Double.isInfinite(nin)) {
      return Double.NaN;
    }
    r = Math.floor(nin + 0.5);
    if (r != nin) {
      return Double.NaN;
    }
    if (Double.isInfinite(pp) || r < 0 || pp < 0. || pp > 1.) {
      return Double.NaN;
    }

    if (r == 0 || pp == 0.) {
      return 0;
    }
    if (pp == 1.) {
      return r;
    }

    if (r >= Integer.MAX_VALUE) {
      return Distributions.qbinom(context.rng.unif_rand(), (int) r, pp, false, false);
    }
    /* else */
    n = (int) r;

    p = Math.min(pp, 1. - pp);
    q = 1. - p;
    np = n * p;
    r = p / q;
    g = r * (n + 1);

    /* Setup, perform only when parameters change [using static (globals): */

    /* FIXING: Want this thread safe
    -- use as little (thread globals) as possible
     */
    if (pp != psave || n != nsave) {
      psave = pp;
      nsave = n;
      if (np < 30.0) {
        /* inverse cdf logic for mean less than 30 */
        qn = Math.pow(q, (double) n);




        /*---------------------- np = n*p < 30 : ------------------------- */
        while (true) {
          ix = 0;
          f = qn;
          u = context.rng.unif_rand();
          while (true) {
            if (u < f) {
              if (psave > 0.5) {
                ix = n - ix;
              }
              return (double) ix;
            }
            if (ix > 110) {
              break;
            }
            u -= f;
            ix++;
            f *= (g / ix - r);
          }
        }



      } else {
        ffm = np + p;
        m = (int) ffm;
        fm = m;
        npq = np * q;
        p1 = (int) (2.195 * Math.sqrt(npq) - 4.6 * q) + 0.5;
        xm = fm + 0.5;
        xl = xm - p1;
        xr = xm + p1;
        c = 0.134 + 20.5 / (15.3 + fm);
        al = (ffm - xl) / (ffm - xl * p);
        xll = al * (1.0 + 0.5 * al);
        al = (xr - ffm) / (xr * q);
        xlr = al * (1.0 + 0.5 * al);
        p2 = p1 * (1.0 + c + c);
        p3 = p2 + c / xll;
        p4 = p3 + c / xlr;
      }
    } else if (n == nsave) {
      if (np < 30.0) {
      }



      /*---------------------- np = n*p < 30 : ------------------------- */
      while (true) {
        ix = 0;
        f = qn;
        u = context.rng.unif_rand();
        while (true) {
          if (u < f) {
            if (psave > 0.5) {
              ix = n - ix;
            }
            return (double) ix;
          }
          if (ix > 110) {
            break;
          }
          u -= f;
          ix++;
          f *= (g / ix - r);
        }
      }

    }

    /*-------------------------- np = n*p >= 30 : ------------------- */
    while (true) {
      u = context.rng.unif_rand() * p4;
      v = context.rng.unif_rand();
      /* triangular region */
      if (u <= p1) {
        ix = (int) (xm - p1 * v + u);

        if (psave > 0.5) {
          ix = n - ix;
        }
        return (double) ix;
      }
      /* parallelogram region */
      if (u <= p2) {
        x = xl + (u - p1) / c;
        v = v * c + 1.0 - Math.abs(xm - x) / p1;
        if (v > 1.0 || v <= 0.) {
          continue;
        }
        ix = (int) x;
      } else {
        if (u > p3) {	/* right tail */
          ix = (int) (xr - Math.log(v) / xlr);
          if (ix > n) {
            continue;
          }
          v = v * (u - p3) * xlr;
        } else {/* left tail */
          ix = (int) (xl + Math.log(v) / xll);
          if (ix < 0) {
            continue;
          }
          v = v * (u - p2) * xll;
        }
      }
      /* determine appropriate way to perform accept/reject test */
      k = Math.abs(ix - m);
      if (k <= 20 || k >= npq / 2 - 1) {
        /* explicit evaluation */
        f = 1.0;
        if (m < ix) {
          for (i = m + 1; i <= ix; i++) {
            f *= (g / i - r);
          }
        } else if (m != ix) {
          for (i = ix + 1; i <= m; i++) {
            f /= (g / i - r);
          }
        }
        if (v <= f) {
          if (psave > 0.5) {
            ix = n - ix;
          }
          return (double) ix;
        }


      } else {
        /* squeezing using upper and lower bounds on log(f(x)) */
        amaxp = (k / npq) * ((k * (k / 3. + 0.625) + 0.1666666666666) / npq + 0.5);
        ynorm = -k * k / (2.0 * npq);
        alv = Math.log(v);
        if (alv < ynorm - amaxp) {
          if (psave > 0.5) {
            ix = n - ix;
          }
          return (double) ix;
        }



        if (alv <= ynorm + amaxp) {
          /* stirling's formula to machine accuracy */
          /* for the final acceptance/rejection test */
          x1 = ix + 1;
          f1 = fm + 1.0;
          z = n + 1 - fm;
          w = n - ix + 1.0;
          z2 = z * z;
          x2 = x1 * x1;
          f2 = f1 * f1;
          w2 = w * w;
          if (alv <= xm * Math.log(f1 / x1) + (n - m + 0.5) * Math.log(z / w) + (ix - m) * Math.log(w * p / (x1 * q)) + (13860.0 - (462.0 - (132.0 - (99.0 - 140.0 / f2) / f2) / f2) / f2) / f1 / 166320.0 + (13860.0 - (462.0 - (132.0 - (99.0 - 140.0 / z2) / z2) / z2) / z2) / z / 166320.0 + (13860.0 - (462.0 - (132.0 - (99.0 - 140.0 / x2) / x2) / x2) / x2) / x1 / 166320.0 + (13860.0 - (462.0 - (132.0 - (99.0 - 140.0 / w2) / w2) / w2) / w2) / w / 166320.) {
          }

          if (psave > 0.5) {
            ix = n - ix;
          }
          return (double) ix;
        }
      }
    }

  }

  /*
   * bd0.c
   */
  public static double bd0(double x, double np) {
    double ej, s, s1, v;
    int j;

    if (!DoubleVector.isFinite(x) || !DoubleVector.isFinite(np) || np == 0.0) {
      return DoubleVector.NaN;
    }

    if (Math.abs(x - np) < 0.1 * (x + np)) {
      v = (x - np) / (x + np);
      s = (x - np) * v;/* s using v -- change by MM */
      ej = 2 * x * v;
      v = v * v;
      for (j = 1;; j++) { /* Taylor series */
        ej *= v;
        s1 = s + ej / ((j << 1) + 1);
        if (s1 == s) /* last term was effectively 0 */ {
          return (s1);
        }
        s = s1;
      }
    }
    /* else:  | x - np |  is not too small */
    return (x * Math.log(x / np) + np - x);
  }

  public static double stirlerr(double n) {

    final double S0 = 0.083333333333333333333;       /* 1/12 */
    final double S1 = 0.00277777777777777777778;     /* 1/360 */
    final double S2 = 0.00079365079365079365079365;  /* 1/1260 */
    final double S3 = 0.000595238095238095238095238; /* 1/1680 */
    final double S4 = 0.0008417508417508417508417508; /* 1/1188 */

    /*
     * error for 0, 0.5, 1.0, 1.5, ..., 14.5, 15.0.
     * length = 31
     */
    final double[] sferr_halves = new double[]{
      0.0, /* n=0 - wrong, place holder only */
      0.1534264097200273452913848, /* 0.5 */
      0.0810614667953272582196702, /* 1.0 */
      0.0548141210519176538961390, /* 1.5 */
      0.0413406959554092940938221, /* 2.0 */
      0.03316287351993628748511048, /* 2.5 */
      0.02767792568499833914878929, /* 3.0 */
      0.02374616365629749597132920, /* 3.5 */
      0.02079067210376509311152277, /* 4.0 */
      0.01848845053267318523077934, /* 4.5 */
      0.01664469118982119216319487, /* 5.0 */
      0.01513497322191737887351255, /* 5.5 */
      0.01387612882307074799874573, /* 6.0 */
      0.01281046524292022692424986, /* 6.5 */
      0.01189670994589177009505572, /* 7.0 */
      0.01110455975820691732662991, /* 7.5 */
      0.010411265261972096497478567, /* 8.0 */
      0.009799416126158803298389475, /* 8.5 */
      0.009255462182712732917728637, /* 9.0 */
      0.008768700134139385462952823, /* 9.5 */
      0.008330563433362871256469318, /* 10.0 */
      0.007934114564314020547248100, /* 10.5 */
      0.007573675487951840794972024, /* 11.0 */
      0.007244554301320383179543912, /* 11.5 */
      0.006942840107209529865664152, /* 12.0 */
      0.006665247032707682442354394, /* 12.5 */
      0.006408994188004207068439631, /* 13.0 */
      0.006171712263039457647532867, /* 13.5 */
      0.005951370112758847735624416, /* 14.0 */
      0.005746216513010115682023589, /* 14.5 */
      0.005554733551962801371038690 /* 15.0 */};
    double nn;

    if (n <= 15.0) {
      nn = n + n;
      if (nn == (int) nn) {
        return (sferr_halves[(int) nn]);
      }
      return (Math.log(MathExt.gamma(n + 1.)) - (n + 0.5) * Math.log(n) + n - Math.log(Math.sqrt(2.0 * Math.PI)));
    }

    nn = n * n;
    if (n > 500) {
      return ((S0 - S1 / nn) / n);
    }
    if (n > 80) {
      return ((S0 - (S1 - S2 / nn) / nn) / n);
    }
    if (n > 35) {
      return ((S0 - (S1 - (S2 - S3 / nn) / nn) / nn) / n);
    }
    /* 15 < n <= 35 : */
    return ((S0 - (S1 - (S2 - (S3 - S4 / nn) / nn) / nn) / nn) / n);
  }

  public static double dbinom_raw(double x, double n, double p, double q, boolean give_log) {
    double lf, lc;

    if (p == 0) {
      return ((x == 0) ? SignRank.R_D__1(true, give_log) : SignRank.R_D__0(true, give_log));
    }
    if (q == 0) {
      return ((x == n) ? SignRank.R_D__1(true, give_log) : SignRank.R_D__0(true, give_log));
    }

    if (x == 0) {
      if (n == 0) {
        return SignRank.R_D__1(true, give_log);
      }
      lc = (p < 0.1) ? -bd0(n, n * q) - n * p : n * Math.log(q);
      return (SignRank.R_D_exp(lc, true, give_log));
    }
    if (x == n) {
      lc = (q < 0.1) ? -bd0(n, n * p) - n * q : n * Math.log(p);
      return (SignRank.R_D_exp(lc, true, give_log));
    }
    if (x < 0 || x > n) {
      return (SignRank.R_D__0(true, give_log));
    }

    /* n*p or n*q can underflow to zero if n and p or q are small.  This
    used to occur in dbeta, and gives NaN as from R 2.3.0.  */
    lc = stirlerr(n) - stirlerr(x) - stirlerr(n - x) - bd0(x, n * p) - bd0(n - x, n * q);

    /* f = (M_2PI*x*(n-x))/n; could overflow or underflow */
    /* Upto R 2.7.1:
     * lf = log(M_2PI) + log(x) + log(n-x) - log(n);
     * -- following is much better for  x << n : */
    lf = Math.log(2.0 * Math.PI) + Math.log(x) + Math.log1p(-x / n);

    return SignRank.R_D_exp(lc - 0.5 * lf, true, give_log);
  }

  private static double do_search(double y, double[] z, double p, double n, double pr, double incr) {
    if (z[0] >= p) {
      /* search to the left */
      for (;;) {
        if (y == 0
                || (z[0] = Distributions.pnbinom(y - incr, (int) n, pr, /*l._t.*/ true, /*log_p*/ false)) < p) {
          return y;
        }
        y = Math.max(0, y - incr);
      }
    } else {		/* search to the right */

      for (;;) {
        y = y + incr;
        if ((z[0] = Distributions.pnbinom(y, (int) n, pr, /*l._t.*/ true, /*log_p*/ false)) >= p) {
          return y;
        }
      }
    }
  }

  public static double qnbinom(double p, double size, double prob, boolean lower_tail, boolean log_p) {
    double P, Q, mu, sigma, gamma, y;
    double[] z = new double[1];


    if (DoubleVector.isNaN(p) || DoubleVector.isNaN(size) || DoubleVector.isNaN(prob)) {
      return p + size + prob;
    }

    if (prob <= 0 || prob > 1 || size <= 0) {
      return DoubleVector.NaN;
    }

    /* FIXME: size = 0 is well defined ! */
    if (prob == 1) {
      return 0;
    }

    //R_Q_P01_boundaries(p, 0, ML_POSINF);
    //#define R_Q_P01_boundaries(p, _LEFT_, _RIGHT_)
    //This macro is defined in /src/nmath/dpq.h
    if (log_p) {
      if (p > 0) {
        return DoubleVector.NaN;
      }
      if (p == 0) {/* upper bound*/
        return lower_tail ? Double.POSITIVE_INFINITY : 0;
      }
      if (p == Double.NEGATIVE_INFINITY) {
        return lower_tail ? 0 : Double.POSITIVE_INFINITY;
      }
    } else { /* !log_p */
      if (p < 0 || p > 1) {
        return DoubleVector.NaN;
      }
      if (p == 0) {
        return lower_tail ? 0 : Double.POSITIVE_INFINITY;
      }
      if (p == 1) {
        return lower_tail ? Double.POSITIVE_INFINITY : 0;
      }
    }


    Q = 1.0 / prob;
    P = (1.0 - prob) * Q;
    mu = size * P;
    sigma = Math.sqrt(size * P * Q);
    gamma = (Q + P) / sigma;

    /* Note : "same" code in qpois.c, qbinom.c, qnbinom.c --
     * FIXME: This is far from optimal [cancellation for p ~= 1, etc]: */
    if (!lower_tail || log_p) {
      //p = R_DT_qIv(p); /* need check again (cancellation!): */
      p = Normal.R_DT_qIv(p, lower_tail ? 1.0 : 0.0, log_p ? 1.0 : 0.0);
      if (p == SignRank.R_DT_0(lower_tail, log_p)) {
        return 0;
      }
      if (p == SignRank.R_DT_1(lower_tail, log_p)) {
        return Double.POSITIVE_INFINITY;
      }
    }
    /* temporary hack --- FIXME --- */
    if (p + 1.01 * SignRank.DBL_EPSILON >= 1.) {
      return Double.POSITIVE_INFINITY;
    }

    /* y := approx.value (Cornish-Fisher expansion) :  */
    z[0] = Distributions.qnorm(p, 0., 1., /*lower_tail*/ true, /*log_p*/ false);
    y = Math.floor(mu + sigma * (z[0] + gamma * (z[0] * z[0] - 1) / 6) + 0.5);

    z[0] = Distributions.pnbinom(y, (int) size, prob, /*lower_tail*/ true, /*log_p*/ false);

    /* fuzz to ensure left continuity: */
    p *= 1 - 64 * SignRank.DBL_EPSILON;

    /* If the C-F value is not too large a simple search is OK */
    if (y < 1e5) {
      return do_search(y, z, p, size, prob, 1);
    }
    /* Otherwise be a bit cleverer in the search */
    {
      double incr = Math.floor(y * 0.001), oldincr;
      do {
        oldincr = incr;
        y = do_search(y, z, p, size, prob, incr);
        incr = Math.max(1, Math.floor(incr / 100));
      } while (oldincr > 1 && incr > y * 1e-15);
      return y;
    }
  }

  public static double dnbinom_mu(double x, double size, double mu, boolean give_log) {
    /* originally, just set  prob :=  size / (size + mu)  and called dbinom_raw(),
     * but that suffers from cancellation when   mu << size  */
    double ans, p;


    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(size) || DoubleVector.isNaN(mu)) {
      return x + size + mu;
    }

    if (mu < 0 || size < 0) {
      return DoubleVector.NaN;
    }

    //R_D_nonint_check(x);

    if (SignRank.R_D_nonint(x, true, give_log)) {
      //MATHLIB_WARNING("non-integer x = %f", x);	
      //How to warn??
      return SignRank.R_D__0(true, give_log);
    }

    if (x < 0 || !DoubleVector.isFinite(x)) {
      return SignRank.R_D__0(true, give_log);
    }
    x = SignRank.R_D_forceint(x);

    if (x == 0)/* be accerate, both for n << mu, and n >> mu :*/ {
      return SignRank.R_D_exp(size * (size < mu ? Math.log(size / (size + mu)) : Math.log1p(-mu / (size + mu))), true, give_log);
    }
    if (x < 1e-10 * size) { /* don't use dbinom_raw() but MM's formula: */
      /* FIXME --- 1e-8 shows problem; rather use algdiv() from ./toms708.c */
      return SignRank.R_D_exp(x * Math.log(size * mu / (size + mu)) - mu - org.apache.commons.math.special.Gamma.logGamma(x + 1)
              + Math.log1p(x * (x - 1) / (2 * size)), true, give_log);
    }
    /* else: no unnecessary cancellation inside dbinom_raw, when
     * x_ = size and n_ = x+size are so close that n_ - x_ loses accuracy
     */
    ans = dbinom_raw(size, x + size, size / (size + mu), mu / (size + mu), give_log);
    p = ((double) size) / (size + x);
    return ((give_log) ? Math.log(p) + ans : p * ans);
  }

  public static double qnbinom_mu(double p, double size, double mu, boolean lower_tail, boolean log_p) {
    /* FIXME!  Implement properly!! (not losing accuracy for very large size (prob ~= 1)*/
    return qnbinom(p, size, /* prob = */ size / (size + mu), lower_tail, log_p);
  }

  public static double pnbinom_mu(double x, double size, double mu, boolean lower_tail, boolean log_p) {

    if (DoubleVector.isNaN(x) || DoubleVector.isNaN(size) || DoubleVector.isNaN(mu)) {
      return x + size + mu;
    }
    if (!DoubleVector.isFinite(size) || !DoubleVector.isFinite(mu)) {
      return DoubleVector.NaN;
    }

    if (size <= 0 || mu < 0) {
      return DoubleVector.NaN;
    }

    if (x < 0) {
      SignRank.R_DT_0(lower_tail, log_p);
    }
    if (!DoubleVector.isFinite(x)) {
      return SignRank.R_DT_1(lower_tail, log_p);
    }
    x = Math.floor(x + 1e-7);
    /* return
     * pbeta(pr, size, x + 1, lower_tail, log_p);  pr = size/(size + mu), 1-pr = mu/(size+mu)
     *
     *= pbeta_raw(pr, size, x + 1, lower_tail, log_p)
     *            x.  pin   qin
     *=  bratio (pin,  qin, x., 1-x., &w, &wc, &ierr, log_p),  and return w or wc ..
     *=  bratio (size, x+1, pr, 1-pr, &w, &wc, &ierr, log_p) */
    {
      int[] ierr = new int[1];
      double[] w = new double[1];
      double[] wc = new double[1];
      Utils.bratio(size, x + 1, size / (size + mu), mu / (size + mu), w, wc, ierr, log_p);
      return lower_tail ? w[0] : wc[0];
    }
  }
}
