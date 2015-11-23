#include <limits.h>

#include "Mutils.h"
#include <R_ext/Lapack.h>

// La_norm_type() & La_rcond_type()  have been in R_ext/Lapack.h
//  but have still not been available to package writers ...
char La_norm_type(const char *typstr)
{
    char typup;

    if (strlen(typstr) != 1)
	error(
	    _("argument type[1]='%s' must be a one-letter character string"),
	    typstr);
    typup = toupper(*typstr);
    if (typup == '1')
	typup = 'O'; /* alias */
    else if (typup == 'E')
	typup = 'F';
    else if (typup != 'M' && typup != 'O' && typup != 'I' && typup != 'F')
	error(_("argument type[1]='%s' must be one of 'M','1','O','I','F' or 'E'"),
	      typstr);
    return typup;
}

char La_rcond_type(const char *typstr)
{
    char typup;

    if (strlen(typstr) != 1)
	error(
	    _("argument type[1]='%s' must be a one-letter character string"),
	    typstr);
    typup = toupper(*typstr);
    if (typup == '1')
	typup = 'O'; /* alias */
    else if (typup != 'O' && typup != 'I')
	error(_("argument type[1]='%s' must be one of '1','O', or 'I'"),
	      typstr);
    return typup;
}

double get_double_by_name(SEXP obj, char *nm)
{
    SEXP nms = getAttrib(obj, R_NamesSymbol);
    int i, len = length(obj);

    if ((!isReal(obj)) || (length(obj) > 0 && nms == R_NilValue))
	error(_("object must be a named, numeric vector"));
    for (i = 0; i < len; i++) {
	if (!strcmp(nm, CHAR(STRING_ELT(nms, i)))) {
	    return REAL(obj)[i];
	}
    }
    return R_NaReal;
}

SEXP
set_double_by_name(SEXP obj, double val, char *nm)
{
    SEXP nms = getAttrib(obj, R_NamesSymbol);
    int i, len = length(obj);

    if ((!isReal(obj)) || (length(obj) > 0 && nms == R_NilValue))
	error(_("object must be a named, numeric vector"));
    for (i = 0; i < len; i++) {
	if (!strcmp(nm, CHAR(STRING_ELT(nms, i)))) {
	    REAL(obj)[i] = val;
	    return obj;
	}
    }
    {
	SEXP nx = PROTECT(allocVector(REALSXP, len + 1)),
	    nnms = allocVector(STRSXP, len + 1);

	setAttrib(nx, R_NamesSymbol, nnms);
	for (i = 0; i < len; i++) {
	    REAL(nx)[i] = REAL(obj)[i];
	    SET_STRING_ELT(nnms, i, duplicate(STRING_ELT(nms, i)));
	}
	REAL(nx)[len] = val;
	SET_STRING_ELT(nnms, len, mkChar(nm));
	UNPROTECT(1);
	return nx;
    }
}

SEXP as_det_obj(double val, int log, int sign)
{
    SEXP det = PROTECT(allocVector(VECSXP, 2)),
	nms = PROTECT(allocVector(STRSXP, 2)),
	vv = PROTECT(ScalarReal(val));

    setAttrib(det, R_NamesSymbol, nms);
    SET_STRING_ELT(nms, 0, mkChar("modulus"));
    SET_STRING_ELT(nms, 1, mkChar("sign"));
    setAttrib(vv, install("logarithm"), ScalarLogical(log));
    SET_VECTOR_ELT(det, 0, vv);
    SET_VECTOR_ELT(det, 1, ScalarInteger(sign));
    setAttrib(det, R_ClassSymbol, mkString("det"));
    UNPROTECT(3);
    return det;
}

SEXP get_factors(SEXP obj, char *nm)
{
    SEXP fac = GET_SLOT(obj, Matrix_factorSym),
	nms = getAttrib(fac, R_NamesSymbol);
    int i, len = length(fac);

    if ((!isNewList(fac)) || (length(fac) > 0 && nms == R_NilValue))
	error(_("'factors' slot must be a named list"));
    for (i = 0; i < len; i++) {
	if (!strcmp(nm, CHAR(STRING_ELT(nms, i)))) {
	    return VECTOR_ELT(fac, i);
	}
    }
    return R_NilValue;
}

/**
 * Caches 'val' in the 'factors' slot of obj, i.e. modifies obj, and
 * returns val.
 * In the past this function installed a duplicate of
 * factors slot for obj then returned the (typically unprotected)
 * val.  This is now changed to return the duplicate, which will be
 * protected if obj is protected.
*/
SEXP set_factors(SEXP obj, SEXP val, char *nm)
{
    SEXP fac = GET_SLOT(obj, Matrix_factorSym),
	nms = getAttrib(fac, R_NamesSymbol);
    int i, len = length(fac);

    if ((!isNewList(fac)) || (length(fac) > 0 && nms == R_NilValue))
	error(_("'factors' slot must be a named list"));
    PROTECT(val); /* set_factors(..) may be called as "finalizer" after UNPROTECT()*/
    // if there's already a 'nm' factor, we replace it and return:
    for (i = 0; i < len; i++) {
	if (!strcmp(nm, CHAR(STRING_ELT(nms, i)))) {
	    SET_VECTOR_ELT(fac, i, duplicate(val));
	    UNPROTECT(1);
	    return val;
	}
    }
    // Otherwise: create a new 'nm' entry in the 'factors' list:
    // create a list of length (len + 1),
    SEXP nfac = PROTECT(allocVector(VECSXP, len + 1)),
	 nnms = PROTECT(allocVector(STRSXP, len + 1));
    setAttrib(nfac, R_NamesSymbol, nnms);
    // copy all the previous entries,
    for (i = 0; i < len; i++) {
	SET_VECTOR_ELT(nfac, i, VECTOR_ELT(fac, i));
	SET_STRING_ELT(nnms, i, duplicate(STRING_ELT(nms, i)));
    }
    // and add the new entry at the end.
    SET_VECTOR_ELT(nfac, len, duplicate(val));
    SET_STRING_ELT(nnms, len, mkChar(nm));
    SET_SLOT(obj, Matrix_factorSym, nfac);
    UNPROTECT(3);
    return VECTOR_ELT(nfac, len);
}

// R interface [for updating the '@ factors' slot of a function *argument* [CARE!]
SEXP R_set_factors(SEXP obj, SEXP val, SEXP name, SEXP warn)
{
    Rboolean do_warn = asLogical(warn);
    if(R_has_slot(obj, Matrix_factorSym))
	return set_factors(obj, val, (char *)CHAR(asChar(name)));
    else {
	if(do_warn) warning(_("Matrix object has no 'factors' slot"));
	return val;
    }
}

