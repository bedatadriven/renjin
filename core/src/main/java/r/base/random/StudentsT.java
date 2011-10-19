package r.base.random;

import r.base.Distributions;
import r.lang.DoubleVector;
import r.util.CDefines;

public class StudentsT {

  /*
   * if df is infinite then a normal random should be returned.
   * I don't know the way of getting whether df is infinite or not
   * so i am leaving it as is. Something needed:
   * 
   * if (df is infinite){
   * return(Normal.norm_rand());
   * }else{
   * return Normal.norm_rand() / Math.sqrt(ChiSquare.rchisq(df) / df);
   * }
   */
  public static double rt(double df) {
    if (Double.isNaN(df) || df <= 0.0) {
      return (Double.NaN);
    }

    return Normal.norm_rand() / Math.sqrt(ChiSquare.rchisq(df) / df);

  }

  public static double pnt(double t, double df, double ncp, boolean lower_tail, boolean log_p) {
    double albeta, a, b, del, errbd, lambda, rxb, tt, x;
    double geven, godd, p, q, s, tnc, xeven, xodd;
    int it;
    boolean negdel;

    /* note - itrmax and errmax may be changed to suit one's needs. */

    final int itrmax = 1000;
    final double errmax = 1.e-12;

    if (df <= 0.0) {
      return DoubleVector.NaN;
    }

    if (ncp == 0.0) {
      return Distributions.pt(t, df, lower_tail, log_p);
    }

    if (!DoubleVector.isFinite(t)) {
      return (t < 0) ? SignRank.R_DT_0(lower_tail, log_p) : SignRank.R_DT_1(lower_tail, log_p);
    }
    if (t >= 0.) {
      negdel = false;
      tt = t;
      del = ncp;
    } else {
      /* We deal quickly with left tail if extreme,
      since pt(q, df, ncp) <= pt(0, df, ncp) = \Phi(-ncp) */
      if (ncp > 40 && (!log_p || !lower_tail)) {
        return SignRank.R_DT_0(lower_tail, log_p);
      }
      negdel = true;
      tt = -t;
      del = -ncp;
    }

    if (df > 4e5 || del * del > 2 * Math.log(2.0) * (-(Double.MIN_EXPONENT))) {
      /*-- 2nd part: if del > 37.62, then p=0 below
      FIXME: test should depend on `df', `tt' AND `del' ! */
      /* Approx. from	 Abramowitz & Stegun 26.7.10 (p.949) */
      s = 1. / (4. * df);

      return Distributions.pnorm(tt * (1. - s), del, Math.sqrt(1. + tt * tt * 2. * s),
              lower_tail != negdel, log_p);
    }

    /* initialize twin series */
    /* Guenther, J. (1978). Statist. Computn. Simuln. vol.6, 199. */

    x = t * t;
    rxb = df / (x + df);/* := (1 - x) {x below} -- but more accurately */
    x = x / (x + df);/* in [0,1) */
    if (x > 0.) {/* <==>  t != 0 */
      lambda = del * del;
      p = .5 * Math.exp(-.5 * lambda);
      if (p == 0.) { /* underflow! */

        /*========== really use an other algorithm for this case !!! */
        //ML_ERROR(ME_UNDERFLOW, "pnt");
        //ML_ERROR(ME_RANGE, "pnt"); /* |ncp| too large */
        return SignRank.R_DT_0(lower_tail, log_p);
      }
      q = Math.sqrt(2.0 / Math.PI) * p * del;
      s = .5 - p;
      /* s = 0.5 - p = 0.5*(1 - exp(-.5 L)) =  -0.5*expm1(-.5 L)) */
      if (s < 1e-7) {
        s = -0.5 * Math.expm1(-0.5 * lambda);
      }
      a = .5;
      b = .5 * df;
      /* rxb = (1 - x) ^ b   [ ~= 1 - b*x for tiny x --> see 'xeven' below]
       *       where '(1 - x)' =: rxb {accurately!} above */
      rxb = Math.pow(rxb, b);
      albeta = Math.log(Math.sqrt(Math.PI)) + org.apache.commons.math.special.Gamma.logGamma(b) - org.apache.commons.math.special.Gamma.logGamma(.5 + b);
      xodd = Distributions.pbeta(x, a, b, /*lower*/ true, /*log_p*/ false);
      godd = 2. * rxb * Math.exp(a * Math.log(x) - albeta);
      tnc = b * x;
      xeven = (tnc < SignRank.DBL_EPSILON) ? tnc : 1. - rxb;
      geven = tnc * rxb;
      tnc = p * xodd + q * xeven;

      /* repeat until convergence or iteration limit */
      for (it = 1; it <= itrmax; it++) {
        a += 1.;
        xodd -= godd;
        xeven -= geven;
        godd *= x * (a + b - 1.) / a;
        geven *= x * (a + b - .5) / (a + .5);
        p *= lambda / (2 * it);
        q *= lambda / (2 * it + 1);
        tnc += p * xodd + q * xeven;
        s -= p;
        /* R 2.4.0 added test for rounding error here. */
        if (s < -1.e-10) { /* happens e.g. for (t,df,ncp)=(40,10,38.5), after 799 it.*/
          //ML_ERROR(ME_PRECISION, "pnt");
          //finis:
          tnc += Distributions.pnorm(-del, 0., 1., /*lower*/ true, /*log_p*/ false);
          lower_tail = lower_tail != negdel; /* xor */
          if (tnc > 1 - 1e-10 && lower_tail) {
            //ML_ERROR(ME_PRECISION, "pnt{final}");
          }
          return SignRank.R_DT_val(Math.min(tnc, 1.) /* Precaution */, lower_tail, log_p);
        }
        if (s <= 0 && it > 1) {
          //finis:
          tnc += Distributions.pnorm(-del, 0., 1., /*lower*/ true, /*log_p*/ false);
          lower_tail = lower_tail != negdel; /* xor */
          if (tnc > 1 - 1e-10 && lower_tail) {
            //ML_ERROR(ME_PRECISION, "pnt{final}");
          }
          return SignRank.R_DT_val(Math.min(tnc, 1.) /* Precaution */, lower_tail, log_p);
        }
        errbd = 2. * s * (xodd - godd);
        if (Math.abs(errbd) < errmax) {
          //finis:
          tnc += Distributions.pnorm(-del, 0., 1., /*lower*/ true, /*log_p*/ false);
          lower_tail = lower_tail != negdel; /* xor */
          if (tnc > 1 - 1e-10 && lower_tail) {
            //ML_ERROR(ME_PRECISION, "pnt{final}");
          }
          return SignRank.R_DT_val(Math.min(tnc, 1.) /* Precaution */, lower_tail, log_p);
        }/*convergence*/
      }
      /* non-convergence:*/
      //ML_ERROR(ME_NOCONV, "pnt");
    } else { /* x = t = 0 */
      tnc = 0.;
    }
    //finis:
    tnc += Distributions.pnorm(-del, 0., 1., /*lower*/ true, /*log_p*/ false);
    lower_tail = lower_tail != negdel; /* xor */
    if (tnc > 1 - 1e-10 && lower_tail) {
      //ML_ERROR(ME_PRECISION, "pnt{final}");
    }
    return SignRank.R_DT_val(Math.min(tnc, 1.) /* Precaution */, lower_tail, log_p);
  }
}
