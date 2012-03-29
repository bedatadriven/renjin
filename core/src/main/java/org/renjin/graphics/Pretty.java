package org.renjin.graphics;

public class Pretty {
  
  private static final double rounding_eps = 1e-7;

//  
//  public static Interval GEPretty(Interval axis) {
//  /*      Set scale and ticks for linear scales.
//   *
//   *      Pre:        x1 == lo < up == x2      ;  ndiv >= 1
//   *      Post: x1 <= y1 := lo < up =: y2 <= x2;  ndiv >= 1
//   */
//      double unit, ns, nu;
//      double high_u_fact[] = new double[] { .8, 1.7 };
////  #ifdef DEBUG_PLOT
//      double x1,x2;
////  #endif
//
//      if(axis.getCount() <= 0)
//           throw new IllegalArgumentException(
//               String.format("invalid axis extents [GEPretty(.,.,n=%d)", axis.getCount()));
//      if(axis.isInfinite()) {
//          throw new IllegalArgumentException(
//              String.format("infinite axis extents [GEPretty(%g,%g,%d)]", axis.getMin(), axis.getMax(), axis.getCount()));
//      }
//
//      ns = axis.getMin(); nu = axis.getMax();
// // #ifdef DEBUG_PLOT
//      x1 = ns; x2 = nu;
// // #endif
//      unit = R_pretty0(&ns, &nu, ndiv, /* min_n = */ 1,
//                       /* shrink_sml = */ 0.25,
//                       high_u_fact,
//                       2, /* do eps_correction in any case */
//                       0 /* return (ns,nu) in  (lo,up) */);
//      /* ==> ../appl/pretty.c */
//
//      /* The following is ugly since it kind of happens already in Rpretty0(..):
//       */
//      if(nu >= ns + 1) {
//          if(               ns * unit < *lo - rounding_eps*unit)
//              ns++;
//          if(nu > ns + 1 && nu * unit > *up + rounding_eps*unit)
//              nu--;
//          *ndiv = nu - ns;
//      }
//      *lo = ns * unit;
//      *up = nu * unit;
//  #ifdef non_working_ALTERNATIVE
//      if(ns * unit > *lo)
//          *lo = ns * unit;
//      if(nu * unit < *up)
//          *up = nu * unit;
//      if(nu - ns >= 1)
//          *ndiv = nu - ns;
//  #endif
//
//  #ifdef DEBUG_PLOT
//      if(*lo < x1)
//          warning(_(" .. GEPretty(.): new *lo = %g < %g = x1"), *lo, x1);
//      if(*up > x2)
//          warning(_(" .. GEPretty(.): new *up = %g > %g = x2"), *up, x2);
//  #endif
//  }
//
//  Interval GLPretty(Interval interval)
//  {
//  /* Generate pretty tick values --       LOGARITHMIC scale
//   * __ ul < uh __
//   * This only does a very simple setup.
//   * The real work happens when the axis is drawn. */
//      int p1, p2;
//      double dl = interval.getMin(), dh = interval.getMax();
//      p1 = (int)Math.ceil(Math.log10(dl));
//      p2 = (int)Math.floor(Math.log10(dh));
//      if(p2 <= p1 &&  dh/dl > 10.0) {
//          p1 = (int)ceil(Math.log10(dl) - 0.5);
//          p2 = (int)floor(Math.log10(dh) + 0.5);
//      }
//
//      if (p2 <= p1) { /* floor(log10(uh)) <= ceil(log10(ul))
//                           * <==>  log10(uh) - log10(ul) < 2
//                           * <==>         uh / ul        < 100 */
//          /* Very small range : Use tickmarks from a LINEAR scale
//           *                    Splus uses n = 9 here, but that is dumb */
//          GPretty(ul, uh, n);
//          *n = -*n;
//      }
//      else { /* extra tickmarks --> CreateAtVector() in ./plot.c */
//          /* round to nice "1e<N>" */
//          *ul = pow(10., (double)p1);
//          *uh = pow(10., (double)p2);
//          if (p2 - p1 <= LPR_SMALL)
//              *n = 3; /* Small range :    Use 1,2,5,10 times 10^k tickmarks */
//          else if (p2 - p1 <= LPR_MEDIUM)
//              *n = 2; /* Medium range :   Use 1,5 times 10^k tickmarks */
//          else
//              *n = 1; /* Large range :    Use 10^k tickmarks
//                       *                  But decimate, when there are too many*/
//      }
//  }
//
//  double R_pretty0(double *lo, double *up, int *ndiv, int min_n,
//      double shrink_sml, double high_u_fact[],
//      int eps_correction, int return_bounds)
//{
///* From version 0.65 on, we had rounding_eps := 1e-5, before, r..eps = 0
//* 1e-7 is consistent with seq.default() */
//#define rounding_eps 1e-7
//
//#define h  high_u_fact[0]
//#define h5 high_u_fact[1]
//
//double dx, cell, unit, base, U;
//double ns, nu;
//int k;
//Rboolean i_small;
//
//dx = *up - *lo;
///* cell := "scale"  here */
//if(dx == 0 && *up == 0) { /*  up == lo == 0  */
//cell = 1;
//i_small = TRUE;
//} else {
//cell = fmax2(fabs(*lo),fabs(*up));
///* U = upper bound on cell/unit */
//U = 1 + (h5 >= 1.5*h+.5) ? 1/(1+h) : 1.5/(1+h5);
///* added times 3, as several calculations here */
//i_small = dx < cell * U * imax2(1,*ndiv) * DBL_EPSILON *3;
//}
//
///*OLD: cell = FLT_EPSILON+ dx / *ndiv; FLT_EPSILON = 1.192e-07 */
//if(i_small) {
//if(cell > 10)
//   cell = 9 + cell/10;
//cell *= shrink_sml;
//if(min_n > 1) cell /= min_n;
//} else {
//cell = dx;
//if(*ndiv > 1) cell /= *ndiv;
//}
//
//if(cell < 20*DBL_MIN) {
//warning(_("Internal(pretty()): very small range.. corrected"));
//cell = 20*DBL_MIN;
//} else if(cell * 10 > DBL_MAX) {
//warning(_("Internal(pretty()): very large range.. corrected"));
//cell = .1*DBL_MAX;
//}
//base = pow(10., floor(log10(cell))); /* base <= cell < 10*base */
//
///* unit : from { 1,2,5,10 } * base
//*   such that |u - cell| is small,
//* favoring larger (if h > 1, else smaller)  u  values;
//* favor '5' more than '2'  if h5 > h  (default h5 = .5 + 1.5 h) */
//unit = base;
//if((U = 2*base)-cell <  h*(cell-unit)) { unit = U;
//if((U = 5*base)-cell < h5*(cell-unit)) { unit = U;
//if((U =10*base)-cell <  h*(cell-unit)) unit = U; }}
///* Result: c := cell,  u := unit,  b := base
//*  c in [  1,            (2+ h) /(1+h) ] b ==> u=  b
//*  c in ( (2+ h)/(1+h),  (5+2h5)/(1+h5)] b ==> u= 2b
//*  c in ( (5+2h)/(1+h), (10+5h) /(1+h) ] b ==> u= 5b
//*  c in ((10+5h)/(1+h),             10 ) b ==> u=10b
//*
//*  ===>    2/5 *(2+h)/(1+h)  <=  c/u  <=  (2+h)/(1+h)      */
//
//ns = floor(*lo/unit+rounding_eps);
//nu = ceil (*up/unit-rounding_eps);
//#ifdef DEBUGpr
//REprintf("pretty(lo=%g,up=%g,ndiv=%d,min_n=%d,shrink=%g,high_u=(%g,%g),"
//    "eps=%d)\n\t dx=%g; is.small:%d. ==> cell=%g; unit=%g\n",
//    *lo, *up, *ndiv, min_n, shrink_sml, h, h5,
//     eps_correction,   dx, (int)i_small, cell, unit);
//#endif
//if(eps_correction && (eps_correction > 1 || !i_small)) {
//if(*lo) *lo *= (1- DBL_EPSILON); else *lo = -DBL_MIN;
//if(*up) *up *= (1+ DBL_EPSILON); else *up = +DBL_MIN;
//}
//
//#ifdef DEBUGpr
//if(ns*unit > *lo)
//REprintf("\t ns= %.0f -- while(ns*unit > *lo) ns--;\n", ns);
//#endif
//while(ns*unit > *lo + rounding_eps*unit) ns--;
//
//#ifdef DEBUGpr
//if(nu*unit < *up)
//REprintf("\t nu= %.0f -- while(nu*unit < *up) nu++;\n", nu);
//#endif
//while(nu*unit < *up - rounding_eps*unit) nu++;
//
//k = .5 + nu - ns;
//if(k < min_n) {
///* ensure that  nu - ns  == min_n */
//#ifdef DEBUGpr
//REprintf("\tnu-ns=%.0f-%.0f=%d SMALL -> ensure nu-ns= min_n=%d\n",
//        nu,ns, k, min_n);
//#endif
//k = min_n - k;
//if(ns >= 0.) {
//   nu += k/2;
//   ns -= k/2 + k%2;/* ==> nu-ns = old(nu-ns) + min_n -k = min_n */
//} else {
//   ns -= k/2;
//   nu += k/2 + k%2;
//}
//*ndiv = min_n;
//}
//else {
//*ndiv = k;
//}
//if(return_bounds) { /* if()'s to ensure that result covers original range */
//if(ns * unit < *lo) *lo = ns * unit;
//if(nu * unit > *up) *up = nu * unit;
//} else {
//*lo = ns;
//*up = nu;
//}
//#ifdef DEBUGpr
//REprintf("\t ns=%.0f ==> lo=%g\n", ns, *lo);
//REprintf("\t nu=%.0f ==> up=%g  ==> ndiv = %d\n", nu, *up, *ndiv);
//#endif
//return unit;
//#undef h
//#undef h5
//}
//
//void R_pretty(double *lo, double *up, int *ndiv, int *min_n,
//     double *shrink_sml, double *high_u_fact, int *eps_correction)
//{
//R_pretty0(lo, up, ndiv,
//   *min_n, *shrink_sml, high_u_fact, *eps_correction, 1);
//}
  
}
