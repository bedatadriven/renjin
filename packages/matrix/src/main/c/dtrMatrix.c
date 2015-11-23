/* double (precision) TRiangular Matrices */

#include "dtrMatrix.h"

SEXP triangularMatrix_validate(SEXP obj)
{
    SEXP val = GET_SLOT(obj, Matrix_DimSym);

    if (LENGTH(val) < 2)
	return mkString(_("'Dim' slot has length less than two"));
    if (INTEGER(val)[0] != INTEGER(val)[1])
        return mkString(_("Matrix is not square"));
    if (isString(val = check_scalar_string(GET_SLOT(obj, Matrix_uploSym),
					   "LU", "uplo"))) return val;
    if (isString(val = check_scalar_string(GET_SLOT(obj, Matrix_diagSym),
					   "NU", "diag"))) return val;
    return ScalarLogical(1);
}

static
double get_norm(SEXP obj, const char *typstr)
{
    char typnm[] = {'\0', '\0'};
    int *dims = INTEGER(GET_SLOT(obj, Matrix_DimSym));
    double *work = (double *) NULL;

    typnm[0] = La_norm_type(typstr);
    if (*typnm == 'I') {
	work = (double *) R_alloc(dims[0], sizeof(double));
    }
    return F77_CALL(dlantr)(typnm, uplo_P(obj), diag_P(obj), dims, dims+1,
			    REAL(GET_SLOT(obj, Matrix_xSym)), dims, work);
}


SEXP dtrMatrix_norm(SEXP obj, SEXP type)
{
    return ScalarReal(get_norm(obj, CHAR(asChar(type))));
}

SEXP dtrMatrix_rcond(SEXP obj, SEXP type)
{
    char typnm[] = {'\0', '\0'};
    int *dims = INTEGER(GET_SLOT(obj, Matrix_DimSym)), info;
    double rcond;

    typnm[0] = La_rcond_type(CHAR(asChar(type)));
    F77_CALL(dtrcon)(typnm, uplo_P(obj), diag_P(obj), dims,
		     REAL(GET_SLOT(obj, Matrix_xSym)), dims, &rcond,
		     (double *) R_alloc(3*dims[0], sizeof(double)),
		     (int *) R_alloc(dims[0], sizeof(int)), &info);
    return ScalarReal(rcond);
}

SEXP dtrMatrix_solve(SEXP a)
{
    SEXP val = PROTECT(duplicate(a));
    int info, *Dim = INTEGER(GET_SLOT(val, Matrix_DimSym));
    F77_CALL(dtrtri)(uplo_P(val), diag_P(val), Dim,
		     REAL(GET_SLOT(val, Matrix_xSym)), Dim, &info);
    UNPROTECT(1);
    return val;
}

SEXP dtrMatrix_chol2inv(SEXP a)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dpoMatrix")));
    int info, n;

    slot_dup(val, a, Matrix_DimSym);
    slot_dup(val, a, Matrix_uploSym);
    slot_dup(val, a, Matrix_diagSym);
    slot_dup(val, a, Matrix_DimNamesSym);
    slot_dup(val, a, Matrix_xSym);
    n = *INTEGER(GET_SLOT(val, Matrix_DimSym));
    F77_CALL(dpotri)(uplo_P(val), &n,
		     REAL(GET_SLOT(val, Matrix_xSym)), &n, &info);
    UNPROTECT(1);
    return val;
}

SEXP dtrMatrix_matrix_solve(SEXP a, SEXP b)
{
    SEXP ans = PROTECT(dup_mMatrix_as_dgeMatrix(b));
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(GET_SLOT(ans, Matrix_DimSym));
    int n = bdims[0], nrhs = bdims[1];
    double one = 1.0;

    if (adims[0] != n || n != adims[1])
	error(_("Dimensions of system to be solved are inconsistent"));
    F77_CALL(dtrsm)("L", uplo_P(a), "N", diag_P(a),
		    &n, &nrhs, &one, REAL(GET_SLOT(a, Matrix_xSym)), &n,
		    REAL(GET_SLOT(ans, Matrix_xSym)), &n);
    UNPROTECT(1);
    return ans;
}

