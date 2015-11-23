				/* Sparse triangular numeric matrices */
#include "dtCMatrix.h"
#include "cs_utils.h"

#define RETURN(_CH_)   UNPROTECT(1); return (_CH_);

/* This is used for *BOTH* triangular and symmetric Csparse: */
SEXP tCMatrix_validate(SEXP x)
{
    SEXP val = xCMatrix_validate(x);/* checks x slot */
    if(isString(val))
	return(val);
    else {
	SEXP
	    islot = GET_SLOT(x, Matrix_iSym),
	    pslot = GET_SLOT(x, Matrix_pSym);
	int uploT = (*uplo_P(x) == 'U'),
	    k, nnz = length(islot),
	    *xi = INTEGER(islot),
	    *xj = INTEGER(PROTECT(allocVector(INTSXP, nnz)));

	expand_cmprPt(length(pslot) - 1, INTEGER(pslot), xj);

	/* Maybe FIXME: ">" should be ">="	for diag = 'U' (uplo = 'U') */
	if(uploT) {
	    for (k = 0; k < nnz; k++)
		if(xi[k] > xj[k]) {
		    RETURN(mkString(_("uplo='U' must not have sparse entries below the diagonal")));
		}
	}
	else {
	    for (k = 0; k < nnz; k++)
		if(xi[k] < xj[k]) {
		    RETURN(mkString(_("uplo='L' must not have sparse entries above the diagonal")));
		}
	}

	RETURN(ScalarLogical(1));
    }
}

/* This is used for *BOTH* triangular and symmetric Rsparse: */
SEXP tRMatrix_validate(SEXP x)
{
    SEXP val = xRMatrix_validate(x);/* checks x slot */
    if(isString(val))
	return(val);
    else {
	SEXP
	    jslot = GET_SLOT(x, Matrix_jSym),
	    pslot = GET_SLOT(x, Matrix_pSym);
	int uploT = (*uplo_P(x) == 'U'),
	    k, nnz = length(jslot),
	    *xj = INTEGER(jslot),
	    *xi = INTEGER(PROTECT(allocVector(INTSXP, nnz)));

	expand_cmprPt(length(pslot) - 1, INTEGER(pslot), xi);

	/* Maybe FIXME: ">" should be ">="	for diag = 'U' (uplo = 'U') */
	if(uploT) {
	    for (k = 0; k < nnz; k++)
		if(xi[k] > xj[k]) {
		    RETURN(mkString(_("uplo='U' must not have sparse entries below the diagonal")));
		}
	}
	else {
	    for (k = 0; k < nnz; k++)
		if(xi[k] < xj[k]) {
		    RETURN(mkString(_("uplo='L' must not have sparse entries above the diagonal")));
		}
	}

	RETURN(ScalarLogical(1));
    }
}

SEXP dtCMatrix_matrix_solve(SEXP a, SEXP b, SEXP classed)
{
    int cl = asLogical(classed);
    SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix")));
    CSP A = AS_CSP(a);
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(cl ? GET_SLOT(b, Matrix_DimSym) :
			 getAttrib(b, R_DimSymbol));
    int j, n = bdims[0], nrhs = bdims[1], lo = (*uplo_P(a) == 'L');
    double *bx;
    R_CheckStack();

    if (adims[0] != n || n != adims[1])
	error(_("Dimensions of system to be solved are inconsistent"));
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2)), bdims, 2);
    // dimnames:
    SEXP dn = PROTECT(allocVector(VECSXP, 2)), dn2;
    SET_VECTOR_ELT(dn, 0, duplicate(VECTOR_ELT(GET_SLOT(a, Matrix_DimNamesSym), 1)));
    if(!cl) {
	dn2 = getAttrib(b, R_DimNamesSymbol);
	if(dn2 != R_NilValue) // either NULL or  list(<dn1>, <dn2>)
	    dn2 = VECTOR_ELT(dn2, 1);
    }
    SET_VECTOR_ELT(dn, 1, duplicate(cl // b can be "Matrix" or not:
				    ? VECTOR_ELT(GET_SLOT(b, Matrix_DimNamesSym), 1)
				    : dn2));
    SET_SLOT(ans, Matrix_DimNamesSym, dn);
    UNPROTECT(1);
    if(n >= 1 && nrhs >=1) {
	bx = Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, n * nrhs)),
		    REAL(cl ? GET_SLOT(b, Matrix_xSym):b), n * nrhs);
	for (j = 0; j < nrhs; j++)
	    lo ? cs_lsolve(A, bx + n * j) : cs_usolve(A, bx + n * j);
    }
    RETURN(ans);
}

SEXP dtCMatrix_sparse_solve(SEXP a, SEXP b)
{
    SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS("dgCMatrix")));
    CSP A = AS_CSP(a), B = AS_CSP(b);
    R_CheckStack();
    if (A->m != A->n || B->n < 1 || A->n < 1 || A->n != B->m)
	error(_("Dimensions of system to be solved are inconsistent"));
    // *before* Calloc()ing below [memory leak]! -- FIXME: 0-extent should work

    int *xp = INTEGER(ALLOC_SLOT(ans, Matrix_pSym, INTSXP, (B->n) + 1)),
	xnz = 10 * B->p[B->n];	/* initial estimate of nnz in x */
    int k, lo = uplo_P(a)[0] == 'L', pos = 0;
    int    *ti = Calloc(xnz, int),     *xi = Calloc(2*A->n, int); /* for cs_reach */
    double *tx = Calloc(xnz, double), *wrk = Calloc(  A->n, double);

    slot_dup(ans, b, Matrix_DimSym);

    xp[0] = 0;
    for (k = 0; k < B->n; k++) {
	int top = cs_spsolve (A, B, k, xi, wrk, (int *)NULL, lo);
	int nz = A->n - top;

	xp[k + 1] = nz + xp[k];
	if (xp[k + 1] > xnz) {
	    while (xp[k + 1] > xnz) xnz *= 2;
	    ti = Realloc(ti, xnz, int);
	    tx = Realloc(tx, xnz, double);
	}
	if (lo)			/* increasing row order */
	    for(int p = top; p < A->n; p++, pos++) {
		ti[pos] = xi[p];
		tx[pos] = wrk[xi[p]];
	    }
	else			/* decreasing order, reverse copy */
	    for(int p = A->n - 1; p >= top; p--, pos++) {
		ti[pos] = xi[p];
		tx[pos] = wrk[xi[p]];
	    }
    }
    xnz = xp[B->n];
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP,  xnz)), ti, xnz);
    Memcpy(   REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, xnz)), tx, xnz);

    Free(ti);  Free(tx);
    Free(wrk); Free(xi);

    // dimnames:
    SEXP dn = PROTECT(allocVector(VECSXP, 2));
    SET_VECTOR_ELT(dn, 0, duplicate(VECTOR_ELT(GET_SLOT(a, Matrix_DimNamesSym), 1)));
    SET_VECTOR_ELT(dn, 1, duplicate(VECTOR_ELT(GET_SLOT(b, Matrix_DimNamesSym), 1)));
    SET_SLOT(ans, Matrix_DimNamesSym, dn);
    UNPROTECT(1);

    RETURN(ans);
}
#undef RETURN

