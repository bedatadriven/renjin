/*------ Definition of a template for [diln]gCMatrix_colsums(...) : *
 *                       --------     ~~~~~~~~~~~~~~~~~~~~~~
 * i.e., included several times from ./dgCMatrix.c
 *                                   ~~~~~~~~~~~~~
 */


/* for all cases with an 'x' slot -- i.e. almost all cases ;
 * just redefine this in the other cases:
 */

#ifdef _dgC_

# define gCMatrix_colSums dgCMatrix_colSums
# define _DOUBLE_ans
# define _has_x_slot_
/*Future? # define _has_x_d_slot_ */
# undef _dgC_

#elif defined (_igC_)

# define gCMatrix_colSums igCMatrix_colSums
# define _DOUBLE_ans
# define _has_x_slot_
/*Future? # define _has_x_d_slot_ */
# undef _igC_

#elif defined (_lgC_)

# define gCMatrix_colSums lgCMatrix_colSums_i
# define _INT_ans
# define _has_x_slot_
/*Future? # define _has_x_l_slot_ */
# undef _lgC_

#elif defined (_lgC_mn)

# define gCMatrix_colSums lgCMatrix_colSums_d
# define _DOUBLE_ans
# define _has_x_slot_
/*Future? # define _has_x_l_slot_ */
# undef _lgC_mn

#elif defined (_ngC_)

# define gCMatrix_colSums ngCMatrix_colSums_i
# define _INT_ans
 /* withOUT 'x' slot */
# undef _ngC_

#elif defined (_ngC_mn)

# define gCMatrix_colSums ngCMatrix_colSums_d
# define _DOUBLE_ans
 /* withOUT 'x' slot */
# undef _ngC_mn

#elif defined (_zgC_)

#  error "zgC* not yet implemented"

#else

#  error "no valid  _[dilnz]gC_ option"

#endif

/* - - - - - - - - - - - - - - - - - - - - */

/* Most of this is maybe for the future,
 * when cholmod has integer 'x' slot :*/
#ifdef _has_x_d_slot_

# define Type_x_ double
# define STYP_x_ REAL
# define _has_x_slot_
# undef _has_x_d_slot_

#elif defined (_has_x_i_slot_)

# define Type_x_ int
# define STYP_x_ INTEGER
# define _has_x_slot_
# undef _has_x_i_slot_

#elif defined (_has_x_l_slot_)

# define Type_x_ int
# define STYP_x_ LOGICAL
# define _has_x_slot_
# undef _has_x_l_slot_

#endif

/* - - - - - - - - - - - - - - - - - - - - */

#ifdef _DOUBLE_ans

# define SparseResult_class "dsparseVector"
# define Type_ans double
# define STYP_ans REAL
# define NA_ans NA_REAL
# define SXP_ans  REALSXP
# define COERCED(x) (x)
#undef _DOUBLE_ans

#elif defined (_INT_ans)

# define SparseResult_class "isparseVector"
# define Type_ans int
# define STYP_ans INTEGER
# define NA_ans NA_INTEGER
# define SXP_ans  INTSXP
# define COERCED(x) (Type_ans)(x != 0)
#undef _INT_ans

#else
#  error "invalid macro logic"
#endif

/* - - - - - - - - - - - - - - - - - - - - */

#ifdef _has_x_slot_

/* currently have x slot always double (cholmod restriction): */
# define is_NA_x_(u) ISNAN(u)

# define ColSUM_column(_i1_,_i2_,_SUM_)					\
		if(mn) dnm = cx->nrow;	/* denominator for means */	\
		for(i = _i1_, _SUM_ = 0; i < _i2_; i++) {		\
		    if (is_NA_x_(xx[i])) {				\
			if(!na_rm) {					\
			    _SUM_ = NA_ans;				\
			    break;					\
			}						\
			/* else: na_rm : skip NAs , */			\
			if(mn) /* but decrement denominator */		\
			    dnm--; 					\
		    } else _SUM_ += COERCED(xx[i]);			\
		}							\
		if(mn) _SUM_ = (dnm > 0) ? _SUM_/dnm : NA_ans

