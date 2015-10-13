// Initial template generated from Lapack.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class Lapack {

  private Lapack() { }



  public static void ilaver_(IntPtr major, IntPtr minor, IntPtr patch) {
     throw new UnimplementedGnuApiMethod("ilaver_");
  }

  public static void dbdsqr_(CharPtr uplo, IntPtr n, IntPtr ncvt, IntPtr nru, IntPtr ncc, DoublePtr d, DoublePtr e, DoublePtr vt, IntPtr ldvt, DoublePtr u, IntPtr ldu, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dbdsqr_");
  }

  public static void ddisna_(CharPtr job, IntPtr m, IntPtr n, DoublePtr d, DoublePtr sep, IntPtr info) {
     throw new UnimplementedGnuApiMethod("ddisna_");
  }

  public static void dgbbrd_(CharPtr vect, IntPtr m, IntPtr n, IntPtr ncc, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, DoublePtr d, DoublePtr e, DoublePtr q, IntPtr ldq, DoublePtr pt, IntPtr ldpt, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbbrd_");
  }

  public static void dgbcon_(CharPtr norm, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, IntPtr ipiv, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbcon_");
  }

  public static void dgbequ_(IntPtr m, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, DoublePtr r, DoublePtr c, DoublePtr rowcnd, DoublePtr colcnd, DoublePtr amax, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbequ_");
  }

  public static void dgbrfs_(CharPtr trans, IntPtr n, IntPtr kl, IntPtr ku, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr afb, IntPtr ldafb, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbrfs_");
  }

  public static void dgbsv_(IntPtr n, IntPtr kl, IntPtr ku, IntPtr nrhs, DoublePtr ab, IntPtr ldab, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbsv_");
  }

  public static void dgbsvx_(IntPtr fact, CharPtr trans, IntPtr n, IntPtr kl, IntPtr ku, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr afb, IntPtr ldafb, IntPtr ipiv, CharPtr equed, DoublePtr r, DoublePtr c, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbsvx_");
  }

  public static void dgbtf2_(IntPtr m, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbtf2_");
  }

  public static void dgbtrf_(IntPtr m, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbtrf_");
  }

  public static void dgbtrs_(CharPtr trans, IntPtr n, IntPtr kl, IntPtr ku, IntPtr nrhs, DoublePtr ab, IntPtr ldab, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgbtrs_");
  }

  public static void dgebak_(CharPtr job, CharPtr side, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr scale, IntPtr m, DoublePtr v, IntPtr ldv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgebak_");
  }

  public static void dgebal_(CharPtr job, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ilo, IntPtr ihi, DoublePtr scale, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgebal_");
  }

  public static void dgebd2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr d, DoublePtr e, DoublePtr tauq, DoublePtr taup, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgebd2_");
  }

  public static void dgebrd_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr d, DoublePtr e, DoublePtr tauq, DoublePtr taup, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgebrd_");
  }

  public static void dgecon_(CharPtr norm, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgecon_");
  }

  public static void dgeequ_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr r, DoublePtr c, DoublePtr rowcnd, DoublePtr colcnd, DoublePtr amax, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeequ_");
  }

  // void F77_NAME() dgees (const char *jobvs, const char *sort, int(*select)(const double *, const double *), const int *n, double *a, const int *lda, int *sdim, double *wr, double *wi, double *vs, const int *ldvs, double *work, const int *lwork, int *bwork, int *info)

  // void F77_NAME() dgeesx (const char *jobvs, const char *sort, int(*select)(const double *, const double *), const char *sense, const int *n, double *a, const int *lda, int *sdim, double *wr, double *wi, double *vs, const int *ldvs, double *rconde, double *rcondv, double *work, const int *lwork, int *iwork, const int *liwork, int *bwork, int *info)

  public static void dgeev_(CharPtr jobvl, CharPtr jobvr, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr wr, DoublePtr wi, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeev_");
  }

  public static void dgeevx_(CharPtr balanc, CharPtr jobvl, CharPtr jobvr, CharPtr sense, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr wr, DoublePtr wi, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, IntPtr ilo, IntPtr ihi, DoublePtr scale, DoublePtr abnrm, DoublePtr rconde, DoublePtr rcondv, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeevx_");
  }

  public static void dgegv_(CharPtr jobvl, CharPtr jobvr, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgegv_");
  }

  public static void dgehd2_(IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgehd2_");
  }

  public static void dgehrd_(IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgehrd_");
  }

  public static void dgelq2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelq2_");
  }

  public static void dgelqf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelqf_");
  }

  public static void dgels_(CharPtr trans, IntPtr m, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgels_");
  }

  public static void dgelss_(IntPtr m, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr s, DoublePtr rcond, IntPtr rank, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelss_");
  }

  public static void dgelsy_(IntPtr m, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr jpvt, DoublePtr rcond, IntPtr rank, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelsy_");
  }

  public static void dgeql2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeql2_");
  }

  public static void dgeqlf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeqlf_");
  }

  public static void dgeqp3_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, IntPtr jpvt, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeqp3_");
  }

  public static void dgeqpf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, IntPtr jpvt, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeqpf_");
  }

  public static void dgeqr2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeqr2_");
  }

  public static void dgeqrf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgeqrf_");
  }

  public static void dgerfs_(CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgerfs_");
  }

  public static void dgerq2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgerq2_");
  }

  public static void dgerqf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgerqf_");
  }

  public static void dgesv_(IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgesv_");
  }

  public static void dgesvd_(CharPtr jobu, CharPtr jobvt, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr s, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgesvd_");
  }

  public static void dgesvx_(CharPtr fact, CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, IntPtr ipiv, CharPtr equed, DoublePtr r, DoublePtr c, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgesvx_");
  }

  public static void dgetf2_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgetf2_");
  }

  public static void dgetrf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgetrf_");
  }

  public static void dgetri_(IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgetri_");
  }

  public static void dgetrs_(CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgetrs_");
  }

  public static void dggbak_(CharPtr job, CharPtr side, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr lscale, DoublePtr rscale, IntPtr m, DoublePtr v, IntPtr ldv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggbak_");
  }

  public static void dggbal_(CharPtr job, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr ilo, IntPtr ihi, DoublePtr lscale, DoublePtr rscale, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggbal_");
  }

  // void F77_NAME() dgges (const char *jobvsl, const char *jobvsr, const char *sort, int(*delztg)(double *, double *, double *), const int *n, double *a, const int *lda, double *b, const int *ldb, double *alphar, double *alphai, const double *beta, double *vsl, const int *ldvsl, double *vsr, const int *ldvsr, double *work, const int *lwork, int *bwork, int *info)

  public static void dggglm_(IntPtr n, IntPtr m, IntPtr p, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr d, DoublePtr x, DoublePtr y, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggglm_");
  }

  public static void dgghrd_(CharPtr compq, CharPtr compz, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr q, IntPtr ldq, DoublePtr z, IntPtr ldz, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgghrd_");
  }

  public static void dgglse_(IntPtr m, IntPtr n, IntPtr p, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr c, DoublePtr d, DoublePtr x, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgglse_");
  }

  public static void dggqrf_(IntPtr n, IntPtr m, IntPtr p, DoublePtr a, IntPtr lda, DoublePtr taua, DoublePtr b, IntPtr ldb, DoublePtr taub, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggqrf_");
  }

  public static void dggrqf_(IntPtr m, IntPtr p, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr taua, DoublePtr b, IntPtr ldb, DoublePtr taub, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggrqf_");
  }

  public static void dggsvd_(CharPtr jobu, CharPtr jobv, CharPtr jobq, IntPtr m, IntPtr n, IntPtr p, IntPtr k, IntPtr l, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alpha, DoublePtr beta, DoublePtr u, IntPtr ldu, DoublePtr v, IntPtr ldv, DoublePtr q, IntPtr ldq, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggsvd_");
  }

  public static void dgtcon_(CharPtr norm, IntPtr n, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr du2, IntPtr ipiv, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgtcon_");
  }

  public static void dgtrfs_(CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr dlf, DoublePtr df, DoublePtr duf, DoublePtr du2, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgtrfs_");
  }

  public static void dgtsv_(IntPtr n, IntPtr nrhs, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgtsv_");
  }

  public static void dgtsvx_(IntPtr fact, CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr dlf, DoublePtr df, DoublePtr duf, DoublePtr du2, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgtsvx_");
  }

  public static void dgttrf_(IntPtr n, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr du2, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgttrf_");
  }

  public static void dgttrs_(CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr du2, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgttrs_");
  }

  public static void dopgtr_(CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr tau, DoublePtr q, IntPtr ldq, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dopgtr_");
  }

  public static void dopmtr_(CharPtr side, CharPtr uplo, CharPtr trans, IntPtr m, IntPtr n, DoublePtr ap, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dopmtr_");
  }

  public static void dorg2l_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorg2l_");
  }

  public static void dorg2r_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorg2r_");
  }

  public static void dorgbr_(CharPtr vect, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgbr_");
  }

  public static void dorghr_(IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorghr_");
  }

  public static void dorgl2_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgl2_");
  }

  public static void dorglq_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorglq_");
  }

  public static void dorgql_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgql_");
  }

  public static void dorgqr_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgqr_");
  }

  public static void dorgr2_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgr2_");
  }

  public static void dorgrq_(IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgrq_");
  }

  public static void dorgtr_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorgtr_");
  }

  public static void dorm2l_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorm2l_");
  }

  public static void dorm2r_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorm2r_");
  }

  public static void dormbr_(CharPtr vect, CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormbr_");
  }

  public static void dormhr_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormhr_");
  }

  public static void dorml2_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dorml2_");
  }

  public static void dormlq_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormlq_");
  }

  public static void dormql_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormql_");
  }

  public static void dormqr_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormqr_");
  }

  public static void dormr2_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormr2_");
  }

  public static void dormrq_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormrq_");
  }

  public static void dormtr_(CharPtr side, CharPtr uplo, CharPtr trans, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormtr_");
  }

  public static void dpbcon_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbcon_");
  }

  public static void dpbequ_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr s, DoublePtr scond, DoublePtr amax, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbequ_");
  }

  public static void dpbrfs_(CharPtr uplo, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr afb, IntPtr ldafb, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbrfs_");
  }

  public static void dpbstf_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbstf_");
  }

  public static void dpbsv_(CharPtr uplo, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbsv_");
  }

  public static void dpbsvx_(IntPtr fact, CharPtr uplo, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr afb, IntPtr ldafb, CharPtr equed, DoublePtr s, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbsvx_");
  }

  public static void dpbtf2_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbtf2_");
  }

  public static void dpbtrf_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbtrf_");
  }

  public static void dpbtrs_(CharPtr uplo, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpbtrs_");
  }

  public static void dpocon_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpocon_");
  }

  public static void dpoequ_(IntPtr n, DoublePtr a, IntPtr lda, DoublePtr s, DoublePtr scond, DoublePtr amax, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpoequ_");
  }

  public static void dporfs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dporfs_");
  }

  public static void dposv_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dposv_");
  }

  public static void dposvx_(IntPtr fact, CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, CharPtr equed, DoublePtr s, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dposvx_");
  }

  public static void dpotf2_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpotf2_");
  }

  public static void dpotrf_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpotrf_");
  }

  public static void dpotri_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpotri_");
  }

  public static void dpotrs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpotrs_");
  }

  public static void dppcon_(CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dppcon_");
  }

  public static void dppequ_(CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr s, DoublePtr scond, DoublePtr amax, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dppequ_");
  }

  public static void dpprfs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr afp, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpprfs_");
  }

  public static void dppsv_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dppsv_");
  }

  public static void dppsvx_(IntPtr fact, CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr afp, CharPtr equed, DoublePtr s, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dppsvx_");
  }

  public static void dpptrf_(CharPtr uplo, IntPtr n, DoublePtr ap, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpptrf_");
  }

  public static void dpptri_(CharPtr uplo, IntPtr n, DoublePtr ap, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpptri_");
  }

  public static void dpptrs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpptrs_");
  }

  public static void dptcon_(IntPtr n, DoublePtr d, DoublePtr e, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dptcon_");
  }

  public static void dpteqr_(CharPtr compz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpteqr_");
  }

  public static void dptrfs_(IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr df, DoublePtr ef, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dptrfs_");
  }

  public static void dptsv_(IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dptsv_");
  }

  public static void dptsvx_(IntPtr fact, IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr df, DoublePtr ef, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dptsvx_");
  }

  public static void dpttrf_(IntPtr n, DoublePtr d, DoublePtr e, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpttrf_");
  }

  public static void dpttrs_(IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpttrs_");
  }

  public static void drscl_(IntPtr n, DoublePtr da, DoublePtr x, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("drscl_");
  }

  public static void dsbev_(CharPtr jobz, CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbev_");
  }

  public static void dsbevd_(CharPtr jobz, CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbevd_");
  }

  public static void dsbevx_(CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr q, IntPtr ldq, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbevx_");
  }

  public static void dsbgst_(CharPtr vect, CharPtr uplo, IntPtr n, IntPtr ka, IntPtr kb, DoublePtr ab, IntPtr ldab, DoublePtr bb, IntPtr ldbb, DoublePtr x, IntPtr ldx, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbgst_");
  }

  public static void dsbgv_(CharPtr jobz, CharPtr uplo, IntPtr n, IntPtr ka, IntPtr kb, DoublePtr ab, IntPtr ldab, DoublePtr bb, IntPtr ldbb, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbgv_");
  }

  public static void dsbtrd_(CharPtr vect, CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr d, DoublePtr e, DoublePtr q, IntPtr ldq, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbtrd_");
  }

  public static void dspcon_(CharPtr uplo, IntPtr n, DoublePtr ap, IntPtr ipiv, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspcon_");
  }

  public static void dspev_(CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspev_");
  }

  public static void dspevd_(CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspevd_");
  }

  public static void dspevx_(CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspevx_");
  }

  public static void dspgst_(IntPtr itype, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr bp, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspgst_");
  }

  public static void dspgv_(IntPtr itype, CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr bp, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspgv_");
  }

  public static void dsprfs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr afp, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsprfs_");
  }

  public static void dspsv_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspsv_");
  }

  public static void dspsvx_(IntPtr fact, CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr afp, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspsvx_");
  }

  public static void dsptrd_(CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr d, DoublePtr e, DoublePtr tau, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsptrd_");
  }

  public static void dsptrf_(CharPtr uplo, IntPtr n, DoublePtr ap, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsptrf_");
  }

  public static void dsptri_(CharPtr uplo, IntPtr n, DoublePtr ap, IntPtr ipiv, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsptri_");
  }

  public static void dsptrs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr ap, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsptrs_");
  }

  public static void dstebz_(CharPtr range, CharPtr order, IntPtr n, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, DoublePtr d, DoublePtr e, IntPtr m, IntPtr nsplit, DoublePtr w, IntPtr iblock, IntPtr isplit, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstebz_");
  }

  public static void dstedc_(CharPtr compz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstedc_");
  }

  public static void dstein_(IntPtr n, DoublePtr d, DoublePtr e, IntPtr m, DoublePtr w, IntPtr iblock, IntPtr isplit, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstein_");
  }

  public static void dsteqr_(CharPtr compz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsteqr_");
  }

  public static void dsterf_(IntPtr n, DoublePtr d, DoublePtr e, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsterf_");
  }

  public static void dstev_(CharPtr jobz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstev_");
  }

  public static void dstevd_(CharPtr jobz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstevd_");
  }

  public static void dstevx_(CharPtr jobz, CharPtr range, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstevx_");
  }

  public static void dsycon_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr anorm, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsycon_");
  }

  public static void dsyev_(CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr w, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsyev_");
  }

  public static void dsyevd_(CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr w, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsyevd_");
  }

  public static void dsyevx_(CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsyevx_");
  }

  public static void dsyevr_(CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, IntPtr isuppz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsyevr_");
  }

  public static void dsygs2_(IntPtr itype, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsygs2_");
  }

  public static void dsygst_(IntPtr itype, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsygst_");
  }

  public static void dsygv_(IntPtr itype, CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr w, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsygv_");
  }

  public static void dsyrfs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsyrfs_");
  }

  public static void dsysv_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsysv_");
  }

  public static void dsysvx_(IntPtr fact, CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr af, IntPtr ldaf, IntPtr ipiv, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr rcond, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsysvx_");
  }

  public static void dsytd2_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr d, DoublePtr e, DoublePtr tau, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytd2_");
  }

  public static void dsytf2_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytf2_");
  }

  public static void dsytrd_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr d, DoublePtr e, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytrd_");
  }

  public static void dsytrf_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytrf_");
  }

  public static void dsytri_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytri_");
  }

  public static void dsytrs_(CharPtr uplo, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsytrs_");
  }

  public static void dtbcon_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtbcon_");
  }

  public static void dtbrfs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtbrfs_");
  }

  public static void dtbtrs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr kd, IntPtr nrhs, DoublePtr ab, IntPtr ldab, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtbtrs_");
  }

  public static void dtgevc_(CharPtr side, CharPtr howmny, IntPtr select, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, IntPtr mm, IntPtr m, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgevc_");
  }

  public static void dtgsja_(CharPtr jobu, CharPtr jobv, CharPtr jobq, IntPtr m, IntPtr p, IntPtr n, IntPtr k, IntPtr l, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr tola, DoublePtr tolb, DoublePtr alpha, DoublePtr beta, DoublePtr u, IntPtr ldu, DoublePtr v, IntPtr ldv, DoublePtr q, IntPtr ldq, DoublePtr work, IntPtr ncycle, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgsja_");
  }

  public static void dtpcon_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr ap, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtpcon_");
  }

  public static void dtprfs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtprfs_");
  }

  public static void dtptri_(CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr ap, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtptri_");
  }

  public static void dtptrs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr nrhs, DoublePtr ap, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtptrs_");
  }

  public static void dtrcon_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr rcond, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrcon_");
  }

  public static void dtrevc_(CharPtr side, CharPtr howmny, IntPtr select, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, IntPtr mm, IntPtr m, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrevc_");
  }

  public static void dtrexc_(CharPtr compq, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr q, IntPtr ldq, IntPtr ifst, IntPtr ILST, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrexc_");
  }

  public static void dtrrfs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr x, IntPtr ldx, DoublePtr ferr, DoublePtr berr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrrfs_");
  }

  public static void dtrsen_(CharPtr job, CharPtr compq, IntPtr select, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr q, IntPtr ldq, DoublePtr wr, DoublePtr wi, IntPtr m, DoublePtr s, DoublePtr sep, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrsen_");
  }

  public static void dtrsna_(CharPtr job, CharPtr howmny, IntPtr select, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, DoublePtr s, DoublePtr sep, IntPtr mm, IntPtr m, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrsna_");
  }

  public static void dtrsyl_(CharPtr trana, CharPtr tranb, IntPtr isgn, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr c, IntPtr ldc, DoublePtr scale, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrsyl_");
  }

  public static void dtrti2_(CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrti2_");
  }

  public static void dtrtri_(CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrtri_");
  }

  public static void dtrtrs_(CharPtr uplo, CharPtr trans, CharPtr diag, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtrtrs_");
  }

  public static void dtzrqf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtzrqf_");
  }

  public static void dhgeqz_(CharPtr job, CharPtr compq, CharPtr compz, IntPtr n, IntPtr ILO, IntPtr IHI, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr q, IntPtr ldq, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dhgeqz_");
  }

  public static void dhsein_(CharPtr side, CharPtr eigsrc, CharPtr initv, IntPtr select, IntPtr n, DoublePtr h, IntPtr ldh, DoublePtr wr, DoublePtr wi, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, IntPtr mm, IntPtr m, DoublePtr work, IntPtr ifaill, IntPtr ifailr, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dhsein_");
  }

  public static void dhseqr_(CharPtr job, CharPtr compz, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr h, IntPtr ldh, DoublePtr wr, DoublePtr wi, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dhseqr_");
  }

  public static void dlabad_(DoublePtr small, DoublePtr large) {
     throw new UnimplementedGnuApiMethod("dlabad_");
  }

  public static void dlabrd_(IntPtr m, IntPtr n, IntPtr nb, DoublePtr a, IntPtr lda, DoublePtr d, DoublePtr e, DoublePtr tauq, DoublePtr taup, DoublePtr x, IntPtr ldx, DoublePtr y, IntPtr ldy) {
     throw new UnimplementedGnuApiMethod("dlabrd_");
  }

  public static void dlacon_(IntPtr n, DoublePtr v, DoublePtr x, IntPtr isgn, DoublePtr est, IntPtr kase) {
     throw new UnimplementedGnuApiMethod("dlacon_");
  }

  public static void dlacpy_(CharPtr uplo, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dlacpy_");
  }

  public static void dladiv_(DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr d, DoublePtr p, DoublePtr q) {
     throw new UnimplementedGnuApiMethod("dladiv_");
  }

  public static void dlae2_(DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr rt1, DoublePtr rt2) {
     throw new UnimplementedGnuApiMethod("dlae2_");
  }

  public static void dlaebz_(IntPtr ijob, IntPtr nitmax, IntPtr n, IntPtr mmax, IntPtr minp, IntPtr nbmin, DoublePtr abstol, DoublePtr reltol, DoublePtr pivmin, DoublePtr d, DoublePtr e, DoublePtr e2, IntPtr nval, DoublePtr ab, DoublePtr c, IntPtr mout, IntPtr nab, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaebz_");
  }

  public static void dlaed0_(IntPtr icompq, IntPtr qsiz, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr q, IntPtr ldq, DoublePtr qstore, IntPtr ldqs, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed0_");
  }

  public static void dlaed1_(IntPtr n, DoublePtr d, DoublePtr q, IntPtr ldq, IntPtr indxq, DoublePtr rho, IntPtr cutpnt, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed1_");
  }

  public static void dlaed2_(IntPtr k, IntPtr n, DoublePtr d, DoublePtr q, IntPtr ldq, IntPtr indxq, DoublePtr rho, DoublePtr z, DoublePtr dlamda, DoublePtr w, DoublePtr q2, IntPtr indx, IntPtr indxc, IntPtr indxp, IntPtr coltyp, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed2_");
  }

  public static void dlaed3_(IntPtr k, IntPtr n, IntPtr n1, DoublePtr d, DoublePtr q, IntPtr ldq, DoublePtr rho, DoublePtr dlamda, DoublePtr q2, IntPtr indx, IntPtr ctot, DoublePtr w, DoublePtr s, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed3_");
  }

  public static void dlaed4_(IntPtr n, IntPtr i, DoublePtr d, DoublePtr z, DoublePtr delta, DoublePtr rho, DoublePtr dlam, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed4_");
  }

  public static void dlaed5_(IntPtr i, DoublePtr d, DoublePtr z, DoublePtr delta, DoublePtr rho, DoublePtr dlam) {
     throw new UnimplementedGnuApiMethod("dlaed5_");
  }

  public static void dlaed6_(IntPtr kniter, IntPtr orgati, DoublePtr rho, DoublePtr d, DoublePtr z, DoublePtr finit, DoublePtr tau, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed6_");
  }

  public static void dlaed7_(IntPtr icompq, IntPtr n, IntPtr qsiz, IntPtr tlvls, IntPtr curlvl, IntPtr curpbm, DoublePtr d, DoublePtr q, IntPtr ldq, IntPtr indxq, DoublePtr rho, IntPtr cutpnt, DoublePtr qstore, DoublePtr qptr, IntPtr prmptr, IntPtr perm, IntPtr givptr, IntPtr givcol, DoublePtr givnum, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed7_");
  }

  public static void dlaed8_(IntPtr icompq, IntPtr k, IntPtr n, IntPtr qsiz, DoublePtr d, DoublePtr q, IntPtr ldq, IntPtr indxq, DoublePtr rho, IntPtr cutpnt, DoublePtr z, DoublePtr dlamda, DoublePtr q2, IntPtr ldq2, DoublePtr w, IntPtr perm, IntPtr givptr, IntPtr givcol, DoublePtr givnum, IntPtr indxp, IntPtr indx, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed8_");
  }

  public static void dlaed9_(IntPtr k, IntPtr kstart, IntPtr kstop, IntPtr n, DoublePtr d, DoublePtr q, IntPtr ldq, DoublePtr rho, DoublePtr dlamda, DoublePtr w, DoublePtr s, IntPtr lds, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaed9_");
  }

  public static void dlaeda_(IntPtr n, IntPtr tlvls, IntPtr curlvl, IntPtr curpbm, IntPtr prmptr, IntPtr perm, IntPtr givptr, IntPtr givcol, DoublePtr givnum, DoublePtr q, IntPtr qptr, DoublePtr z, DoublePtr ztemp, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaeda_");
  }

  public static void dlaein_(IntPtr rightv, IntPtr noinit, IntPtr n, DoublePtr h, IntPtr ldh, DoublePtr wr, DoublePtr wi, DoublePtr vr, DoublePtr vi, DoublePtr b, IntPtr ldb, DoublePtr work, DoublePtr eps3, DoublePtr smlnum, DoublePtr bignum, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaein_");
  }

  public static void dlaev2_(DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr rt1, DoublePtr rt2, DoublePtr cs1, DoublePtr sn1) {
     throw new UnimplementedGnuApiMethod("dlaev2_");
  }

  public static void dlaexc_(IntPtr wantq, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr q, IntPtr ldq, IntPtr j1, IntPtr n1, IntPtr n2, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaexc_");
  }

  public static void dlag2_(DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr safmin, DoublePtr scale1, DoublePtr scale2, DoublePtr wr1, DoublePtr wr2, DoublePtr wi) {
     throw new UnimplementedGnuApiMethod("dlag2_");
  }

  public static void dlags2_(IntPtr upper, DoublePtr a1, DoublePtr a2, DoublePtr a3, DoublePtr b1, DoublePtr b2, DoublePtr b3, DoublePtr csu, DoublePtr snu, DoublePtr csv, DoublePtr snv, DoublePtr csq, DoublePtr snq) {
     throw new UnimplementedGnuApiMethod("dlags2_");
  }

  public static void dlagtf_(IntPtr n, DoublePtr a, DoublePtr lambda, DoublePtr b, DoublePtr c, DoublePtr tol, DoublePtr d, IntPtr in, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlagtf_");
  }

  public static void dlagtm_(CharPtr trans, IntPtr n, IntPtr nrhs, DoublePtr alpha, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr x, IntPtr ldx, DoublePtr beta, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dlagtm_");
  }

  public static void dlagts_(IntPtr job, IntPtr n, DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr d, IntPtr in, DoublePtr y, DoublePtr tol, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlagts_");
  }

  public static void dlahqr_(IntPtr wantt, IntPtr wantz, IntPtr n, IntPtr ilo, IntPtr ihi, DoublePtr H, IntPtr ldh, DoublePtr wr, DoublePtr wi, IntPtr iloz, IntPtr ihiz, DoublePtr z, IntPtr ldz, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlahqr_");
  }

  public static void dlahrd_(IntPtr n, IntPtr k, IntPtr nb, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr t, IntPtr ldt, DoublePtr y, IntPtr ldy) {
     throw new UnimplementedGnuApiMethod("dlahrd_");
  }

  public static void dlaic1_(IntPtr job, IntPtr j, DoublePtr x, DoublePtr sest, DoublePtr w, DoublePtr gamma, DoublePtr sestpr, DoublePtr s, DoublePtr c) {
     throw new UnimplementedGnuApiMethod("dlaic1_");
  }

  public static void dlaln2_(IntPtr ltrans, IntPtr na, IntPtr nw, DoublePtr smin, DoublePtr ca, DoublePtr a, IntPtr lda, DoublePtr d1, DoublePtr d2, DoublePtr b, IntPtr ldb, DoublePtr wr, DoublePtr wi, DoublePtr x, IntPtr ldx, DoublePtr scale, DoublePtr xnorm, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaln2_");
  }

  public static double dlamch_(CharPtr cmach) {
     throw new UnimplementedGnuApiMethod("dlamch_");
  }

  public static void dlamrg_(IntPtr n1, IntPtr n2, DoublePtr a, IntPtr dtrd1, IntPtr dtrd2, IntPtr index) {
     throw new UnimplementedGnuApiMethod("dlamrg_");
  }

  public static double dlangb_(CharPtr norm, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlangb_");
  }

  public static double dlange_(CharPtr norm, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlange_");
  }

  public static double dlangt_(CharPtr norm, IntPtr n, DoublePtr dl, DoublePtr d, DoublePtr du) {
     throw new UnimplementedGnuApiMethod("dlangt_");
  }

  public static double dlanhs_(CharPtr norm, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlanhs_");
  }

  public static double dlansb_(CharPtr norm, CharPtr uplo, IntPtr n, IntPtr k, DoublePtr ab, IntPtr ldab, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlansb_");
  }

  public static double dlansp_(CharPtr norm, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlansp_");
  }

  public static double dlanst_(CharPtr norm, IntPtr n, DoublePtr d, DoublePtr e) {
     throw new UnimplementedGnuApiMethod("dlanst_");
  }

  public static double dlansy_(CharPtr norm, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlansy_");
  }

  public static double dlantb_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr n, IntPtr k, DoublePtr ab, IntPtr ldab, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlantb_");
  }

  public static double dlantp_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr n, DoublePtr ap, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlantp_");
  }

  public static double dlantr_(CharPtr norm, CharPtr uplo, CharPtr diag, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlantr_");
  }

  public static void dlanv2_(DoublePtr a, DoublePtr b, DoublePtr c, DoublePtr d, DoublePtr rt1r, DoublePtr rt1i, DoublePtr rt2r, DoublePtr rt2i, DoublePtr cs, DoublePtr sn) {
     throw new UnimplementedGnuApiMethod("dlanv2_");
  }

  public static void dlapll_(IntPtr n, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr ssmin) {
     throw new UnimplementedGnuApiMethod("dlapll_");
  }

  public static void dlapmt_(IntPtr forwrd, IntPtr m, IntPtr n, DoublePtr x, IntPtr ldx, IntPtr k) {
     throw new UnimplementedGnuApiMethod("dlapmt_");
  }

  public static double dlapy2_(DoublePtr x, DoublePtr y) {
     throw new UnimplementedGnuApiMethod("dlapy2_");
  }

  public static double dlapy3_(DoublePtr x, DoublePtr y, DoublePtr z) {
     throw new UnimplementedGnuApiMethod("dlapy3_");
  }

  public static void dlaqgb_(IntPtr m, IntPtr n, IntPtr kl, IntPtr ku, DoublePtr ab, IntPtr ldab, DoublePtr r, DoublePtr c, DoublePtr rowcnd, DoublePtr colcnd, DoublePtr amax, CharPtr equed) {
     throw new UnimplementedGnuApiMethod("dlaqgb_");
  }

  public static void dlaqge_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr r, DoublePtr c, DoublePtr rowcnd, DoublePtr colcnd, DoublePtr amax, CharPtr equed) {
     throw new UnimplementedGnuApiMethod("dlaqge_");
  }

  public static void dlaqsb_(CharPtr uplo, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr s, DoublePtr scond, DoublePtr amax, CharPtr equed) {
     throw new UnimplementedGnuApiMethod("dlaqsb_");
  }

  public static void dlaqsp_(CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr s, DoublePtr scond, DoublePtr amax, IntPtr equed) {
     throw new UnimplementedGnuApiMethod("dlaqsp_");
  }

  public static void dlaqsy_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr s, DoublePtr scond, DoublePtr amax, IntPtr equed) {
     throw new UnimplementedGnuApiMethod("dlaqsy_");
  }

  public static void dlaqtr_(IntPtr ltran, IntPtr lreal, IntPtr n, DoublePtr t, IntPtr ldt, DoublePtr b, DoublePtr w, DoublePtr scale, DoublePtr x, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlaqtr_");
  }

  public static void dlar2v_(IntPtr n, DoublePtr x, DoublePtr y, DoublePtr z, IntPtr incx, DoublePtr c, DoublePtr s, IntPtr incc) {
     throw new UnimplementedGnuApiMethod("dlar2v_");
  }

  public static void dlarf_(CharPtr side, IntPtr m, IntPtr n, DoublePtr v, IntPtr incv, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlarf_");
  }

  public static void dlarfb_(CharPtr side, CharPtr trans, CharPtr direct, CharPtr storev, IntPtr m, IntPtr n, IntPtr k, DoublePtr v, IntPtr ldv, DoublePtr t, IntPtr ldt, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork) {
     throw new UnimplementedGnuApiMethod("dlarfb_");
  }

  public static void dlarfg_(IntPtr n, DoublePtr alpha, DoublePtr x, IntPtr incx, DoublePtr tau) {
     throw new UnimplementedGnuApiMethod("dlarfg_");
  }

  public static void dlarft_(CharPtr direct, CharPtr storev, IntPtr n, IntPtr k, DoublePtr v, IntPtr ldv, DoublePtr tau, DoublePtr t, IntPtr ldt) {
     throw new UnimplementedGnuApiMethod("dlarft_");
  }

  public static void dlarfx_(CharPtr side, IntPtr m, IntPtr n, DoublePtr v, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlarfx_");
  }

  public static void dlargv_(IntPtr n, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr c, IntPtr incc) {
     throw new UnimplementedGnuApiMethod("dlargv_");
  }

  public static void dlarnv_(IntPtr idist, IntPtr iseed, IntPtr n, DoublePtr x) {
     throw new UnimplementedGnuApiMethod("dlarnv_");
  }

  public static void dlartg_(DoublePtr f, DoublePtr g, DoublePtr cs, DoublePtr sn, DoublePtr r) {
     throw new UnimplementedGnuApiMethod("dlartg_");
  }

  public static void dlartv_(IntPtr n, DoublePtr x, IntPtr incx, DoublePtr y, IntPtr incy, DoublePtr c, DoublePtr s, IntPtr incc) {
     throw new UnimplementedGnuApiMethod("dlartv_");
  }

  public static void dlaruv_(IntPtr iseed, IntPtr n, DoublePtr x) {
     throw new UnimplementedGnuApiMethod("dlaruv_");
  }

  public static void dlas2_(DoublePtr f, DoublePtr g, DoublePtr h, DoublePtr ssmin, DoublePtr ssmax) {
     throw new UnimplementedGnuApiMethod("dlas2_");
  }

  public static void dlascl_(CharPtr type, IntPtr kl, IntPtr ku, DoublePtr cfrom, DoublePtr cto, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlascl_");
  }

  public static void dlaset_(CharPtr uplo, IntPtr m, IntPtr n, DoublePtr alpha, DoublePtr beta, DoublePtr a, IntPtr lda) {
     throw new UnimplementedGnuApiMethod("dlaset_");
  }

  public static void dlasq1_(IntPtr n, DoublePtr d, DoublePtr e, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasq1_");
  }

  public static void dlasq2_(IntPtr m, DoublePtr q, DoublePtr e, DoublePtr qq, DoublePtr ee, DoublePtr eps, DoublePtr tol2, DoublePtr small2, DoublePtr sup, IntPtr kend, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasq2_");
  }

  public static void dlasq3_(IntPtr n, DoublePtr q, DoublePtr e, DoublePtr qq, DoublePtr ee, DoublePtr sup, DoublePtr sigma, IntPtr kend, IntPtr off, IntPtr iphase, IntPtr iconv, DoublePtr eps, DoublePtr tol2, DoublePtr small2) {
     throw new UnimplementedGnuApiMethod("dlasq3_");
  }

  public static void dlasq4_(IntPtr n, DoublePtr q, DoublePtr e, DoublePtr tau, DoublePtr sup) {
     throw new UnimplementedGnuApiMethod("dlasq4_");
  }

  public static void dlasr_(CharPtr side, CharPtr pivot, CharPtr direct, IntPtr m, IntPtr n, DoublePtr c, DoublePtr s, DoublePtr a, IntPtr lda) {
     throw new UnimplementedGnuApiMethod("dlasr_");
  }

  public static void dlasrt_(CharPtr id, IntPtr n, DoublePtr d, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasrt_");
  }

  public static void dlassq_(IntPtr n, DoublePtr x, IntPtr incx, DoublePtr scale, DoublePtr sumsq) {
     throw new UnimplementedGnuApiMethod("dlassq_");
  }

  public static void dlasv2_(DoublePtr f, DoublePtr g, DoublePtr h, DoublePtr ssmin, DoublePtr ssmax, DoublePtr snr, DoublePtr csr, DoublePtr snl, DoublePtr csl) {
     throw new UnimplementedGnuApiMethod("dlasv2_");
  }

  public static void dlaswp_(IntPtr n, DoublePtr a, IntPtr lda, IntPtr k1, IntPtr k2, IntPtr ipiv, IntPtr incx) {
     throw new UnimplementedGnuApiMethod("dlaswp_");
  }

  public static void dlasy2_(IntPtr ltranl, IntPtr ltranr, IntPtr isgn, IntPtr n1, IntPtr n2, DoublePtr tl, IntPtr ldtl, DoublePtr tr, IntPtr ldtr, DoublePtr b, IntPtr ldb, DoublePtr scale, DoublePtr x, IntPtr ldx, DoublePtr xnorm, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasy2_");
  }

  public static void dlasyf_(CharPtr uplo, IntPtr n, IntPtr nb, IntPtr kb, DoublePtr a, IntPtr lda, IntPtr ipiv, DoublePtr w, IntPtr ldw, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasyf_");
  }

  public static void dlatbs_(CharPtr uplo, CharPtr trans, CharPtr diag, CharPtr normin, IntPtr n, IntPtr kd, DoublePtr ab, IntPtr ldab, DoublePtr x, DoublePtr scale, DoublePtr cnorm, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlatbs_");
  }

  public static void dlatps_(CharPtr uplo, CharPtr trans, CharPtr diag, CharPtr normin, IntPtr n, DoublePtr ap, DoublePtr x, DoublePtr scale, DoublePtr cnorm, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlatps_");
  }

  public static void dlatrd_(CharPtr uplo, IntPtr n, IntPtr nb, DoublePtr a, IntPtr lda, DoublePtr e, DoublePtr tau, DoublePtr w, IntPtr ldw) {
     throw new UnimplementedGnuApiMethod("dlatrd_");
  }

  public static void dlatrs_(CharPtr uplo, CharPtr trans, CharPtr diag, CharPtr normin, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr x, DoublePtr scale, DoublePtr cnorm, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlatrs_");
  }

  public static void dlatzm_(CharPtr side, IntPtr m, IntPtr n, DoublePtr v, IntPtr incv, DoublePtr tau, DoublePtr c1, DoublePtr c2, IntPtr ldc, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlatzm_");
  }

  public static void dlauu2_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlauu2_");
  }

  public static void dlauum_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlauum_");
  }

  // int F77_NAME() izmax1 (const int *n, Rcomplex *cx, const int *incx)

  // void F77_NAME() zgecon (const char *norm, const int *n, const Rcomplex *a, const int *lda, const double *anorm, double *rcond, Rcomplex *work, double *rwork, int *info)

  // void F77_NAME() zgesv (const int *n, const int *nrhs, Rcomplex *a, const int *lda, int *ipiv, Rcomplex *b, const int *ldb, int *info)

  // void F77_NAME() zgeqp3 (const int *m, const int *n, Rcomplex *a, const int *lda, int *jpvt, Rcomplex *tau, Rcomplex *work, const int *lwork, double *rwork, int *info)

  // void F77_NAME() zunmqr (const char *side, const char *trans, const int *m, const int *n, const int *k, Rcomplex *a, const int *lda, Rcomplex *tau, Rcomplex *c, const int *ldc, Rcomplex *work, const int *lwork, int *info)

  // void F77_NAME() ztrtrs (const char *uplo, const char *trans, const char *diag, const int *n, const int *nrhs, Rcomplex *a, const int *lda, Rcomplex *b, const int *ldb, int *info)

  // void F77_NAME() zgesvd (const char *jobu, const char *jobvt, const int *m, const int *n, Rcomplex *a, const int *lda, double *s, Rcomplex *u, const int *ldu, Rcomplex *vt, const int *ldvt, Rcomplex *work, const int *lwork, double *rwork, int *info)

  // void F77_NAME() zheev (const char *jobz, const char *uplo, const int *n, Rcomplex *a, const int *lda, double *w, Rcomplex *work, const int *lwork, double *rwork, int *info)

  // void F77_NAME() zgeev (const char *jobvl, const char *jobvr, const int *n, Rcomplex *a, const int *lda, Rcomplex *wr, Rcomplex *vl, const int *ldvl, Rcomplex *vr, const int *ldvr, Rcomplex *work, const int *lwork, double *rwork, int *info)

  // double F77_NAME() dzsum1 (const int *n, Rcomplex *CX, const int *incx)

  // void F77_NAME() zlacn2 (const int *n, Rcomplex *v, Rcomplex *x, double *est, int *kase, int *isave)

  // double F77_NAME() zlantr (const char *norm, const char *uplo, const char *diag, const int *m, const int *n, Rcomplex *a, const int *lda, double *work)

  public static void dbdsdc_(CharPtr uplo, CharPtr compq, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, DoublePtr q, IntPtr iq, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dbdsdc_");
  }

  public static void dgegs_(CharPtr jobvsl, CharPtr jobvsr, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr vsl, IntPtr ldvsl, DoublePtr vsr, IntPtr ldvsr, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgegs_");
  }

  public static void dgelsd_(IntPtr m, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr s, DoublePtr rcond, IntPtr rank, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelsd_");
  }

  public static void dgelsx_(IntPtr m, IntPtr n, IntPtr nrhs, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, IntPtr jpvt, DoublePtr rcond, IntPtr rank, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgelsx_");
  }

  public static void dgesc2_(IntPtr n, DoublePtr a, IntPtr lda, DoublePtr rhs, IntPtr ipiv, IntPtr jpiv, DoublePtr scale) {
     throw new UnimplementedGnuApiMethod("dgesc2_");
  }

  public static void dgesdd_(CharPtr jobz, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr s, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgesdd_");
  }

  public static void dgetc2_(IntPtr n, DoublePtr a, IntPtr lda, IntPtr ipiv, IntPtr jpiv, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dgetc2_");
  }

  // void F77_NAME() dggesx (char *jobvsl, char *jobvsr, char *sort, L_fp delctg, char *sense, int *n, double *a, int *lda, double *b, int *ldb, int *sdim, double *alphar, double *alphai, double *beta, double *vsl, int *ldvsl, double *vsr, int *ldvsr, double *rconde, double *rcondv, double *work, int *lwork, int *iwork, int *liwork, int *bwork, int *info)

  public static void dggev_(CharPtr jobvl, CharPtr jobvr, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggev_");
  }

  public static void dggevx_(CharPtr balanc, CharPtr jobvl, CharPtr jobvr, CharPtr sense, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, IntPtr ilo, IntPtr ihi, DoublePtr lscale, DoublePtr rscale, DoublePtr abnrm, DoublePtr bbnrm, DoublePtr rconde, DoublePtr rcondv, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr bwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggevx_");
  }

  public static void dggsvp_(CharPtr jobu, CharPtr jobv, CharPtr jobq, IntPtr m, IntPtr p, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr tola, DoublePtr tolb, IntPtr k, IntPtr l, DoublePtr u, IntPtr ldu, DoublePtr v, IntPtr ldv, DoublePtr q, IntPtr ldq, IntPtr iwork, DoublePtr tau, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dggsvp_");
  }

  public static void dgtts2_(IntPtr itrans, IntPtr n, IntPtr nrhs, DoublePtr dl, DoublePtr d, DoublePtr du, DoublePtr du2, IntPtr ipiv, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dgtts2_");
  }

  public static void dlagv2_(DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr csl, DoublePtr snl, DoublePtr csr, DoublePtr snr) {
     throw new UnimplementedGnuApiMethod("dlagv2_");
  }

  public static void dlals0_(IntPtr icompq, IntPtr nl, IntPtr nr, IntPtr sqre, IntPtr nrhs, DoublePtr b, IntPtr ldb, DoublePtr bx, IntPtr ldbx, IntPtr perm, IntPtr givptr, IntPtr givcol, IntPtr ldgcol, DoublePtr givnum, IntPtr ldgnum, DoublePtr poles, DoublePtr difl, DoublePtr difr, DoublePtr z, IntPtr k, DoublePtr c, DoublePtr s, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlals0_");
  }

  public static void dlalsa_(IntPtr icompq, IntPtr smlsiz, IntPtr n, IntPtr nrhs, DoublePtr b, IntPtr ldb, DoublePtr bx, IntPtr ldbx, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr k, DoublePtr difl, DoublePtr difr, DoublePtr z, DoublePtr poles, IntPtr givptr, IntPtr givcol, IntPtr ldgcol, IntPtr perm, DoublePtr givnum, DoublePtr c, DoublePtr s, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlalsa_");
  }

  public static void dlalsd_(CharPtr uplo, IntPtr smlsiz, IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr b, IntPtr ldb, DoublePtr rcond, IntPtr rank, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlalsd_");
  }

  public static void dlamc1_(IntPtr beta, IntPtr t, IntPtr rnd, IntPtr ieee1) {
     throw new UnimplementedGnuApiMethod("dlamc1_");
  }

  public static void dlamc2_(IntPtr beta, IntPtr t, IntPtr rnd, DoublePtr eps, IntPtr emin, DoublePtr rmin, IntPtr emax, DoublePtr rmax) {
     throw new UnimplementedGnuApiMethod("dlamc2_");
  }

  public static double dlamc3_(DoublePtr a, DoublePtr b) {
     throw new UnimplementedGnuApiMethod("dlamc3_");
  }

  public static void dlamc4_(IntPtr emin, DoublePtr start, IntPtr base) {
     throw new UnimplementedGnuApiMethod("dlamc4_");
  }

  public static void dlamc5_(IntPtr beta, IntPtr p, IntPtr emin, IntPtr ieee, IntPtr emax, DoublePtr rmax) {
     throw new UnimplementedGnuApiMethod("dlamc5_");
  }

  public static void dlaqp2_(IntPtr m, IntPtr n, IntPtr offset, DoublePtr a, IntPtr lda, IntPtr jpvt, DoublePtr tau, DoublePtr vn1, DoublePtr vn2, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlaqp2_");
  }

  public static void dlaqps_(IntPtr m, IntPtr n, IntPtr offset, IntPtr nb, IntPtr kb, DoublePtr a, IntPtr lda, IntPtr jpvt, DoublePtr tau, DoublePtr vn1, DoublePtr vn2, DoublePtr auxv, DoublePtr f, IntPtr ldf) {
     throw new UnimplementedGnuApiMethod("dlaqps_");
  }

  public static void dlar1v_(IntPtr n, IntPtr b1, IntPtr bn, DoublePtr sigma, DoublePtr d, DoublePtr l, DoublePtr ld, DoublePtr lld, DoublePtr gersch, DoublePtr z, DoublePtr ztz, DoublePtr mingma, IntPtr r, IntPtr isuppz, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlar1v_");
  }

  public static void dlarrb_(IntPtr n, DoublePtr d, DoublePtr l, DoublePtr ld, DoublePtr lld, IntPtr ifirst, IntPtr ilast, DoublePtr sigma, DoublePtr reltol, DoublePtr w, DoublePtr wgap, DoublePtr werr, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlarrb_");
  }

  public static void dlarre_(IntPtr n, DoublePtr d, DoublePtr e, DoublePtr tol, IntPtr nsplit, IntPtr isplit, IntPtr m, DoublePtr w, DoublePtr woff, DoublePtr gersch, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlarre_");
  }

  public static void dlarrf_(IntPtr n, DoublePtr d, DoublePtr l, DoublePtr ld, DoublePtr lld, IntPtr ifirst, IntPtr ilast, DoublePtr w, DoublePtr dplus, DoublePtr lplus, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlarrf_");
  }

  public static void dlarrv_(IntPtr n, DoublePtr d, DoublePtr l, IntPtr isplit, IntPtr m, DoublePtr w, IntPtr iblock, DoublePtr gersch, DoublePtr tol, DoublePtr z, IntPtr ldz, IntPtr isuppz, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlarrv_");
  }

  public static void dlarz_(CharPtr side, IntPtr m, IntPtr n, IntPtr l, DoublePtr v, IntPtr incv, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlarz_");
  }

  public static void dlarzb_(CharPtr side, CharPtr trans, CharPtr direct, CharPtr storev, IntPtr m, IntPtr n, IntPtr k, IntPtr l, DoublePtr v, IntPtr ldv, DoublePtr t, IntPtr ldt, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr ldwork) {
     throw new UnimplementedGnuApiMethod("dlarzb_");
  }

  public static void dlarzt_(CharPtr direct, CharPtr storev, IntPtr n, IntPtr k, DoublePtr v, IntPtr ldv, DoublePtr tau, DoublePtr t, IntPtr ldt) {
     throw new UnimplementedGnuApiMethod("dlarzt_");
  }

  public static void dlasd0_(IntPtr n, IntPtr sqre, DoublePtr d, DoublePtr e, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, IntPtr smlsiz, IntPtr iwork, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd0_");
  }

  public static void dlasd1_(IntPtr nl, IntPtr nr, IntPtr sqre, DoublePtr d, DoublePtr alpha, DoublePtr beta, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, IntPtr idxq, IntPtr iwork, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd1_");
  }

  public static void dlasd2_(IntPtr nl, IntPtr nr, IntPtr sqre, IntPtr k, DoublePtr d, DoublePtr z, DoublePtr alpha, DoublePtr beta, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr ldvt, DoublePtr dsigma, DoublePtr u2, IntPtr ldu2, DoublePtr vt2, IntPtr ldvt2, IntPtr idxp, IntPtr idx, IntPtr idxc, IntPtr idxq, IntPtr coltyp, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd2_");
  }

  public static void dlasd3_(IntPtr nl, IntPtr nr, IntPtr sqre, IntPtr k, DoublePtr d, DoublePtr q, IntPtr ldq, DoublePtr dsigma, DoublePtr u, IntPtr ldu, DoublePtr u2, IntPtr ldu2, DoublePtr vt, IntPtr ldvt, DoublePtr vt2, IntPtr ldvt2, IntPtr idxc, IntPtr ctot, DoublePtr z, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd3_");
  }

  public static void dlasd4_(IntPtr n, IntPtr i, DoublePtr d, DoublePtr z, DoublePtr delta, DoublePtr rho, DoublePtr sigma, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd4_");
  }

  public static void dlasd5_(IntPtr i, DoublePtr d, DoublePtr z, DoublePtr delta, DoublePtr rho, DoublePtr dsigma, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlasd5_");
  }

  public static void dlasd6_(IntPtr icompq, IntPtr nl, IntPtr nr, IntPtr sqre, DoublePtr d, DoublePtr vf, DoublePtr vl, DoublePtr alpha, DoublePtr beta, IntPtr idxq, IntPtr perm, IntPtr givptr, IntPtr givcol, IntPtr ldgcol, DoublePtr givnum, IntPtr ldgnum, DoublePtr poles, DoublePtr difl, DoublePtr difr, DoublePtr z, IntPtr k, DoublePtr c, DoublePtr s, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd6_");
  }

  public static void dlasd7_(IntPtr icompq, IntPtr nl, IntPtr nr, IntPtr sqre, IntPtr k, DoublePtr d, DoublePtr z, DoublePtr zw, DoublePtr vf, DoublePtr vfw, DoublePtr vl, DoublePtr vlw, DoublePtr alpha, DoublePtr beta, DoublePtr dsigma, IntPtr idx, IntPtr idxp, IntPtr idxq, IntPtr perm, IntPtr givptr, IntPtr givcol, IntPtr ldgcol, DoublePtr givnum, IntPtr ldgnum, DoublePtr c, DoublePtr s, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd7_");
  }

  public static void dlasd8_(IntPtr icompq, IntPtr k, DoublePtr d, DoublePtr z, DoublePtr vf, DoublePtr vl, DoublePtr difl, DoublePtr difr, IntPtr lddifr, DoublePtr dsigma, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd8_");
  }

  public static void dlasd9_(IntPtr icompq, IntPtr ldu, IntPtr k, DoublePtr d, DoublePtr z, DoublePtr vf, DoublePtr vl, DoublePtr difl, DoublePtr difr, DoublePtr dsigma, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasd9_");
  }

  public static void dlasda_(IntPtr icompq, IntPtr smlsiz, IntPtr n, IntPtr sqre, DoublePtr d, DoublePtr e, DoublePtr u, IntPtr ldu, DoublePtr vt, IntPtr k, DoublePtr difl, DoublePtr difr, DoublePtr z, DoublePtr poles, IntPtr givptr, IntPtr givcol, IntPtr ldgcol, IntPtr perm, DoublePtr givnum, DoublePtr c, DoublePtr s, DoublePtr work, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasda_");
  }

  public static void dlasdq_(CharPtr uplo, IntPtr sqre, IntPtr n, IntPtr ncvt, IntPtr nru, IntPtr ncc, DoublePtr d, DoublePtr e, DoublePtr vt, IntPtr ldvt, DoublePtr u, IntPtr ldu, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dlasdq_");
  }

  public static void dlasdt_(IntPtr n, IntPtr lvl, IntPtr nd, IntPtr inode, IntPtr ndiml, IntPtr ndimr, IntPtr msub) {
     throw new UnimplementedGnuApiMethod("dlasdt_");
  }

  public static void dlasq5_(IntPtr i0, IntPtr n0, DoublePtr z, IntPtr pp, DoublePtr tau, DoublePtr dmin, DoublePtr dmin1, DoublePtr dmin2, DoublePtr dn, DoublePtr dnm1, DoublePtr dnm2, IntPtr ieee) {
     throw new UnimplementedGnuApiMethod("dlasq5_");
  }

  public static void dlasq6_(IntPtr i0, IntPtr n0, DoublePtr z, IntPtr pp, DoublePtr dmin, DoublePtr dmin1, DoublePtr dmin2, DoublePtr dn, DoublePtr dnm1, DoublePtr dnm2) {
     throw new UnimplementedGnuApiMethod("dlasq6_");
  }

  public static void dlatdf_(IntPtr ijob, IntPtr n, DoublePtr z, IntPtr ldz, DoublePtr rhs, DoublePtr rdsum, DoublePtr rdscal, IntPtr ipiv, IntPtr jpiv) {
     throw new UnimplementedGnuApiMethod("dlatdf_");
  }

  public static void dlatrz_(IntPtr m, IntPtr n, IntPtr l, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work) {
     throw new UnimplementedGnuApiMethod("dlatrz_");
  }

  public static void dormr3_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, IntPtr l, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormr3_");
  }

  public static void dormrz_(CharPtr side, CharPtr trans, IntPtr m, IntPtr n, IntPtr k, IntPtr l, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr c, IntPtr ldc, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dormrz_");
  }

  public static void dptts2_(IntPtr n, IntPtr nrhs, DoublePtr d, DoublePtr e, DoublePtr b, IntPtr ldb) {
     throw new UnimplementedGnuApiMethod("dptts2_");
  }

  public static void dsbgvd_(CharPtr jobz, CharPtr uplo, IntPtr n, IntPtr ka, IntPtr kb, DoublePtr ab, IntPtr ldab, DoublePtr bb, IntPtr ldbb, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbgvd_");
  }

  public static void dsbgvx_(CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, IntPtr ka, IntPtr kb, DoublePtr ab, IntPtr ldab, DoublePtr bb, IntPtr ldbb, DoublePtr q, IntPtr ldq, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsbgvx_");
  }

  public static void dspgvd_(IntPtr itype, CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr bp, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspgvd_");
  }

  public static void dspgvx_(IntPtr itype, CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, DoublePtr ap, DoublePtr bp, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dspgvx_");
  }

  public static void dstegr_(CharPtr jobz, CharPtr range, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, IntPtr isuppz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstegr_");
  }

  public static void dstevr_(CharPtr jobz, CharPtr range, IntPtr n, DoublePtr d, DoublePtr e, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, IntPtr isuppz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dstevr_");
  }

  public static void dsygvd_(IntPtr itype, CharPtr jobz, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr w, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsygvd_");
  }

  public static void dsygvx_(IntPtr itype, CharPtr jobz, CharPtr range, CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr vl, DoublePtr vu, IntPtr il, IntPtr iu, DoublePtr abstol, IntPtr m, DoublePtr w, DoublePtr z, IntPtr ldz, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr ifail, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dsygvx_");
  }

  public static void dtgex2_(IntPtr wantq, IntPtr wantz, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr q, IntPtr ldq, DoublePtr z, IntPtr ldz, IntPtr j1, IntPtr n1, IntPtr n2, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgex2_");
  }

  public static void dtgexc_(IntPtr wantq, IntPtr wantz, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr q, IntPtr ldq, DoublePtr z, IntPtr ldz, IntPtr ifst, IntPtr ilst, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgexc_");
  }

  public static void dtgsen_(IntPtr ijob, IntPtr wantq, IntPtr wantz, IntPtr select, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr alphar, DoublePtr alphai, DoublePtr beta, DoublePtr q, IntPtr ldq, DoublePtr z, IntPtr ldz, IntPtr m, DoublePtr pl, DoublePtr pr, DoublePtr dif, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr liwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgsen_");
  }

  public static void dtgsna_(CharPtr job, CharPtr howmny, IntPtr select, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr vl, IntPtr ldvl, DoublePtr vr, IntPtr ldvr, DoublePtr s, DoublePtr dif, IntPtr mm, IntPtr m, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgsna_");
  }

  public static void dtgsy2_(CharPtr trans, IntPtr ijob, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr c, IntPtr ldc, DoublePtr d, IntPtr ldd, DoublePtr e, IntPtr lde, DoublePtr f, IntPtr ldf, DoublePtr scale, DoublePtr rdsum, DoublePtr rdscal, IntPtr iwork, IntPtr pq, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgsy2_");
  }

  public static void dtgsyl_(CharPtr trans, IntPtr ijob, IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr b, IntPtr ldb, DoublePtr c, IntPtr ldc, DoublePtr d, IntPtr ldd, DoublePtr e, IntPtr lde, DoublePtr f, IntPtr ldf, DoublePtr scale, DoublePtr dif, DoublePtr work, IntPtr lwork, IntPtr iwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtgsyl_");
  }

  public static void dtzrzf_(IntPtr m, IntPtr n, DoublePtr a, IntPtr lda, DoublePtr tau, DoublePtr work, IntPtr lwork, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dtzrzf_");
  }

  public static void dpstrf_(CharPtr uplo, IntPtr n, DoublePtr a, IntPtr lda, IntPtr piv, IntPtr rank, DoublePtr tol, DoublePtr work, IntPtr info) {
     throw new UnimplementedGnuApiMethod("dpstrf_");
  }

  public static int lsame_(CharPtr ca, CharPtr cb) {
     throw new UnimplementedGnuApiMethod("lsame_");
  }

  // void F77_NAME() zbdsqr (char *uplo, int *n, int *ncvt, int *nru, int *ncc, double *d, double *e, Rcomplex *vt, int *ldvt, Rcomplex *u, int *ldu, Rcomplex *c, int *ldc, double *rwork, int *info)

  // void F77_NAME() zdrot (int *n, Rcomplex *cx, int *incx, Rcomplex *cy, int *incy, double *c, double *s)

  // void F77_NAME() zgebak (char *job, char *side, int *n, int *ilo, int *ihi, double *scale, int *m, Rcomplex *v, int *ldv, int *info)

  // void F77_NAME() zgebal (char *job, int *n, Rcomplex *a, int *lda, int *ilo, int *ihi, double *scale, int *info)

  // void F77_NAME() zgebd2 (int *m, int *n, Rcomplex *a, int *lda, double *d, double *e, Rcomplex *tauq, Rcomplex *taup, Rcomplex *work, int *info)

  // void F77_NAME() zgebrd (int *m, int *n, Rcomplex *a, int *lda, double *d, double *e, Rcomplex *tauq, Rcomplex *taup, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zgehd2 (int *n, int *ilo, int *ihi, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zgehrd (int *n, int *ilo, int *ihi, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zgelq2 (int *m, int *n, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zgelqf (int *m, int *n, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zgeqr2 (int *m, int *n, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zgeqrf (int *m, int *n, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zgetf2 (int *m, int *n, Rcomplex *a, int *lda, int *ipiv, int *info)

  // void F77_NAME() zgetrf (int *m, int *n, Rcomplex *a, int *lda, int *ipiv, int *info)

  // void F77_NAME() zgetrs (char *trans, int *n, int *nrhs, Rcomplex *a, int *lda, int *ipiv, Rcomplex *b, int *ldb, int *info)

  // void F77_NAME() zhetd2 (char *uplo, int *n, Rcomplex *a, int *lda, double *d, double *e, Rcomplex *tau, int *info)

  // void F77_NAME() zhetrd (char *uplo, int *n, Rcomplex *a, int *lda, double *d, double *e, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zhseqr (char *job, char *compz, int *n, int *ilo, int *ihi, Rcomplex *h, int *ldh, Rcomplex *w, Rcomplex *z, int *ldz, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zlabrd (int *m, int *n, int *nb, Rcomplex *a, int *lda, double *d, double *e, Rcomplex *tauq, Rcomplex *taup, Rcomplex *x, int *ldx, Rcomplex *y, int *ldy)

  // void F77_NAME() zlacgv (int *n, Rcomplex *x, int *incx)

  // void F77_NAME() zlacpy (char *uplo, int *m, int *n, Rcomplex *a, int *lda, Rcomplex *b, int *ldb)

  // void F77_NAME() zlahqr (int *wantt, int *wantz, int *n, int *ilo, int *ihi, Rcomplex *h, int *ldh, Rcomplex *w, int *iloz, int *ihiz, Rcomplex *z, int *ldz, int *info)

  // void F77_NAME() zlahrd (int *n, int *k, int *nb, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *t, int *ldt, Rcomplex *y, int *ldy)

  // double F77_NAME() zlange (char *norm, int *m, int *n, Rcomplex *a, int *lda, double *work)

  // double F77_NAME() zlanhe (char *norm, char *uplo, int *n, Rcomplex *a, int *lda, double *work)

  // double F77_NAME() zlanhs (char *norm, int *n, Rcomplex *a, int *lda, double *work)

  // void F77_NAME() zlaqp2 (int *m, int *n, int *offset, Rcomplex *a, int *lda, int *jpvt, Rcomplex *tau, double *vn1, double *vn2, Rcomplex *work)

  // void F77_NAME() zlaqps (int *m, int *n, int *offset, int *nb, int *kb, Rcomplex *a, int *lda, int *jpvt, Rcomplex *tau, double *vn1, double *vn2, Rcomplex *auxv, Rcomplex *f, int *ldf)

  // void F77_NAME() zlarf (char *side, int *m, int *n, Rcomplex *v, int *incv, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work)

  // void F77_NAME() zlarfb (char *side, char *trans, char *direct, char *storev, int *m, int *n, int *k, Rcomplex *v, int *ldv, Rcomplex *t, int *ldt, Rcomplex *c, int *ldc, Rcomplex *work, int *ldwork)

  // void F77_NAME() zlarfg (int *n, Rcomplex *alpha, Rcomplex *x, int *incx, Rcomplex *tau)

  // void F77_NAME() zlarft (char *direct, char *storev, int *n, int *k, Rcomplex *v, int *ldv, Rcomplex *tau, Rcomplex *t, int *ldt)

  // void F77_NAME() zlarfx (char *side, int *m, int *n, Rcomplex *v, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work)

  // void F77_NAME() zlascl (char *type, int *kl, int *ku, double *cfrom, double *cto, int *m, int *n, Rcomplex *a, int *lda, int *info)

  // void F77_NAME() zlaset (char *uplo, int *m, int *n, Rcomplex *alpha, Rcomplex *beta, Rcomplex *a, int *lda)

  // void F77_NAME() zlasr (char *side, char *pivot, char *direct, int *m, int *n, double *c, double *s, Rcomplex *a, int *lda)

  // void F77_NAME() zlassq (int *n, Rcomplex *x, int *incx, double *scale, double *sumsq)

  // void F77_NAME() zlaswp (int *n, Rcomplex *a, int *lda, int *k1, int *k2, int *ipiv, int *incx)

  // void F77_NAME() zlatrd (char *uplo, int *n, int *nb, Rcomplex *a, int *lda, double *e, Rcomplex *tau, Rcomplex *w, int *ldw)

  // void F77_NAME() zlatrs (char *uplo, char *trans, char *diag, char *normin, int *n, Rcomplex *a, int *lda, Rcomplex *x, double *scale, double *cnorm, int *info)

  // void F77_NAME() zsteqr (char *compz, int *n, double *d, double *e, Rcomplex *z, int *ldz, double *work, int *info)

  // void F77_NAME() ztrcon (const char *norm, const char *uplo, const char *diag, const int *n, const Rcomplex *a, const int *lda, double *rcond, Rcomplex *work, double *rwork, int *info)

  // void F77_NAME() ztrevc (char *side, char *howmny, int *select, int *n, Rcomplex *t, int *ldt, Rcomplex *vl, int *ldvl, Rcomplex *vr, int *ldvr, int *mm, int *m, Rcomplex *work, double *rwork, int *info)

  // void F77_NAME() zung2l (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zung2r (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zungbr (char *vect, int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zunghr (int *n, int *ilo, int *ihi, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zungl2 (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zunglq (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zungql (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zungqr (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zungr2 (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *info)

  // void F77_NAME() zungrq (int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zungtr (char *uplo, int *n, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zunm2r (char *side, char *trans, int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work, int *info)

  // void F77_NAME() zunmbr (char *vect, char *side, char *trans, int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zunml2 (char *side, char *trans, int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work, int *info)

  // void F77_NAME() zunmlq (char *side, char *trans, int *m, int *n, int *k, Rcomplex *a, int *lda, Rcomplex *tau, Rcomplex *c, int *ldc, Rcomplex *work, int *lwork, int *info)

  // void F77_NAME() zgesdd (const char *jobz, const int *m, const int *n, Rcomplex *a, const int *lda, double *s, Rcomplex *u, const int *ldu, Rcomplex *vt, const int *ldvt, Rcomplex *work, const int *lwork, double *rwork, int *iwork, int *info)

  // void F77_NAME() zgelsd (int *m, int *n, int *nrhs, Rcomplex *a, int *lda, Rcomplex *b, int *ldb, double *s, double *rcond, int *rank, Rcomplex *work, int *lwork, double *rwork, int *iwork, int *info)
}
