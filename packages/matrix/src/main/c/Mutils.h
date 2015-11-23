#ifndef MATRIX_MUTILS_H
#define MATRIX_MUTILS_H

#undef Matrix_with_SPQR

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h> // C99 for int64_t
#include <ctype.h>
#include <R.h>  /* includes Rconfig.h */
#include <Rversion.h>
#include <Rdefines.h> /* Rinternals.h + GET_SLOT etc */

#ifdef ENABLE_NLS
#include <libintl.h>
#define _(String) dgettext ("Matrix", String)
#else
#define _(String) (String)
/* Note that this is not yet supported (for Windows, e.g.) in R 2.9.0 : */
#define dngettext(pkg, String, StringP, N) (N > 1 ? StringP : String)
#endif

#ifdef __GNUC__
# undef alloca
# define alloca(x) __builtin_alloca((x))
#elif defined(__sun) || defined(_AIX)
/* this is necessary (and sufficient) for Solaris 10 and AIX 6: */
# include <alloca.h>
#endif

#ifndef LONG_VECTOR_SUPPORT
// notably for  R <= 2.15.x :
# define XLENGTH(x) LENGTH(x)
# if R_VERSION < R_Version(2,16,0)
  typedef int R_xlen_t;
# endif
#endif

#define Alloca(n, t)   (t *) alloca( (size_t) ( (n) * sizeof(t) ) )

#define SMALL_4_Alloca 10000
//			==== R uses the same cutoff in several places

#define C_or_Alloca_TO(_VAR_, _N_, _TYPE_)			\
	if(_N_ < SMALL_4_Alloca) {				\
	    _VAR_ = Alloca(_N_, _TYPE_);  R_CheckStack();	\
	} else {						\
	    _VAR_ = Calloc(_N_, _TYPE_);			\
	}
// and user needs to   if(_N_ >= SMALL_4_Alloca)  Free(_VAR_);

SEXP triangularMatrix_validate(SEXP obj);
SEXP symmetricMatrix_validate(SEXP obj);
SEXP dense_nonpacked_validate(SEXP obj);
SEXP dim_validate(SEXP Dim, const char* name);
SEXP Dim_validate(SEXP obj, SEXP name);
SEXP dimNames_validate(SEXP obj);

// La_norm_type() & La_rcond_type()  have been in R_ext/Lapack.h
//  but have still not been available to package writers ...
char La_norm_type (const char *typstr);
char La_rcond_type(const char *typstr);

/* enum constants from cblas.h and some short forms */
enum CBLAS_ORDER {CblasRowMajor=101, CblasColMajor=102};
enum CBLAS_TRANSPOSE {CblasNoTrans=111, CblasTrans=112, CblasConjTrans=113};
enum CBLAS_UPLO {CblasUpper=121, CblasLower=122};
enum CBLAS_DIAG {CblasNonUnit=131, CblasUnit=132};
enum CBLAS_SIDE {CblasLeft=141, CblasRight=142};
#define RMJ CblasRowMajor
#define CMJ CblasColMajor
#define NTR CblasNoTrans
#define TRN CblasTrans
#define CTR CblasConjTrans
#define UPP CblasUpper
#define LOW CblasLower
#define NUN CblasNonUnit
#define UNT CblasUnit
#define LFT CblasLeft
#define RGT CblasRight

double get_double_by_name(SEXP obj, char *nm);
SEXP set_double_by_name(SEXP obj, double val, char *nm);
SEXP as_det_obj(double val, int log, int sign);
SEXP get_factors(SEXP obj, char *nm);
SEXP set_factors(SEXP obj, SEXP val, char *nm);
SEXP R_set_factors(SEXP obj, SEXP val, SEXP name, SEXP warn);

#if 0
SEXP dgCMatrix_set_Dim(SEXP x, int nrow);
#endif	/* unused */

/* int csc_unsorted_columns(int ncol, const int p[], const int i[]); */
/* void csc_sort_columns(int ncol, const int p[], int i[], double x[]); */
/* SEXP csc_check_column_sorting(SEXP A); */