#if 0 				/* unused */
/* useful for all the ..CMatrix classes (and ..R by [0] <-> [1]); but unused */
SEXP CMatrix_set_Dim(SEXP x, int nrow)
{
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym));

    dims[0] = nrow;
    dims[1] = length(GET_SLOT(x, Matrix_pSym)) - 1;
    return x;
}
#endif	/* unused */

/* Fill in the "trivial remainder" in  n*m  array ;
 *  typically the 'x' slot of a "dtrMatrix", such that
 *  it should be usable for double/logical/int/complex : */
#define MAKE_TRIANGULAR_BODY(_TO_, _FROM_, _ZERO_, _ONE_)	\
{								\
    int i, j, *dims = INTEGER(GET_SLOT(_FROM_, Matrix_DimSym));	\
    int n = dims[0], m = dims[1];				\
								\
    if (*uplo_P(_FROM_) == 'U') {				\
	for (j = 0; j < n; j++)					\
	    for (i = j+1; i < m; i++)				\
		_TO_[i + j*m] = _ZERO_;				\
    } else {							\
	for (j = 1; j < n; j++)					\
	    for (i = 0; i < j && i < m; i++)			\
		_TO_[i + j*m] = _ZERO_;				\
    }								\
    if (*diag_P(_FROM_) == 'U') {				\
	j = (n < m) ? n : m;					\
	for (i = 0; i < j; i++)					\
	    _TO_[i * (m + 1)] = _ONE_;				\
    }								\
}

void make_d_matrix_triangular(double *to, SEXP from)
    MAKE_TRIANGULAR_BODY(to, from, 0., 1.)
void make_i_matrix_triangular(int *to, SEXP from)
    MAKE_TRIANGULAR_BODY(to, from, 0, 1)


/* Should work for double/logical/int/complex : */
#define MAKE_SYMMETRIC_BODY(_TO_, _FROM_)			\
{								\
    int i, j, n = INTEGER(GET_SLOT(_FROM_, Matrix_DimSym))[0];	\
								\
    if (*uplo_P(_FROM_) == 'U') {				\
	for (j = 0; j < n; j++)					\
	    for (i = j+1; i < n; i++)				\
		_TO_[i + j*n] = _TO_[j + i*n];			\
    } else {							\
	for (j = 1; j < n; j++)					\
	    for (i = 0; i < j && i < n; i++)			\
		_TO_[i + j*n] = _TO_[j + i*n];			\
    }								\
}

void make_d_matrix_symmetric(double *to, SEXP from)
    MAKE_SYMMETRIC_BODY(to, from)

void make_i_matrix_symmetric(int *to, SEXP from)
    MAKE_SYMMETRIC_BODY(to, from)


#define Matrix_Error_Bufsiz    4096

/**
 * Check validity of 1-letter string from a set of possible values
 * (typically used in  S4 validity method)
 *
 * @param sP
 * @param vals a string containing the possible valid letters
 * @param nm   the name of the slot being checked
 *
 * @return a SEXP, either NULL (= success) or an error message
 */
SEXP check_scalar_string(SEXP sP, char *vals, char *nm)
{
    SEXP val = ScalarLogical(1);
    char *buf;
    /* only allocate when needed: in good case, none is needed */
#define SPRINTF buf = Alloca(Matrix_Error_Bufsiz, char); R_CheckStack(); sprintf

    if (length(sP) != 1) {
	SPRINTF(buf, _("'%s' slot must have length 1"), nm);
    } else {
	const char *str = CHAR(STRING_ELT(sP, 0));
	if (strlen(str) != 1) {
	    SPRINTF(buf, _("'%s' must have string length 1"), nm);
	} else {
	    int i, len;
	    for (i = 0, len = strlen(vals); i < len; i++) {
		if (str[0] == vals[i])
		    return R_NilValue;
	    }
	    SPRINTF(buf, _("'%s' must be in '%s'"), nm, vals);
	}
    }
    /* 'error' returns : */
    val = mkString(buf);
    return val;
#undef SPRINTF
}

/* FIXME? Something like this should be part of the R API ?
 *        But then, R has the more general  R_compute_identical()
 * in src/main/identical.c: Rboolean R_compute_identical(SEXP x, SEXP y);
*/
Rboolean equal_string_vectors(SEXP s1, SEXP s2)
{
    Rboolean n1 = isNull(s1), n2 = isNull(s2);
    if (n1 || n2) // if one is NULL : "equal" if both are
	return (n1 == n2) ? TRUE : FALSE;
    else if (TYPEOF(s1) != STRSXP || TYPEOF(s2) != STRSXP) {
	error(_("'s1' and 's2' must be \"character\" vectors"));
	return FALSE; /* -Wall */
    } else {
	int n = LENGTH(s1), i;
	if (n != LENGTH(s2))
	    return FALSE;
	for(i = 0; i < n; i++) {
	    /* note that compute_identical() code for STRSXP
	       is careful about NA's which we don't need */
	    if(strcmp(CHAR(STRING_ELT(s1, i)),
		      CHAR(STRING_ELT(s2, i))))
		return FALSE;
	}
	return TRUE; /* they *are* equal */
    }
}


SEXP dense_nonpacked_validate(SEXP obj)
{
    int *dims = INTEGER(GET_SLOT(obj, Matrix_DimSym));
    if ((dims[0] * dims[1]) != length(GET_SLOT(obj, Matrix_xSym)))
	return mkString(_("length of x slot != prod(Dim)"));
    return ScalarLogical(1);
}

SEXP dim_validate(SEXP Dim, const char* name) {
    if (length(Dim) != 2)
	return mkString(_("Dim slot must have length 2"));
    if (TYPEOF(Dim) != INTSXP && TYPEOF(Dim) != REALSXP)
	return mkString(_("Dim slot is not numeric"));
    int
	m = INTEGER(Dim)[0],
	n = INTEGER(Dim)[1];
    if (m < 0 || n < 0)
	return mkString(dngettext(name,
				  "Negative value in Dim",
				  "Negative values in Dim",
				  (m*n > 0) ? 2 : 1));
    return ScalarLogical(1);
}
// to be called from R :
SEXP Dim_validate(SEXP obj, SEXP name) {
    return dim_validate(GET_SLOT(obj, Matrix_DimSym),
			CHAR(STRING_ELT(name, 0)));
}