#else /* no 'x' slot -> no NAs ... */

# define ColSUM_column(_i1_,_i2_,_SUM_)		\
		_SUM_ = _i2_ - _i1_;		\
		if(mn) _SUM_ /= cx->nrow
#endif

/* Now the template which depends on the above macros : */

/**
 * colSums(), colMeans(),  rowSums() and rowMeans() for all sparce *gCMatrix()es
 * @param x a ?gCMatrix, i.e. sparse column-compressed Matrix
 * @param NArm logical indicating if NA's should be remove 'na.rm' in R
 * @param spRes logical = 'sparseResult' indicating if result should be sparse
 * @param trans logical: TRUE <==> row[Sums/Means] <==> compute col*s( t(x) )
 * @param means logical: TRUE <==> compute [row/col]Means() , not *Sums()
 */
SEXP gCMatrix_colSums(SEXP x, SEXP NArm, SEXP spRes, SEXP trans, SEXP means)
{
    int mn = asLogical(means), sp = asLogical(spRes), tr = asLogical(trans);
    /* cholmod_sparse: drawback of coercing lgC to double: */
    CHM_SP cx = AS_CHM_SP__(x);
    R_CheckStack();

    if (tr) {
	cholmod_sparse *cxt = cholmod_transpose(cx, (int)cx->xtype, &c);
	cx = cxt;
    }

    /* everything else *after* the above potential transpose : */

    int j, nc = cx->ncol;
    int *xp = (int *)(cx -> p);
#ifdef _has_x_slot_
    int na_rm = asLogical(NArm), // can have NAs only with an 'x' slot
	i, dnm = 0/*Wall*/;
    double *xx = (double *)(cx -> x);
#endif
    // result value:  sparseResult (==> "*sparseVector") or dense (atomic)vector
    SEXP ans = PROTECT(sp ? NEW_OBJECT(MAKE_CLASS(SparseResult_class))
		       : allocVector(SXP_ans, nc));
    if (sp) { // sparseResult, i.e. *sparseVector (never allocating length-nc)
	int nza, i1, i2, p, *ai;
	Type_ans *ax;

	for (j = 0, nza = 0; j < nc; j++)
	    if(xp[j] < xp[j + 1])
		nza++;

	ai =  INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP,  nza));
	ax = STYP_ans(ALLOC_SLOT(ans, Matrix_xSym, SXP_ans, nza));

	SET_SLOT(ans, Matrix_lengthSym, ScalarInteger(nc));

	i2 = xp[0];
	for (j = 1, p = 0; j <= nc; j++) {
	    /* j' =j+1, since 'i' slot will be 1-based */
	    i1 = i2; i2 = xp[j];
	    if(i1 < i2) {
		Type_ans sum;
		ColSUM_column(i1,i2, sum);

		ai[p]	= j;
		ax[p++] = sum;
	    }
	}
    }
    else { /* "numeric" (non sparse) result */
	Type_ans *a = STYP_ans(ans);
	for (j = 0; j < nc; j++) {
	    ColSUM_column(xp[j], xp[j + 1], a[j]);
	}
    }

    if (tr) cholmod_free_sparse(&cx, &c);
    if (!sp) {
	SEXP nms = VECTOR_ELT(GET_SLOT(x, Matrix_DimNamesSym), tr ? 0 : 1);
	if (!isNull(nms))
	    setAttrib(ans, R_NamesSymbol, duplicate(nms));
    }
    UNPROTECT(1);
    return ans;
}

#undef ColSUM_column

#undef NA_ans
#undef STYP_ans
#undef SXP_ans
#undef SparseResult_class
#undef Type_ans

#undef COERCED

#ifdef _has_x_slot_
# undef NA_x_
# undef Type_x_
# undef STYP_x_
# undef _has_x_slot_
#endif

#undef gCMatrix_colSums
