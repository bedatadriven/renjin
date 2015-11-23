/*------ Definition of a template for [dilnz]sparseVector_sub(...) : *
 *                       --------     ~~~~~~~~~~~~~~~~~~~~~~
 * i.e., included several times from ./Mutils.h
 *                                     ~~~~~~~~
 */

/* for all cases with an 'x' slot -- i.e. almost all cases ;
 * just redefine this in the other cases:
 */

#ifdef _dspV_

# define sparseVector_sub dsparseVector_sub
# define _DOUBLE_ans
# define _has_x_slot_
# undef _dspV_

#elif defined (_ispV_)

# define sparseVector_sub isparseVector_sub
# define _INT_ans
# define _has_x_slot_
# undef _ispV_

#elif defined (_lspV_)

# define sparseVector_sub lsparseVector_sub
# define _LGL_ans
# define _has_x_slot_
# undef _lspV_

#elif defined (_nspV_)

# define sparseVector_sub nsparseVector_sub
# define _LGL_ans
 /* withOUT 'x' slot */
# undef _nspV_

#elif defined (_zspV_)

# define sparseVector_sub zsparseVector_sub
# define _CPLX_ans
# define _has_x_slot_
# undef _zspV_

#else

#  error "no valid  _[dilnz]spV_ option"

#endif

/* - - - - - - - - - - - - - - - - - - - - */

#ifdef _DOUBLE_ans

# define Type_ans double
# define STYP_ans REAL
# define NA_ans NA_REAL
# define z_ans (0.)
# define SXP_ans  REALSXP
#undef _DOUBLE_ans

#elif defined (_INT_ans)

# define Type_ans int
# define STYP_ans INTEGER
# define NA_ans NA_INTEGER
# define z_ans (0)
# define SXP_ans  INTSXP
#undef _INT_ans

#elif defined (_LGL_ans)

# define Type_ans int
# define STYP_ans LOGICAL
# define NA_ans NA_LOGICAL
# define z_ans (0)
# define SXP_ans  LGLSXP
#undef _LGL_ans

#elif defined (_CPLX_ans)

static Rcomplex cmplx_zero() {
    Rcomplex z;
    z.r = z.i = 0.;
    return z;
}
#ifdef _using_NA_ans // <-- get rid of "non-used" warning message
static Rcomplex cmplx_NA() {
    Rcomplex z;
    z.r = z.i = NA_REAL;
    return z;
}
#endif

# define Type_ans Rcomplex
# define STYP_ans COMPLEX
# define NA_ans cmplx_NA(); // "FIXME": NA_COMPLEX does not yet exist
# define z_ans  cmplx_zero();
# define SXP_ans  CPLXSXP
#undef _CPLX_ans

#else
#  error "invalid macro logic"
#endif

/* - - - - - - - - - - - - - - - - - - - - */

#ifdef _has_x_slot_

/* currently have x slot always double (cholmod restriction): */
# define is_NA_x_(u) ISNAN(u)

#endif


/* Now the template which depends on the above macros : */

/**
 * Indexing a sparseVector 'vec', including recycling it (conceptually), i.e.
 * return  vec[i]
 *
 * @param i index (0-based, contrary to the i-slot)
 * @param nnz_v the number of non-zero entries of 'vec' == length(vec@ i)
 * @param v_i (a int * pointer to) the 'i' slot of 'vec'
 * @param v_x (a ... * pointer to) the 'x' slot of 'vec'
 * @param len_v integer = the 'length' slot of 'vec
 *
 * @return
 */
static R_INLINE
Type_ans sparseVector_sub(int64_t i, int nnz_v, double* v_i, Type_ans* v_x, int64_t len_v)
{
// double *v_i = INTEGER(GET_SLOT(vec, Matrix_iSym));
// double *v_x =   REAL (GET_SLOT(vec, Matrix_xSym)); -- try to be agnostic about type
// int64_t  len_v = (int64_t) asReal(GET_SLOT(vec, Matrix_lengthSym));
    int64_t i1 = (i % len_v) +1;
    // NB: Rely on the "validity": the i-slot  v_i[] is strictly sorted increasingly
    for(int j=0; j < nnz_v; j++) {
	if(i1 > v_i[j])
	    continue;
	// else: i1 <= v_i[j]
	if(i1 == v_i[j]) // have a match
#ifdef _has_x_slot_
	    return v_x[j];
#else
  	    return 1;
#endif
	else // no match: the element is zero
	    return z_ans;
    }
    return z_ans;
}

#undef Type_ans
#undef STYP_ans
#undef NA_ans
#undef z_ans
#undef SXP_ans


#undef _has_x_slot_
#undef sparseVector_sub
