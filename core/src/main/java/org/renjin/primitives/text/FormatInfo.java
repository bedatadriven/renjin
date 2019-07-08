package org.renjin.primitives.text;


import org.renjin.sexp.DoubleVector;

import java.math.BigDecimal;

/**
 * Determines the best format for a column of numbers.
 *
 * <p>Ported from the the format.info routine from GNU R, licensed under GPL v2.</p>
 */
public class FormatInfo {

  private static final int R_print_digits = 7;
  private static final int R_print_na_width = 2;
  private static final int R_print_scipen = 0;
  private static final int R_dec_min_exponent = (int) Math.floor(Math.log10(Double.MIN_VALUE));

  private final int nsmall = 0;

  private int width;
  private int d;
  private int e;
  private int neg_i;
  private int kpower;
  private int nsig;
  private boolean roundingwidens;

  private static final int KP_MAX = 22;

  private static final double[] POWERS_OF_TEN = {
      1e-1,
      1e00, 1e01, 1e02, 1e03, 1e04, 1e05, 1e06, 1e07, 1e08, 1e09,
      1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19,
      1e20, 1e21, 1e22};

    /*
       The return values are
         w : the required field width
         d : use %w.df in fixed format, %#w.de in scientific format
         e : use scientific format if != 0, value is number of exp digits - 1

       nsmall specifies the minimum number of decimal digits in fixed format:
       it is 0 except when called from do_format.
    */

  /**
   * Determines the best way to format a {@code ColumnView} of numbers.
   */
  public FormatInfo(DoubleVector x) {
    int n = x.length();
    int left, right, sleft;
    int mnl, mxl, rgt, mxsl, maxSignificantDigits;
    int neg;
    boolean naflag, nanflag, posinf, neginf;

    nanflag = false;
    naflag = false;
    posinf = false;
    neginf = false;
    neg = 0;
    rgt = mxl = mxsl = maxSignificantDigits = Integer.MIN_VALUE;
    mnl = Integer.MAX_VALUE;

    for (int i = 0; i < n; i++) {
      double xi = x.getElementAsDouble(i);
      if (!Double.isFinite(xi)) {
        if(DoubleVector.isNA(xi)) {
          naflag = true;
        } else if(Double.isNaN(xi)) {
          nanflag = true;
        } else if(xi > 0) {
          posinf = true;
        } else {
          neginf = true;
        }
      } else {
        scientific(xi);

        left = kpower + 1;
        if (roundingwidens) {
          left--;
        }

        sleft = neg_i + ((left <= 0) ? 1 : left); /* >= 1 */
        right = nsig - left; /* #{digits} right of '.' ( > 0 often)*/
        if (neg_i != 0) {
          neg = 1;	 /* if any < 0, need extra space for sign */
        }

        /* Infinite precision "F" Format : */
        if (right > rgt) {
          rgt = right;	/* max digits to right of . */
        }
        if (left > mxl) {
          mxl = left;	/* max digits to  left of . */
        }
        if (left < mnl) {
          mnl = left;	/* min digits to  left of . */
        }
        if (sleft> mxsl) {
          mxsl = sleft;	/* max left including sign(s)*/
        }
        if (nsig > maxSignificantDigits) {
          maxSignificantDigits = nsig;
        }
      }
    }
    /* F Format: use "F" format WHENEVER we use not more space than 'E'
     *		and still satisfy 'R_print.digits' {but as if nsmall==0 !}
     *
     * E Format has the form   [S]X[.XXX]E+XX[X]
     *
     * This is indicated by setting *e to non-zero (usually 1)
     * If the additional exponent digit is required *e is set to 2
     */

    /*-- These	'mxsl' & 'rgt'	are used in F Format
     *	 AND in the	____ if(.) "F" else "E" ___   below: */
    if (R_print_digits == 0) {
      rgt = 0;
    }
    if (mxl < 0) {
      mxsl = 1 + neg;  /* we use %#w.dg, so have leading zero */
    }

    /* use nsmall only *after* comparing "F" vs "E": */
    if (rgt < 0) {
      rgt = 0;
    }

    int fixedWidth = mxsl + rgt + (rgt != 0 ? 1 : 0);	/* width for F format */

    /*-- 'see' how "E" Exponential format would be like : */
    e = (mxl > 100 || mnl <= -99) ? 2 /* 3 digit exponent */ : 1;
    if (maxSignificantDigits != Integer.MIN_VALUE) {
      d = maxSignificantDigits - 1;
      width = neg + (d > 0 ? 1 : 0) + d + 4 + e; /* width for E format */
      if (fixedWidth <= width + R_print_scipen) { /* Fixpoint if it needs less space */
        e = 0;
        if (nsmall > rgt) {
          rgt = nsmall;
          fixedWidth = mxsl + rgt + (rgt != 0 ? 1 : 0);
        }
        d = rgt;
        width = fixedWidth;
      } /* else : "E" Exponential format -- all done above */
    }
    else { /* when all x[i] are non-finite */
      width = 0;/* to be increased */
      d = 0;
      e = 0;
    }
    if (naflag && width < R_print_na_width) {
      width = R_print_na_width;
    }
    if (nanflag && width < 3) {
      width = 3;
    }
    if (posinf && width < 3) {
      width = 3;
    }
    if (neginf && width < 4) {
      width = 4;
    }
  }


