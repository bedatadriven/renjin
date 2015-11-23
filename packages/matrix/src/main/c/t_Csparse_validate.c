/* Included from ./Csparse.c
 *                 ----------
 */
#ifdef _t_Csparse_sort

# define CSPARSE_VAL_RES_TYPE   static int
# define CSPARSE_VAL_FN_NAME	Csparse_sort_2
# define CSPARSE_VAL_RETURN_TRUE    	return 1
# define CSPARSE_VAL_RETURN_STRING(STR) return 0

# undef _t_Csparse_sort

#elif defined (_t_Csparse_validate)

# define CSPARSE_VAL_RES_TYPE   SEXP
# define CSPARSE_VAL_FN_NAME	Csparse_validate_
# define CSPARSE_VAL_RETURN_TRUE    	return ScalarLogical(1)
# define CSPARSE_VAL_RETURN_STRING(STR) return mkString(_(STR))

# undef _t_Csparse_validate

#else
#  error "no valid  _t_Csparse_* option"
#endif


CSPARSE_VAL_RES_TYPE CSPARSE_VAL_FN_NAME(SEXP x, Rboolean maybe_modify)
{
    /* NB: we do *NOT* check a potential 'x' slot here, at all */
    SEXP pslot = GET_SLOT(x, Matrix_pSym),
	islot = GET_SLOT(x, Matrix_iSym);
    Rboolean sorted, strictly;
    int j, k,
	*dims = INTEGER(GET_SLOT(x, Matrix_DimSym)),
	nrow = dims[0],
	ncol = dims[1],
	*xp = INTEGER(pslot),
	*xi = INTEGER(islot);

    if (length(pslot) != dims[1] + 1)
	CSPARSE_VAL_RETURN_STRING("slot p must have length = ncol(.) + 1");
    if (xp[0] != 0)
	CSPARSE_VAL_RETURN_STRING("first element of slot p must be zero");
    if (length(islot) < xp[ncol]) /* allow larger slots from over-allocation!*/
	CSPARSE_VAL_RETURN_STRING("last element of slot p must match length of slots i and x");
    for (j = 0; j < xp[ncol]; j++) {
	if (xi[j] < 0 || xi[j] >= nrow)
	    CSPARSE_VAL_RETURN_STRING("all row indices must be between 0 and nrow-1");
    }
    sorted = TRUE; strictly = TRUE;
    for (j = 0; j < ncol; j++) {
	if (xp[j] > xp[j + 1])
	    CSPARSE_VAL_RETURN_STRING("slot p must be non-decreasing");
	if(sorted) /* only act if >= 2 entries in column j : */
	    for (k = xp[j] + 1; k < xp[j + 1]; k++) {
		if (xi[k] < xi[k - 1])
		    sorted = FALSE;
		else if (xi[k] == xi[k - 1])
		    strictly = FALSE;
	    }
    }
    if (!sorted) {
	if(maybe_modify) {
	    CHM_SP chx = (CHM_SP) alloca(sizeof(cholmod_sparse));
	    R_CheckStack();
	    as_cholmod_sparse(chx, x, FALSE, TRUE);/*-> cholmod_l_sort() ! */
	    /* as chx = AS_CHM_SP__(x)  but  ^^^^ sorting x in_place !!! */

	    /* Now re-check that row indices are *strictly* increasing
	     * (and not just increasing) within each column : */
	    for (j = 0; j < ncol; j++) {
		for (k = xp[j] + 1; k < xp[j + 1]; k++)
		    if (xi[k] == xi[k - 1])
			CSPARSE_VAL_RETURN_STRING("slot i is not *strictly* increasing inside a column (even after cholmod_l_sort)");
	    }
	} else { /* no modifying sorting : */
	    CSPARSE_VAL_RETURN_STRING("row indices are not sorted within columns");
	}
    } else if(!strictly) {  /* sorted, but not strictly */
	CSPARSE_VAL_RETURN_STRING("slot i is not *strictly* increasing inside a column");
    }
    CSPARSE_VAL_RETURN_TRUE;
}

#undef CSPARSE_VAL_RES_TYPE
#undef CSPARSE_VAL_FN_NAME
#undef CSPARSE_VAL_RETURN_TRUE
#undef CSPARSE_VAL_RETURN_STRING