SEXP check_scalar_string(SEXP sP, char *vals, char *nm);
Rboolean equal_string_vectors(SEXP s1, SEXP s2);

void d_packed_getDiag(double *dest, SEXP x, int n);
void l_packed_getDiag(   int *dest, SEXP x, int n);
SEXP d_packed_setDiag(double *diag, int l_d, SEXP x, int n);
SEXP l_packed_setDiag(   int *diag, int l_d, SEXP x, int n);
SEXP d_packed_addDiag(double *diag, int l_d, SEXP x, int n);

void tr_d_packed_getDiag(double *dest, SEXP x, int n);
void tr_l_packed_getDiag(   int *dest, SEXP x, int n);

SEXP tr_d_packed_setDiag(double *diag, int l_d, SEXP x, int n);
SEXP tr_l_packed_setDiag(   int *diag, int l_d, SEXP x, int n);
SEXP tr_d_packed_addDiag(double *diag, int l_d, SEXP x, int n);

SEXP Matrix_getElement(SEXP list, char *nm);

#define PACKED_TO_FULL(TYPE)						\
TYPE *packed_to_full_ ## TYPE(TYPE *dest, const TYPE *src,		\
			     int n, enum CBLAS_UPLO uplo)
PACKED_TO_FULL(double);
PACKED_TO_FULL(int);
#undef PACKED_TO_FULL

#define FULL_TO_PACKED(TYPE)						\
TYPE *full_to_packed_ ## TYPE(TYPE *dest, const TYPE *src, int n,	\
			      enum CBLAS_UPLO uplo, enum CBLAS_DIAG diag)
FULL_TO_PACKED(double);
FULL_TO_PACKED(int);
#undef FULL_TO_PACKED


extern	 /* stored pointers to symbols initialized in R_init_Matrix */
#include "Syms.h"

/* zero an array */
#define AZERO(x, n) {int _I_, _SZ_ = (n); for(_I_ = 0; _I_ < _SZ_; _I_++) (x)[_I_] = 0;}
// R's  RS.h :
#define Memzero(p,n)  memset(p, 0, (size_t)(n) * sizeof(*p))

/* number of elements in one triangle of a square matrix of order n */
#define PACKED_LENGTH(n)   ((n) * ((n) + 1))/2

/* duplicate the slot with name given by sym from src to dest */

#define slot_dup(dest, src, sym)  SET_SLOT(dest, sym, duplicate(GET_SLOT(src, sym)))

/* is not yet used: */
#define slot_nonNull_dup(dest, src, sym)			\
    if(GET_SLOT(src, sym) != R_NilValue)			\
	SET_SLOT(dest, sym, duplicate(GET_SLOT(src, sym)))

#define slot_dup_if_has(dest, src, sym)				\
    if(R_has_slot(src, sym))					\
	SET_SLOT(dest, sym, duplicate(GET_SLOT(src, sym)))

static R_INLINE
void SET_DimNames(SEXP dest, SEXP src) {
    SEXP dn = GET_SLOT(src, Matrix_DimNamesSym);
    // Be fast (do nothing!) for the case where dimnames = list(NULL,NULL) :
    if (!(isNull(VECTOR_ELT(dn,0)) && isNull(VECTOR_ELT(dn,1))))
	SET_SLOT(dest, Matrix_DimNamesSym, duplicate(dn));
}

// code in ./Mutils.c :
SEXP symmetric_DimNames(SEXP dn);
SEXP R_symmetric_Dimnames(SEXP x);
void SET_DimNames_symm(SEXP dest, SEXP src);


#define uplo_P(_x_) CHAR(STRING_ELT(GET_SLOT(_x_, Matrix_uploSym), 0))
#define diag_P(_x_) CHAR(STRING_ELT(GET_SLOT(_x_, Matrix_diagSym), 0))
#define Diag_P(_x_) (R_has_slot(x, Matrix_diagSym) ?			\
		     CHAR(STRING_ELT(GET_SLOT(_x_, Matrix_diagSym), 0)) : " ")
#define class_P(_x_) CHAR(asChar(getAttrib(_x_, R_ClassSymbol)))


