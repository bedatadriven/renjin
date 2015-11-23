#include <Rinternals.h>
/* for R_LEN... */

#include "dgTMatrix.h"

#include "chm_common.h"
#include "Tsparse.h"

SEXP xTMatrix_validate(SEXP x)
{
    /* Almost everything now in Tsparse_validate ( ./Tsparse.c )
     * *but* the checking of the 'x' slot : */
    if (LENGTH(GET_SLOT(x, Matrix_iSym)) !=
	LENGTH(GET_SLOT(x, Matrix_xSym)))
	return mkString(_("lengths of slots i and x must match"));
    return ScalarLogical(1);
}

static void
d_insert_triplets_in_array(int m, int n, int nnz,
			   const int xi[], const int xj[], const double xx[],
			   /* --> */ double vx[])
{
    int i;
    memset(vx, 0, sizeof(double) * m * n);
    for (i = 0; i < nnz; i++) {
	vx[xi[i] + xj[i] * m] += xx[i];	/* allow redundant entries in x */
    }
}

static void
l_insert_triplets_in_array(int m, int n, int nnz,
			   const int xi[], const int xj[], const int xx[],
			   /* --> */ int vx[])
{
    int i;
    memset(vx, 0, sizeof(int) * m * n);
    for (i = 0; i < nnz; i++) {
	int ind = xi[i] + xj[i] * m;
	if(vx[ind] == NA_LOGICAL) {
	    // do nothing: remains NA
	} else if(xx[i] == NA_LOGICAL)
	    vx[ind] = NA_LOGICAL;
	else // "or" :
	    vx[ind] |= xx[i];
    }
}

#define MAKE_gTMatrix_to_geMatrix(_t1_, _SEXPTYPE_, _SEXP_)		\
SEXP _t1_ ## gTMatrix_to_ ## _t1_ ## geMatrix(SEXP x)			\
{									\
    SEXP dd = GET_SLOT(x, Matrix_DimSym),				\
	islot = GET_SLOT(x, Matrix_iSym),				\
	ans = PROTECT(NEW_OBJECT(MAKE_CLASS(#_t1_ "geMatrix")));	\
									\
    int *dims = INTEGER(dd),						\
	m = dims[0],							\
	n = dims[1];							\
    double len = m * (double)n;						\
									\
    if (len > R_LEN_T_MAX)						\
	error(_("Cannot coerce to too large *geMatrix with %.0f entries"), \
              len);							\
									\
    SET_SLOT(ans, Matrix_factorSym, allocVector(VECSXP, 0));		\
    SET_SLOT(ans, Matrix_DimSym, duplicate(dd));			\
    SET_DimNames(ans, x);						\
    SET_SLOT(ans, Matrix_xSym, allocVector(_SEXPTYPE_, (R_len_t)len));	\
    _t1_ ## _insert_triplets_in_array(m, n, length(islot),		\
				      INTEGER(islot),			\
				      INTEGER(GET_SLOT(x, Matrix_jSym)),\
				      _SEXP_(GET_SLOT(x, Matrix_xSym)),	\
				      _SEXP_(GET_SLOT(ans, Matrix_xSym))); \
    UNPROTECT(1);							\
    return ans;								\
}

MAKE_gTMatrix_to_geMatrix(d, REALSXP, REAL)

MAKE_gTMatrix_to_geMatrix(l, LGLSXP, LOGICAL)

#undef MAKE_gTMatrix_to_geMatrix

#define MAKE_gTMatrix_to_matrix(_t1_, _SEXPTYPE_, _SEXP_)		\
SEXP _t1_ ## gTMatrix_to_matrix(SEXP x)					\
{									\
    SEXP dd = GET_SLOT(x, Matrix_DimSym),				\
	dn = GET_SLOT(x, Matrix_DimNamesSym),				\
	islot = GET_SLOT(x, Matrix_iSym);				\
    int m = INTEGER(dd)[0],						\
	n = INTEGER(dd)[1];						\
    SEXP ans = PROTECT(allocMatrix(_SEXPTYPE_, m, n));			\
    if(VECTOR_ELT(dn, 0) != R_NilValue || VECTOR_ELT(dn, 1) != R_NilValue) \
	/* matrix() with non-trivial dimnames */			\
	setAttrib(ans, R_DimNamesSymbol, duplicate(dn));		\
    _t1_ ## _insert_triplets_in_array(m, n, length(islot),		\
				      INTEGER(islot),			\
				      INTEGER(GET_SLOT(x, Matrix_jSym)),\
				      _SEXP_(GET_SLOT(x, Matrix_xSym)),	\
				      _SEXP_(ans));			\
    UNPROTECT(1);							\
    return ans;								\
}

MAKE_gTMatrix_to_matrix(d, REALSXP, REAL)

MAKE_gTMatrix_to_matrix(l, LGLSXP, LOGICAL)

#undef MAKE_gTMatrix_to_matrix