// obj must have @Dim and @Dimnames. Assume 'Dim' is already checked.
SEXP dimNames_validate(SEXP obj)
{
    int *dims = INTEGER(GET_SLOT(obj, Matrix_DimSym));
    SEXP dmNms = GET_SLOT(obj, Matrix_DimNamesSym);
    if(!isNewList(dmNms))
	return mkString(_("Dimnames slot is not a list"));
    if(length(dmNms) != 2)
	return mkString(_("Dimnames slot is a list, but not of length 2"));
    char buf[99];
    for(int j=0; j < 2; j++) { // x@Dimnames[j] must be NULL or character(length(x@Dim[j]))
	if(!isNull(VECTOR_ELT(dmNms, j))) {
	    if(TYPEOF(VECTOR_ELT(dmNms, j)) != STRSXP) {
		sprintf(buf, _("Dimnames[%d] is not a character vector"), j+1);
		return mkString(buf);
	    }
	    if(LENGTH(VECTOR_ELT(dmNms, j)) != 0 && // character(0) allowed here
	       LENGTH(VECTOR_ELT(dmNms, j)) != dims[j]) {
		sprintf(buf, _("length(Dimnames[%d]) differs from Dim[%d] which is %d"),
			j+1, j+1, dims[j]);
		return mkString(buf);
	    }
	}
    }
    return ScalarLogical(1);
}



#define PACKED_TO_FULL(TYPE)						\
TYPE *packed_to_full_ ## TYPE(TYPE *dest, const TYPE *src,		\
		        int n, enum CBLAS_UPLO uplo)			\
{									\
    int i, j, pos = 0;							\
									\
    AZERO(dest, n*n);							\
    for (j = 0; j < n; j++) {						\
	switch(uplo) {							\
	case UPP:							\
	    for (i = 0; i <= j; i++) dest[i + j * n] = src[pos++];	\
	    break;							\
	case LOW:							\
	    for (i = j; i < n; i++) dest[i + j * n] = src[pos++];	\
	    break;							\
	default:							\
	    error(_("'uplo' must be UPP or LOW"));			\
	}								\
    }									\
    return dest;							\
}

PACKED_TO_FULL(double)
PACKED_TO_FULL(int)

#define FULL_TO_PACKED(TYPE)						\
TYPE *full_to_packed_ ## TYPE(TYPE *dest, const TYPE *src, int n,	\
		      enum CBLAS_UPLO uplo, enum CBLAS_DIAG diag)	\
{									\
    int i, j, pos = 0;							\
									\
    for (j = 0; j < n; j++) {						\
	switch(uplo) {							\
	case UPP:							\
	    for (i = 0; i <= j; i++)					\
		dest[pos++] = (i == j && diag== UNT) ? 1 : src[i + j*n]; \
	    break;							\
	case LOW:							\
	    for (i = j; i < n; i++)					\
		dest[pos++] = (i == j && diag== UNT) ? 1 : src[i + j*n]; \
	    break;							\
	default:							\
	    error(_("'uplo' must be UPP or LOW"));			\
	}								\
    }									\
    return dest;							\
}

FULL_TO_PACKED(double)
FULL_TO_PACKED(int)



/**
 * Copy the diagonal elements of the packed denseMatrix x to dest
 *
 * @param dest vector of length ncol(x)
 * @param x (pointer to) a "d?pMatrix" object
 * @param n number of columns in the matrix represented by x
 *
 * @return dest
 */
void d_packed_getDiag(double *dest, SEXP x, int n)
{
    double *xx = REAL(GET_SLOT(x, Matrix_xSym));

#define END_packed_getDiag						\
    int j, pos = 0;							\
									\
    if (*uplo_P(x) == 'U') {						\
	for(pos= 0, j=0; j < n; pos += 1+(++j))	 dest[j] = xx[pos];	\
    } else {								\
	for(pos= 0, j=0; j < n; pos += (n - j), j++) dest[j] = xx[pos]; \
    }									\
    return

    END_packed_getDiag;
}

void l_packed_getDiag(int *dest, SEXP x, int n)
{
    int *xx = LOGICAL(GET_SLOT(x, Matrix_xSym));

    END_packed_getDiag;
}

#undef END_packed_getDiag

//----------------------------------------------------------------------

SEXP d_packed_setDiag(double *diag, int l_d, SEXP x, int n)
{
#define SET_packed_setDiag				\
    SEXP ret = PROTECT(duplicate(x)),			\
	r_x = GET_SLOT(ret, Matrix_xSym);		\
    Rboolean d_full = (l_d == n);			\
    if (!d_full && l_d != 1)				\
	error(_("replacement diagonal has wrong length"))

#define END_packed_setDiag						\
    int j, pos = 0;							\
									\
    if (*uplo_P(x) == 'U') {						\
	if(d_full)							\
	    for(pos= 0, j=0; j < n; pos += 1+(++j))	 xx[pos] = diag[j]; \
	else /* l_d == 1 */						\
	    for(pos= 0, j=0; j < n; pos += 1+(++j))	 xx[pos] = *diag; \
    } else {								\
	if(d_full)							\
	    for(pos= 0, j=0; j < n; pos += (n - j), j++) xx[pos] = diag[j]; \
	else /* l_d == 1 */						\
	    for(pos= 0, j=0; j < n; pos += (n - j), j++) xx[pos] = *diag; \
    }									\
    UNPROTECT(1);							\
    return ret

    SET_packed_setDiag; double *xx = REAL(r_x);
    END_packed_setDiag;
}

SEXP l_packed_setDiag(int *diag, int l_d, SEXP x, int n)
{
    SET_packed_setDiag; int *xx = LOGICAL(r_x);
    END_packed_setDiag;
}

#define tr_END_packed_setDiag						\
    if (*diag_P(x) == 'U') { /* uni-triangular */			\
	/* after setting, typically is not uni-triangular anymore: */	\
	SET_STRING_ELT(GET_SLOT(ret, Matrix_diagSym), 0, mkChar("N"));	\
    }									\
    END_packed_setDiag


SEXP tr_d_packed_setDiag(double *diag, int l_d, SEXP x, int n)
{
    SET_packed_setDiag; double *xx = REAL(r_x);
    tr_END_packed_setDiag;
}

SEXP tr_l_packed_setDiag(int *diag, int l_d, SEXP x, int n)
{
    SET_packed_setDiag; int *xx = LOGICAL(r_x);
    tr_END_packed_setDiag;
}


#undef SET_packed_setDiag
#undef END_packed_setDiag
#undef tr_END_packed_setDiag
//----------------------------------------------------------------------

SEXP d_packed_addDiag(double *diag, int l_d, SEXP x, int n)
{
    SEXP ret = PROTECT(duplicate(x)),
	r_x = GET_SLOT(ret, Matrix_xSym);
    double *xx = REAL(r_x);
    int j, pos = 0;

    if (*uplo_P(x) == 'U') {
	for(pos= 0, j=0; j < n; pos += 1+(++j))	     xx[pos] += diag[j];
    } else {
	for(pos= 0, j=0; j < n; pos += (n - j), j++) xx[pos] += diag[j];
    }
    UNPROTECT(1);
    return ret;
}

