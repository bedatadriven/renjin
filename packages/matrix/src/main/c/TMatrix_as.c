			/* Sparse symmetric matrices in triplet format */
#include "TMatrix_as.h"

#define MAYBE_DECLARE_AND_GET_X_SLOT(__T__, __S__)	\
	      DECLARE_AND_GET_X_SLOT(__T__, __S__)

#define Matrix_T_as_DENSE(_C_TYPE_, _SEXP_, _SEXPTYPE_, _SYMM_)		\
    SEXP dimP = GET_SLOT(x, Matrix_DimSym),				\
	  xiP = GET_SLOT(x, Matrix_iSym);				\
    int k, n = INTEGER(dimP)[0], nnz = length(xiP);			\
    int *xi = INTEGER(xiP), *xj = INTEGER(GET_SLOT(x, Matrix_jSym)),	\
	sz = n * n;							\
    _C_TYPE_ *tx = _SEXP_(ALLOC_SLOT(val, Matrix_xSym, _SEXPTYPE_, sz)); \
    MAYBE_DECLARE_AND_GET_X_SLOT(_C_TYPE_, _SEXP_);			\
									\
    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));			\
    if(_SYMM_)								\
	SET_DimNames_symm(val, x);					\
    else								\
	SET_DimNames(val, x);						\
    slot_dup(val, x, Matrix_uploSym)

#define Matrix_T_as_DENSE_FINISH(_X_k_)		\
    AZERO(tx, sz);				\
    for (k = 0; k < nnz; k++)			\
	tx[xi[k] + xj[k] * n] = _X_k_;		\
    UNPROTECT(1);				\
    return val


SEXP dsTMatrix_as_dsyMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dsyMatrix")));

    Matrix_T_as_DENSE(double, REAL, REALSXP, FALSE);
    Matrix_T_as_DENSE_FINISH(xx[k]);
}

SEXP lsTMatrix_as_lsyMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("lsyMatrix")));

    Matrix_T_as_DENSE(int, LOGICAL, LGLSXP, FALSE);
    Matrix_T_as_DENSE_FINISH(xx[k]);
}

/* ---- Now the triangular ones --  have an extra  'diag'  slot : ------ */

SEXP dtTMatrix_as_dtrMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dtrMatrix")));

    Matrix_T_as_DENSE(double, REAL, REALSXP, FALSE);
    slot_dup(val, x, Matrix_diagSym);
    Matrix_T_as_DENSE_FINISH(xx[k]);
}

SEXP ltTMatrix_as_ltrMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("ltrMatrix")));

    Matrix_T_as_DENSE(int, LOGICAL, LGLSXP, FALSE);
    slot_dup(val, x, Matrix_diagSym);
    Matrix_T_as_DENSE_FINISH(xx[k]);
}

/*===================== Coercion to  gTMatrix ================================*/

#undef  MAYBE_DECLARE_AND_GET_X_SLOT
#define MAYBE_DECLARE_AND_GET_X_SLOT(__T__, __S__)		\
	      DECLARE_AND_GET_X_SLOT(__T__, __S__), *vx

#define ALLOC_val_x_SLOT(__SEXP__, __S_TYPE__)			\
    vx = __SEXP__(ALLOC_SLOT(val, Matrix_xSym,__S_TYPE__, nv))

#define MAYBE_ALLOC_val_x_SLOT(_S1_, _S2_)	\
	      ALLOC_val_x_SLOT(_S1_, _S2_)

#define MEMCPY_x_SLOT		Memcpy(&vx[nv], xx, nnz)
#define MAYBE_MEMCPY_x_SLOT     MEMCPY_x_SLOT

#define SET_x_SLOT		vx[nv] = xx[i]
#define MAYBE_SET_x_SLOT	SET_x_SLOT

#define Matrix_sT_as_GENERAL(_C_TYPE_, _SEXP_, _SEXPTYPE_)		\
    SEXP xiP = GET_SLOT(x, Matrix_iSym);				\
    /* , uplo = GET_SLOT(x, Matrix_uploSym); */				\
    int i, nnz = length(xiP), n0d, nv,					\
	*xi = INTEGER(xiP),						\
	*xj = INTEGER(GET_SLOT(x, Matrix_jSym)),			\
	*vi, *vj;							\
    MAYBE_DECLARE_AND_GET_X_SLOT(_C_TYPE_, _SEXP_);			\
									\
    /* Find *length* of result slots: */				\
    /*  = 2 * nnz - n0d; n0d := #{non-0 diagonals} :*/			\
    for(i = 0, n0d = 0; i < nnz; i++)					\
	if(xi[i] == xj[i]) n0d++ ;					\
    nv = 2 * nnz - n0d;							\
									\
    vi = INTEGER(ALLOC_SLOT(val, Matrix_iSym, INTSXP, nv));		\
    vj = INTEGER(ALLOC_SLOT(val, Matrix_jSym, INTSXP, nv));		\
    MAYBE_ALLOC_val_x_SLOT(_SEXP_, _SEXPTYPE_);				\
									\
    slot_dup(val, x, Matrix_DimSym);					\
    SET_DimNames_symm(val, x);						\
    /* copy the upper/lower triangle (including the diagonal)*/		\
    /* "at end" ([nv]): */						\
    nv = nnz - n0d;							\
    Memcpy(&vi[nv], xi, nnz);						\
    Memcpy(&vj[nv], xj, nnz);						\
    MAYBE_MEMCPY_x_SLOT;						\
									\
    for(i = 0, nv = 0; i < nnz; i++) { /* copy the other triangle */	\
	if(xi[i] != xj[i]) { /* but not the diagonal */			\
	    vi[nv] = xj[i];						\
	    vj[nv] = xi[i];						\
	    MAYBE_SET_x_SLOT;						\
	    nv++;							\
	}								\
    }									\
									\
    UNPROTECT(1);							\
    return val


/* this corresponds to changing 'stype' of a cholmod_triplet;
 * seems not available there */
SEXP dsTMatrix_as_dgTMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dgTMatrix")));
    Matrix_sT_as_GENERAL(double, REAL, REALSXP);
}


SEXP lsTMatrix_as_lgTMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("lgTMatrix")));
    Matrix_sT_as_GENERAL(int, LOGICAL, LGLSXP);
}

/* Now the 'nsparseMatrix' ones where input has no 'x' slot : ---------------*/

#undef  MAYBE_DECLARE_AND_GET_X_SLOT
#define MAYBE_DECLARE_AND_GET_X_SLOT(__T__, __S__)
#undef  MAYBE_ALLOC_val_x_SLOT
#define MAYBE_ALLOC_val_x_SLOT(_S1_, _S2_)
#undef  MAYBE_MEMCPY_x_SLOT
#define MAYBE_MEMCPY_x_SLOT
#undef  MAYBE_SET_x_SLOT
#define MAYBE_SET_x_SLOT

SEXP nsTMatrix_as_nsyMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("nsyMatrix")));

    Matrix_T_as_DENSE(int, LOGICAL, LGLSXP, FALSE);
    Matrix_T_as_DENSE_FINISH(1);
}

SEXP ntTMatrix_as_ntrMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("ntrMatrix")));

    Matrix_T_as_DENSE(int, LOGICAL, LGLSXP, FALSE);
    slot_dup(val, x, Matrix_diagSym);
    Matrix_T_as_DENSE_FINISH(1);
}

SEXP nsTMatrix_as_ngTMatrix(SEXP x)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("ngTMatrix")));
    Matrix_sT_as_GENERAL(int, LOGICAL, LGLSXP);
}
