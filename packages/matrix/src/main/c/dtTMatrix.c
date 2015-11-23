			/* Sparse triangular matrices in triplet format */
#include "dtTMatrix.h"
#include "dgTMatrix.h" /* xTMatrix_validate */

/* This should be use for *BOTH* triangular and symmetric Tsparse: */
SEXP tTMatrix_validate(SEXP x)
{
    SEXP val = xTMatrix_validate(x);/* checks x slot */
    if(isString(val))
	return(val);
    else {
	SEXP
	    islot = GET_SLOT(x, Matrix_iSym),
	    jslot = GET_SLOT(x, Matrix_jSym);
	int uploT = (*uplo_P(x) == 'U'),
	    k, nnz = length(islot),
	    *xj = INTEGER(jslot),
	    *xi = INTEGER(islot);

	/* Maybe FIXME: ">" should be ">="	for diag = 'U' (uplo = 'U') */
	if(uploT) {
	    for (k = 0; k < nnz; k++)
		if(xi[k] > xj[k])
		    return mkString(_("uplo='U' must not have sparse entries below the diagonal"));
	}
	else {
	    for (k = 0; k < nnz; k++)
		if(xi[k] < xj[k])
		    return mkString(_("uplo='L' must not have sparse entries above the diagonal"));
	}

	return ScalarLogical(1);
    }
}

/* SEXP dtTMatrix_as_dtrMatrix(SEXP x) ---> now in ./TMatrix_as.c */