// to be used for all three: '%*%', crossprod() and tcrossprod()
/** Matrix products  dense triangular Matrices o  <matrix>
 *
 * @param a triangular matrix of class "dtrMatrix"
 * @param b a <matrix> or <any-denseMatrix>
 * @param right logical, if true, compute b %*% a,  else  a %*% b
 * @param trans logical, if true, "transpose a", i.e., use t(a), otherwise a
 *
 * @return the matrix product, one of   a %*% b, t(a) %*% b,  b %*% a, or  b %*% t(a)
 *      depending on (right, trans) =    (F, F)    (F, T)      (T, F)        (T, T)
 */
SEXP dtrMatrix_matrix_mm(SEXP a, SEXP b, SEXP right, SEXP trans)
{
    /* called from "%*%", crossprod() and tcrossprod() in  ../R/products.R
     *
     * Because 'a' must be square, the size of the answer 'val',
     * is the same as the size of 'b' */
    SEXP val = PROTECT(dup_mMatrix_as_dgeMatrix(b));
    int rt = asLogical(right); /* if(rt), compute b %*% op(a),  else  op(a) %*% b */
    int tr = asLogical(trans);/* if true, use t(a) */
    int *adims = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*bdims = INTEGER(GET_SLOT(val, Matrix_DimSym));
    int m = bdims[0], n = bdims[1];
    double one = 1.;

    if (adims[0] != adims[1])
	error(_("dtrMatrix must be square"));
    if ((rt && adims[0] != n) || (!rt && adims[1] != m))
	error(_("Matrices are not conformable for multiplication"));
    if (m >= 1 && n >= 1) {
	// Level 3 BLAS - DTRMM() --> see call further below
	F77_CALL(dtrmm)(rt ? "R" : "L", uplo_P(a),
			/*trans_A = */ tr ? "T" : "N",
			diag_P(a), &m, &n, &one,
			REAL(GET_SLOT(a, Matrix_xSym)), adims,
			REAL(GET_SLOT(val, Matrix_xSym)), &m);
    }

    SEXP
	dn_a = GET_SLOT( a,  Matrix_DimNamesSym),
	dn   = GET_SLOT(val, Matrix_DimNamesSym);
    /* matrix product   a %*% b, t(a) %*% b,  b %*% a, or  b %*% t(a)
     * (right, trans) =  (F, F)    (F, T)      (T, F)        (T, T)
     *   set:from_a   =   0:0       0:1         1:1           1:0
     */
    SET_VECTOR_ELT(dn, rt ? 1 : 0, VECTOR_ELT(dn_a, (rt+tr) % 2));

    UNPROTECT(1);
    return val;
}

/** Matrix products of dense triangular Matrices
 *
 * @param a triangular matrix of class "dtrMatrix"
 * @param b  ( ditto )
 * @param right logical, if true, compute b %*% a,  else  a %*% b
 * @param trans logical, if true, "transpose a", i.e., use t(a), otherwise a
 *
 * @return the matrix product, one of   a %*% b, t(a) %*% b,  b %*% a, or  b %*% t(a)
 *      depending on (right, trans) =    (F, F)    (F, T)      (T, F)        (T, T)
 */
