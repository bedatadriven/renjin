				/* Sparse matrices in triplet form */
#include "Tsparse.h"
#include "chm_common.h"

SEXP Tsparse_validate(SEXP x)
{
    /* NB: we do *NOT* check a potential 'x' slot here, at all */
    SEXP
	islot = GET_SLOT(x, Matrix_iSym),
	jslot = GET_SLOT(x, Matrix_jSym),
	dimslot = GET_SLOT(x, Matrix_DimSym);
    int j,
	nrow = INTEGER(dimslot)[0],
	ncol = INTEGER(dimslot)[1],
	nnz = length(islot),
	*xj = INTEGER(jslot),
	*xi = INTEGER(islot);

    if (length(jslot) != nnz)
	return mkString(_("lengths of slots i and j must match"));
    /* FIXME: this is checked in super class -- no need to do here: */
    if (length(dimslot) != 2)
	return mkString(_("slot Dim must have length 2"));

    for (j = 0; j < nnz; j++) {
	if (xi[j] < 0 || xi[j] >= nrow)
	    return mkString(_("all row indices (slot 'i') must be between 0 and nrow-1 in a TsparseMatrix"));
	if (xj[j] < 0 || xj[j] >= ncol)
	    return mkString(_("all column indices (slot 'j') must be between 0 and ncol-1 in a TsparseMatrix"));
    }
    return ScalarLogical(1);
}

SEXP Tsparse_to_Csparse(SEXP x, SEXP tri)
{
    CHM_TR chxt = AS_CHM_TR__(x); /* << should *preserve*  diag = "U" ! */
    CHM_SP chxs = cholmod_triplet_to_sparse(chxt, chxt->nnz, &c);
    int tr = asLogical(tri);
    int Rkind = (chxt->xtype != CHOLMOD_PATTERN) ? Real_kind(x) : 0;
    R_CheckStack();

    return chm_sparse_to_SEXP(chxs, 1,
			      tr ? ((*uplo_P(x) == 'U') ? 1 : -1) : 0,
			      Rkind, tr ? diag_P(x) : "",
			      GET_SLOT(x, Matrix_DimNamesSym));
}

/* speedup utility, needed e.g. after subsetting: */
SEXP Tsparse_to_tCsparse(SEXP x, SEXP uplo, SEXP diag)
{
    CHM_TR chxt = AS_CHM_TR__(x);
    CHM_SP chxs = cholmod_triplet_to_sparse(chxt, chxt->nnz, &c);
    int Rkind = (chxt->xtype != CHOLMOD_PATTERN) ? Real_kind(x) : 0;
    R_CheckStack();

    return chm_sparse_to_SEXP(chxs, 1,
			      /* uploT = */ (*CHAR(asChar(uplo)) == 'U')? 1: -1,
			      Rkind,
			      /* diag = */ CHAR(STRING_ELT(diag, 0)),
			      GET_SLOT(x, Matrix_DimNamesSym));
}

SEXP Tsparse_diagU2N(SEXP x)
{
    static const char *valid[] = {
	"dtTMatrix", /* 0 */
	"ltTMatrix", /* 1 */
	"ntTMatrix", /* 2 : no x slot */
	"ztTMatrix", /* 3 */
	""};
/* #define xSXP(iTyp) ((iTyp == 0) ? REALSXP : ((iTyp == 1) ? LGLSXP : /\* else *\/ CPLXSXP)); */
/* #define xTYPE(iTyp) ((iTyp == 0) ? double : ((iTyp == 1) ? int : /\* else *\/ Rcomplex)); */
    int ctype = Matrix_check_class_etc(x, valid);

    if (ctype < 0 || *diag_P(x) != 'U') {
	/* "trivially fast" when not triangular (<==> no 'diag' slot),
	   or not *unit* triangular */
	return (x);
    }
    else { /* instead of going to Csparse -> Cholmod -> Csparse -> Tsparse, work directly: */
	int i, n = INTEGER(GET_SLOT(x, Matrix_DimSym))[0],
	    nnz = length(GET_SLOT(x, Matrix_iSym)),
	    new_n = nnz + n;
	SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS(class_P(x))));
	int *islot = INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP, new_n)),
	    *jslot = INTEGER(ALLOC_SLOT(ans, Matrix_jSym, INTSXP, new_n));

	slot_dup(ans, x, Matrix_DimSym);
	SET_DimNames(ans, x);
	slot_dup(ans, x, Matrix_uploSym);
	SET_SLOT(ans, Matrix_diagSym, mkString("N"));

	/* Build the new i- and j- slots : first copy the current : */
	Memcpy(islot, INTEGER(GET_SLOT(x, Matrix_iSym)), nnz);
	Memcpy(jslot, INTEGER(GET_SLOT(x, Matrix_jSym)), nnz);
	/* then, add the new (i,j) slot entries: */
	for(i = 0; i < n; i++) {
	    islot[i + nnz] = i;
	    jslot[i + nnz] = i;
	}

	/* build the new x-slot : */
	switch(ctype) {
	case 0: { /* "d" */
	    double *x_new = REAL(ALLOC_SLOT(ans, Matrix_xSym,
					    REALSXP, new_n));
	    Memcpy(x_new, REAL(GET_SLOT(x, Matrix_xSym)), nnz);
	    for(i = 0; i < n; i++) /* add  x[i,i] = 1. */
		x_new[i + nnz] = 1.;
	    break;
	}
	case 1: { /* "l" */
	    int *x_new = LOGICAL(ALLOC_SLOT(ans, Matrix_xSym,
					    LGLSXP, new_n));
	    Memcpy(x_new, LOGICAL(GET_SLOT(x, Matrix_xSym)), nnz);
	    for(i = 0; i < n; i++) /* add  x[i,i] = 1 (= TRUE) */
		x_new[i + nnz] = 1;
	    break;
	}
	case 2: /* "n" */
		/* nothing to do here */
	    break;

	case 3: { /* "z" */
	    Rcomplex *x_new = COMPLEX(ALLOC_SLOT(ans, Matrix_xSym,
						 CPLXSXP, new_n));
	    Memcpy(x_new, COMPLEX(GET_SLOT(x, Matrix_xSym)), nnz);
	    for(i = 0; i < n; i++) /* add  x[i,i] = 1 (= TRUE) */
		x_new[i + nnz] = (Rcomplex) {1., 0.};
	    break;
	}

	}/* switch() */

	UNPROTECT(1);
	return ans;
    }
}
