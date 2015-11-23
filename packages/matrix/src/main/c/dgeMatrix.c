#include "dgeMatrix.h"

SEXP dMatrix_validate(SEXP obj)
{
    SEXP x = GET_SLOT(obj, Matrix_xSym),
	Dim = GET_SLOT(obj, Matrix_DimSym);
    if (!isReal(x))
	return mkString(_("x slot must be numeric \"double\""));
    SEXP val;
    if (isString(val = dim_validate(Dim, "Matrix")))
	return val;
    return ScalarLogical(1);
}

SEXP dgeMatrix_validate(SEXP obj)
{
    SEXP val;
    if (isString(val = dim_validate(GET_SLOT(obj, Matrix_DimSym), "dgeMatrix")))
	return(val);
    if (isString(val = dense_nonpacked_validate(obj)))
	return(val);
    SEXP fact = GET_SLOT(obj, Matrix_factorSym);
    if (length(fact) > 0 && getAttrib(fact, R_NamesSymbol) == R_NilValue)
	return mkString(_("factors slot must be named list"));
    return ScalarLogical(1);
}

static
double get_norm(SEXP obj, const char *typstr)
{
    if(any_NA_in_x(obj))
	return NA_REAL;
    else {
	char typnm[] = {'\0', '\0'};
	int *dims = INTEGER(GET_SLOT(obj, Matrix_DimSym));
	double *work = (double *) NULL;

	typnm[0] = La_norm_type(typstr);
	if (*typnm == 'I') {
	    work = (double *) R_alloc(dims[0], sizeof(double));
	}
	return F77_CALL(dlange)(typstr, dims, dims+1,
				REAL(GET_SLOT(obj, Matrix_xSym)),
				dims, work);
    }
}

SEXP dgeMatrix_norm(SEXP obj, SEXP type)
{
    return ScalarReal(get_norm(obj, CHAR(asChar(type))));
}

SEXP dgeMatrix_rcond(SEXP obj, SEXP type)
{
    SEXP LU = PROTECT(dgeMatrix_LU_(obj, FALSE));/* <- not warning about singularity */
    char typnm[] = {'\0', '\0'};
    int *dims = INTEGER(GET_SLOT(LU, Matrix_DimSym)), info;
    double anorm, rcond;

    if (dims[0] != dims[1] || dims[0] < 1) {
	UNPROTECT(1);
	error(_("rcond requires a square, non-empty matrix"));
    }
    typnm[0] = La_rcond_type(CHAR(asChar(type)));
    anorm = get_norm(obj, typnm);
    F77_CALL(dgecon)(typnm,
		     dims, REAL(GET_SLOT(LU, Matrix_xSym)),
		     dims, &anorm, &rcond,
		     (double *) R_alloc(4*dims[0], sizeof(double)),
		     (int *) R_alloc(dims[0], sizeof(int)), &info);
    UNPROTECT(1);
    return ScalarReal(rcond);
}

SEXP dgeMatrix_crossprod(SEXP x, SEXP trans)
{
#define DGE_CROSS_1							\
    int tr = asLogical(trans);/* trans=TRUE: tcrossprod(x) */		\
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dpoMatrix"))),		\
	nms = VECTOR_ELT(GET_SLOT(x, Matrix_DimNamesSym), tr ? 0 : 1),	\
	vDnms = ALLOC_SLOT(val, Matrix_DimNamesSym, VECSXP, 2);		\
    int *Dims = INTEGER(GET_SLOT(x, Matrix_DimSym)),			\
	*vDims = INTEGER(ALLOC_SLOT(val, Matrix_DimSym, INTSXP, 2));	\
    int k = tr ? Dims[1] : Dims[0],					\
	n = tr ? Dims[0] : Dims[1];					\
    double *vx = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, n * n)),	\
	one = 1.0, zero = 0.0;						\
    									\
    Memzero(vx, n * n);							\
    SET_SLOT(val, Matrix_uploSym, mkString("U"));			\
    ALLOC_SLOT(val, Matrix_factorSym, VECSXP, 0);			\
    vDims[0] = vDims[1] = n;						\
    SET_VECTOR_ELT(vDnms, 0, duplicate(nms));				\
    SET_VECTOR_ELT(vDnms, 1, duplicate(nms))

#define DGE_CROSS_DO(_X_X_)					\
    if(n)							\
	F77_CALL(dsyrk)("U", tr ? "N" : "T", &n, &k, &one,	\
			_X_X_, Dims, &zero, vx, &n);		\
    UNPROTECT(1);						\
    return val

    DGE_CROSS_1;
    DGE_CROSS_DO(REAL(GET_SLOT(x, Matrix_xSym)));
}


