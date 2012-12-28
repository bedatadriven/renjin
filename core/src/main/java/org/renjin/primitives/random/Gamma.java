/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.renjin.primitives.random;

import org.renjin.eval.Session;
import org.renjin.primitives.MathExt;


/**
 *
 * @author hako
 */
public class Gamma {

  final static double sqrt32 = 5.656854;
  final static double exp_m1 = 0.36787944117144232159;/* exp(-1) = 1/e */

  /* Coefficients q[k] - for q0 = sum(q[k]*a^(-k))
   * Coefficients a[k] - for q = q0+(t*t/2)*sum(a[k]*v^k)
   * Coefficients e[k] - for exp(q)-1 = sum(e[k]*q^k)
   */
  final static double q1 = 0.04166669;
  final static double q2 = 0.02083148;
  final static double q3 = 0.00801191;
  final static double q4 = 0.00144121;
  final static double q5 = -7.388e-5;
  final static double q6 = 2.4511e-4;
  final static double q7 = 2.424e-4;
  final static double a1 = 0.3333333;
  final static double a2 = -0.250003;
  final static double a3 = 0.2000062;
  final static double a4 = -0.1662921;
  final static double a5 = 0.1423657;
  final static double a6 = -0.1367177;
  final static double a7 = 0.1233795;

  /* State variables [FIXME for threading!] :*/
  static double aa = 0.;
  static double aaa = 0.;
  static double s, s2, d;    /* no. 1 (step 1) */

  static double q0, b, si, c;/* no. 2 (step 4) */


  public static double rgamma(Session context, double a, double scale) {

    double e, p, q, r, t, u, v, w, x, ret_val;

    if (a < 0.0 || scale <= 0.0) {
      if (scale == 0.) {
        return 0.;
      }
      return (Double.NaN);
    }

    if (a < 1.) { /* GS algorithm for parameters a < 1 */
      if (a == 0) {
        return 0.;
      }
      e = 1.0 + exp_m1 * a;
      for (;;) {
        p = e * context.rng.unif_rand();
        if (p >= 1.0) {
          x = -Math.log((e - p) / a);
          if (Exponantial.exp_rand(context) >= (1.0 - a) * Math.log(x)) {
            break;
          }
        } else {
          x = Math.exp(Math.log(p) / a);
          if (Exponantial.exp_rand(context) >= x) {
            break;
          }
        }
      }
      return scale * x;
    }

    /* --- a >= 1 : GD algorithm --- */

    /* Step 1: Recalculations of s2, s, d if a has changed */
    if (a != aa) {
      aa = a;
      s2 = a - 0.5;
      s = Math.sqrt(s2);
      d = sqrt32 - s * 12.0;
    }
    /* Step 2: t = standard normal deviate,
    x = (s,1/2) -normal deviate. */

    /* immediate acceptance (i) */
    t = Normal.norm_rand(context);
    x = s + 0.5 * t;
    ret_val = x * x;
    if (t >= 0.0) {
      return scale * ret_val;
    }

    /* Step 3: u = 0,1 - uniform sample. squeeze acceptance (s) */
    u = context.rng.unif_rand();
    if (d * u <= t * t * t) {
      return scale * ret_val;
    }

    /* Step 4: recalculations of q0, b, si, c if necessary */

    if (a != aaa) {
      aaa = a;
      r = 1.0 / a;
      q0 = ((((((q7 * r + q6) * r + q5) * r + q4) * r + q3) * r
              + q2) * r + q1) * r;

      /* Approximation depending on size of parameter a */
      /* The constants in the expressions for b, si and c */
      /* were established by numerical experiments */

      if (a <= 3.686) {
        b = 0.463 + s + 0.178 * s2;
        si = 1.235;
        c = 0.195 / s - 0.079 + 0.16 * s;
      } else if (a <= 13.022) {
        b = 1.654 + 0.0076 * s2;
        si = 1.68 / s + 0.275;
        c = 0.062 / s + 0.024;
      } else {
        b = 1.77;
        si = 0.75;
        c = 0.1515 / s;
      }
    }
    /* Step 5: no quotient test if x not positive */

    if (x > 0.0) {
      /* Step 6: calculation of v and quotient q */
      v = t / (s + s);
      if (Math.abs(v) <= 0.25) {
        q = q0 + 0.5 * t * t * ((((((a7 * v + a6) * v + a5) * v + a4) * v
                + a3) * v + a2) * v + a1) * v;
      } else {
        q = q0 - s * t + 0.25 * t * t + (s2 + s2) * Math.log(1.0 + v);
      }


      /* Step 7: quotient acceptance (q) */
      if (Math.log(1.0 - u) <= q) {
        return scale * ret_val;
      }
    }

    for (;;) {
      /* Step 8: e = standard exponential deviate
       *	u =  0,1 -uniform deviate
       *	t = (b,si)-double exponential (laplace) sample */
      e = Exponantial.exp_rand(context);
      u = context.rng.unif_rand();
      u = u + u - 1.0;
      if (u < 0.0) {
        t = b - si * e;
      } else {
        t = b + si * e;
      }
      /* Step	 9:  rejection if t < tau(1) = -0.71874483771719 */
      if (t >= -0.71874483771719) {
        /* Step 10:	 calculation of v and quotient q */
        v = t / (s + s);
        if (Math.abs(v) <= 0.25) {
          q = q0 + 0.5 * t * t
                  * ((((((a7 * v + a6) * v + a5) * v + a4) * v + a3) * v
                  + a2) * v + a1) * v;
        } else {
          q = q0 - s * t + 0.25 * t * t + (s2 + s2) * Math.log(1.0 + v);
        }
        /* Step 11:	 hat acceptance (h) */
        /* (if q not positive go to step 8) */
        if (q > 0.0) {
          w = MathExt.expm1(q);
          /*  ^^^^^ original code had approximation with rel.err < 2e-7 */
          /* if t is rejected sample again at step 8 */
          if (c * Math.abs(u) <= w * Math.exp(e - 0.5 * t * t)) {
            break;
          }
        }
      }
    } /* repeat .. until  `t' is accepted */
    x = s + 0.5 * t;
    return scale * x * x;
  }
}
