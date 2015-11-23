#include "lgCMatrix.h"

#include "dgCMatrix.h"
/* validate: -> xCMatrix_validate() in ./dgCMatrix.c */

SEXP lgC_to_matrix(SEXP x)
{
    SEXP ans, pslot = GET_SLOT(x, Matrix_pSym),
	dn = GET_SLOT(x, Matrix_DimNamesSym);
    int j, ncol = length(pslot) - 1,
	nrow = INTEGER(GET_SLOT(x, Matrix_DimSym))[0],
	*xp = INTEGER(pslot),
	*xi = INTEGER(GET_SLOT(x, Matrix_iSym));
    int *xx = LOGICAL(GET_SLOT(x, Matrix_xSym)), *ax;

    ax = LOGICAL(ans = PROTECT(allocMatrix(LGLSXP, nrow, ncol)));
    for (j = 0; j < (nrow * ncol); j++) ax[j] = 0;
    for (j = 0; j < ncol; j++) {
	int ind;
	for (ind = xp[j]; ind < xp[j+1]; ind++)
	    ax[j * nrow + xi[ind]] = xx[ind];
    }
    if (!(isNull(VECTOR_ELT(dn,0)) && isNull(VECTOR_ELT(dn,1))))
	setAttrib(ans, R_DimNamesSymbol, duplicate(dn));
    UNPROTECT(1);
    return ans;
}

/* as above,  '1' instead of 'x' slot: */
SEXP ngC_to_matrix(SEXP x)
{
    SEXP ans, pslot = GET_SLOT(x, Matrix_pSym),
	dn = GET_SLOT(x, Matrix_DimNamesSym);
    int j, ncol = length(pslot) - 1,
	nrow = INTEGER(GET_SLOT(x, Matrix_DimSym))[0],
	*xp = INTEGER(pslot),
	*xi = INTEGER(GET_SLOT(x, Matrix_iSym));
    int *ax;

    ax = LOGICAL(ans = PROTECT(allocMatrix(LGLSXP, nrow, ncol)));
    for (j = 0; j < (nrow * ncol); j++) ax[j] = 0;
    for (j = 0; j < ncol; j++) {
	int ind;
	for (ind = xp[j]; ind < xp[j+1]; ind++)
	    ax[j * nrow + xi[ind]] = 1;
    }
    if (!(isNull(VECTOR_ELT(dn,0)) && isNull(VECTOR_ELT(dn,1))))
	setAttrib(ans, R_DimNamesSymbol, duplicate(dn));
    UNPROTECT(1);
    return ans;
}

#ifdef _NEED_logical_to_csc_FIRST_
/* very parallel to matrix_to_csc() in ./dgCMatrix.c */
SEXP matrix_to_lcsc(SEXP A)
{
    if (!(isMatrix(A) && isLogical(A)))
	error(_("A must be a logical matrix"));
    return logical_to_csc(LOGICAL(A),
			  INTEGER(getAttrib(A, R_DimSymbol)));
}
#endif