double* gematrix_real_x(SEXP x, int nn) {
    if(class_P(x)[0] == 'd') // <<- FIXME: use Matrix_check_class_etc(x, valid)  !!!
	return REAL(GET_SLOT(x, Matrix_xSym));
#ifdef _potentically_more_efficient_but_not_working
    // else : 'l' or 'n' (for now !!)
    int *xi = INTEGER(GET_SLOT(x, Matrix_xSym));
    double *x_x;
    C_or_Alloca_TO(x_x, nn, double);
    for(int i=0; i < nn; i++)
	x_x[i] = (double) xi[i];

    // FIXME: this is not possible either; the *caller* would have to Free(.)
    if(nn >= SMALL_4_Alloca) Free(x_x);
#else
    // ideally should be PROTECT()ed ==> make sure R does not run gc() now!
    double *x_x = REAL(coerceVector(GET_SLOT(x, Matrix_xSym), REALSXP));
#endif
    return x_x;
}

//!  As  dgeMatrix_crossprod(), but x can be [dln]geMatrix
SEXP _geMatrix_crossprod(SEXP x, SEXP trans)
{
    DGE_CROSS_1;
    double *x_x = gematrix_real_x(x, k * n);
    DGE_CROSS_DO(x_x);
}

SEXP geMatrix_crossprod(SEXP x, SEXP trans)
{
    SEXP y = PROTECT(dup_mMatrix_as_geMatrix(x)),
	val = _geMatrix_crossprod(y, trans);
    UNPROTECT(1);
    return val;
}

SEXP dgeMatrix_dgeMatrix_crossprod(SEXP x, SEXP y, SEXP trans)
{
#define DGE_DGE_CROSS_1							\
    int tr = asLogical(trans);/* trans=TRUE: tcrossprod(x,y) */		\
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),		\
	dn = PROTECT(allocVector(VECSXP, 2));				\
    int *xDims = INTEGER(GET_SLOT(x, Matrix_DimSym)),			\
	*yDims = INTEGER(GET_SLOT(y, Matrix_DimSym)),			\
	*vDims;								\
    int m  = xDims[!tr],  n = yDims[!tr];/* -> result dim */		\
    int xd = xDims[ tr], yd = yDims[ tr];/* the conformable dims */	\
    double one = 1.0, zero = 0.0;					\
									\
    if (xd != yd)							\
	error(_("Dimensions of x and y are not compatible for %s"),	\
	      tr ? "tcrossprod" : "crossprod");				\
    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));		\
    /* establish dimnames */						\
    SET_VECTOR_ELT(dn, 0,						\
		   duplicate(VECTOR_ELT(GET_SLOT(x, Matrix_DimNamesSym), \
					tr ? 0 : 1)));			\
    SET_VECTOR_ELT(dn, 1,						\
		   duplicate(VECTOR_ELT(GET_SLOT(y, Matrix_DimNamesSym), \
					tr ? 0 : 1)));			\
    SET_SLOT(val, Matrix_DimNamesSym, dn);				\
    vDims = INTEGER(ALLOC_SLOT(val, Matrix_DimSym, INTSXP, 2));		\
    vDims[0] = m; vDims[1] = n;						\
    double *v = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, m * n))

#define DGE_DGE_CROSS_DO(_X_X_, _Y_Y_)					\
    if (xd > 0 && n > 0 && m > 0)					\
	F77_CALL(dgemm)(tr ? "N" : "T", tr ? "T" : "N", &m, &n, &xd, &one, \
			_X_X_, xDims,					\
			_Y_Y_, yDims, &zero, v, &m);			\
    else								\
	Memzero(v, m * n);						\
    UNPROTECT(2);							\
    return val

    DGE_DGE_CROSS_1;
    DGE_DGE_CROSS_DO(REAL(GET_SLOT(x, Matrix_xSym)),
		     REAL(GET_SLOT(y, Matrix_xSym)));
}

//!  As  dgeMatrix_dgeMatrix_crossprod(), but x and y can be [dln]geMatrix
SEXP _geMatrix__geMatrix_crossprod(SEXP x, SEXP y, SEXP trans)
{
    DGE_DGE_CROSS_1;

    double *x_x = gematrix_real_x(x, m * xd);
    double *y_x = gematrix_real_x(y, n * yd);

    DGE_DGE_CROSS_DO(x_x, y_x);
}
#undef DGE_DGE_CROSS_1
#undef DGE_DGE_CROSS_DO

SEXP geMatrix_geMatrix_crossprod(SEXP x, SEXP y, SEXP trans)
{
    SEXP gx = PROTECT(dup_mMatrix_as_geMatrix(x)),
	 gy = PROTECT(dup_mMatrix_as_geMatrix(y)),
	val = _geMatrix__geMatrix_crossprod(gx, gy, trans);
    UNPROTECT(2);
    return val;
}

