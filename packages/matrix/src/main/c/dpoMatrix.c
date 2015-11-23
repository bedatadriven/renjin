#include "dpoMatrix.h"

SEXP dpoMatrix_validate(SEXP obj)
{
    SEXP val;
    if (isString(val = dense_nonpacked_validate(obj)))
	return(val);

    int n = INTEGER(GET_SLOT(obj, Matrix_DimSym))[0],
	np1 = n + 1;
    double *x = REAL(GET_SLOT(obj, Matrix_xSym));

    /* quick but nondefinitive check on positive definiteness */
    for (int i = 0; i < n; i++)
	if (x[i * np1] < 0)
	    return mkString(_("dpoMatrix is not positive definite"));
    return ScalarLogical(1);
}

SEXP dpoMatrix_chol(SEXP x)
{
    SEXP val = get_factors(x, "Cholesky"),
	dimP = GET_SLOT(x, Matrix_DimSym),
	uploP = GET_SLOT(x, Matrix_uploSym);
    const char *uplo = CHAR(STRING_ELT(uploP, 0));
    int *dims = INTEGER(dimP), info;
    int n = dims[0];
    double *vx;

    if (val != R_NilValue) return val;// use x@factors$Cholesky if available
    dims = INTEGER(dimP);
    val = PROTECT(NEW_OBJECT(MAKE_CLASS("Cholesky")));
    SET_SLOT(val, Matrix_uploSym, duplicate(uploP));
    SET_SLOT(val, Matrix_diagSym, mkString("N"));
    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    vx = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, n * n));
    AZERO(vx, n * n);
    F77_CALL(dlacpy)(uplo, &n, &n, REAL(GET_SLOT(x, Matrix_xSym)), &n, vx, &n);
    if (n > 0) {
	F77_CALL(dpotrf)(uplo, &n, vx, &n, &info);
	if (info) {
	    if(info > 0)
		error(_("the leading minor of order %d is not positive definite"),
		      info);
	    else /* should never happen! */
		error(_("Lapack routine %s returned error code %d"), "dpotrf", info);
	}
    }
    UNPROTECT(1);
    return set_factors(x, val, "Cholesky");
}

SEXP dpoMatrix_rcond(SEXP obj, SEXP type)
{
    SEXP Chol = dpoMatrix_chol(obj);
    const char typnm[] = {'O', '\0'};	/* always use the one norm */
    int *dims = INTEGER(GET_SLOT(Chol, Matrix_DimSym)), info;
    double anorm = get_norm_sy(obj, typnm), rcond;

    F77_CALL(dpocon)(uplo_P(Chol),
		     dims, REAL(GET_SLOT(Chol, Matrix_xSym)),
		     dims, &anorm, &rcond,
		     (double *) R_alloc(3*dims[0], sizeof(double)),
		     (int *) R_alloc(dims[0], sizeof(int)), &info);
    return ScalarReal(rcond);
}

SEXP dpoMatrix_solve(SEXP x)
{
    SEXP Chol = dpoMatrix_chol(x);
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dpoMatrix")));
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym)), info;

    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));
    slot_dup(val, Chol, Matrix_uploSym);
    slot_dup(val, Chol, Matrix_xSym);
    slot_dup(val, Chol, Matrix_DimSym);
    SET_SLOT(val, Matrix_DimNamesSym,
	     duplicate(GET_SLOT(x, Matrix_DimNamesSym)));
    F77_CALL(dpotri)(uplo_P(val), dims,
		     REAL(GET_SLOT(val, Matrix_xSym)), dims, &info);
    UNPROTECT(1);
    return val;
}

SEXP dpoMatrix_dgeMatrix_solve(SEXP a, SEXP b)
{
    SEXP Chol = dpoMatrix_chol(a),
	val = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix")));
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(GET_SLOT(b, Matrix_DimSym)),
	info;

    if (adims[1] != bdims[0])
	error(_("Dimensions of system to be solved are inconsistent"));
    if (adims[0] < 1 || bdims[1] < 1)
	error(_("Cannot solve() for matrices with zero extents"));
    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));
    slot_dup(val, b, Matrix_DimSym);
    slot_dup(val, b, Matrix_xSym);
    F77_CALL(dpotrs)(uplo_P(Chol), adims, bdims + 1,
		     REAL(GET_SLOT(Chol, Matrix_xSym)), adims,
		     REAL(GET_SLOT(val, Matrix_xSym)),
		     bdims, &info);
    UNPROTECT(1);
    return val;
}

SEXP dpoMatrix_matrix_solve(SEXP a, SEXP b)
{
    SEXP Chol = dpoMatrix_chol(a),
	val = PROTECT(duplicate(b));
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(getAttrib(b, R_DimSymbol)),
	info;

    if (!(isReal(b) && isMatrix(b)))
	error(_("Argument b must be a numeric matrix"));
    if (*adims != *bdims || bdims[1] < 1 || *adims < 1)
	error(_("Dimensions of system to be solved are inconsistent"));
    F77_CALL(dpotrs)(uplo_P(Chol), adims, bdims + 1,
		     REAL(GET_SLOT(Chol, Matrix_xSym)), adims,
		     REAL(val), bdims, &info);
    UNPROTECT(1);
    return val;
}