enum dense_enum { ddense, ldense, ndense };

// Define this "Cholmod compatible" to some degree
enum x_slot_kind {x_pattern=-1, x_double=0, x_logical=1, x_integer=2, x_complex=3};
//		    n		  d	      l		   i		z

/* should also work for "matrix" matrices: */
#define Real_KIND(_x_)	(IS_S4_OBJECT(_x_) ? Real_kind(_x_) : \
			 (isReal(_x_) ? x_double : (isLogical(_x_) ? x_logical : -1)))
/* This one gives '0' also for integer "matrix" :*/
#define Real_KIND2(_x_)	(IS_S4_OBJECT(_x_) ? Real_kind(_x_) : \
			 (isLogical(_x_) ? x_logical : 0))

/* requires 'x' slot, i.e., not for ..nMatrix.  FIXME ? via R_has_slot(obj, name) */
#define Real_kind(_x_)	(isReal(GET_SLOT(_x_, Matrix_xSym)) ? 0	:	\
			 (isLogical(GET_SLOT(_x_, Matrix_xSym)) ? 1 : -1))

#define DECLARE_AND_GET_X_SLOT(__C_TYPE, __SEXP)	\
    __C_TYPE *xx = __SEXP(GET_SLOT(x, Matrix_xSym))


/**
 * Check for valid length of a packed triangular array and return the
 * corresponding number of columns
 *
 * @param len length of a packed triangular array
 *
 * @return number of columns
 */
static R_INLINE
int packed_ncol(int len)
{
    int disc = 8 * len + 1;	/* discriminant */
    int sqrtd = (int) sqrt((double) disc);

    if (len < 0 || disc != sqrtd * sqrtd)
	error(_("invalid 'len' = %d in packed_ncol"));
    return (sqrtd - 1)/2;
}

/**
 * Allocate an SEXP of given type and length, assign it as slot nm in
 * the object, and return the SEXP.  The validity of this function
 * depends on SET_SLOT not duplicating val when NAMED(val) == 0.  If
 * this behavior changes then ALLOC_SLOT must use SET_SLOT followed by
 * GET_SLOT to ensure that the value returned is indeed the SEXP in
 * the slot.
 * NOTE:  GET_SLOT(x, what)        :== R_do_slot       (x, what)
 * ----   SET_SLOT(x, what, value) :== R_do_slot_assign(x, what, value)
 * and the R_do_slot* are in src/main/attrib.c
 *
 * @param obj object in which to assign the slot
 * @param nm name of the slot, as an R name object
 * @param type type of SEXP to allocate
 * @param length length of SEXP to allocate
 *
 * @return SEXP of given type and length assigned as slot nm in obj
 */
static R_INLINE
SEXP ALLOC_SLOT(SEXP obj, SEXP nm, SEXPTYPE type, int length)
{
    SEXP val = allocVector(type, length);

    SET_SLOT(obj, nm, val);
    return val;
}

/**
 * Expand compressed pointers in the array mp into a full set of indices
 * in the array mj.
 *
 * @param ncol number of columns (or rows)
 * @param mp column pointer vector of length ncol + 1
 * @param mj vector of length mp[ncol] to hold the result
 *
 * @return mj
 */
static R_INLINE
int* expand_cmprPt(int ncol, const int mp[], int mj[])
{
    int j;
    for (j = 0; j < ncol; j++) {
	int j2 = mp[j+1], jj;
	for (jj = mp[j]; jj < j2; jj++) mj[jj] = j;
    }
    return mj;
}

/**
 * Check if slot(obj, "x") contains any NA (or NaN).
 *
 * @param obj   a 'Matrix' object with a (double precision) 'x' slot.
 *
 * @return Rboolean :== any(is.na(slot(obj, "x") )
 */
static R_INLINE
Rboolean any_NA_in_x(SEXP obj)
{
    double *x = REAL(GET_SLOT(obj, Matrix_xSym));
    int i, n = LENGTH(GET_SLOT(obj, Matrix_xSym));
    for(i=0; i < n; i++)
	if(ISNAN(x[i])) return TRUE;
    /* else */
    return FALSE;
}



