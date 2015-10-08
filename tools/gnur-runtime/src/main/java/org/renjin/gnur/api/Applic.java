// Initial template generated from Applic.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

@SuppressWarnings("unused")
public final class Applic {

  private Applic() { }



  // void Rdqags (integr_fn f, void *ex, double *a, double *b, double *epsabs, double *epsrel, double *result, double *abserr, int *neval, int *ier, int *limit, int *lenw, int *last, int *iwork, double *work)

  // void Rdqagi (integr_fn f, void *ex, double *bound, int *inf, double *epsabs, double *epsrel, double *result, double *abserr, int *neval, int *ier, int *limit, int *lenw, int *last, int *iwork, double *work)

  // void vmmin (int n, double *b, double *Fmin, optimfn fn, optimgr gr, int maxit, int trace, int *mask, double abstol, double reltol, int nREPORT, void *ex, int *fncount, int *grcount, int *fail)

  // void nmmin (int n, double *Bvec, double *X, double *Fmin, optimfn fn, int *fail, double abstol, double intol, void *ex, double alpha, double bet, double gamm, int trace, int *fncount, int maxit)

  // void cgmin (int n, double *Bvec, double *X, double *Fmin, optimfn fn, optimgr gr, int *fail, double abstol, double intol, void *ex, int type, int trace, int *fncount, int *grcount, int maxit)

  // void lbfgsb (int n, int m, double *x, double *l, double *u, int *nbd, double *Fmin, optimfn fn, optimgr gr, int *fail, void *ex, double factr, double pgtol, int *fncount, int *grcount, int maxit, char *msg, int trace, int nREPORT)

  // void samin (int n, double *pb, double *yb, optimfn fn, int maxit, int tmax, double ti, int trace, void *ex)

  public static int findInterval(DoublePtr xt, int n, double x, boolean rightmost_closed, boolean all_inside, int ilo, IntPtr mflag) {
     throw new UnimplementedGnuApiMethod("findInterval");
  }

  public static void dqrqty_(DoublePtr x, IntPtr n, IntPtr k, DoublePtr qraux, DoublePtr y, IntPtr ny, DoublePtr qty) {
     throw new UnimplementedGnuApiMethod("dqrqty_");
  }

  public static void dqrqy_(DoublePtr x, IntPtr n, IntPtr k, DoublePtr qraux, DoublePtr y, IntPtr ny, DoublePtr qy) {
     throw new UnimplementedGnuApiMethod("dqrqy_");
  }

  public static void dqrcf_(DoublePtr x, IntPtr n, IntPtr k, DoublePtr qraux, DoublePtr y, IntPtr ny, DoublePtr b, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dqrcf_");
  }

  public static void dqrrsd_(DoublePtr x, IntPtr n, IntPtr k, DoublePtr qraux, DoublePtr y, IntPtr ny, DoublePtr rsd) {
     throw new UnimplementedGnuApiMethod("dqrrsd_");
  }

  public static void dqrxb_(DoublePtr x, IntPtr n, IntPtr k, DoublePtr qraux, DoublePtr y, IntPtr ny, DoublePtr xb) {
     throw new UnimplementedGnuApiMethod("dqrxb_");
  }

  public static double R_pretty(DoublePtr lo, DoublePtr up, IntPtr ndiv, int min_n, double shrink_sml, double high_u_fact, int eps_correction, int return_bounds) {
     throw new UnimplementedGnuApiMethod("R_pretty");
  }

  // void fdhess (int n, double *x, double fval, fcn_p fun, void *state, double *h, int nfd, double *step, double *f, int ndigit, double *typx)

  // void optif9 (int nr, int n, double *x, fcn_p fcn, fcn_p d1fcn, d2fcn_p d2fcn, void *state, double *typsiz, double fscale, int method, int iexp, int *msg, int ndigit, int itnlim, int iagflg, int iahflg, double dlt, double gradtl, double stepmx, double steptl, double *xpls, double *fpls, double *gpls, int *itrmcd, double *a, double *wrk, int *itncnt)

  public static void dqrdc2_(DoublePtr x, IntPtr ldx, IntPtr n, IntPtr p, DoublePtr tol, IntPtr rank, DoublePtr qraux, IntPtr pivot, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dqrdc2_");
  }

  public static void dqrls_(DoublePtr x, IntPtr n, IntPtr p, DoublePtr y, IntPtr ny, DoublePtr tol, DoublePtr b, DoublePtr rsd, DoublePtr qty, IntPtr k, IntPtr jpvt, DoublePtr qraux, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dqrls_");
  }
}