SEXP dgeMatrix_matrix_crossprod(SEXP x, SEXP y, SEXP trans)
{
#define DGE_MAT_CROSS_1							\
    int tr = asLogical(trans);/* trans=TRUE: tcrossprod(x,y) */		\
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),		\
	dn = PROTECT(allocVector(VECSXP, 2)),				\
	yDnms = R_NilValue, yD;						\
    int *xDims = INTEGER(GET_SLOT(x, Matrix_DimSym)),			\
	*yDims, *vDims, nprot = 2;					\
    int m  = xDims[!tr],						\
	xd = xDims[ tr];						\
    double one = 1.0, zero = 0.0;					\
    Rboolean y_has_dimNames;						\
									\
    if (!isReal(y)) {							\
	if(isInteger(y) || isLogical(y)) {				\
	    y = PROTECT(coerceVector(y, REALSXP));			\
	    nprot++;							\
	}								\
	else								\
	    error(_("Argument y must be numeric, integer or logical"));	\
    }									\
    if(isMatrix(y)) {							\
	yDims = INTEGER(getAttrib(y, R_DimSymbol));			\
	yDnms = getAttrib(y, R_DimNamesSymbol);				\
	y_has_dimNames = yDnms != R_NilValue;				\
    } else { /* ! matrix */ 						\
	yDims = INTEGER(yD = PROTECT(allocVector(INTSXP, 2))); nprot++;	\
	if(xDims[0] == 1) {						\
             /* "new" (2014-10-10): "be tolerant" as for R 3.2.0*/ 	\
	    yDims[0] = 1;						\
	    yDims[1] = LENGTH(y);					\
	} else {							\
	    yDims[0] = LENGTH(y);					\
	    yDims[1] = 1;						\
	}								\
	y_has_dimNames = FALSE;						\
    }									\
    int  n = yDims[!tr],/* (m,n) -> result dim */			\
	yd = yDims[ tr];/* (xd,yd): the conformable dims */		\
    if (xd != yd)							\
	error(_("Dimensions of x and y are not compatible for %s"),	\
	      tr ? "tcrossprod" : "crossprod");				\
    SET_SLOT(val, Matrix_factorSym, allocVector(VECSXP, 0));		\
    vDims = INTEGER(ALLOC_SLOT(val, Matrix_DimSym, INTSXP, 2));		\
    vDims[0] = m; vDims[1] = n;						\
    /* establish dimnames */						\
    SET_VECTOR_ELT(dn, 0,						\
		   duplicate(VECTOR_ELT(GET_SLOT(x, Matrix_DimNamesSym), \
					tr ? 0 : 1)));			\
    if(y_has_dimNames)							\
	SET_VECTOR_ELT(dn, 1,						\
		       duplicate(VECTOR_ELT(yDnms, tr ? 0 : 1)));	\
    SET_SLOT(val, Matrix_DimNamesSym, dn);				\
									\
    double *v = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, m * n))

#define DGE_MAT_CROSS_DO(_X_X_)						\
    if (xd > 0 && n > 0 && m > 0)					\
	F77_CALL(dgemm)(tr ? "N" : "T", tr ? "T" : "N", &m, &n, &xd, &one, \
			_X_X_, xDims, REAL(y), yDims, 			\
			&zero, v, &m);					\
    else								\
	Memzero(v, m * n);						\
    UNPROTECT(nprot);							\
    return val

    DGE_MAT_CROSS_1;
    DGE_MAT_CROSS_DO(REAL(GET_SLOT(x, Matrix_xSym)));
}

//! as dgeMatrix_matrix_crossprod() but x can be  [dln]geMatrix
SEXP _geMatrix_matrix_crossprod(SEXP x, SEXP y, SEXP trans) {
    DGE_MAT_CROSS_1;

    double *x_x = gematrix_real_x(x, m * xd);

    DGE_MAT_CROSS_DO(x_x);
}

SEXP geMatrix_matrix_crossprod(SEXP x, SEXP y, SEXP trans) {
    SEXP dx = PROTECT(dup_mMatrix_as_geMatrix(x)),
	val = _geMatrix_matrix_crossprod(dx, y, trans);
    UNPROTECT(1);
    return val;
}

//  right = TRUE:  %*%  is called as  *(y, x, right=TRUE)
SEXP dgeMatrix_matrix_mm(SEXP a, SEXP bP, SEXP right)
{
#define DGE_MAT_MM_1(N_PROT)						\
    SEXP val= PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),		\
	 dn = PROTECT(allocVector(VECSXP, 2));				\
    int nprot = N_PROT + 2,						\
	*adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),			\
	*bdims = INTEGER(GET_SLOT(b, Matrix_DimSym)),			\
	*cdims = INTEGER(ALLOC_SLOT(val, Matrix_DimSym, INTSXP, 2)),	\
	Rt = asLogical(right), m, k, n;					\
    double one = 1., zero = 0.;						\
									\
    if (Rt) { /* b %*% a : (m x k) (k x n) -> (m x n) */		\
	m = bdims[0]; k = bdims[1]; n = adims[1];			\
	if (adims[0] != k)						\
	    error(_("Matrices are not conformable for multiplication")); \
    } else {  /* a %*% b : (m x k) (k x n) -> (m x n) */		\
	m = adims[0]; k = adims[1]; n = bdims[1];			\
	if (bdims[0] != k)						\
	    error(_("Matrices are not conformable for multiplication")); \
    }									\
									\
    cdims[0] = m; cdims[1] = n;						\
    /* establish dimnames */						\
    SET_VECTOR_ELT(dn, 0, duplicate(					\
		       VECTOR_ELT(GET_SLOT(Rt ? b : a,			\
					   Matrix_DimNamesSym), 0)));	\
    SET_VECTOR_ELT(dn, 1,						\
		   duplicate(						\
		       VECTOR_ELT(GET_SLOT(Rt ? a : b,			\
					   Matrix_DimNamesSym), 1)));	\
    SET_SLOT(val, Matrix_DimNamesSym, dn);				\
    double *v = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, m * n))

