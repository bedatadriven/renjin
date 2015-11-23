/*------ Definition of a template for [diln]Csparse_subassign(...) : *
 *                       --------     ~~~~~~~~~~~~~~~~~~~~~~
 * i.e., included several times from ./Csparse.c
 *                                   ~~~~~~~~~~~
 *
 _slot_kind : use the integer codes matching  x_slot_kind in ./Mutils.h
 *							       ~~~~~~~~
 */

#ifdef _d_Csp_

# define Csparse_subassign dCsparse_subassign
# define x_CLASSES "dgCMatrix",/* 0 */ "dtCMatrix" /* 1 */
# define sparseVECTOR "dsparseVector"
# define slot_kind_x 0
# define _DOUBLE_x
# define _has_x_slot_

# undef _d_Csp_

#elif defined (_l_Csp_)

# define Csparse_subassign lCsparse_subassign
# define x_CLASSES "lgCMatrix",/* 0 */ "ltCMatrix" /* 1 */
# define sparseVECTOR "lsparseVector"
# define slot_kind_x 1
# define _LGL_x
# define _has_x_slot_

# undef _l_Csp_

#elif defined (_i_Csp_)

# define Csparse_subassign iCsparse_subassign
# define x_CLASSES "igCMatrix",/* 0 */ "itCMatrix" /* 1 */
# define sparseVECTOR "isparseVector"
# define slot_kind_x 2
# define _INT_x
# define _has_x_slot_

# undef _i_Csp_

#elif defined (_n_Csp_)

# define Csparse_subassign nCsparse_subassign
# define x_CLASSES "ngCMatrix",/* 0 */ "ntCMatrix" /* 1 */
# define sparseVECTOR "nsparseVector"
# define slot_kind_x -1
# define _INT_x
/* withOUT 'x' slot -- CARE! we assume that this is the *ONLY* case w/o x slot */

# undef _n_Csp_

#elif defined (_z_Csp_)

// #  error "zgC* not yet implemented"
# define Csparse_subassign zCsparse_subassign
# define x_CLASSES "zgCMatrix",/* 0 */ "ztCMatrix" /* 1 */
# define sparseVECTOR "zsparseVector"
# define slot_kind_x 3
# define _CPLX_x
# define _has_x_slot_

# undef _z_Csp_

#else

#  error "no valid  _[dilnz]gC_ option"

#endif
// -------------------------------------------------

#ifdef _DOUBLE_x

# define Type_x double
# define Type_x_0_init(_VAR_) double _VAR_ = 0.
# define Type_x_1_init(_VAR_) double _VAR_ = 1.
# define STYP_x REAL
# define SXP_x  REALSXP
#undef _DOUBLE_x

#elif defined (_LGL_x)

# define Type_x int
# define Type_x_0_init(_VAR_) int _VAR_ = 0
# define Type_x_1_init(_VAR_) int _VAR_ = 1
# define STYP_x LOGICAL
# define SXP_x  LGLSXP
#undef _LGL_x

#elif defined (_INT_x)

# define Type_x int
# define Type_x_0_init(_VAR_) int _VAR_ = 0
# define Type_x_1_init(_VAR_) int _VAR_ = 1
# define STYP_x INTEGER
# define SXP_x  INTSXP
#undef _INT_x

#elif defined (_CPLX_x)

# define Type_x Rcomplex
# define Type_x_0_init(_VAR_) Rcomplex _VAR_;  _VAR_.r = _VAR_.i = 0.
# define Type_x_1_init(_VAR_) Rcomplex _VAR_;  _VAR_.r = 1.; _VAR_.i = 0.
# define STYP_x COMPLEX
# define SXP_x  CPLXSXP

#else
#  error "invalid macro logic"
#endif



/**
 * Subassignment:  x[i,j]  <- value
 *
 * @param x
 * @param i_ integer row    index 0-origin vector (as returned from R .ind.prep2())
 * @param j_ integer column index 0-origin vector
 * @param value must be a [dln]sparseVector {which is recycled if needed}
 *
 * @return a Csparse matrix like x, but with the values replaced
 */
