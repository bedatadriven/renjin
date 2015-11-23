#include "dppMatrix.h"

SEXP dppMatrix_validate(SEXP obj)
{
/*     int i, n = INTEGER(GET_SLOT(obj, Matrix_DimSym))[0]; */
/*     double *x = REAL(GET_SLOT(obj, Matrix_xSym)); */

    /* quick but nondefinitive check on positive definiteness */
/*     for (i = 0; i < n; i++) */
/* 	if (x[i * np1] < 0) */
/* 	    return mkString(_("dppMatrix is not positive definite")); */
    return dspMatrix_validate(obj);
}

SEXP dppMatrix_chol(SEXP x)
{
    SEXP val = get_factors(x, "pCholesky"),
	dimP = GET_SLOT(x, Matrix_DimSym),
	uploP = GET_SLOT(x, Matrix_uploSym);
    const char *uplo = CHAR(STRING_ELT(uploP, 0));
    int *dims = INTEGER(dimP), info;

    if (val != R_NilValue) return val;
    dims = INTEGER(dimP);
    val = PROTECT(NEW_OBJECT(MAKE_CLASS("pCholesky")));
    SET_SLOT(val, Matrix_uploSym, duplicate(uploP));
    SET_SLOT(val, Matrix_diagSym, mkString("N"));
    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    slot_dup(val, x, Matrix_xSym);
    F77_CALL(dpptrf)(uplo, dims, REAL(GET_SLOT(val, Matrix_xSym)), &info);
    if (info) {
	if(info > 0) /* e.g. x singular */
	    error(_("the leading minor of order %d is not positive definite"),
		    info);
	else /* should never happen! */
	    error(_("Lapack routine %s returned error code %d"), "dpptrf", info);
    }
    UNPROTECT(1);
    return set_factors(x, val, "pCholesky");
}

SEXP dppMatrix_rcond(SEXP obj, SEXP type)
{
    SEXP Chol = dppMatrix_chol(obj);
    char typnm[] = {'O', '\0'};	/* always use the one norm */
    int *dims = INTEGER(GET_SLOT(Chol, Matrix_DimSym)), info;
    double anorm = get_norm_sp(obj, typnm), rcond;

    F77_CALL(dppcon)(uplo_P(Chol), dims,
		     REAL(GET_SLOT(Chol, Matrix_xSym)), &anorm, &rcond,
		     (double *) R_alloc(3*dims[0], sizeof(double)),
		     (int *) R_alloc(dims[0], sizeof(int)), &info);
    return ScalarReal(rcond);
}

SEXP dppMatrix_solve(SEXP x)
{
    SEXP Chol = dppMatrix_chol(x);
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dppMatrix")));
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym)), info;

    slot_dup(val, Chol, Matrix_uploSym);
    slot_dup(val, Chol, Matrix_xSym);
    slot_dup(val, Chol, Matrix_DimSym);
    F77_CALL(dpptri)(uplo_P(val), dims,
		     REAL(GET_SLOT(val, Matrix_xSym)), &info);
    UNPROTECT(1);
    return val;
}

SEXP dppMatrix_matrix_solve(SEXP a, SEXP b)
{
    SEXP val = PROTECT(dup_mMatrix_as_dgeMatrix(b));
    SEXP Chol = dppMatrix_chol(a);
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(GET_SLOT(val, Matrix_DimSym));
    int n = bdims[0], nrhs = bdims[1], info;

    if (*adims != *bdims || bdims[1] < 1 || *adims < 1)
	error(_("Dimensions of system to be solved are inconsistent"));
    F77_CALL(dpptrs)(uplo_P(Chol), &n, &nrhs,
		     REAL(GET_SLOT(Chol, Matrix_xSym)),
		     REAL(GET_SLOT(val, Matrix_xSym)), &n, &info);
    UNPROTECT(1);
    return val;
}
