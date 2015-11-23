/*------ Definition of a template for Matrix_rle_[di](...) : *
 *                       --------     ~~~~~~~~~~~~~~~~~~~~~~
 * i.e., included several times from ./abIndex.c
 *                                   ~~~~~~~~~~~
 */

/* for all cases with an 'x' slot -- i.e. almost all cases ;
 * just redefine this in the other cases:
 */

#ifdef _rle_d_

# define Matrix_RLE_ Matrix_rle_d
# define Type_x_ double
# define STYP_x_ REAL
# define SXP_ans  REALSXP

#elif defined _rle_i_

# define Matrix_RLE_ Matrix_rle_i
# define Type_x_ int
# define STYP_x_ INTEGER
# define SXP_ans  INTSXP

#else
#  error "invalid _rle_ macro logic"
#endif

/**
 * RLE (Run Length Encoding) -- only when it's worth
 *
 * @param x_  R vector which can be coerced to "double" / "integer"
 * @param force_  R logical indicating if the result must be "RLE" even when inefficient
 *
 * @return NULL or a valid R object of class "rle"
 */
SEXP Matrix_RLE_(SEXP x_, SEXP force_)
{
    int n = LENGTH(PROTECT(x_ = coerceVector(x_, SXP_ans)));
    Rboolean no_force = !asLogical(force_);
    if (no_force && n < 3) {
	UNPROTECT(1); return R_NilValue;
    } else {
	register Type_x_ lv;
	register int ln, i, c = 0;
	int n2 = (no_force) ? n / 3 : n;
	/* upper bound: ==> max RAM requirement 2 x n2, (= 2/3 n);
	 * using 2 instead of 3 would need 50% more time, have max
	 * RAM requirement 2.5x for savings of any size */
	Type_x_ *x = STYP_x_(x_), *val;
	int *len;
	const char *res_nms[] = {"lengths", "values", ""};
	SEXP ans;
	if(n > 0) { /* needed for force=TRUE */
	    len = Calloc(n2, int);
	    val = Calloc(n2, Type_x_);

	    lv = x[0];
	    ln = 1;
	    for(i = 1; i < n; i++) {
		if (x[i] == lv) {
		    ln++;
		} else {
		    val[c] = lv;
		    len[c] = ln;
		    c++;
		    if (no_force && c == n2) { /* reached the "efficiency bound" */
			Free(len);
			Free(val);
			UNPROTECT(1); return R_NilValue;
		    }
		    lv = x[i];
		    ln = 1;
		}
	    }
	    val[c] = lv;
	    len[c] = ln;
	    c++;
        }
	ans = PROTECT(Rf_mkNamed(VECSXP, res_nms));
	SET_VECTOR_ELT(ans, 0, allocVector(INTSXP, c)); /* lengths */
	SET_VECTOR_ELT(ans, 1, allocVector(SXP_ans, c)); /* values */
	if(n > 0) {
	    Memcpy(INTEGER(VECTOR_ELT(ans, 0)), len, c);
	    Memcpy(STYP_x_(VECTOR_ELT(ans, 1)), val, c);
	}
	setAttrib(ans, R_ClassSymbol, mkString("rle"));

	if(n > 0) { Free(len); Free(val); }
	UNPROTECT(2);
	return ans;
    }
} /* Matrix_RLE_() template */

#undef Matrix_RLE_
#undef Type_x_
#undef STYP_x_
#undef SXP_ans