  private void scientific(double x) {
    /* for a number x , determine
     *	neg    = 1_{x < 0}  {0/1}
     *	kpower = Exponent of 10;
     *	nsig   = min(R_print.digits, #{significant digits of alpha})
     *  roundingwidens = TRUE iff rounding causes x to increase in width
     *
     * where  |x| = alpha * 10^kpower	and	 1 <= alpha < 10
     */
    double alpha;
    double r;
    int kp;
    int j;

    if (x == 0.0) {
      kpower = 0;
      nsig = 1;
      neg_i = 0;
      roundingwidens = false;
    } else {
      if(x < 0.0) {
        neg_i = 1;
        r = -x;
      } else {
        neg_i = 0;
        r = x;
      }

      kp = (int) Math.floor(Math.log10(r)) - R_print_digits + 1;/* r = |x|; 10^(kp + digits - 1) <= r */

      double r_prec = r;

      /* use exact scaling factor in double precision, if possible */
      if (Math.abs(kp) <= KP_MAX) {
        if (kp >= 0) {
          r_prec /= POWERS_OF_TEN[  kp+1];
        } else {
          r_prec *= POWERS_OF_TEN[ -kp+1];
        }
      }
        /* For IEC60559 1e-308 is not representable except by gradual underflow.
           Shifting by 303 allows for any potential denormalized numbers x,
           and makes the reasonable assumption that R_dec_min_exponent+303
           is in range. Representation of 1e+303 has low error.
         */
      else if (kp <= R_dec_min_exponent) {
        r_prec = (r_prec * 1e+303)/Rexp10((double)(kp+303));
      } else {
        r_prec = BigDecimal.valueOf(r_prec).divide(BigDecimal.TEN.pow(kp)).doubleValue();
      }

      if (r_prec < POWERS_OF_TEN[R_print_digits]) {
        r_prec *= 10.0;
        kp--;
      }
      /* round alpha to integer, 10^(digits-1) <= alpha <= 10^digits */
            /* accuracy limited by double rounding problem,
               alpha already rounded to 53 bits */
      alpha = Math.round(r_prec);
      nsig = R_print_digits;
      for (j = 1; j <= R_print_digits; j++) {
        alpha /= 10.0;
        if (alpha == Math.floor(alpha)) {
          nsig--;
        } else {
          break;
        }
      }
      if (nsig == 0 && R_print_digits > 0) {
        nsig = 1;
        kp += 1;
      }
      kpower = kp + R_print_digits - 1;

            /* Scientific format may do more rounding than fixed format, e.g.
               9996 with 3 digits is 1e+04 in scientific, but 9996 in fixed.
               This happens when the true value r is less than 10^(kpower+1)
               and would not round up to it in fixed format.
               Here rgt is the decimal place that will be cut off by rounding */

      int rgt = R_print_digits - kpower;
      /* bound rgt by 0 and KP_MAX */
      rgt = rgt < 0 ? 0 : rgt > KP_MAX ? KP_MAX : rgt;
      double fuzz = 0.5/(double) POWERS_OF_TEN[1 + rgt];
      // kpower can be bigger than the table.
      roundingwidens = kpower > 0 && kpower <= KP_MAX && r < POWERS_OF_TEN[kpower + 1] - fuzz;
    }
  }

  private double Rexp10(double v) {
    return Math.pow(v, 10);
  }

  public int getWidth() {
    return width;
  }

  public int getFractionDigits() {
    return d;
  }

  public int getExponentDigits() {
    return e;
  }

  public boolean useScientificFormat() {
    return e != 0;
  }

}