SEXP Csparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value)
{
    // TODO: for other classes consider using a trick as  RallocedReal() in ./chm_common.c
    static const char
	*valid_cM [] = { // the only ones, for "the moment". FIXME: extend (!)
	x_CLASSES,
	""},
	// value: assume a  "dsparseVector" for now -- slots: (i, length, x)
	*valid_spv[] = { sparseVECTOR, // = "the one with the same slot-class"
			 // all others: ctype_v   slot_kind
			 "nsparseVector",// 1     -1
			 "lsparseVector",// 2      1
			 "isparseVector",// 3      2
			 "dsparseVector",// 4      0
			 "zsparseVector",// 5      3
			 ""};

    int ctype_x = Matrix_check_class_etc(x,     valid_cM),
	ctype_v = Matrix_check_class_etc(value, valid_spv);
    if (ctype_x < 0)
	error(_("invalid class of 'x' in Csparse_subassign()"));
    if (ctype_v < 0)
	error(_("invalid class of 'value' in Csparse_subassign()"));
    Rboolean value_is_nsp = ctype_v == 1;
#ifndef _has_x_slot_ // i.e. "n.CMatrix" : sparseVECTOR == "nsparseVector"
    if(!value_is_nsp) value_is_nsp = (ctype_v == 0);
#endif

    SEXP
	islot   = GET_SLOT(x, Matrix_iSym),
	dimslot = GET_SLOT(x, Matrix_DimSym),
	i_cp = PROTECT(coerceVector(i_, INTSXP)),
	j_cp = PROTECT(coerceVector(j_, INTSXP));
    // for d.CMatrix and l.CMatrix  but not n.CMatrix:

    int *dims = INTEGER(dimslot),
	ncol = dims[1],	/* nrow = dims[0], */
	*i = INTEGER(i_cp), len_i = LENGTH(i_cp),
	*j = INTEGER(j_cp), len_j = LENGTH(j_cp),
	k,
	nnz_x = LENGTH(islot);
    int nnz = nnz_x;

#define MATRIX_SUBASSIGN_VERBOSE
// Temporary hack for debugging --- remove eventually -- FIXME
#ifdef MATRIX_SUBASSIGN_VERBOSE
    Rboolean verbose = i[0] < 0;
    if(verbose) {
	i[0] = -i[0];
	REprintf("Csparse_subassign() x[i,j] <- val; x is \"%s\"; value \"%s\" is_nsp=%d\n",
		 valid_cM[ctype_x], valid_spv[ctype_v], (int)value_is_nsp);
    }
#endif

    SEXP val_i_slot, val_x_slot;
    val_i_slot = PROTECT(coerceVector(GET_SLOT(value, Matrix_iSym), REALSXP));
    double *val_i = REAL(val_i_slot);
    int nnz_val = LENGTH(GET_SLOT(value, Matrix_iSym)), n_prot = 4;
    Type_x *val_x = NULL;
    if(!value_is_nsp) {
	if(ctype_v) { // matrix 'x' and 'value' are of different kinds
	    switch((enum x_slot_kind) slot_kind_x) {
	    case x_pattern:// "n"
	    case x_logical:// "l"
		if(ctype_v >= 3)
		    warning(_("x[] <- val: val is coerced to logical for \"%s\" x"),
			    valid_cM[ctype_x]);
		break;
	    case x_integer:
		if(ctype_v >= 4)
		    error(_("x[] <- val: val should be integer or logical, is coerced to integer, for \"%s\" x"),
			  valid_cM[ctype_x]);
		break;
	    case x_double:
	    case x_complex: // coercion should be tried (and fail for complex -> double) below
		break;
	    default:
		error(_("programming error in Csparse_subassign() should never happen"));
	    }
	    // otherwise: "coerce" :  as(., <sparseVector>) :
	    val_x_slot = PROTECT(coerceVector(GET_SLOT(value, Matrix_xSym), SXP_x)); n_prot++;
	    val_x = STYP_x(val_x_slot);
	} else {
	    val_x = STYP_x(		      GET_SLOT(value, Matrix_xSym));
	}
    }
    int64_t len_val = (int64_t) asReal(GET_SLOT(value, Matrix_lengthSym));
    /* llen_i = (int64_t) len_i; */

    SEXP ans;
    /* Instead of simple "duplicate": PROTECT(ans = duplicate(x)) , build up: */
    // Assuming that ans will have the same basic Matrix type as x :
    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(valid_cM[ctype_x])));
    SET_SLOT(ans, Matrix_DimSym,      duplicate(dimslot));
    slot_dup(ans, x, Matrix_DimNamesSym);
    slot_dup(ans, x, Matrix_pSym);
    SEXP r_pslot = GET_SLOT(ans, Matrix_pSym);
    // and assign the i- and x- slots at the end, as they are potentially modified
    // not just in content, but also in their *length*
    int *rp = INTEGER(r_pslot),
	*ri = Calloc(nnz_x, int);       // to contain the final i - slot
    Memcpy(ri, INTEGER(islot), nnz_x);
    Type_x_0_init(z_ans);
    Type_x_1_init(one_ans);