SEXP tr_d_packed_addDiag(double *diag, int l_d, SEXP x, int n)
{
    SEXP ret = PROTECT(d_packed_addDiag(diag, l_d, x, n));
    if (*diag_P(x) == 'U') /* uni-triangular */
	SET_STRING_ELT(GET_SLOT(ret, Matrix_diagSym), 0, mkChar("N"));
    UNPROTECT(1);
    return ret;
}


//----------------------------------------------------------------------

void tr_d_packed_getDiag(double *dest, SEXP x, int n)
{
    if (*diag_P(x) == 'U') {
	for (int j = 0; j < n; j++) dest[j] = 1.;
    } else {
	d_packed_getDiag(dest, x, n);
    }
    return;
}

void tr_l_packed_getDiag(   int *dest, SEXP x, int n)
{
    if (*diag_P(x) == 'U')
	for (int j = 0; j < n; j++) dest[j] = 1;
    else
	l_packed_getDiag(dest, x, n);
    return;
}


SEXP Matrix_expand_pointers(SEXP pP)
{
    int n = length(pP) - 1;
    int *p = INTEGER(pP);
    SEXP ans = PROTECT(allocVector(INTSXP, p[n]));

    expand_cmprPt(n, p, INTEGER(ans));
    UNPROTECT(1);
    return ans;
}


/**
 * Return the element of a given name from a named list
 *
 * @param list
 * @param nm name of desired element
 *
 * @return element of list with name nm
 */
SEXP
Matrix_getElement(SEXP list, char *nm) {
    SEXP names = getAttrib(list, R_NamesSymbol);
    int i;

    for (i = 0; i < LENGTH(names); i++)
	if (!strcmp(CHAR(STRING_ELT(names, i)), nm))
	    return(VECTOR_ELT(list, i));
    return R_NilValue;
}

/**
 * Zero a square matrix of size nc then copy a vector to the diagonal
 *
 * @param dest destination array of length nc * nc
 * @param A pointer to a square Matrix object
 *
 * @return dest
 */
static double *
install_diagonal(double *dest, SEXP A)
{
    int nc = INTEGER(GET_SLOT(A, Matrix_DimSym))[0];
    int i, ncp1 = nc + 1, unit = *diag_P(A) == 'U';
    double *ax = REAL(GET_SLOT(A, Matrix_xSym));

    AZERO(dest, nc * nc);
    for (i = 0; i < nc; i++)
	dest[i * ncp1] = (unit) ? 1. : ax[i];
    return dest;
}

static int *
install_diagonal_int(int *dest, SEXP A)
{
    int nc = INTEGER(GET_SLOT(A, Matrix_DimSym))[0];
    int i, ncp1 = nc + 1, unit = *diag_P(A) == 'U';
    int *ax = INTEGER(GET_SLOT(A, Matrix_xSym));

    AZERO(dest, nc * nc);
    for (i = 0; i < nc; i++)
	dest[i * ncp1] = (unit) ? 1 : ax[i];
    return dest;
}


/** @brief Duplicate a [dln]denseMatrix _or_ a numeric matrix or even a vector
 *  as a [dln]geMatrix.
 *
 *  This is for the many "*_matrix_{prod,crossprod,tcrossprod, etc.}"
 *  functions that work with both classed and unclassed matrices.
 *
 *  Used generally for Generalized -- "geMatrix" -- dispatch where needed.
 *
 * @param A either a denseMatrix, a diagonalMatrix or a traditional matrix object
 *
 */
SEXP dup_mMatrix_as_geMatrix(SEXP A)
{
 /* NOTA BENE: If you enlarge this list, do change '14' and '6' below !
  * ---------  ddiMatrix & ldiMatrix  are no longer ddense or ldense on the R level,
  *            ---         ---        but are still dealt with here: */
    static const char *valid[] = {
	"_NOT_A_CLASS_",// *_CLASSES defined in ./Mutils.h  :
	MATRIX_VALID_ddense, /* 14 */
	MATRIX_VALID_ldense, /* 6  */
	MATRIX_VALID_ndense, /* 5  */
	""};
    SEXP ans, ad = R_NilValue, an = R_NilValue;	/* -Wall */
    int sz, ctype = Matrix_check_class_etc(A, valid),
	nprot = 1;
    enum dense_enum M_type = ddense /* -Wall */;

    if (ctype > 0) { /* a [nld]denseMatrix or [dl]diMatrix object */
	ad = GET_SLOT(A, Matrix_DimSym);
	an = GET_SLOT(A, Matrix_DimNamesSym);
	M_type = (ctype <= 14) ? ddense :
	    ((ctype <= 14+6) ? ldense : ndense);
    }
    else if (ctype < 0) {	/* not a (recognized) classed matrix */

	if (isReal(A))
	    M_type = ddense;
	else if (isInteger(A)) {
	    A = PROTECT(coerceVector(A, REALSXP));
	    nprot++;
	    M_type = ddense;
	}
	else if (isLogical(A))
	    M_type = ldense;
	else
	    error(_("invalid class '%s' to dup_mMatrix_as_geMatrix"),
		  class_P(A));

#define	DUP_MMATRIX_NON_CLASS(transpose_if_vec)				\
	if (isMatrix(A)) {	/* "matrix" */				\
	    ad = getAttrib(A, R_DimSymbol);				\
	    an = getAttrib(A, R_DimNamesSymbol);			\
	} else {/* maybe "numeric" (incl integer,logical) --> (n x 1) */ \
	    int* dd = INTEGER(ad = PROTECT(allocVector(INTSXP, 2)));	\
	    nprot++;							\
	    if(transpose_if_vec) {					\
		dd[0] = 1;						\
		dd[1] = LENGTH(A);					\
	    } else {							\
		dd[0] = LENGTH(A);					\
		dd[1] = 1;						\
	    }								\
	    SEXP nms = getAttrib(A, R_NamesSymbol);			\
	    if(nms != R_NilValue) {					\
		an = PROTECT(allocVector(VECSXP, 2));			\
		nprot++;						\
	        SET_VECTOR_ELT(an, (transpose_if_vec)? 1 : 0, nms);	\
		/* not needed: SET_VECTOR_ELT(an, 1, R_NilValue); */    \
	    } /* else nms = NULL ==> an remains NULL */                 \
 	}								\
	ctype = 0

	DUP_MMATRIX_NON_CLASS(FALSE);
    }

    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(M_type == ddense ? "dgeMatrix" :
					(M_type == ldense ? "lgeMatrix" :
					 "ngeMatrix"))));