#define DGE_MAT_MM_DO(_A_X_, _B_X_)					\
    if (m < 1 || n < 1 || k < 1) {/* zero extent matrices should work */ \
	Memzero(v, m * n);						\
    } else {								\
	if (Rt) { /* b %*% a  */					\
	    F77_CALL(dgemm) ("N", "N", &m, &n, &k, &one,		\
			     _B_X_, &m, _A_X_, &k, &zero, v, &m);	\
	} else {  /* a %*% b  */					\
	    F77_CALL(dgemm) ("N", "N", &m, &n, &k, &one,		\
			     _A_X_, &m,	_B_X_, &k, &zero, v, &m); 	\
	}								\
    }									\
    UNPROTECT(nprot);							\
    return val

    SEXP b = PROTECT(mMatrix_as_dgeMatrix(bP));
    DGE_MAT_MM_1(1);
    DGE_MAT_MM_DO(REAL(GET_SLOT(a, Matrix_xSym)),
                  REAL(GET_SLOT(b, Matrix_xSym)));
}

//! as dgeMatrix_matrix_mm() but a can be  [dln]geMatrix
SEXP _geMatrix_matrix_mm(SEXP a, SEXP b, SEXP right) {
    DGE_MAT_MM_1(0);
    double *a_x = gematrix_real_x(a, k * (Rt ? n : m));
    double *b_x = gematrix_real_x(b, k * (Rt ? m : n));
    DGE_MAT_MM_DO(a_x, b_x);
}

//! %*% -- generalized from dge to *ge():
SEXP geMatrix_matrix_mm(SEXP a, SEXP b, SEXP right) {
    SEXP
	da = PROTECT(dup_mMatrix_as_geMatrix(a)),
	db = PROTECT(dup_mMatrix_as_geMatrix(b)),
	val = _geMatrix_matrix_mm(da, db, right);
    UNPROTECT(2);
    return val;
}

//---------------------------------------------------------------------

SEXP dgeMatrix_getDiag(SEXP x)
{
#define geMatrix_getDiag_1					\
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym));		\
    int i, m = dims[0], nret = (m < dims[1]) ? m : dims[1];	\
    SEXP x_x = GET_SLOT(x, Matrix_xSym)

    geMatrix_getDiag_1;
    SEXP ret = PROTECT(allocVector(REALSXP, nret));
    double *rv = REAL(ret),
	   *xv = REAL(x_x);

#define geMatrix_getDiag_2			\
    for (i = 0; i < nret; i++) {		\
	rv[i] = xv[i * (m + 1)];		\
    }						\
    UNPROTECT(1);				\
    return ret

    geMatrix_getDiag_2;
}

SEXP lgeMatrix_getDiag(SEXP x)
{
    geMatrix_getDiag_1;

    SEXP ret = PROTECT(allocVector(LGLSXP, nret));
    int *rv = LOGICAL(ret),
	*xv = LOGICAL(x_x);

    geMatrix_getDiag_2;
}

#undef geMatrix_getDiag_1
#undef geMatrix_getDiag_2


SEXP dgeMatrix_setDiag(SEXP x, SEXP d)
{
#define geMatrix_setDiag_1					\
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym));		\
    int m = dims[0], nret = (m < dims[1]) ? m : dims[1];	\
    SEXP ret = PROTECT(duplicate(x));				\
    SEXP r_x = GET_SLOT(ret, Matrix_xSym);			\
    int l_d = LENGTH(d); Rboolean d_full = (l_d == nret);	\
    if (!d_full && l_d != 1)					\
	error(_("replacement diagonal has wrong length"))

    geMatrix_setDiag_1;
    double *dv = REAL(d), *rv = REAL(r_x);

#define geMatrix_setDiag_2			\
    if(d_full) for (int i = 0; i < nret; i++)	\
	rv[i * (m + 1)] = dv[i];		\
    else for (int i = 0; i < nret; i++)		\
	rv[i * (m + 1)] = *dv;			\
    UNPROTECT(1);				\
    return ret

    geMatrix_setDiag_2;
}

SEXP lgeMatrix_setDiag(SEXP x, SEXP d)
{
    geMatrix_setDiag_1;
    int *dv = INTEGER(d), *rv = INTEGER(r_x);

    geMatrix_setDiag_2;
}

#undef geMatrix_setDiag_1
#undef geMatrix_setDiag_2

