#include "ldense.h"

/* dense logical Matrices "ldenseMatrix" classes --- almost identical to
 * dense nonzero-pattern: "ndenseMatrix" ones
 */

/* this is very close to dspMatrix_as_dsy* () in ./dspMatrix.c : */
SEXP lspMatrix_as_lsyMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "nsyMatrix" :
					     "lsyMatrix"))),
	uplo = GET_SLOT(from, Matrix_uploSym),
	dimP = GET_SLOT(from, Matrix_DimSym),
	dmnP = GET_SLOT(from, Matrix_DimNamesSym);
    int n = *INTEGER(dimP);

    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    SET_SLOT(val, Matrix_DimNamesSym, duplicate(dmnP));
    SET_SLOT(val, Matrix_uploSym, duplicate(uplo));
    packed_to_full_int(LOGICAL(ALLOC_SLOT(val, Matrix_xSym, LGLSXP, n*n)),
		       LOGICAL( GET_SLOT(from, Matrix_xSym)), n,
		       *CHAR(STRING_ELT(uplo, 0)) == 'U' ? UPP : LOW);
    UNPROTECT(1);
    return val;
}

// this is very close to dsyMatrix_as_lsp*() in ./dsyMatrix.c  -- keep synced !
SEXP lsyMatrix_as_lspMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "nspMatrix" :
					     "lspMatrix"))),
	uplo = GET_SLOT(from, Matrix_uploSym),
	dimP = GET_SLOT(from, Matrix_DimSym);
    int n = *INTEGER(dimP);

    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    SET_SLOT(val, Matrix_uploSym, duplicate(uplo));
    full_to_packed_int(
	LOGICAL(ALLOC_SLOT(val, Matrix_xSym, LGLSXP, (n*(n+1))/2)),
	LOGICAL( GET_SLOT(from, Matrix_xSym)), n,
	*CHAR(STRING_ELT(uplo, 0)) == 'U' ? UPP : LOW, NUN);
    SET_SLOT(val, Matrix_DimNamesSym,
	     duplicate(GET_SLOT(from, Matrix_DimNamesSym)));
    SET_SLOT(val, Matrix_factorSym,
	     duplicate(GET_SLOT(from, Matrix_factorSym)));
    UNPROTECT(1);
    return val;
}

// this is very close to dtpMatrix_as_dtr*() in ./dtpMatrix.c -- keep synced!
SEXP ltpMatrix_as_ltrMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "ntrMatrix" :
					     "ltrMatrix"))),
	uplo = GET_SLOT(from, Matrix_uploSym),
	diag = GET_SLOT(from, Matrix_diagSym),
	dimP = GET_SLOT(from, Matrix_DimSym),
	dmnP = GET_SLOT(from, Matrix_DimNamesSym);
    int n = *INTEGER(dimP);

    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    SET_SLOT(val, Matrix_DimNamesSym, duplicate(dmnP));
    SET_SLOT(val, Matrix_diagSym, duplicate(diag));
    SET_SLOT(val, Matrix_uploSym, duplicate(uplo));
    packed_to_full_int(LOGICAL(ALLOC_SLOT(val, Matrix_xSym, LGLSXP, n*n)),
		       LOGICAL(GET_SLOT(from, Matrix_xSym)), n,
		       *CHAR(STRING_ELT(uplo, 0)) == 'U' ? UPP : LOW);
    SET_SLOT(val, Matrix_DimNamesSym,
	     duplicate(GET_SLOT(from, Matrix_DimNamesSym)));
    UNPROTECT(1);
    return val;
}

/* this is very close to dtrMatrix_as_dtp* () in ./dtrMatrix.c : */
SEXP ltrMatrix_as_ltpMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "ntpMatrix" :
					     "ltpMatrix"))),
	uplo = GET_SLOT(from, Matrix_uploSym),
	diag = GET_SLOT(from, Matrix_diagSym),
	dimP = GET_SLOT(from, Matrix_DimSym);
    int n = *INTEGER(dimP);

    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    SET_SLOT(val, Matrix_diagSym, duplicate(diag));
    SET_SLOT(val, Matrix_uploSym, duplicate(uplo));
    full_to_packed_int(
	LOGICAL(ALLOC_SLOT(val, Matrix_xSym, LGLSXP, (n*(n+1))/2)),
	LOGICAL(GET_SLOT(from, Matrix_xSym)), n,
	*CHAR(STRING_ELT(uplo, 0)) == 'U' ? UPP : LOW,
	*CHAR(STRING_ELT(diag, 0)) == 'U' ? UNT : NUN);
    SET_SLOT(val, Matrix_DimNamesSym,
	     duplicate(GET_SLOT(from, Matrix_DimNamesSym)));
    UNPROTECT(1);
    return val;
}

/* this is very close to dtrMatrix_as_dge*() :*/
SEXP ltrMatrix_as_lgeMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "ngeMatrix" :
					     "lgeMatrix")));

    slot_dup(val, from, Matrix_xSym);
    slot_dup(val, from, Matrix_DimSym);
    slot_dup(val, from, Matrix_DimNamesSym);
    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));

    make_i_matrix_triangular(LOGICAL(GET_SLOT(val, Matrix_xSym)), from);
    UNPROTECT(1);
    return val;
}

/* this is very close to dsyMatrix_as_dge*() :*/
SEXP lsyMatrix_as_lgeMatrix(SEXP from, SEXP kind)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS((asInteger(kind) == 1) ?
					     "ngeMatrix" :
					     "lgeMatrix")));

    slot_dup(val, from, Matrix_xSym);
    slot_dup(val, from, Matrix_DimSym);
    slot_dup(val, from, Matrix_DimNamesSym);
    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));

    make_i_matrix_symmetric(LOGICAL(GET_SLOT(val, Matrix_xSym)), from);
    UNPROTECT(1);
    return val;
}