SEXP dtrMatrix_dtrMatrix_mm(SEXP a, SEXP b, SEXP right, SEXP trans)
{
    /* called from "%*%" : (x,y, FALSE,FALSE),
             crossprod() : (x,y, FALSE, TRUE) , and
	     tcrossprod(): (y,x, TRUE , TRUE)
     * 	     -
     * TWO cases : (1) result is triangular  <=> uplo's "match" (i.e., non-equal iff trans)
     * ===         (2) result is "general"
     */
    SEXP val,/* = in case (2):  PROTECT(dup_mMatrix_as_dgeMatrix(b)); */
	d_a = GET_SLOT(a, Matrix_DimSym),
	uplo_a = GET_SLOT(a, Matrix_uploSym),  diag_a = GET_SLOT(a, Matrix_diagSym),
	uplo_b = GET_SLOT(b, Matrix_uploSym),  diag_b = GET_SLOT(b, Matrix_diagSym);
    int rt = asLogical(right);
    int tr = asLogical(trans);
    int *adims = INTEGER(d_a), n = adims[0];
    double *valx = (double *) NULL /*Wall*/;
    const char
	*uplo_a_ch = CHAR(STRING_ELT(uplo_a, 0)), /* = uplo_P(a) */
	*diag_a_ch = CHAR(STRING_ELT(diag_a, 0)), /* = diag_P(a) */
	*uplo_b_ch = CHAR(STRING_ELT(uplo_b, 0)), /* = uplo_P(b) */
	*diag_b_ch = CHAR(STRING_ELT(diag_b, 0)); /* = diag_P(b) */
    Rboolean same_uplo = (*uplo_a_ch == *uplo_b_ch),
	matching_uplo = tr ? (!same_uplo) : same_uplo,
	uDiag_b = /* -Wall: */ FALSE;

    if (INTEGER(GET_SLOT(b, Matrix_DimSym))[0] != n)
	/* validity checking already "assures" square matrices ... */
	error(_("\"dtrMatrix\" objects in '%*%' must have matching (square) dimension"));
    if(matching_uplo) {
	/* ==> result is triangular -- "dtrMatrix" !
	 * val := dup_mMatrix_as_dtrMatrix(b) : */
	int sz = n * n;
	val = PROTECT(NEW_OBJECT(MAKE_CLASS("dtrMatrix")));
	SET_SLOT(val, Matrix_uploSym, duplicate(uplo_b));
	SET_SLOT(val, Matrix_DimSym,  duplicate(d_a));
	SET_DimNames(val, b);
	valx = REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, sz));
	Memcpy(valx, REAL(GET_SLOT(b, Matrix_xSym)), sz);
	if((uDiag_b = (*diag_b_ch == 'U'))) {
	    /* unit-diagonal b - may contain garbage in diagonal */
	    for (int i = 0; i < n; i++)
		valx[i * (n+1)] = 1.;
	}
    } else { /* different "uplo" ==> result is "dgeMatrix" ! */
	val = PROTECT(dup_mMatrix_as_dgeMatrix(b));
	SEXP
	    dn_a = GET_SLOT( a , Matrix_DimNamesSym),
	    dn   = GET_SLOT(val, Matrix_DimNamesSym);
	/* matrix product   a %*% b, t(a) %*% b,  b %*% a, or  b %*% t(a)
	 * (right, trans) =  (F, F)    (F, T)      (T, F)        (T, T)
	 *   set:from_a   =   0:0       0:1         1:1           1:0
	 */
	SET_VECTOR_ELT(dn, rt ? 1 : 0, VECTOR_ELT(dn_a, (rt+tr) % 2));
    }
    if (n >= 1) {
	double alpha = 1.;
	/* Level 3 BLAS - DTRMM(): Compute one of the matrix multiplication operations
	 * B := alpha*op( A )*B ["L"], or B := alpha*B*op( A ) ["R"],
	 *	where trans_A determines  op(A):=  A   "N"one  or
	 *				  op(A):= t(A) "T"ransposed */
	F77_CALL(dtrmm)(rt ? "R" : "L", uplo_a_ch,
			/*trans_A = */ tr ? "T" : "N", diag_a_ch, &n, &n, &alpha,
			REAL(GET_SLOT(a,   Matrix_xSym)), adims,
			REAL(GET_SLOT(val, Matrix_xSym)), &n);
    }
    if(matching_uplo) {
	make_d_matrix_triangular(valx, tr ? b : a); /* set "other triangle" to 0 */
	if(*diag_a_ch == 'U' && uDiag_b) /* result remains uni-diagonal */
	    SET_SLOT(val, Matrix_diagSym, duplicate(diag_a));
    }
    UNPROTECT(1);
    return val;
}


SEXP dtrMatrix_as_matrix(SEXP from, SEXP keep_dimnames)
{
    int *Dim = INTEGER(GET_SLOT(from, Matrix_DimSym));
    int m = Dim[0], n = Dim[1];
    SEXP val = PROTECT(allocMatrix(REALSXP, m, n));
    make_d_matrix_triangular(Memcpy(REAL(val),
				    REAL(GET_SLOT(from, Matrix_xSym)), m * n),
			     from);
    if(asLogical(keep_dimnames))
	setAttrib(val, R_DimNamesSymbol, GET_SLOT(from, Matrix_DimNamesSym));
    UNPROTECT(1);
    return val;
}