#ifdef _has_x_slot_
    Type_x *rx = Calloc(nnz_x, Type_x); // to contain the final x - slot
    Memcpy(rx, STYP_x(GET_SLOT(x, Matrix_xSym)), nnz_x);
#endif
    // NB:  nnz_x : will always be the "current allocated length" of (i, x) slots
    // --   nnz   : the current *used* length; always   nnz <= nnz_x

    int jj, j_val = 0; // in "running" conceptionally through all value[i+ jj*len_i]
    // values, we are "below"/"before" the (j_val)-th non-zero one.
    // e.g. if value = (0,0,...,0), have nnz_val == 0, j_val must remain == 0
    int64_t ii_val;// == "running" index (i + jj*len_i) % len_val for value[]
    for(jj = 0, ii_val=0; jj < len_j; jj++) {
	int j__ = j[jj];
	/* int64_t j_l = jj * llen_i; */
	R_CheckUserInterrupt();
	for(int ii = 0; ii < len_i; ii++, ii_val++) {
	    int i__ = i[ii], p1, p2;
	    if(nnz_val && ii_val >= len_val) { // "recycle" indexing into value[]
		ii_val -= len_val; // = (ii + jj*len_i) % len_val
		j_val = 0;
	    }
	    int64_t ii_v1;//= ii_val + 1;
	    Type_x v, /* := value[(ii + j_l) % len_val]
			 = .sparseVector_sub((ii + j_l) % len_val,
			 nnz_val, val_i, val_x, len_val)
		      */
		M_ij;
	    int ind;
	    Rboolean have_entry = FALSE;

	    // note that rp[]'s may have *changed* even when 'j' remained!
	    // "FIXME": do this only *when* rp[] has changed
	    p1 = rp[j__], p2 = rp[j__ + 1];

	    // v :=  value[(ii + j_l) % len_val] = value[ii_val]
	    v = z_ans;
	    if(j_val < nnz_val) { // maybe find v := non-zero value[ii_val]
		ii_v1 = ii_val + 1;
		if(ii_v1 < val_i[j_val]) { // typical case: are still in zero-stretch
		    // v = z_ans (== 0)
		} else if(ii_v1 == val_i[j_val]) { // have a match
		    v = (value_is_nsp) ? one_ans : val_x[j_val];
		    j_val++;// from now on, look at the next non-zero entry
		} else { //  ii_v1 > val_i[j_val]
		    REprintf("programming thinko in Csparse_subassign(*, i=%d,j=%d): ii_v=%d, v@i[j_val=%ld]=%g\n",
			     i__,j__, ii_v1, j_val, val_i[j_val]);
		    j_val++;// from now on, look at the next non-zero entry
		}
	    }
	    // --------------- M_ij := getM(i., j.) --------------------------------
	    M_ij = z_ans; // as in  ./t_sparseVector.c
	    for(ind = p1; ind < p2; ind++) {
		if(ri[ind] >= i__) {
		    if(ri[ind] == i__) {
#ifdef _has_x_slot_
			M_ij = rx[ind];
#else
			M_ij = 1;
#endif
#ifdef MATRIX_SUBASSIGN_VERBOSE
			if(verbose)
			    REprintf("have entry x[%d, %d] = %g\n", i__, j__,
#  ifdef _CPLX_x
				     (double)M_ij.r);
#  else
			             (double)M_ij);
#  endif
#endif
		        have_entry = TRUE;
		    } else { // ri[ind] > i__
#ifdef MATRIX_SUBASSIGN_VERBOSE
		        if(verbose)
			    REprintf("@i > i__ = %d --> ind-- = %d\n", i__, ind);
#endif
		}
		break;
	    }
	}

	//-- R:  if(getM(i., j.) != (v <- getV(ii, jj)))

	// if(contents differ) ==> value needs to be changed :
#ifdef _CPLX_x
	if(M_ij.r != v.r || M_ij.i != v.i) {
#else
	    if(M_ij != v) {
#endif

#ifdef MATRIX_SUBASSIGN_VERBOSE
		if(verbose)
		    REprintf("setting x[%d, %d] <- %g", i__,j__,
#  ifdef _CPLX_x
			     (double) v.r);
#  else
		             (double) v);
#  endif
#endif
	    // (otherwise: nothing to do):
	    // setM(i__, j__, v)
	    // ----------------------------------------------------------

#ifndef _has_x_slot_
	    if(v == z_ans) {
		// Case I ----- remove x[i, j] = M_ij  which we know is *non*-zero
//  BUT it is more efficient (memory-management!) *NOT* to remove,
/// but --- in the case of x slot put a 0 zero there, and only at the very end drop them,
//  currently using  drop0() in R code
		// we know : have_entry = TRUE ;
		//  ri[ind] == i__; M_ij = rx[ind];
#ifdef MATRIX_SUBASSIGN_VERBOSE
		if(verbose)
		    REprintf(" rm ind=%d\n", ind);
#endif
		// remove the 'ind'-th element from x@i and x@x :
		nnz-- ;
		for(k=ind; k < nnz; k++) {
		    ri[k] = ri[k+1];
#ifdef _has_x_slot_
		    rx[k] = rx[k+1];
#endif
		}
		for(k=j__ + 1; k <= ncol; k++) {
		    rp[k] = rp[k] - 1;
		}
	    }
	    else
#endif
		if(have_entry) {
		    // Case II ----- replace (non-empty) x[i,j] by v -------
#ifdef MATRIX_SUBASSIGN_VERBOSE
		    if(verbose)
			REprintf(" repl.  ind=%d\n", ind);
#endif
#ifdef _has_x_slot_
		    rx[ind] = v;
#endif
		} else {
		    // Case III ----- v != 0 : insert v into "empty" x[i,j] ----

		    // extend the  i  and  x  slot by one entry : ---------------------
		    if(nnz+1 > nnz_x) { // need to reallocate:
#ifdef MATRIX_SUBASSIGN_VERBOSE
			if(verbose) REprintf(" Realloc()ing: nnz_x=%d", nnz_x);
#endif
			// do it "only" 1x,..4x at the very most increasing by the
			// nnz-length of "value":
			nnz_x += (1 + nnz_val / 4);
#ifdef MATRIX_SUBASSIGN_VERBOSE
			if(verbose) REprintf("(nnz_v=%d) --> %d ", nnz_val, nnz_x);
#endif
			// C doc on realloc() says that the old content is *preserve*d
			ri = Realloc(ri, nnz_x, int);
#ifdef _has_x_slot_
			rx = Realloc(rx, nnz_x, Type_x);
#endif
		    }
		    // 3) fill them ...

		    int i1 = ind;
#ifdef MATRIX_SUBASSIGN_VERBOSE
		    if(verbose)
			REprintf(" INSERT p12=(%d,%d) -> ind=%d -> i1 = %d\n",
				 p1,p2, ind, i1);
#endif

		    // shift the "upper values" *before* the insertion:
		    for(int l = nnz-1; l >= i1; l--) {
			ri[l+1] = ri[l];
#ifdef _has_x_slot_
			rx[l+1] = rx[l];
#endif
		    }
		    ri[i1] = i__;
#ifdef _has_x_slot_
		    rx[i1] = v;
#endif
		    nnz++;
		    // the columns j "right" of the current one :
		    for(k=j__ + 1; k <= ncol; k++)
			rp[k]++;
		}
	}
#ifdef MATRIX_SUBASSIGN_VERBOSE
	else if(verbose) REprintf("M_ij == v = %g\n",
#  ifdef _CPLX_x
				  (double) v.r);
#  else
	                          (double) v);
#  endif
#endif
	}// for( ii )
    }// for( jj )

    if(ctype_x == 1) { // triangularMatrix: copy the 'diag' and 'uplo' slots
	slot_dup(ans, x, Matrix_uploSym);
	slot_dup(ans, x, Matrix_diagSym);
    }
    // now assign the i- and x- slots,  free memory and return :
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_iSym,  INTSXP, nnz)), ri, nnz);
#ifdef _has_x_slot_
    Memcpy( STYP_x(ALLOC_SLOT(ans, Matrix_xSym,   SXP_x, nnz)), rx, nnz);
    Free(rx);
#endif
    Free(ri);
    UNPROTECT(n_prot);
    return ans;
}

#undef Csparse_subassign
#undef x_CLASSES
#undef sparseVECTOR

#undef Type_x
#undef STYP_x
#undef SXP_x
#undef Type_x_0_init
#undef Type_x_1_init
#undef _has_x_slot_
#undef slot_kind_x

#ifdef _CPLX_x
# undef _CPLX_x
#endif



