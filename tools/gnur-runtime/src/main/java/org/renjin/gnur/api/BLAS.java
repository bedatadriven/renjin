// Initial template generated from BLAS.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class BLAS {

  private BLAS() { }



  public static double dasum_(IntPtr n, DoublePtr dx, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dasum_");
  }

  public static void daxpy_(IntPtr n, DoublePtr alpha, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("daxpy_");
  }

  public static void dcopy_(IntPtr n, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dcopy_");
  }

  public static double ddot_(IntPtr n, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("ddot_");
  }

  public static double dnrm2_(IntPtr n, DoublePtr dx, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dnrm2_");
  }

  public static void drot_(IntPtr n, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy, DoublePtr c, DoublePtr s) {
     throw new UnimplementedGnuApiMethod("drot_");
  }

  public static void drotg_(DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr s) {
     throw new UnimplementedGnuApiMethod("drotg_");
  }

  public static void drotm_(IntPtr n, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy, DoublePtr dparam) {
     throw new UnimplementedGnuApiMethod("drotm_");
  }

  public static void drotmg_(DoublePtr dd1, DoublePtr dd2, DoublePtr dx1, DoublePtr dy1, DoublePtr param) {
     throw new UnimplementedGnuApiMethod("drotmg_");
  }

  public static void dscal_(IntPtr n, DoublePtr alpha, DoublePtr dx, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dscal_");
  }

  public static void dswap_(IntPtr n, DoublePtr dx, IntPtr incx, DoublePtr dy, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dswap_");
  }

  public static int idamax_(IntPtr n, DoublePtr dx, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("idamax_");
  }

  public static void dgbmv_(CharPtr trans, IntPtr m, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx, DoublePtr beta, DoublePtr y, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dgbmv_");
  }

  public static void dgemv_(CharPtr trans, IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx, DoublePtr beta, DoublePtr y, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dgemv_");
  }

  public static void dsbmv_(CharPtr uplo, IntPtr n, IntPtr k, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx, DoublePtr beta, DoublePtr y, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dsbmv_");
  }

  public static void dspmv_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr ap, DoublePtr x, IntPtr incx, DoublePtr beta, DoublePtr y, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dspmv_");
  }

  public static void dsymv_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx, DoublePtr beta, DoublePtr y, IntPtr incy) {
     throw new UnimplementedGnuApiMethod("dsymv_");
  }

  public static void dtbmv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtbmv_");
  }

  public static void dtpmv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, DoublePtr ap, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtpmv_");
  }

  public static void dtrmv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtrmv_");
  }

  public static void dtbsv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtbsv_");
  }

  public static void dtpsv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, DoublePtr ap, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtpsv_");
  }

  public static void dtrsv_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dtrsv_");
  }

  public static void dger_(IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr a, IntPtr lda) {
     throw new UnimplementedGnuApiMethod("dger_");
  }

  public static void dsyr_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr a, IntPtr lda) {
     throw new UnimplementedGnuApiMethod("dsyr_");
  }

  public static void dspr_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr ap) {
     throw new UnimplementedGnuApiMethod("dspr_");
  }

  public static void dsyr2_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr a, IntPtr lda) {
     throw new UnimplementedGnuApiMethod("dsyr2_");
  }

  public static void dspr2_(CharPtr uplo, IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr ap) {
     throw new UnimplementedGnuApiMethod("dspr2_");
  }

  public static void dgemm_(CharPtr transa, CharPtr transb, IntPtr m, IntPtr n, IntPtr k, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr beta, DoublePtr c, IntPtr ldc) {
     throw new UnimplementedGnuApiMethod("dgemm_");
  }

  public static void dtrsm_(CharPtr side, CharPtr uplo, CharPtr transa, CharPtr diag, IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dtrsm_");
  }

  public static void dtrmm_(CharPtr side, CharPtr uplo, CharPtr transa, CharPtr diag, IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dtrmm_");
  }

  public static void dsymm_(CharPtr side, CharPtr uplo, IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr beta, DoublePtr c, IntPtr ldc) {
     throw new UnimplementedGnuApiMethod("dsymm_");
  }

  public static void dsyrk_(CharPtr uplo, CharPtr trans, IntPtr n, IntPtr k, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr beta, DoublePtr c, IntPtr ldc) {
     throw new UnimplementedGnuApiMethod("dsyrk_");
  }

  public static void dsyr2k_(CharPtr uplo, CharPtr trans, IntPtr n, IntPtr k, DoublePtr alpha, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr beta, DoublePtr c, IntPtr ldc) {
     throw new UnimplementedGnuApiMethod("dsyr2k_");
  }

  public static double dcabs1_(DoublePtr z) {
     throw new UnimplementedGnuApiMethod("dcabs1_");
  }

  // double F77_NAME() dzasum (int *n, Rcomplex *zx, int *incx)

  // double F77_NAME() dznrm2 (int *n, Rcomplex *x, int *incx)

  // int F77_NAME() izamax (int *n, Rcomplex *zx, int *incx)

  // void F77_NAME() zaxpy (int *n, Rcomplex *za, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy)

  // void F77_NAME() zcopy (int *n, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy)

  // Rcomplex F77_NAME() zdotc (int *n, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy)

  // Rcomplex F77_NAME() zdotu (int *n, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy)

  // void F77_NAME() zdrot (int *n, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy, double *c, double *s)

  // void F77_NAME() zdscal (int *n, double *da, Rcomplex *zx, int *incx)

  // void F77_NAME() zgbmv (char *trans, int *m, int *n, int *kl, int *ku, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *x, int *incx, Rcomplex *beta, Rcomplex *y, int *incy)

  // void F77_NAME() zgemm (const char *transa, const char *transb, const int *m, const int *n, const int *k, const Rcomplex *alpha, const Rcomplex *a, const int *lda, const Rcomplex *b, const int *ldb, const Rcomplex *beta, Rcomplex *c, const int *ldc)

  // void F77_NAME() zgemv (char *trans, int *m, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *x, int *incx, Rcomplex *beta, Rcomplex *y, int *incy)

  // void F77_NAME() zgerc (int *m, int *n, Rcomplex *alpha, Rcomplex *x, int *incx, Rcomplex *y, int *incy, Rcomplex *a, int *lda)

  // void F77_NAME() zgeru (int *m, int *n, Rcomplex *alpha, Rcomplex *x, int *incx, Rcomplex *y, int *incy, Rcomplex *a, int *lda)

  // void F77_NAME() zhbmv (char *uplo, int *n, int *k, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *x, int *incx, Rcomplex *beta, Rcomplex *y, int *incy)

  // void F77_NAME() zhemm (char *side, char *uplo, int *m, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb, Rcomplex *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() zhemv (char *uplo, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *x, int *incx, Rcomplex *beta, Rcomplex *y, int *incy)

  // void F77_NAME() zher (char *uplo, int *n, double *alpha, Rcomplex *x, int *incx, Rcomplex *a, int *lda)

  // void F77_NAME() zher2 (char *uplo, int *n, Rcomplex *alpha, Rcomplex *x, int *incx, Rcomplex *y, int *incy, Rcomplex *a, int *lda)

  // void F77_NAME() zher2k (char *uplo, char *trans, int *n, int *k, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb, double *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() zherk (char *uplo, char *trans, int *n, int *k, double *alpha, Rcomplex *a, int *lda, double *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() zhpmv (char *uplo, int *n, Rcomplex *alpha, Rcomplex *ap, Rcomplex *x, int *incx, Rcomplex *beta, Rcomplex *y, int *incy)

  // void F77_NAME() zhpr (char *uplo, int *n, double *alpha, Rcomplex *x, int *incx, Rcomplex *ap)

  // void F77_NAME() zhpr2 (char *uplo, int *n, Rcomplex *alpha, Rcomplex *x, int *incx, Rcomplex *y, int *incy, Rcomplex *ap)

  // void F77_NAME() zrotg (Rcomplex *ca, Rcomplex *cb, double *c, Rcomplex *s)

  // void F77_NAME() zscal (int *n, Rcomplex *za, Rcomplex *zx, int *incx)

  // void F77_NAME() zswap (int *n, Rcomplex *zx, int *incx, Rcomplex *zy, int *incy)

  // void F77_NAME() zsymm (char *side, char *uplo, int *m, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb, Rcomplex *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() zsyr2k (char *uplo, char *trans, int *n, int *k, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb, Rcomplex *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() zsyrk (char *uplo, char *trans, int *n, int *k, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *beta, Rcomplex *c, int *ldc)

  // void F77_NAME() ztbmv (char *uplo, char *trans, char *diag, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *x, int *incx)

  // void F77_NAME() ztbsv (char *uplo, char *trans, char *diag, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *x, int *incx)

  // void F77_NAME() ztpmv (char *uplo, char *trans, char *diag, int *n, Rcomplex *ap, Rcomplex *x, int *incx)

  // void F77_NAME() ztpsv (char *uplo, char *trans, char *diag, int *n, Rcomplex *ap, Rcomplex *x, int *incx)

  // void F77_NAME() ztrmm (char *side, char *uplo, char *transa, char *diag, int *m, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb)

  // void F77_NAME() ztrmv (char *uplo, char *trans, char *diag, int *n, Rcomplex *a, int *lda, Rcomplex *x, int *incx)

  // void F77_NAME() ztrsm (char *side, char *uplo, char *transa, char *diag, int *m, int *n, Rcomplex *alpha, Rcomplex *a, int *lda, Rcomplex *b, int *ldb)

  // void F77_NAME() ztrsv (char *uplo, char *trans, char *diag, int *n, Rcomplex *a, int *lda, Rcomplex *x, int *incx)
}