#define GET_trMatrix_Diag(_C_TYPE_, _SEXPTYPE_, _SEXP_, _ONE_)		\
    int i, n = INTEGER(GET_SLOT(x, Matrix_DimSym))[0];			\
    SEXP x_x = GET_SLOT(x, Matrix_xSym);				\
									\
    SEXP ret = PROTECT(allocVector(_SEXPTYPE_, n));			\
    _C_TYPE_ *rv = _SEXP_(ret),						\
	     *xv = _SEXP_(x_x);						\
									\
    if ('U' == diag_P(x)[0]) {						\
	for (i = 0; i < n; i++) rv[i] = _ONE_;				\
    } else {								\
	for (i = 0; i < n; i++) rv[i] = xv[i * (n + 1)];		\
    }									\
    UNPROTECT(1);							\
    return ret


SEXP dtrMatrix_getDiag(SEXP x) {
    GET_trMatrix_Diag(double, REALSXP, REAL, 1.);
}

SEXP ltrMatrix_getDiag(SEXP x) {
    GET_trMatrix_Diag(  int, LGLSXP, LOGICAL, 1);
}

#define SET_trMatrix_Diag(_C_TYPE_, _SEXP_)				\
    if ('U' == diag_P(x)[0])						\
	error(_("cannot set diag() as long as 'diag = \"U\"'"));	\
			    /* careful to recycle RHS value: */		\
    int n = INTEGER(GET_SLOT(x, Matrix_DimSym))[0];			\
    int l_d = LENGTH(d); Rboolean d_full = (l_d == n);			\
    if (!d_full && l_d != 1)						\
	error(_("replacement diagonal has wrong length"));		\
    SEXP ret = PROTECT(duplicate(x)),					\
	r_x = GET_SLOT(ret, Matrix_xSym);				\
    _C_TYPE_ *dv = _SEXP_(d),						\
	     *rv = _SEXP_(r_x);						\
									\
    if(d_full) for (int i = 0; i < n; i++)				\
	rv[i * (n + 1)] = dv[i];					\
    else for (int i = 0; i < n; i++)					\
	rv[i * (n + 1)] = *dv;						\
									\
    UNPROTECT(1);							\
    return ret

SEXP dtrMatrix_setDiag(SEXP x, SEXP d) {
    SET_trMatrix_Diag(double, REAL);
}

SEXP ltrMatrix_setDiag(SEXP x, SEXP d) {
    SET_trMatrix_Diag(  int, LOGICAL);
}

SEXP dtrMatrix_addDiag(SEXP x, SEXP d) {
    int n = INTEGER(GET_SLOT(x, Matrix_DimSym))[0];
    SEXP ret = PROTECT(duplicate(x)),
	r_x = GET_SLOT(ret, Matrix_xSym);
    double *dv = REAL(d), *rv = REAL(r_x);

    if ('U' == diag_P(x)[0])
	error(_("cannot add diag() as long as 'diag = \"U\"'"));
    for (int i = 0; i < n; i++) rv[i * (n + 1)] += dv[i];

    UNPROTECT(1);
    return ret;
}


SEXP dtrMatrix_as_dtpMatrix(SEXP from)
{
    SEXP val = PROTECT(NEW_OBJECT(MAKE_CLASS("dtpMatrix"))),
	uplo = GET_SLOT(from, Matrix_uploSym),
	diag = GET_SLOT(from, Matrix_diagSym),
	dimP = GET_SLOT(from, Matrix_DimSym);
    int n = *INTEGER(dimP);

    SET_SLOT(val, Matrix_DimSym, duplicate(dimP));
    SET_SLOT(val, Matrix_diagSym, duplicate(diag));
    SET_SLOT(val, Matrix_uploSym, duplicate(uplo));
    full_to_packed_double(
	REAL(ALLOC_SLOT(val, Matrix_xSym, REALSXP, (n*(n+1))/2)),
	REAL(GET_SLOT(from, Matrix_xSym)), n,
	*CHAR(STRING_ELT(uplo, 0)) == 'U' ? UPP : LOW,
	*CHAR(STRING_ELT(diag, 0)) == 'U' ? UNT : NUN);
    SET_SLOT(val, Matrix_DimNamesSym,
	     duplicate(GET_SLOT(from, Matrix_DimNamesSym)));
    UNPROTECT(1);
    return val;
}