SEXP dgeMatrix_addDiag(SEXP x, SEXP d)
{
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym)),
	m = dims[0], nret = (m < dims[1]) ? m : dims[1];
    SEXP ret = PROTECT(duplicate(x)),
	r_x = GET_SLOT(ret, Matrix_xSym);
    double *dv = REAL(d), *rv = REAL(r_x);
    int l_d = LENGTH(d); Rboolean d_full = (l_d == nret);
    if (!d_full && l_d != 1)
	error(_("diagonal to be added has wrong length"));

    if(d_full) for (int i = 0; i < nret; i++) rv[i * (m + 1)] += dv[i];
    else for (int i = 0; i < nret; i++)	      rv[i * (m + 1)] += *dv;
    UNPROTECT(1);
    return ret;
}



SEXP dgeMatrix_LU_(SEXP x, Rboolean warn_sing)
{
    SEXP val = get_factors(x, "LU");
    int *dims, npiv, info;

    if (val != R_NilValue) /* nothing to do if it's there in 'factors' slot */
	return val;
    dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    if (dims[0] < 1 || dims[1] < 1)
	error(_("Cannot factor a matrix with zero extents"));
    npiv = (dims[0] < dims[1]) ? dims[0] : dims[1];
    val = PROTECT(NEW_OBJECT(MAKE_CLASS("denseLU")));
    slot_dup(val, x, Matrix_xSym);
    slot_dup(val, x, Matrix_DimSym);
    slot_dup(val, x, Matrix_DimNamesSym);
    F77_CALL(dgetrf)(dims, dims + 1, REAL(GET_SLOT(val, Matrix_xSym)),
		     dims,
		     INTEGER(ALLOC_SLOT(val, Matrix_permSym, INTSXP, npiv)),
		     &info);
    if (info < 0)
	error(_("Lapack routine %s returned error code %d"), "dgetrf", info);
    else if (info > 0 && warn_sing)
	warning(_("Exact singularity detected during LU decomposition: %s, i=%d."),
		"U[i,i]=0", info);
    UNPROTECT(1);
    return set_factors(x, val, "LU");
}
// FIXME: also allow an interface to LAPACK's  dgesvx()  which uses LU fact.
//        and then optionally does "equilibration" (row and column scaling)
//  maybe also allow low-level interface to  dgeEQU() ...

SEXP dgeMatrix_LU(SEXP x, SEXP warn_singularity)
{
    return dgeMatrix_LU_(x, asLogical(warn_singularity));
}

SEXP dgeMatrix_determinant(SEXP x, SEXP logarithm)
{
    int lg = asLogical(logarithm);
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym)),
	n = dims[0], sign = 1;
    double modulus = lg ? 0. : 1; /* initialize; = result for n == 0 */

    if (n != dims[1])
	error(_("Determinant requires a square matrix"));
    if (n > 0) {
	SEXP lu = dgeMatrix_LU_(x, /* do not warn about singular LU: */ FALSE);
	int i, *jpvt = INTEGER(GET_SLOT(lu, Matrix_permSym));
	double *luvals = REAL(GET_SLOT(lu, Matrix_xSym));

	for (i = 0; i < n; i++) if (jpvt[i] != (i + 1)) sign = -sign;
	if (lg) {
	    for (i = 0; i < n; i++) {
		double dii = luvals[i*(n + 1)]; /* ith diagonal element */
		modulus += log(dii < 0 ? -dii : dii);
		if (dii < 0) sign = -sign;
	    }
	} else {
	    for (i = 0; i < n; i++)
		modulus *= luvals[i*(n + 1)];
	    if (modulus < 0) {
		modulus = -modulus;
		sign = -sign;
	    }
	}
    }
    return as_det_obj(modulus, lg, sign);
}

SEXP dgeMatrix_solve(SEXP a)
{
    /*  compute the 1-norm of the matrix, which is needed
	later for the computation of the reciprocal condition number. */
    double aNorm = get_norm(a, "1");

    /* the LU decomposition : */
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),
	lu = dgeMatrix_LU_(a, TRUE);
    int *dims = INTEGER(GET_SLOT(lu, Matrix_DimSym)),
	*pivot = INTEGER(GET_SLOT(lu, Matrix_permSym));

    /* prepare variables for the dgetri calls */
    double *x, tmp;
    int	info, lwork = -1;


    if (dims[0] != dims[1]) error(_("Solve requires a square matrix"));
    slot_dup(val, lu, Matrix_xSym);
    x = REAL(GET_SLOT(val, Matrix_xSym));
    slot_dup(val, lu, Matrix_DimSym);

    if(dims[0]) /* the dimension is not zero */
    {
        /* is the matrix is *computationally* singular ? */
        double rcond;
        F77_CALL(dgecon)("1", dims, x, dims, &aNorm, &rcond,
                         (double *) R_alloc(4*dims[0], sizeof(double)),
                         (int *) R_alloc(dims[0], sizeof(int)), &info);
        if (info)
            error(_("error [%d] from Lapack 'dgecon()'"), info);
        if(rcond < DOUBLE_EPS)
            error(_("Lapack dgecon(): system computationally singular, reciprocal condition number = %g"),
		  rcond);

        /* only now try the inversion and check if the matrix is *exactly* singular: */
	F77_CALL(dgetri)(dims, x, dims, pivot, &tmp, &lwork, &info);
	lwork = (int) tmp;
	F77_CALL(dgetri)(dims, x, dims, pivot,
			 (double *) R_alloc((size_t) lwork, sizeof(double)),
			 &lwork, &info);
	if (info)
	    error(_("Lapack routine dgetri: system is exactly singular"));
    }
    UNPROTECT(1);
    return val;
}