#define DUP_MMATRIX_SET_1					\
    SET_SLOT(ans, Matrix_DimSym, duplicate(ad));		\
    SET_SLOT(ans, Matrix_DimNamesSym, (LENGTH(an) == 2) ? 	\
	     duplicate(an): allocVector(VECSXP, 2));		\
    sz = INTEGER(ad)[0] * INTEGER(ad)[1]

    DUP_MMATRIX_SET_1;

    if(M_type == ddense) { /* ddense -> dge */

	double *ansx;

#define DUP_MMATRIX_ddense_CASES						\
	ansx = REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, sz));			\
	switch(ctype) {								\
	case 0:			/* unclassed real matrix */			\
	    Memcpy(ansx, REAL(A), sz);						\
	    break;								\
	case 1:			/* dgeMatrix */					\
	    Memcpy(ansx, REAL(GET_SLOT(A, Matrix_xSym)), sz);			\
	    break;								\
	case 2:			/* dtrMatrix   and subclasses */		\
	case 9: case 10: case 11:   /* ---	Cholesky, LDL, BunchKaufman */	\
	    Memcpy(ansx, REAL(GET_SLOT(A, Matrix_xSym)), sz);			\
	    make_d_matrix_triangular(ansx, A);					\
	    break;								\
	case 3:			/* dsyMatrix */					\
	case 4:			/* dpoMatrix  + subclass */			\
	case 14:	 		/* ---	corMatrix */			\
	    Memcpy(ansx, REAL(GET_SLOT(A, Matrix_xSym)), sz);			\
	    make_d_matrix_symmetric(ansx, A);					\
	    break;								\
	case 5:			/* ddiMatrix */					\
	    install_diagonal(ansx, A);						\
	    break;								\
	case 6:			/* dtpMatrix  + subclasses */			\
	case 12: case 13: 		/* ---	pCholesky, pBunchKaufman */	\
	    packed_to_full_double(ansx, REAL(GET_SLOT(A, Matrix_xSym)),		\
				  INTEGER(ad)[0],				\
				  *uplo_P(A) == 'U' ? UPP : LOW);		\
	    make_d_matrix_triangular(ansx, A);					\
	    break;								\
	case 7:			/* dspMatrix */					\
	case 8:			/* dppMatrix */					\
	    packed_to_full_double(ansx, REAL(GET_SLOT(A, Matrix_xSym)),		\
				  INTEGER(ad)[0],				\
				  *uplo_P(A) == 'U' ? UPP : LOW);		\
	    make_d_matrix_symmetric(ansx, A);					\
	    break;								\
	}  /* switch(ctype) */

	DUP_MMATRIX_ddense_CASES;
    }
    else { /* M_type == ldense || M_type = ndense  */
	/* ldense -> lge */
	/* ndense -> nge */
	int *ansx = LOGICAL(ALLOC_SLOT(ans, Matrix_xSym, LGLSXP, sz));

	switch(ctype) {
	case 0:			/* unclassed logical matrix */
	    Memcpy(ansx, LOGICAL(A), sz);
	    break;

	case 1+14:			/* lgeMatrix */
	case 1+14+6:			/* ngeMatrix */
	    Memcpy(ansx, LOGICAL(GET_SLOT(A, Matrix_xSym)), sz);
	    break;
	case 2+14:			/* ltrMatrix */
	case 2+14+6:			/* ntrMatrix */
	    Memcpy(ansx, LOGICAL(GET_SLOT(A, Matrix_xSym)), sz);
	    make_i_matrix_triangular(ansx, A);
	    break;
	case 3+14:			/* lsyMatrix */
	case 3+14+6:			/* nsyMatrix */
	    Memcpy(ansx, LOGICAL(GET_SLOT(A, Matrix_xSym)), sz);
	    make_i_matrix_symmetric(ansx, A);
	    break;
	case 4+14:			/* ldiMatrix */
	    // case 4+14+6:      /* ndiMatrix _DOES NOT EXIST_ */
	    install_diagonal_int(ansx, A);
	    break;
	case 5+14:			/* ltpMatrix */
	case 4+14+6:			/* ntpMatrix */
	    packed_to_full_int(ansx, LOGICAL(GET_SLOT(A, Matrix_xSym)),
			       INTEGER(ad)[0],
			       *uplo_P(A) == 'U' ? UPP : LOW);
	    make_i_matrix_triangular(ansx, A);
	    break;
	case 6+14:			/* lspMatrix */
	case 5+14+6:			/* nspMatrix */
	    packed_to_full_int(ansx, LOGICAL(GET_SLOT(A, Matrix_xSym)),
			       INTEGER(ad)[0],
			       *uplo_P(A) == 'U' ? UPP : LOW);
	    make_i_matrix_symmetric(ansx, A);
	    break;

	default:
	    error(_("unexpected ctype = %d in dup_mMatrix_as_geMatrix"), ctype);
	}  /* switch(ctype) */

    }  /* if(M_type == .) */

    UNPROTECT(nprot);
    return ans;
}

SEXP dup_mMatrix_as_dgeMatrix2(SEXP A, Rboolean tr_if_vec)
{
    SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),
	ad = R_NilValue , an = R_NilValue;	/* -Wall */
    static const char *valid[] = {"_NOT_A_CLASS_", MATRIX_VALID_ddense, ""};
    int ctype = Matrix_check_class_etc(A, valid),
	nprot = 1, sz;
    double *ansx;

    if (ctype > 0) {		/* a ddenseMatrix object */
	ad = GET_SLOT(A, Matrix_DimSym);
	an = GET_SLOT(A, Matrix_DimNamesSym);
    }
    else if (ctype < 0) {	/* not a (recognized) classed matrix */
	if (!isReal(A)) {
	    if (isInteger(A) || isLogical(A)) {
		A = PROTECT(coerceVector(A, REALSXP));
		nprot++;
	    } else
		error(_("invalid class '%s' to dup_mMatrix_as_dgeMatrix"),
		      class_P(A));
	}
	DUP_MMATRIX_NON_CLASS(tr_if_vec);
    }

    DUP_MMATRIX_SET_1;
    DUP_MMATRIX_ddense_CASES;
    UNPROTECT(nprot);
    return ans;
}

SEXP dup_mMatrix_as_dgeMatrix(SEXP A) {
    return dup_mMatrix_as_dgeMatrix2(A, FALSE);
}