/** Inverse Permutation
 * C version of   .inv.perm.R <- function(p) { p[p] <- seq_along(p) ; p }
 */
static R_INLINE
SEXP inv_permutation(SEXP p_, SEXP zero_p, SEXP zero_res)
{
    int np = 0;
    if(!isInteger(p_)) {p_ = PROTECT(coerceVector(p_, INTSXP)); np++; }
    int *p = INTEGER(p_), n = LENGTH(p_);
    SEXP val = allocVector(INTSXP, n);// (not PROTECT()ing: no alloc from here on)
    int *v = INTEGER(val), p_0 = asLogical(zero_p), r_0 = asLogical(zero_res);
    if(!p_0) v--; // ==> use 1-based indices
    // shorter (but not 100% sure if ok: is LHS always eval'ed *before* RHS ?) :
    // for(int i=0; i < n; ) v[p[i]] = ++i;
    for(int i=0; i < n; ) {
	int j = p[i]; v[j] = (r_0) ? i++ : ++i;
    }
    UNPROTECT(np);
    return val;
}

SEXP Mmatrix(SEXP args);

void make_d_matrix_triangular(double *x, SEXP from);
void make_i_matrix_triangular(   int *x, SEXP from);

void make_d_matrix_symmetric(double *to, SEXP from);
void make_i_matrix_symmetric(   int *to, SEXP from);

SEXP Matrix_expand_pointers(SEXP pP);

SEXP dup_mMatrix_as_dgeMatrix2(SEXP A, Rboolean tr_if_vec);
SEXP dup_mMatrix_as_dgeMatrix (SEXP A);
SEXP dup_mMatrix_as_geMatrix  (SEXP A);

SEXP new_dgeMatrix(int nrow, int ncol);
SEXP m_encodeInd (SEXP ij,        SEXP di, SEXP orig_1, SEXP chk_bnds);
SEXP m_encodeInd2(SEXP i, SEXP j, SEXP di, SEXP orig_1, SEXP chk_bnds);

SEXP R_rbind2_vector(SEXP a, SEXP b);

SEXP R_all0(SEXP x);
SEXP R_any0(SEXP x);

static R_INLINE SEXP
mMatrix_as_dgeMatrix(SEXP A) {
    return strcmp(class_P(A), "dgeMatrix") ? dup_mMatrix_as_dgeMatrix(A) : A;
}
static R_INLINE SEXP
mMatrix_as_dgeMatrix2(SEXP A, Rboolean tr_if_vec) {
    return strcmp(class_P(A), "dgeMatrix") ? dup_mMatrix_as_dgeMatrix2(A, tr_if_vec) : A;
}

static R_INLINE SEXP
mMatrix_as_geMatrix(SEXP A)
{
    return strcmp(class_P(A) + 1, "geMatrix") ? dup_mMatrix_as_geMatrix(A) : A;
}

// Keep centralized --- *and* in sync with ../inst/include/Matrix.h :
#define MATRIX_VALID_ge_dense			\
        "dmatrix", "dgeMatrix",			\
	"lmatrix", "lgeMatrix",			\
	"nmatrix", "ngeMatrix",			\
	"zmatrix", "zgeMatrix"

/* NB:  ddiMatrix & ldiMatrix are part of VALID_ddense / VALID_ldense
 * --   even though they are no longer "denseMatrix" formally.
 * CARE: dup_mMatrix_as_geMatrix() code depends on  14 ddense and 6 ldense
 * ----  entries here :
*/
#define MATRIX_VALID_ddense					\
		    "dgeMatrix", "dtrMatrix",			\
		    "dsyMatrix", "dpoMatrix", "ddiMatrix",	\
		    "dtpMatrix", "dspMatrix", "dppMatrix",	\
		    /* sub classes of those above:*/		\
		    /* dtr */ "Cholesky", "LDL", "BunchKaufman",\
		    /* dtp */ "pCholesky", "pBunchKaufman",	\
		    /* dpo */ "corMatrix"