SEXP dgeMatrix_matrix_solve(SEXP a, SEXP b)
{
    SEXP val = PROTECT(dup_mMatrix_as_dgeMatrix(b)),
	lu = PROTECT(dgeMatrix_LU_(a, TRUE));
    int *adims = INTEGER(GET_SLOT(lu, Matrix_DimSym)),
	*bdims = INTEGER(GET_SLOT(val, Matrix_DimSym));
    int info, n = bdims[0], nrhs = bdims[1];

    if (adims[0] != n || adims[1] != n)
	error(_("Dimensions of system to be solved are inconsistent"));
    if(nrhs >= 1 && n >= 1) {
	F77_CALL(dgetrs)("N", &n, &nrhs, REAL(GET_SLOT(lu, Matrix_xSym)), &n,
			 INTEGER(GET_SLOT(lu, Matrix_permSym)),
			 REAL(GET_SLOT(val, Matrix_xSym)), &n, &info);
	if (info)
	    error(_("Lapack routine dgetrs: system is exactly singular"));
    }
    UNPROTECT(2);
    return val;
}

SEXP dgeMatrix_svd(SEXP x, SEXP nnu, SEXP nnv)
{
    int /* nu = asInteger(nnu),
	   nv = asInteger(nnv), */
	*dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    double *xx = REAL(GET_SLOT(x, Matrix_xSym));
    SEXP val = PROTECT(allocVector(VECSXP, 3));

    if (dims[0] && dims[1]) {
	int m = dims[0], n = dims[1], mm = (m < n)?m:n,
	    lwork = -1, info;
	double tmp, *work;
	int *iwork, n_iw = 8 * mm;
	C_or_Alloca_TO(iwork, n_iw, int);

	SET_VECTOR_ELT(val, 0, allocVector(REALSXP, mm));
	SET_VECTOR_ELT(val, 1, allocMatrix(REALSXP, m, mm));
	SET_VECTOR_ELT(val, 2, allocMatrix(REALSXP, mm, n));
	F77_CALL(dgesdd)("S", &m, &n, xx, &m,
			 REAL(VECTOR_ELT(val, 0)),
			 REAL(VECTOR_ELT(val, 1)), &m,
			 REAL(VECTOR_ELT(val, 2)), &mm,
			 &tmp, &lwork, iwork, &info);
	lwork = (int) tmp;
	C_or_Alloca_TO(work, lwork, double);

	F77_CALL(dgesdd)("S", &m, &n, xx, &m,
			 REAL(VECTOR_ELT(val, 0)),
			 REAL(VECTOR_ELT(val, 1)), &m,
			 REAL(VECTOR_ELT(val, 2)), &mm,
			 work, &lwork, iwork, &info);

	if(n_iw  >= SMALL_4_Alloca) Free(iwork);
	if(lwork >= SMALL_4_Alloca) Free(work);
    }
    UNPROTECT(1);
    return val;
}

const static double padec [] = /* for matrix exponential calculation. */
{
  5.0000000000000000e-1,
  1.1666666666666667e-1,
  1.6666666666666667e-2,
  1.6025641025641026e-3,
  1.0683760683760684e-4,
  4.8562548562548563e-6,
  1.3875013875013875e-7,
  1.9270852604185938e-9,
};

/**
 * Matrix exponential - based on the _corrected_ code for Octave's expm function.
 *
 * @param x real square matrix to exponentiate
 *
 * @return matrix exponential of x
 */