SEXP new_dgeMatrix(int nrow, int ncol)
{
    SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS("dgeMatrix"))),
	 ad = PROTECT(allocVector(INTSXP, 2));

    INTEGER(ad)[0] = nrow;
    INTEGER(ad)[1] = ncol;
    SET_SLOT(ans, Matrix_DimSym, ad);
    SET_SLOT(ans, Matrix_DimNamesSym, allocVector(VECSXP, 2));
    ALLOC_SLOT(ans, Matrix_xSym, REALSXP, nrow * ncol);

    UNPROTECT(2);
    return ans;
}

/**
 * Encode Matrix index (i,j)  |-->  i + j * nrow   {i,j : 0-origin}
 *
 * @param ij: 2-column integer matrix
 * @param di: dim(.), i.e. length 2 integer vector
 * @param chk_bnds: logical indicating  0 <= ij[,k] < di[k]  need to be checked.
 *
 * @return encoded index; integer if prod(dim) is small; double otherwise
 */
SEXP m_encodeInd(SEXP ij, SEXP di, SEXP orig_1, SEXP chk_bnds)
{
    SEXP ans;
    int *ij_di = NULL, n, nprot=1;
    Rboolean check_bounds = asLogical(chk_bnds), one_ind = asLogical(orig_1);

    if(TYPEOF(di) != INTSXP) {di = PROTECT(coerceVector(di, INTSXP)); nprot++; }
    if(TYPEOF(ij) != INTSXP) {ij = PROTECT(coerceVector(ij, INTSXP)); nprot++; }
    if(!isMatrix(ij) ||
       (ij_di = INTEGER(getAttrib(ij, R_DimSymbol)))[1] != 2)
	error(_("Argument ij must be 2-column integer matrix"));
    n = ij_di[0];
    int *Di = INTEGER(di), *IJ = INTEGER(ij),
	*j_ = IJ+n;/* pointer offset! */

    if((Di[0] * (double) Di[1]) >= 1 + (double)INT_MAX) { /* need double */
	ans = PROTECT(allocVector(REALSXP, n));
	double *ii = REAL(ans), nr = (double) Di[0];
#define do_ii_FILL(_i_, _j_)						\
	int i;								\
	if(check_bounds) {						\
	    for(i=0; i < n; i++) {					\
		if(_i_[i] == NA_INTEGER || _j_[i] == NA_INTEGER)	\
		    ii[i] = NA_INTEGER;					\
		else {							\
		    register int i_i, j_i;				\
	            if(one_ind) { i_i = _i_[i]-1; j_i = _j_[i]-1; }	\
	            else        { i_i = _i_[i]  ; j_i = _j_[i]  ; }	\
		    if(i_i < 0 || i_i >= Di[0])				\
			error(_("subscript 'i' out of bounds in M[ij]")); \
		    if(j_i < 0 || j_i >= Di[1])				\
			error(_("subscript 'j' out of bounds in M[ij]")); \
		    ii[i] = i_i + j_i * nr;				\
		}							\
	    }								\
	} else {							\
	    for(i=0; i < n; i++)					\
		ii[i] = (_i_[i] == NA_INTEGER || _j_[i] == NA_INTEGER)	\
		    ? NA_INTEGER					\
 	            : (one_ind ? ((_i_[i]-1) + (_j_[i]-1)*nr)		\
	                       :   _i_[i]    +  _j_[i]   *nr);		\
	}

	do_ii_FILL(IJ, j_);
    } else {
	ans = PROTECT(allocVector(INTSXP, n));
	int *ii = INTEGER(ans), nr = Di[0];

	do_ii_FILL(IJ, j_);
    }
    UNPROTECT(nprot);
    return ans;
}

/**
 * Encode Matrix index (i,j)  |-->  i + j * nrow   {i,j : 0-origin}
 *
 * @param i: integer vector
 * @param j: integer vector of same length as 'i'
 * @param orig_1: logical: if TRUE, "1-origin" otherwise "0-origin"
 * @param di: dim(.), i.e. length 2 integer vector
 * @param chk_bnds: logical indicating  0 <= ij[,k] < di[k]  need to be checked.
 *
 * @return encoded index; integer if prod(dim) is small; double otherwise
 */
SEXP m_encodeInd2(SEXP i, SEXP j, SEXP di, SEXP orig_1, SEXP chk_bnds)
{
    SEXP ans;
    int n = LENGTH(i), nprot = 1;
    Rboolean check_bounds = asLogical(chk_bnds), one_ind = asLogical(orig_1);

    if(TYPEOF(di)!= INTSXP) {di = PROTECT(coerceVector(di,INTSXP)); nprot++; }
    if(TYPEOF(i) != INTSXP) { i = PROTECT(coerceVector(i, INTSXP)); nprot++; }
    if(TYPEOF(j) != INTSXP) { j = PROTECT(coerceVector(j, INTSXP)); nprot++; }
    if(LENGTH(j) != n)
	error(_("i and j must be integer vectors of the same length"));
    int *Di = INTEGER(di), *i_ = INTEGER(i), *j_ = INTEGER(j);

    if((Di[0] * (double) Di[1]) >= 1 + (double)INT_MAX) { /* need double */
	ans = PROTECT(allocVector(REALSXP, n));
	double *ii = REAL(ans), nr = (double) Di[0];

	do_ii_FILL(i_, j_);
    } else {
	ans = PROTECT(allocVector(INTSXP, n));
	int *ii = INTEGER(ans), nr = Di[0];

	do_ii_FILL(i_, j_);
    }
    UNPROTECT(nprot);
    return ans;
}
#undef do_ii_FILL