#define MATRIX_VALID_ldense			\
		    "lgeMatrix", "ltrMatrix",	\
		    "lsyMatrix", "ldiMatrix",	\
		    "ltpMatrix", "lspMatrix"

#define MATRIX_VALID_ndense			\
		    "ngeMatrix", "ntrMatrix",	\
		    "nsyMatrix",		\
		    "ntpMatrix", "nspMatrix"

#define MATRIX_VALID_Csparse			\
 "dgCMatrix", "dsCMatrix", "dtCMatrix",		\
 "lgCMatrix", "lsCMatrix", "ltCMatrix",		\
 "ngCMatrix", "nsCMatrix", "ntCMatrix",		\
 "zgCMatrix", "zsCMatrix", "ztCMatrix"

#define MATRIX_VALID_Tsparse			\
 "dgTMatrix", "dsTMatrix", "dtTMatrix",		\
 "lgTMatrix", "lsTMatrix", "ltTMatrix",		\
 "ngTMatrix", "nsTMatrix", "ntTMatrix",		\
 "zgTMatrix", "zsTMatrix", "ztTMatrix"

#define MATRIX_VALID_Rsparse			\
 "dgRMatrix", "dsRMatrix", "dtRMatrix",		\
 "lgRMatrix", "lsRMatrix", "ltRMatrix",		\
 "ngRMatrix", "nsRMatrix", "ntRMatrix",		\
 "zgRMatrix", "zsRMatrix", "ztRMatrix"

#define MATRIX_VALID_tri_Csparse		\
   "dtCMatrix", "ltCMatrix", "ntCMatrix", "ztCMatrix"

#ifdef __UN_USED__
#define MATRIX_VALID_tri_sparse			\
 "dtCMatrix",  "dtTMatrix", "dtRMatrix",	\
 "ltCMatrix",  "ltTMatrix", "ltRMatrix",	\
 "ntCMatrix",  "ntTMatrix", "ntRMatrix",	\
 "ztCMatrix",  "ztTMatrix", "ztRMatrix"

#define MATRIX_VALID_tri_dense			\
 "dtrMatrix",  "dtpMatrix"			\
 "ltrMatrix",  "ltpMatrix"			\
 "ntrMatrix",  "ntpMatrix"			\
 "ztrMatrix",  "ztpMatrix"
#endif

#define MATRIX_VALID_CHMfactor "dCHMsuper", "dCHMsimpl", "nCHMsuper", "nCHMsimpl"

/**
 * Return the 0-based index of a string match in a vector of strings
 * terminated by an empty string.  Returns -1 for no match.
 *
 * @param class string to match
 * @param valid vector of possible matches terminated by an empty string
 *
 * @return index of match or -1 for no match
 */
static R_INLINE int
Matrix_check_class(char *class, const char **valid)
{
    int ans;
    for (ans = 0; ; ans++) {
	if (!strlen(valid[ans])) return -1;
	if (!strcmp(class, valid[ans])) return ans;
    }
}

/**
 * These are the ones "everyone" should use -- is() versions, also looking
 * at super classes:
 */
# define Matrix_check_class_etc R_check_class_etc
# define Matrix_check_class_and_super R_check_class_and_super

/** Accessing  *sparseVectors :  fast (and recycling)  v[i] for v = ?sparseVector:
 * -> ./sparseVector.c  -> ./t_sparseVector.c :
 */
// Type_ans sparseVector_sub(int64_t i, int nnz_v, int* v_i, Type_ans* v_x, int len_v):

/* Define all of
 *  dsparseVector_sub(....)
 *  isparseVector_sub(....)
 *  lsparseVector_sub(....)
 *  nsparseVector_sub(....)
 *  zsparseVector_sub(....)
 */
#define _dspV_
#include "t_sparseVector.c"

#define _ispV_
#include "t_sparseVector.c"

#define _lspV_
#include "t_sparseVector.c"

#define _nspV_
#include "t_sparseVector.c"

#define _zspV_
#include "t_sparseVector.c"


#ifdef __cplusplus
}
#endif

#endif /* MATRIX_MUTILS_H_ */