SEXP dgeMatrix_exp(SEXP x)
{
    const double one = 1.0, zero = 0.0;
    const int i1 = 1;
    int *Dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    const int n = Dims[1], nsqr = n * n, np1 = n + 1;

    SEXP val = PROTECT(duplicate(x));
    int i, ilo, ilos, ihi, ihis, j, sqpow;
    int *pivot = Calloc(n, int);
    double *dpp = Calloc(nsqr, double), /* denominator power Pade' */
	*npp = Calloc(nsqr, double), /* numerator power Pade' */
	*perm = Calloc(n, double),
	*scale = Calloc(n, double),
	*v = REAL(GET_SLOT(val, Matrix_xSym)),
	*work = Calloc(nsqr, double), inf_norm, m1_j/*= (-1)^j */, trshift;
    R_CheckStack();

    if (n < 1 || Dims[0] != n)
	error(_("Matrix exponential requires square, non-null matrix"));
    if(n == 1) {
	v[0] = exp(v[0]);
	UNPROTECT(1);
	return val;
    }

    /* Preconditioning 1.  Shift diagonal by average diagonal if positive. */
    trshift = 0;		/* determine average diagonal element */
    for (i = 0; i < n; i++) trshift += v[i * np1];
    trshift /= n;
    if (trshift > 0.) {		/* shift diagonal by -trshift */
	for (i = 0; i < n; i++) v[i * np1] -= trshift;
    }

    /* Preconditioning 2. Balancing with dgebal. */
    F77_CALL(dgebal)("P", &n, v, &n, &ilo, &ihi, perm, &j);
    if (j) error(_("dgeMatrix_exp: LAPACK routine dgebal returned %d"), j);
    F77_CALL(dgebal)("S", &n, v, &n, &ilos, &ihis, scale, &j);
    if (j) error(_("dgeMatrix_exp: LAPACK routine dgebal returned %d"), j);

    /* Preconditioning 3. Scaling according to infinity norm */
    inf_norm = F77_CALL(dlange)("I", &n, &n, v, &n, work);
    sqpow = (inf_norm > 0) ? (int) (1 + log(inf_norm)/log(2.)) : 0;
    if (sqpow < 0) sqpow = 0;
    if (sqpow > 0) {
	double scale_factor = 1.0;
	for (i = 0; i < sqpow; i++) scale_factor *= 2.;
	for (i = 0; i < nsqr; i++) v[i] /= scale_factor;
    }

    /* Pade' approximation. Powers v^8, v^7, ..., v^1 */
    AZERO(npp, nsqr);
    AZERO(dpp, nsqr);
    m1_j = -1;
    for (j = 7; j >=0; j--) {
	double mult = padec[j];
	/* npp = m * npp + padec[j] *m */
	F77_CALL(dgemm)("N", "N", &n, &n, &n, &one, v, &n, npp, &n,
			&zero, work, &n);
	for (i = 0; i < nsqr; i++) npp[i] = work[i] + mult * v[i];
	/* dpp = m * dpp + (m1_j * padec[j]) * m */
	mult *= m1_j;
	F77_CALL(dgemm)("N", "N", &n, &n, &n, &one, v, &n, dpp, &n,
			&zero, work, &n);
	for (i = 0; i < nsqr; i++) dpp[i] = work[i] + mult * v[i];
	m1_j *= -1;
    }
    /* Zero power */
    for (i = 0; i < nsqr; i++) dpp[i] *= -1.;
    for (j = 0; j < n; j++) {
	npp[j * np1] += 1.;
	dpp[j * np1] += 1.;
    }

    /* Pade' approximation is solve(dpp, npp) */
    F77_CALL(dgetrf)(&n, &n, dpp, &n, pivot, &j);
    if (j) error(_("dgeMatrix_exp: dgetrf returned error code %d"), j);
    F77_CALL(dgetrs)("N", &n, &n, dpp, &n, pivot, npp, &n, &j);
    if (j) error(_("dgeMatrix_exp: dgetrs returned error code %d"), j);
    Memcpy(v, npp, nsqr);

    /* Now undo all of the preconditioning */
    /* Preconditioning 3: square the result for every power of 2 */
    while (sqpow--) {
	F77_CALL(dgemm)("N", "N", &n, &n, &n, &one, v, &n, v, &n,
			&zero, work, &n);
	Memcpy(v, work, nsqr);
    }
    /* Preconditioning 2: apply inverse scaling */
    for (j = 0; j < n; j++)
	for (i = 0; i < n; i++)
	    v[i + j * n] *= scale[i]/scale[j];


    /* 2 b) Inverse permutation  (if not the identity permutation) */
    if (ilo != 1 || ihi != n) {
	/* Martin Maechler's code */

#define SWAP_ROW(I,J) F77_CALL(dswap)(&n, &v[(I)], &n, &v[(J)], &n)

#define SWAP_COL(I,J) F77_CALL(dswap)(&n, &v[(I)*n], &i1, &v[(J)*n], &i1)

#define RE_PERMUTE(I)				\
	int p_I = (int) (perm[I]) - 1;		\
	SWAP_COL(I, p_I);			\
	SWAP_ROW(I, p_I)

	/* reversion of "leading permutations" : in reverse order */
	for (i = (ilo - 1) - 1; i >= 0; i--) {
	    RE_PERMUTE(i);
	}

	/* reversion of "trailing permutations" : applied in forward order */
	for (i = (ihi + 1) - 1; i < n; i++) {
	    RE_PERMUTE(i);
	}
    }

    /* Preconditioning 1: Trace normalization */
    if (trshift > 0.) {
	double mult = exp(trshift);
	for (i = 0; i < nsqr; i++) v[i] *= mult;
    }

    /* Clean up */
    Free(work); Free(scale); Free(perm); Free(npp); Free(dpp); Free(pivot);
    UNPROTECT(1);
    return val;
}