// Almost "Cut n Paste" from ...R../src/main/array.c  do_matrix() :
// used in ../R/Matrix.R as
//
// .External(Mmatrix,
//	     data, nrow, ncol, byrow, dimnames,
//	     missing(nrow), missing(ncol))
SEXP Mmatrix(SEXP args)
{
    SEXP vals, ans, snr, snc, dimnames;
    int nr = 1, nc = 1, byrow, miss_nr, miss_nc;
    R_xlen_t lendat;

    args = CDR(args); /* skip 'name' */
    vals = CAR(args); args = CDR(args);
    /* Supposedly as.vector() gave a vector type, but we check */
    switch(TYPEOF(vals)) {
	case LGLSXP:
	case INTSXP:
	case REALSXP:
	case CPLXSXP:
	case STRSXP:
	case RAWSXP:
	case EXPRSXP:
	case VECSXP:
	    break;
	default:
	    error(_("'data' must be of a vector type"));
    }
    lendat = XLENGTH(vals);
    snr = CAR(args); args = CDR(args);
    snc = CAR(args); args = CDR(args);
    byrow = asLogical(CAR(args)); args = CDR(args);
    if (byrow == NA_INTEGER)
	error(_("invalid '%s' argument"), "byrow");
    dimnames = CAR(args);
    args = CDR(args);
    miss_nr = asLogical(CAR(args)); args = CDR(args);
    miss_nc = asLogical(CAR(args));

    if (!miss_nr) {
	if (!isNumeric(snr)) error(_("non-numeric matrix extent"));
	nr = asInteger(snr);
	if (nr == NA_INTEGER)
	    error(_("invalid 'nrow' value (too large or NA)"));
	if (nr < 0)
	    error(_("invalid 'nrow' value (< 0)"));
    }
    if (!miss_nc) {
	if (!isNumeric(snc)) error(_("non-numeric matrix extent"));
	nc = asInteger(snc);
	if (nc == NA_INTEGER)
	    error(_("invalid 'ncol' value (too large or NA)"));
	if (nc < 0)
	    error(_("invalid 'ncol' value (< 0)"));
    }
    if (miss_nr && miss_nc) {
	if (lendat > INT_MAX) error("data is too long");
	nr = (int) lendat;
    } else if (miss_nr) {
	if (lendat > (double) nc * INT_MAX) error("data is too long");
	nr = (int) ceil((double) lendat / (double) nc);
    } else if (miss_nc) {
	if (lendat > (double) nr * INT_MAX) error("data is too long");
	nc = (int) ceil((double) lendat / (double) nr);
    }

    if(lendat > 0) {
	R_xlen_t nrc = (R_xlen_t) nr * nc;
	if (lendat > 1 && nrc % lendat != 0) {
	    if (((lendat > nr) && (lendat / nr) * nr != lendat) ||
		((lendat < nr) && (nr / lendat) * lendat != nr))
		warning(_("data length [%d] is not a sub-multiple or multiple of the number of rows [%d]"), lendat, nr);
	    else if (((lendat > nc) && (lendat / nc) * nc != lendat) ||
		     ((lendat < nc) && (nc / lendat) * lendat != nc))
		warning(_("data length [%d] is not a sub-multiple or multiple of the number of columns [%d]"), lendat, nc);
	}
	else if ((lendat > 1) && (nrc == 0)){
	    warning(_("data length exceeds size of matrix"));
	}
    }

#ifndef LONG_VECTOR_SUPPORT
   if ((double)nr * (double)nc > INT_MAX)
	error(_("too many elements specified"));
#endif

    PROTECT(ans = allocMatrix(TYPEOF(vals), nr, nc));
    if(lendat) {
	if (isVector(vals))
	    copyMatrix(ans, vals, byrow);
	else
	    copyListMatrix(ans, vals, byrow);
    } else if (isVector(vals)) { /* fill with NAs */
	R_xlen_t N = (R_xlen_t) nr * nc, i;
	switch(TYPEOF(vals)) {
	case STRSXP:
	    for (i = 0; i < N; i++)
		SET_STRING_ELT(ans, i, NA_STRING);
	    break;
	case LGLSXP:
	    for (i = 0; i < N; i++)
		LOGICAL(ans)[i] = NA_LOGICAL;
	    break;
	case INTSXP:
	    for (i = 0; i < N; i++)
		INTEGER(ans)[i] = NA_INTEGER;
	    break;
	case REALSXP:
	    for (i = 0; i < N; i++)
		REAL(ans)[i] = NA_REAL;
	    break;
	case CPLXSXP:
	    {
		Rcomplex na_cmplx;
		na_cmplx.r = NA_REAL;
		na_cmplx.i = 0;
		for (i = 0; i < N; i++)
		    COMPLEX(ans)[i] = na_cmplx;
	    }
	    break;
	case RAWSXP:
	    memset(RAW(ans), 0, N);
	    break;
	default:
	    /* don't fill with anything */
	    ;
	}
    }
    if(!isNull(dimnames)&& length(dimnames) > 0)
	ans = dimnamesgets(ans, dimnames);
    UNPROTECT(1);
    return ans;
}

/**
 * From the two 'x' slots of two dense matrices a and b,
 * compute the 'x' slot of rbind(a, b)
 *
 * Currently, an auxiliary only for setMethod rbind2(<denseMatrix>, <denseMatrix>)
 * in ../R/bind2.R
 *
 * @param a
 * @param b
 *
 * @return
 */SEXP R_rbind2_vector(SEXP a, SEXP b) {
    int *d_a = INTEGER(GET_SLOT(a, Matrix_DimSym)),
	*d_b = INTEGER(GET_SLOT(b, Matrix_DimSym)),
	n1 = d_a[0], m = d_a[1],
	n2 = d_b[0],
	nprot = 1;
    SEXP ans,
	a_x = GET_SLOT(a, Matrix_xSym),
	b_x = GET_SLOT(b, Matrix_xSym);
    if(d_b[1] != m)
	error(_("the number of columns differ in R_rbind2_vector: %d != %d"),
	      m, d_b[1]);
    // Care: can have "ddenseMatrix" "ldenseMatrix" or "ndenseMatrix"
    if(TYPEOF(a_x) != TYPEOF(b_x)) { // choose the "common type"
	// Now know: either LGLSXP or REALSXP. FIXME for iMatrix, zMatrix,..
	if(TYPEOF(a_x) != REALSXP)
	    a_x = PROTECT(duplicate(coerceVector(a_x, REALSXP)));
	else if(TYPEOF(b_x) != REALSXP)
	    b_x = PROTECT(duplicate(coerceVector(b_x, REALSXP)));
	nprot++;
    }

    ans = PROTECT(allocVector(TYPEOF(a_x), m * (n1 + n2)));
    int i, ii = 0;
    switch(TYPEOF(a_x)) {
    case LGLSXP: {
	int
	    *r = LOGICAL(ans),
	    *ax= LOGICAL(a_x),
	    *bx= LOGICAL(b_x);

#define COPY_a_AND_b_j						\
/*  FIXME faster: use Memcpy() : */				\
	for(int j=0; j < m; j++) {				\
	    for(i=0; i < n1; i++) r[ii++] = ax[j*n1 + i];	\
	    for(i=0; i < n2; i++) r[ii++] = bx[j*n2 + i];	\
	}

	COPY_a_AND_b_j;
    }
    case REALSXP: {
	double
	    *r = REAL(ans),
	    *ax= REAL(a_x),
	    *bx= REAL(b_x);

	COPY_a_AND_b_j;
    }
    } // switch

    UNPROTECT(nprot);
    return ans;
}

#define TRUE_  ScalarLogical(1)
#define FALSE_ ScalarLogical(0)

// Fast implementation of [ originally in  ../R/Auxiliaries.R ]
// all0     <- function(x) !any(is.na(x)) && all(!x) ## ~= allFalse
// allFalse <- function(x) !any(x) && !any(is.na(x)) ## ~= all0
SEXP R_all0(SEXP x) {
    if (!isVectorAtomic(x)) {
	if(length(x) == 0) return TRUE_;
	// Typically S4.  TODO: Call the R code above, instead!
	error(_("Argument must be numeric-like atomic vector"));
    }
    R_xlen_t i, n = XLENGTH(x);
    if(n == 0) return TRUE_;

    switch(TYPEOF(x)) {
    case LGLSXP: {
	int *xx = LOGICAL(x);
	for(i=0; i < n; i++)
	    if(xx[i] == NA_LOGICAL || xx[i] != 0) return FALSE_;
	return TRUE_;
    }
    case INTSXP: {
	int *xx = INTEGER(x);
	for(i=0; i < n; i++)
	    if(xx[i] == NA_INTEGER || xx[i] != 0) return FALSE_;
	return TRUE_;
    }
    case REALSXP: {
	double *xx = REAL(x);
	for(i=0; i < n; i++)
	    if(ISNAN(xx[i]) || xx[i] != 0.) return FALSE_;
	return TRUE_;
    }
    case RAWSXP: {
	unsigned char *xx = RAW(x);
	for(i=0; i < n; i++)
	    if(xx[i] != 0) return FALSE_;
	return TRUE_;
    }
    }
    error(_("Argument must be numeric-like atomic vector"));
    return R_NilValue; // -Wall
}

// Fast implementation of [ originally in  ../R/Auxiliaries.R ]
// any0 <- function(x) isTRUE(any(x == 0)) ## ~= anyFalse
// anyFalse <- function(x) isTRUE(any(!x)) ## ~= any0
SEXP R_any0(SEXP x) {
    if (!isVectorAtomic(x)) {
	if(length(x) == 0) return FALSE_;
	// Typically S4.  TODO: Call the R code above, instead!
	error(_("Argument must be numeric-like atomic vector"));
    }
    R_xlen_t i, n = XLENGTH(x);
    if(n == 0) return FALSE_;

    switch(TYPEOF(x)) {
    case LGLSXP: {
	int *xx = LOGICAL(x);
	for(i=0; i < n; i++) if(xx[i] == 0) return TRUE_;
	return FALSE_;
    }
    case INTSXP: {
	int *xx = INTEGER(x);
	for(i=0; i < n; i++) if(xx[i] == 0) return TRUE_;
	return FALSE_;
    }
    case REALSXP: {
	double *xx = REAL(x);
	for(i=0; i < n; i++) if(xx[i] == 0.) return TRUE_;
	return FALSE_;
    }
    case RAWSXP: {
	unsigned char *xx = RAW(x);
	for(i=0; i < n; i++) if(xx[i] == 0) return TRUE_;
	return FALSE_;
    }
    }
    error(_("Argument must be numeric-like atomic vector"));
    return R_NilValue; // -Wall
}

#undef TRUE_
#undef FALSE_

/* FIXME: Compare and synchronize with MK_SYMMETRIC_DIMNAMES.. in ./dense.c
 * -----  which *also* considers  names(dimnames(.)) !!
*/

/**
 * Produce symmetric 'Dimnames' from possibly asymmetric ones.
 *
 * @param dn  list of length 2; typically 'Dimnames' slot of "symmetricMatrix"
 */
SEXP symmetric_DimNames(SEXP dn) {
    Rboolean do_nm = FALSE;
#define NON_TRIVIAL_DN					\
  !isNull(VECTOR_ELT(dn, 0)) ||				\
  !isNull(VECTOR_ELT(dn, 1)) ||				\
 (do_nm = !isNull(getAttrib(dn, R_NamesSymbol)))

#define SYMM_DIMNAMES							\
	/* Fixup dimnames to be symmetric <==>				\
	   symmetricDimnames() in ../R/symmetricMatrix.R */		\
	PROTECT(dn = duplicate(dn));					\
	if (isNull(VECTOR_ELT(dn,0)))					\
	    SET_VECTOR_ELT(dn, 0, VECTOR_ELT(dn, 1));			\
	if (isNull(VECTOR_ELT(dn,1)))					\
	    SET_VECTOR_ELT(dn, 1, VECTOR_ELT(dn, 0));			\
	if(do_nm) { /* names(dimnames(.)): */				\
	    SEXP nms_dn = getAttrib(dn, R_NamesSymbol);			\
	    if(!R_compute_identical(STRING_ELT(nms_dn, 0),		\
				    STRING_ELT(nms_dn, 1), 16)) {	\
		PROTECT(nms_dn);					\
		int J = LENGTH(STRING_ELT(nms_dn, 0)) == 0; /* 0/1 */   \
		SET_STRING_ELT(nms_dn, !J, STRING_ELT(nms_dn, J));	\
		setAttrib(dn, R_NamesSymbol, nms_dn);			\
		UNPROTECT(1);						\
            }								\
	}								\
	UNPROTECT(1)

    // Be fast (do nothing!) for the case where dimnames = list(NULL,NULL) :
    if (NON_TRIVIAL_DN) {
	SYMM_DIMNAMES;
    }
    return dn;
}

/**
 * Even if the Dimnames slot is list(NULL, <names>) etc, return
 * __symmetric__ dimnames: Get . @Dimnames and modify when needed.
 *
 * Called e.g., from symmetricDimnames() in ../R/symmetricMatrix.R
 *
 * @param from a symmetricMatrix
 *
 * @return symmetric dimnames: length-2 list twice the same, NULL or
 * character vector (of correct length)
 */
SEXP R_symmetric_Dimnames(SEXP x) {
    return symmetric_DimNames(GET_SLOT(x, Matrix_DimNamesSym));
}


/**
 * Set 'Dimnames' slot of 'dest' from the one of 'src' when
 * 'src' is a "symmetricMatrix" with possibly asymmetric dimnames,
 * and 'dest' must contain corresponding symmetric dimnames.
 *
 * @param dest Matrix, typically *not* symmetric
 * @param src  symmetricMatrix
 */
void SET_DimNames_symm(SEXP dest, SEXP src) {
    SEXP dn = GET_SLOT(src, Matrix_DimNamesSym);
    Rboolean do_nm = FALSE;
    // Be fast (do nothing!) for the case where dimnames = list(NULL,NULL) :
    if (NON_TRIVIAL_DN) {
	SYMM_DIMNAMES;
	SET_SLOT(dest, Matrix_DimNamesSym, dn);
    }
    return;
}