SEXP dgeMatrix_Schur(SEXP x, SEXP vectors, SEXP isDGE)
{
// 'x' is either a traditional matrix or a  dgeMatrix, as indicated by isDGE.
    int *dims, n, vecs = asLogical(vectors), is_dge = asLogical(isDGE),
	info, izero = 0, lwork = -1, nprot = 1;

    if(is_dge) {
	dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    } else { // traditional matrix
	dims = INTEGER(getAttrib(x, R_DimSymbol));
	if(!isReal(x)) { // may not be "numeric" ..
	    x = PROTECT(coerceVector(x, REALSXP)); // -> maybe error
	    nprot++;
	}
    }
    double *work, tmp;
    const char *nms[] = {"WR", "WI", "T", "Z", ""};
    SEXP val = PROTECT(Rf_mkNamed(VECSXP, nms));

    n = dims[0];
    if (n != dims[1] || n < 1)
	error(_("dgeMatrix_Schur: argument x must be a non-null square matrix"));
    SET_VECTOR_ELT(val, 0, allocVector(REALSXP, n));
    SET_VECTOR_ELT(val, 1, allocVector(REALSXP, n));
    SET_VECTOR_ELT(val, 2, allocMatrix(REALSXP, n, n));
    Memcpy(REAL(VECTOR_ELT(val, 2)),
	   REAL(is_dge ? GET_SLOT(x, Matrix_xSym) : x),
	   n * n);
    SET_VECTOR_ELT(val, 3, allocMatrix(REALSXP, vecs ? n : 0, vecs ? n : 0));
    F77_CALL(dgees)(vecs ? "V" : "N", "N", NULL, dims, (double *) NULL, dims, &izero,
		    (double *) NULL, (double *) NULL, (double *) NULL, dims,
		    &tmp, &lwork, (int *) NULL, &info);
    if (info) error(_("dgeMatrix_Schur: first call to dgees failed"));
    lwork = (int) tmp;
    C_or_Alloca_TO(work, lwork, double);

    F77_CALL(dgees)(vecs ? "V" : "N", "N", NULL, dims, REAL(VECTOR_ELT(val, 2)), dims,
		    &izero, REAL(VECTOR_ELT(val, 0)), REAL(VECTOR_ELT(val, 1)),
		    REAL(VECTOR_ELT(val, 3)), dims, work, &lwork,
		    (int *) NULL, &info);
    if(lwork >= SMALL_4_Alloca) Free(work);
    if (info) error(_("dgeMatrix_Schur: dgees returned code %d"), info);
    UNPROTECT(nprot);
    return val;
} // dgeMatrix_Schur

// colSums(), colMeans(), rowSums() and rowMeans() -- called from ../R/colSums.R
SEXP dgeMatrix_colsums(SEXP x, SEXP naRmP, SEXP cols, SEXP mean)
{
    int keepNA = !asLogical(naRmP); // <==>  na.rm = FALSE,  the default
    int doMean = asLogical(mean);
    int useCols = asLogical(cols);
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    int i, j, m = dims[0], n = dims[1];
    SEXP ans = PROTECT(allocVector(REALSXP, (useCols) ? n : m));
    double *aa = REAL(ans), *xx = REAL(GET_SLOT(x, Matrix_xSym));

    if (useCols) {  /* col(Sums|Means) : */
	int cnt = m; // := number of 'valid' entries in current column
	for (j = 0; j < n; j++) { // column j :
	    double *x_j = xx + m * j, s = 0.;

	    if (keepNA)
		for (i = 0; i < m; i++) s += x_j[i];
	    else {
		cnt = 0;
		for (i = 0; i < m; i++)
		    if (!ISNAN(x_j[i])) {cnt++; s += x_j[i];}
	    }
	    if (doMean) {
		if (cnt > 0) s /= cnt; else s = NA_REAL;
	    }
	    aa[j] = s;
	}
    } else { /* row(Sums|Means) : */
	Rboolean do_count = (!keepNA) && doMean;
	int *cnt = (int*) NULL;
	if(do_count) { C_or_Alloca_TO(cnt, m, int); }

	// (taking care to access x contiguously: vary i inside j)
	for (i = 0; i < m; i++) {
	    aa[i] = 0.;
	    if(do_count) cnt[i] = 0;
	}
	for (j = 0; j < n; j++) {
	    if (keepNA)
		for (i = 0; i < m; i++) aa[i] += xx[i + j * m];
	    else
		for (i = 0; i < m; i++) {
		    double el = xx[i + j * m];
		    if (!ISNAN(el)) {
			aa[i] += el;
			if (doMean) cnt[i]++;
		    }
		}
	}
	if (doMean) {
	    if (keepNA)
		for (i = 0; i < m; i++) aa[i] /= n;
	    else
		for (i = 0; i < m; i++)
		    aa[i] = (cnt[i] > 0) ? aa[i]/cnt[i] : NA_REAL;
	}
	if(do_count && m >= SMALL_4_Alloca) Free(cnt);
    }

    SEXP nms = VECTOR_ELT(GET_SLOT(x, Matrix_DimNamesSym), useCols ? 1 : 0);
    if(!isNull(nms))
	setAttrib(ans, R_NamesSymbol, duplicate(nms));
    UNPROTECT(1);
    return ans;
}
