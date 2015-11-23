/** @file chm_common.c
 */
#include "chm_common.h"
#include "Mutils.h"

Rboolean isValid_Csparse(SEXP x); /* -> Csparse.c */

SEXP get_SuiteSparse_version() {
    SEXP ans = allocVector(INTSXP, 3);
    int* version = INTEGER(ans);
    SuiteSparse_version(version);
    return ans;
}

cholmod_common c;
cholmod_common cl;

SEXP chm_common_env;
static SEXP dboundSym, grow0Sym, grow1Sym, grow2Sym, maxrankSym,
    supernodal_switchSym, supernodalSym, final_asisSym, final_superSym,
    final_llSym, final_packSym, final_monotonicSym, final_resymbolSym,
    prefer_zomplexSym, prefer_upperSym, quick_return_if_not_posdefSym,
    nmethodsSym, m0_ordSym, postorderSym;

void CHM_store_common() {
    SEXP rho = chm_common_env;
    defineVar(dboundSym, ScalarReal(c.dbound), rho);
    defineVar(grow0Sym, ScalarReal(c.grow0), rho);
    defineVar(grow1Sym, ScalarReal(c.grow1), rho);
    defineVar(grow2Sym, ScalarInteger(c.grow2), rho);
    defineVar(maxrankSym, ScalarInteger(c.maxrank), rho);
    defineVar(supernodal_switchSym,
	      ScalarReal(c.supernodal_switch), rho);
    defineVar(supernodalSym, ScalarInteger(c.supernodal), rho);
    defineVar(final_asisSym, ScalarLogical(c.final_asis), rho);
    defineVar(final_superSym, ScalarLogical(c.final_super), rho);
    defineVar(final_llSym, ScalarLogical(c.final_ll), rho);
    defineVar(final_packSym, ScalarLogical(c.final_pack), rho);
    defineVar(final_monotonicSym, ScalarLogical(c.final_monotonic), rho);
    defineVar(final_resymbolSym, ScalarLogical(c.final_resymbol), rho);
    defineVar(prefer_zomplexSym, ScalarLogical(c.prefer_zomplex), rho);
    defineVar(prefer_upperSym, ScalarLogical(c.prefer_upper), rho);
    defineVar(quick_return_if_not_posdefSym,
	      ScalarLogical(c.quick_return_if_not_posdef), rho);
    defineVar(nmethodsSym, ScalarInteger(c.nmethods), rho);
    defineVar(m0_ordSym, ScalarInteger(c.method[0].ordering), rho);
    defineVar(postorderSym, ScalarLogical(c.postorder), rho);
}

void CHM_restore_common() {
    SEXP rho = chm_common_env;
    c.dbound = asReal(findVarInFrame(rho, dboundSym));
    c.grow0 = asReal(findVarInFrame(rho, grow0Sym));
    c.grow1 = asReal(findVarInFrame(rho, grow1Sym));
    c.grow2 = asInteger(findVarInFrame(rho, grow2Sym));
    c.maxrank = asInteger(findVarInFrame(rho, maxrankSym));
    c.supernodal_switch = asReal(findVarInFrame(rho, supernodal_switchSym));
    c.supernodal = asLogical(findVarInFrame(rho, supernodalSym));
    c.final_asis = asLogical(findVarInFrame(rho, final_asisSym));
    c.final_super = asLogical(findVarInFrame(rho, final_superSym));
    c.final_ll = asLogical(findVarInFrame(rho, final_llSym));
    c.final_pack = asLogical(findVarInFrame(rho, final_packSym));
    c.final_monotonic = asLogical(findVarInFrame(rho, final_monotonicSym));
    c.final_resymbol = asLogical(findVarInFrame(rho, final_resymbolSym));
    c.prefer_zomplex = asLogical(findVarInFrame(rho, prefer_zomplexSym));
    c.prefer_upper = asLogical(findVarInFrame(rho, prefer_upperSym));
    c.quick_return_if_not_posdef =
	asLogical(findVarInFrame(rho, quick_return_if_not_posdefSym));
    c.nmethods = asInteger(findVarInFrame(rho, nmethodsSym));
    c.method[0].ordering = asInteger(findVarInFrame(rho, m0_ordSym));
    c.postorder = asLogical(findVarInFrame(rho, postorderSym));
}

SEXP CHM_set_common_env(SEXP rho) {
    if (!isEnvironment(rho))
	error(_("Argument rho must be an environment"));
    chm_common_env = rho;
    dboundSym = install("dbound");
    grow0Sym = install("grow0");
    grow1Sym = install("grow1");
    grow2Sym = install("grow2");
    maxrankSym = install("maxrank");
    supernodal_switchSym = install("supernodal_switch");
    supernodalSym = install("supernodal");
    final_asisSym = install("final_asis");
    final_superSym = install("final_super");
    final_llSym = install("final_ll");
    final_packSym = install("final_pack");
    final_monotonicSym = install("final_monotonic");
    final_resymbolSym = install("final_resymbol");
    prefer_zomplexSym = install("final_zomplex");
    prefer_upperSym = install("final_upper");
    quick_return_if_not_posdefSym = install("quick_return_if_not_posdef");
    nmethodsSym = install("nmethods");
    m0_ordSym = install("m0.ord");
    postorderSym = install("postorder");
    CHM_store_common();
    return R_NilValue;
}

/** @brief stype := "symmetry type".
 *
 *  ./CHOLMOD/Include/cholmod_core.h says about  'int stype' entry of cholmod_sparse_struct:
 *    ------------------------------
 * 0:  matrix is "unsymmetric": use both upper and lower triangular parts
 *     (the matrix may actually be symmetric in pattern and value, but
 *     both parts are explicitly stored and used).  May be square or
 *     rectangular.
 * >0: matrix is square and symmetric, use upper triangular part.
 *     Entries in the lower triangular part are ignored.
 * <0: matrix is square and symmetric, use lower triangular part.
 *     Entries in the upper triangular part are ignored.
 */
static int stype(int ctype, SEXP x)
{
    if ((ctype % 3) == 1) return (*uplo_P(x) == 'U') ? 1 : -1;
    return 0;
}

/** @brief xtype: the _kind_ of numeric (think "x slot") of Cholmod sparse matrices.
  #define CHOLMOD_PATTERN 0	 pattern only, no numerical values
  #define CHOLMOD_REAL    1	 a real matrix
  #define CHOLMOD_COMPLEX 2	 a complex matrix (ANSI C99 compatible)
  #define CHOLMOD_ZOMPLEX 3	 a complex matrix (MATLAB compatible)
*/
static int xtype(int ctype)
{
    switch(ctype / 3) {
    case 0: /* "d" */
    case 1: /* "l" */
	return CHOLMOD_REAL;
    case 2: /* "n" */
	return CHOLMOD_PATTERN;
    case 3: /* "z" */
	return CHOLMOD_COMPLEX;
    }
    return -1;
}

/* coerce a vector to REAL and copy the result to freshly R_alloc'd memory */
static void *RallocedREAL(SEXP x)
{
    SEXP rx = PROTECT(coerceVector(x, REALSXP));
    int lx = LENGTH(rx);
    /* We over-allocate the memory chunk so that it is never NULL. */
    /* The CHOLMOD code checks for a NULL pointer even in the length-0 case. */
    double *ans = Memcpy((double*)R_alloc(lx + 1, sizeof(double)),
			 REAL(rx), lx);
    UNPROTECT(1);
    return (void*)ans;
}


static void *xpt(int ctype, SEXP x)
{
    switch(ctype / 3) {
    case 0: /* "d" */
	return (void *) REAL(GET_SLOT(x, Matrix_xSym));
    case 1: /* "l" */
	return RallocedREAL(GET_SLOT(x, Matrix_xSym));
    case 2: /* "n" */
	return (void *) NULL;
    case 3: /* "z" */
	return (void *) COMPLEX(GET_SLOT(x, Matrix_xSym));
    }
    return (void *) NULL; 	/* -Wall */
}

Rboolean check_sorted_chm(CHM_SP A)
{
    int *Ai = (int*)(A->i), *Ap = (int*)(A->p);
    int j, p;

    for (j = 0; j < A->ncol; j++) {
	int p1 = Ap[j], p2 = Ap[j + 1] - 1;
	for (p = p1; p < p2; p++)
	    if (Ai[p] >= Ai[p + 1])
		return FALSE;
    }
    return TRUE;
}

/**
   Copy cholmod_sparse, to an R_alloc()ed version of it
 */
static void chm2Ralloc(CHM_SP dest, CHM_SP src)
{
    int np1, nnz;

    /* copy all the characteristics of src to dest */
    memcpy(dest, src, sizeof(cholmod_sparse));

    /* R_alloc the vector storage for dest and copy the contents from src */
    np1 = src->ncol + 1;
    nnz = (int) cholmod_nnz(src, &c);
    dest->p = (void*) Memcpy((int*)R_alloc(np1, sizeof(int)),
			     (int*)(src->p), np1);
    dest->i = (void*) Memcpy((int*)R_alloc(nnz, sizeof(int)),
			     (int*)(src->i), nnz);
    if(src->xtype)
	dest->x = (void*) Memcpy((double*)R_alloc(nnz, sizeof(double)),
				 (double*)(src->x), nnz);
}

/**
   Copy cholmod_triplet to an R_alloc()ed version of it
 */
static void chTr2Ralloc(CHM_TR dest, CHM_TR src)
{
    int nnz;

    /* copy all the (non-pointer) characteristics of src to dest */
    memcpy(dest, src, sizeof(cholmod_triplet));

    /* R_alloc the vector storage for dest and copy the contents from src */
    nnz = src->nnz;
    dest->i = (void*) Memcpy((int*)R_alloc(nnz, sizeof(int)),
			     (int*)(src->i), nnz);
    dest->j = (void*) Memcpy((int*)R_alloc(nnz, sizeof(int)),
			     (int*)(src->j), nnz);
    if(src->xtype)
	dest->x = (void*) Memcpy((double*)R_alloc(nnz, sizeof(double)),
				 (double*)(src->x), nnz);
}

/**
 * Populate ans with the pointers from x and modify its scalar
 * elements accordingly. Note that later changes to the contents of
 * ans will change the contents of the SEXP.
 *
 * In most cases this function is called through the macros
 * AS_CHM_SP() or AS_CHM_SP__().  It is unusual to call it directly.
 *
 * @param ans a CHM_SP pointer
 * @param x pointer to an object that inherits from CsparseMatrix
 * @param check_Udiag boolean - should a check for (and consequent
 *  expansion of) a unit diagonal be performed.
 * @param sort_in_place boolean - if the i and x slots are to be sorted
 *  should they be sorted in place?  If the i and x slots are pointers
 *  to an input SEXP they should not be modified.
 *
 * @return ans containing pointers to the slots of x, *unless*
 *	check_Udiag and x is unitriangular.
 */
CHM_SP as_cholmod_sparse(CHM_SP ans, SEXP x,
			 Rboolean check_Udiag, Rboolean sort_in_place)
{
    static const char *valid[] = { MATRIX_VALID_Csparse, ""};
    int *dims = INTEGER(GET_SLOT(x, Matrix_DimSym)),
	ctype = Matrix_check_class_etc(x, valid);
    SEXP islot = GET_SLOT(x, Matrix_iSym);

    if (ctype < 0) error(_("invalid class of object to as_cholmod_sparse"));
    if (!isValid_Csparse(x))
	error(_("invalid object passed to as_cholmod_sparse"));
    memset(ans, 0, sizeof(cholmod_sparse)); /* zero the struct */

    ans->itype = CHOLMOD_INT;	/* characteristics of the system */
    ans->dtype = CHOLMOD_DOUBLE;
    ans->packed = TRUE;
				/* slots always present */
    ans->i = INTEGER(islot);
    ans->p = INTEGER(GET_SLOT(x, Matrix_pSym));
				/* dimensions and nzmax */
    ans->nrow = dims[0];
    ans->ncol = dims[1];
    /* Allow for over-allocation of the i and x slots.  Needed for
     * sparse X form in lme4.  Right now it looks too difficult to
     * check for the length of the x slot, because of the xpt
     * utility, but the lengths of x and i should agree. */
    ans->nzmax = LENGTH(islot);
				/* values depending on ctype */
    ans->x = xpt(ctype, x);
    ans->stype = stype(ctype, x);
    ans->xtype = xtype(ctype);

    /* are the columns sorted (increasing row numbers) ?*/
    ans->sorted = check_sorted_chm(ans);
    if (!(ans->sorted)) { /* sort columns */
	if(sort_in_place) {
	    if (!cholmod_sort(ans, &c))
		error(_("in_place cholmod_sort returned an error code"));
	    ans->sorted = 1;
	}
	else {
	    CHM_SP tmp = cholmod_copy_sparse(ans, &c);
	    if (!cholmod_sort(tmp, &c))
		error(_("cholmod_sort returned an error code"));

#ifdef DEBUG_Matrix
	    /* This "triggers" exactly for return values of dtCMatrix_sparse_solve():*/
	    /* Don't want to translate this: want it report */
	    Rprintf("Note: as_cholmod_sparse() needed cholmod_sort()ing\n");
#endif
	    chm2Ralloc(ans, tmp);
	    cholmod_free_sparse(&tmp, &c);
	}
    }

    if (check_Udiag && ctype % 3 == 2 // triangular
	&& (*diag_P(x) == 'U')) { /* diagU2N(.)  "in place" : */
	double one[] = {1, 0};
	CHM_SP eye = cholmod_speye(ans->nrow, ans->ncol, ans->xtype, &c);
	CHM_SP tmp = cholmod_add(ans, eye, one, one, TRUE, TRUE, &c);

#ifdef DEBUG_Matrix_verbose /* happens quite often, e.g. in ../tests/indexing.R : */
	Rprintf("Note: as_cholmod_sparse(<ctype=%d>) - diagU2N\n", ctype);
#endif
	chm2Ralloc(ans, tmp);
	cholmod_free_sparse(&tmp, &c);
	cholmod_free_sparse(&eye, &c);
    } /* else :
       * NOTE: if(*diag_P(x) == 'U'), the diagonal is lost (!);
       * ---- that may be ok, e.g. if we are just converting from/to Tsparse,
       *      but is *not* at all ok, e.g. when used before matrix products */

    return ans;
}

/**
 * Copy the contents of a to an appropriate CsparseMatrix object and,
 * optionally, free a or free both a and its the pointers to its contents.
 *
 * @param a  (cholmod_sparse) matrix to be converted
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 * @param uploT 0 - not triangular; > 0 upper triangular; < 0 lower
 * @param Rkind - vector type to store for a->xtype == CHOLMOD_REAL,
 *                0 - REAL; 1 - LOGICAL  [unused for other a->xtype]
 * @param diag character string suitable for the diag slot of a
 *          triangular matrix (not accessed if uploT == 0).
 * @param dn either R_NilValue or an SEXP suitable for the Dimnames slot.
 *
 * @return SEXP containing a copy of a
 */
SEXP chm_sparse_to_SEXP(CHM_SP a, int dofree, int uploT, int Rkind,
			const char* diag, SEXP dn)
{
    SEXP ans;
    char *cls = "";/* -Wall */
    int *dims, nnz, *ansp, *ansi, *aii = (int*)(a->i), *api = (int*)(a->p),
	longi = (a->itype) == CHOLMOD_LONG;
    SuiteSparse_long *ail = (SuiteSparse_long*)(a->i), *apl = (SuiteSparse_long*)(a->p);

    PROTECT(dn);  /* dn is usually UNPROTECTed before the call */

				/* ensure a is sorted and packed */
    if (!a->sorted || !a->packed)
	longi ? cholmod_l_sort(a, &cl) : cholmod_sort(a, &c);
				/* determine the class of the result */

#define DOFREE_MAYBE							\
    if (dofree > 0)							\
	longi ? cholmod_l_free_sparse(&a, &cl) : cholmod_free_sparse(&a, &c); \
    else if (dofree < 0) Free(a)

    switch(a->xtype){
    case CHOLMOD_PATTERN:
	cls = uploT ? "ntCMatrix": ((a->stype) ? "nsCMatrix" : "ngCMatrix");
	break;
    case CHOLMOD_REAL:
	switch(Rkind) {
	case 0:
	    cls = uploT ? "dtCMatrix": ((a->stype) ? "dsCMatrix" : "dgCMatrix");
	    break;
	case 1:
	    cls = uploT ? "ltCMatrix": ((a->stype) ? "lsCMatrix" : "lgCMatrix");
	    break;
	default:
	    DOFREE_MAYBE;
	    error(_("chm_sparse_to_SEXP(<real>, *): invalid 'Rkind' (real kind code)"));
	}
	break;
    case CHOLMOD_COMPLEX:
	cls = uploT ? "ztCMatrix": ((a->stype) ? "zsCMatrix" : "zgCMatrix");
	break;
    default:
	DOFREE_MAYBE;
	error(_("unknown xtype in cholmod_sparse object"));
    }
    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cls)));
				/* allocate and copy common slots */
    nnz = longi ? cholmod_l_nnz(a, &cl) : cholmod_nnz(a, &c);
    dims = INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2));
    dims[0] = a->nrow; dims[1] = a->ncol;
    ansp = INTEGER(ALLOC_SLOT(ans, Matrix_pSym, INTSXP, a->ncol + 1));
    ansi = INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP, nnz));
    for (int j = 0; j <= a->ncol; j++) ansp[j] = longi ? (int)(apl[j]) : api[j];
    for (int p = 0; p < nnz; p++) ansi[p] = longi ? (int)(ail[p]) : aii[p];
				/* copy data slot if present */
    if (a->xtype == CHOLMOD_REAL) {
	int i, *m_x;
	double *a_x = (double *) a->x;
	switch(Rkind) {
	case 0:
	    Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, nnz)),
		   a_x, nnz);
	    break;
	case 1:
	    m_x = LOGICAL(ALLOC_SLOT(ans, Matrix_xSym, LGLSXP, nnz));
	    for (i=0; i < nnz; i++)
		m_x[i] = ISNAN(a_x[i]) ? NA_LOGICAL : (a_x[i] != 0);
	    break;
	}
    }
    else if (a->xtype == CHOLMOD_COMPLEX) {
	DOFREE_MAYBE;
	error(_("complex sparse matrix code not yet written"));
/* 	Memcpy(COMPLEX(ALLOC_SLOT(ans, Matrix_xSym, CPLXSXP, nnz)), */
/* 	       (complex *) a->x, nnz); */
    }
    if (uploT) {		/* slots for triangularMatrix */
	if (a->stype) error(_("Symmetric and triangular both set"));
	SET_SLOT(ans, Matrix_uploSym, mkString((uploT > 0) ? "U" : "L"));
	SET_SLOT(ans, Matrix_diagSym, mkString(diag));
    }
    if (a->stype)		/* slot for symmetricMatrix */
	SET_SLOT(ans, Matrix_uploSym,
		 mkString((a->stype > 0) ? "U" : "L"));
    DOFREE_MAYBE;
    if (dn != R_NilValue)
	SET_SLOT(ans, Matrix_DimNamesSym, duplicate(dn));

    UNPROTECT(2);
    return ans;
}
#undef DOFREE_MAYBE


/**
* Change the "type" of a cholmod_sparse matrix, i.e. modify it "in place"
*
* @param to_xtype requested xtype (pattern, real, complex, zomplex)
* @param A sparse matrix to change
* @param Common cholmod's common
*
* @return TRUE/FALSE , TRUE iff success
*/
Rboolean chm_MOD_xtype(int to_xtype, cholmod_sparse *A, CHM_CM Common) {
//     *MOD*: shouting, as A is modified in place

/* --------------------------------------------------------------------------
 * cholmod_sparse_xtype: change the xtype of a sparse matrix
 * --------------------------------------------------------------------------
  int cholmod_sparse_xtype
  (
      // ---- input ----
      int to_xtype,	//
      // ---- in/out ---
      cholmod_sparse *A, //
      // ---------------
      cholmod_common *Common
  ) ;

  int cholmod_l_sparse_xtype (int, cholmod_sparse *, cholmod_common *) ;
*/
    if((A->itype) == CHOLMOD_LONG) {
	return (Rboolean) cholmod_l_sparse_xtype (to_xtype, A, Common);
    } else {
	return (Rboolean) cholmod_sparse_xtype   (to_xtype, A, Common);
    }
}


/**
 * Populate ans with the pointers from x and modify its scalar
 * elements accordingly. Note that later changes to the contents of
 * ans will change the contents of the SEXP.
 *
 * In most cases this function is called through the macros
 * AS_CHM_TR() or AS_CHM_TR__().  It is unusual to call it directly.
 *
 * @param ans a CHM_TR pointer
 * @param x pointer to an object that inherits from TsparseMatrix
 * @param check_Udiag boolean - should a check for (and consequent
 *  expansion of) a unit diagonal be performed.
 *
 * @return ans containing pointers to the slots of x, *unless*
 *	check_Udiag and x is unitriangular.
 */
CHM_TR as_cholmod_triplet(CHM_TR ans, SEXP x, Rboolean check_Udiag)
{
    static const char *valid[] = { MATRIX_VALID_Tsparse, ""};
    int ctype = Matrix_check_class_etc(x, valid),
	*dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    SEXP islot = GET_SLOT(x, Matrix_iSym);
    int m = LENGTH(islot);
    Rboolean do_Udiag = (check_Udiag && ctype % 3 == 2 && (*diag_P(x) == 'U'));
    if (ctype < 0) error(_("invalid class of object to as_cholmod_triplet"));

    memset(ans, 0, sizeof(cholmod_triplet)); /* zero the struct */

    ans->itype = CHOLMOD_INT;	/* characteristics of the system */
    ans->dtype = CHOLMOD_DOUBLE;
				/* nzmax, dimensions, types and slots : */
    ans->nnz = ans->nzmax = m;
    ans->nrow = dims[0];
    ans->ncol = dims[1];
    ans->stype = stype(ctype, x);
    ans->xtype = xtype(ctype);
    ans->i = (void *) INTEGER(islot);
    ans->j = (void *) INTEGER(GET_SLOT(x, Matrix_jSym));
    ans->x = xpt(ctype, x);

    if(do_Udiag) {
	/* diagU2N(.) "in place", similarly to Tsparse_diagU2N [./Tsparse.c]
	   (but without new SEXP): */
	int k = m + dims[0];
	CHM_TR tmp = cholmod_l_copy_triplet(ans, &c);
	int *a_i, *a_j;

	if(!cholmod_reallocate_triplet((size_t) k, tmp, &c))
	    error(_("as_cholmod_triplet(): could not reallocate for internal diagU2N()"
		      ));

	/* TODO? instead of copy_triplet() & reallocate_triplet()
	 * ---- allocate to correct length + Memcpy() here, as in
	 * Tsparse_diagU2N() & chTr2Ralloc() below */
	a_i = tmp->i;
	a_j = tmp->j;
	/* add (@i, @j)[k+m] = k, @x[k+m] = 1.   for k = 0,..,(n-1) */
	for(k=0; k < dims[0]; k++) {
	    a_i[k+m] = k;
	    a_j[k+m] = k;

	    switch(ctype / 3) {
	    case 0: { /* "d" */
		double *a_x = tmp->x;
		a_x[k+m] = 1.;
		break;
	    }
	    case 1: { /* "l" */
		int *a_x = tmp->x;
		a_x[k+m] = 1;
		break;
	    }
	    case 2: /* "n" */
		break;
	    case 3: { /* "z" */
		double *a_x = tmp->x;
		a_x[2*(k+m)  ] = 1.;
		a_x[2*(k+m)+1] = 0.;
		break;
	    }
	    }
	} /* for(k) */

	chTr2Ralloc(ans, tmp);
	cholmod_l_free_triplet(&tmp, &c);

    } /* else :
       * NOTE: if(*diag_P(x) == 'U'), the diagonal is lost (!);
       * ---- that may be ok, e.g. if we are just converting from/to Tsparse,
       *      but is *not* at all ok, e.g. when used before matrix products */

    return ans;
}

/**
 * Copy the contents of a to an appropriate TsparseMatrix object and,
 * optionally, free a or free both a and its the pointers to its contents.
 *
 * @param a matrix to be converted
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 * @param uploT 0 - not triangular; > 0 upper triangular; < 0 lower
 * @param Rkind - vector type to store for a->xtype == CHOLMOD_REAL,
 *                0 - REAL; 1 - LOGICAL
 * @param diag character string suitable for the diag slot of a
 *          triangular matrix (not accessed if uploT == 0).
 * @param dn either R_NilValue or an SEXP suitable for the Dimnames slot.
 *
 * @return SEXP containing a copy of a
 */
SEXP chm_triplet_to_SEXP(CHM_TR a, int dofree, int uploT, int Rkind,
			 const char* diag, SEXP dn)
{
    SEXP ans;
    char *cl = "";		/* -Wall */
    int *dims;

    PROTECT(dn);  /* dn is usually UNPROTECTed before the call */
				/* determine the class of the result */

#define DOFREE_MAYBE					\
    if (dofree > 0) cholmod_free_triplet(&a, &c);	\
    else if (dofree < 0) Free(a)

    switch(a->xtype) {
    case CHOLMOD_PATTERN:
	cl = uploT ? "ntTMatrix" :
	    ((a->stype) ? "nsTMatrix" : "ngTMatrix");
	break;
    case CHOLMOD_REAL:
	switch(Rkind) {
	case 0:
	    cl = uploT ? "dtTMatrix" :
		((a->stype) ? "dsTMatrix" : "dgTMatrix");
	    break;
	case 1:
	    cl = uploT ? "ltTMatrix" :
		((a->stype) ? "lsTMatrix" : "lgTMatrix");
	    break;
	}
	break;
    case CHOLMOD_COMPLEX:
	cl = uploT ? "ztTMatrix" :
	    ((a->stype) ? "zsTMatrix" : "zgTMatrix");
	break;
    default:
	DOFREE_MAYBE;
	error(_("unknown xtype in cholmod_triplet object"));
    }
    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cl)));
				/* allocate and copy common slots */
    dims = INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2));
    dims[0] = a->nrow; dims[1] = a->ncol;
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP, a->nnz)),
	   (int *) a->i, a->nnz);
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_jSym, INTSXP, a->nnz)),
	   (int *) a->j, a->nnz);
				/* copy data slot if present */
    if (a->xtype == CHOLMOD_REAL) {
	int i, *m_x;
	double *a_x = (double *) a->x;
	switch(Rkind) {
	case 0:
	    Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, a->nnz)),
		   a_x, a->nnz);
	    break;
	case 1:
	    m_x = LOGICAL(ALLOC_SLOT(ans, Matrix_xSym, LGLSXP, a->nnz));
	    for (i=0; i < a->nnz; i++)
		m_x[i] = ISNAN(a_x[i]) ? NA_LOGICAL : (a_x[i] != 0);
	    break;
	}
    }
    else if (a->xtype == CHOLMOD_COMPLEX) {
	DOFREE_MAYBE;
	error(_("complex sparse matrix code not yet written"));
/* 	Memcpy(COMPLEX(ALLOC_SLOT(ans, Matrix_xSym, CPLXSXP, a->nnz)), */
/* 	       (complex *) a->x, a->nz); */
    }
    if (uploT) {		/* slots for triangularMatrix */
	if (a->stype) error(_("Symmetric and triangular both set"));
	SET_SLOT(ans, Matrix_uploSym, mkString((uploT > 0) ? "U" : "L"));
	SET_SLOT(ans, Matrix_diagSym, mkString(diag));
    }
				/* set symmetry attributes */
    if (a->stype)
	SET_SLOT(ans, Matrix_uploSym,
		 mkString((a->stype > 0) ? "U" : "L"));
    DOFREE_MAYBE;
    if (dn != R_NilValue)
	SET_SLOT(ans, Matrix_DimNamesSym, duplicate(dn));
    UNPROTECT(2);
    return ans;
}
#undef DOFREE_MAYBE

/**
 * Populate ans with the pointers from x and modify its scalar
 * elements accordingly. Note that later changes to the contents of
 * ans will change the contents of the SEXP.
 *
 * In most cases this function is called through the macro AS_CHM_DN.
 * It is unusual to call it directly.
 *
 * @param ans a CHM_DN pointer.
 * @param x pointer to an object that inherits from (denseMatrix ^ generalMatrix)
 *
 * @return ans containing pointers to the slots of x.
 */
CHM_DN as_cholmod_dense(CHM_DN ans, SEXP x)
{
#define _AS_cholmod_dense_1						\
    static const char *valid[] = { MATRIX_VALID_ge_dense, ""};		\
    int dims[2], ctype = Matrix_check_class_etc(x, valid), nprot = 0;	\
									\
    if (ctype < 0) {		/* not a classed matrix */		\
	if (isMatrix(x)) Memcpy(dims, INTEGER(getAttrib(x, R_DimSymbol)), 2); \
	else {dims[0] = LENGTH(x); dims[1] = 1;}			\
	if (isInteger(x)) {						\
	    x = PROTECT(coerceVector(x, REALSXP));			\
	    nprot++;							\
	}								\
	ctype = (isReal(x) ? 0 :					\
		 (isLogical(x) ? 2 : /* logical -> default to "l", not "n" */ \
		  (isComplex(x) ? 6 : -1)));				\
    } else Memcpy(dims, INTEGER(GET_SLOT(x, Matrix_DimSym)), 2);	\
    if (ctype < 0) error(_("invalid class of object to as_cholmod_dense")); \
    memset(ans, 0, sizeof(cholmod_dense)); /* zero the struct */        \
                                                                        \
    ans->dtype = CHOLMOD_DOUBLE; /* characteristics of the system */	\
    ans->x = ans->z = (void *) NULL;					\
				/* dimensions and nzmax */		\
    ans->d = ans->nrow = dims[0];					\
    ans->ncol = dims[1];						\
    ans->nzmax = dims[0] * dims[1];					\
				/* set the xtype and any elements */	\
    switch(ctype / 2) {							\
    case 0: /* "d" */							\
	ans->xtype = CHOLMOD_REAL;					\
	ans->x = (void *) REAL((ctype % 2) ? GET_SLOT(x, Matrix_xSym) : x); \
	break

    _AS_cholmod_dense_1;

    case 1: /* "l" */
	ans->xtype = CHOLMOD_REAL;
	ans->x = RallocedREAL((ctype % 2) ? GET_SLOT(x, Matrix_xSym) : x);
	break;
    case 2: /* "n" */
	ans->xtype = CHOLMOD_PATTERN;
	ans->x = (void *) LOGICAL((ctype % 2) ? GET_SLOT(x, Matrix_xSym) : x);
	break;

#define _AS_cholmod_dense_2						\
    case 3: /* "z" */							\
	ans->xtype = CHOLMOD_COMPLEX;					\
	ans->x = (void *) COMPLEX((ctype % 2) ? GET_SLOT(x, Matrix_xSym) : x); \
	break;								\
    }									\
    UNPROTECT(nprot);							\
    return ans

    _AS_cholmod_dense_2;
}

/* version of as_cholmod_dense() that produces a cholmod_dense matrix
 * with REAL 'x' slot -- i.e. treats "nMatrix" as "lMatrix" -- as only difference;
 * Not just via a flag in as_cholmod_dense() since that has fixed API */
CHM_DN as_cholmod_x_dense(CHM_DN ans, SEXP x)
{
    _AS_cholmod_dense_1;

    case 1: /* "l" */
    case 2: /* "n" (no NA in 'x', but *has* 'x' slot => treat as "l" */
	ans->xtype = CHOLMOD_REAL;
	ans->x = RallocedREAL((ctype % 2) ? GET_SLOT(x, Matrix_xSym) : x);
	break;

    _AS_cholmod_dense_2;
}

#undef _AS_cholmod_dense_1
#undef _AS_cholmod_dense_2

/**
* Transpose a cholmod_dense matrix  ("too trivial to be in CHOLMOD?")
*
* @param ans (pointer to) already allocated result of correct dimension
* @param x   (pointer to) cholmod_dense matrix to be transposed
*
*/
void chm_transpose_dense(CHM_DN ans, CHM_DN x)
{
    if (x->xtype != CHOLMOD_REAL)
	error(_("chm_transpose_dense(ans, x) not yet implemented for %s different from %s"),
	      "x->xtype", "CHOLMOD_REAL");
    double *xx = x->x, *ansx = ans->x;
    // Inspired from R's do_transpose() in .../R/src/main/array.c :
    int i,j, nrow = x->nrow, len = x->nzmax, l_1 = len-1;
    for (i = 0, j = 0; i < len; i++, j += nrow) {
	if (j > l_1) j -= l_1;
	ansx[i] = xx[j];
    }
    return;
}

void R_cholmod_error(int status, const char *file, int line, const char *message)
{
    CHM_restore_common(); /* restore any setting that may have been changed */

/* NB: keep in sync with M_R_cholmod_error(), ../inst/include/Matrix_stubs.c */

    /* From CHOLMOD/Include/cholmod_core.h : ...status values.
       zero means success, negative means a fatal error, positive is a warning.
    */
#ifndef R_CHOLMOD_ALWAYS_ERROR
    if(status < 0) {
#endif
	error(_("Cholmod error '%s' at file %s, line %d"), message, file, line);
#ifndef R_CHOLMOD_ALWAYS_ERROR
    }
    else
	warning(_("Cholmod warning '%s' at file %s, line %d"),
		message, file, line);
#endif
}

/* just to get 'int' instead of 'void' as required by CHOLMOD's print_function */
static
int R_cholmod_printf(const char* fmt, ...)
{
    va_list(ap);

    va_start(ap, fmt);
    Rprintf((char *)fmt, ap);
    va_end(ap);
    return 0;
}

/**
 * Initialize the CHOLMOD library and replace the print and error functions
 * by R-specific versions.
 *
 * @param c pointer to a cholmod_common structure to be initialized
 *
 * @return TRUE if successful
 */
int R_cholmod_start(CHM_CM c)
{
    int res;
    if (!(res = cholmod_start(c)))
	error(_("Unable to initialize cholmod: error code %d"), res);
    c->print_function = R_cholmod_printf; /* Rprintf gives warning */
    /* Since we provide an error handler, it may not be a good idea to allow CHOLMOD printing,
     * because that's not easily suppressed on the R level :
     * Hence consider, at least temporarily *  c->print_function = NULL; */
    c->error_handler = R_cholmod_error;
    return TRUE;
}

/**
 * Copy the contents of a to an appropriate denseMatrix object and,
 * optionally, free a or free both a and its pointer to its contents.
 *
 * @param a matrix to be converted
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 * @param Rkind type of R matrix to be generated (special to this function)
 * @param dn   -- dimnames [list(.,.) or NULL;  __already__ transposed when transp is TRUE ]
 * @param transp Rboolean, if TRUE, the result must be a copy of  t(a), i.e., "a transposed"
 *
 * @return SEXP containing a copy of a
 */
SEXP chm_dense_to_SEXP(CHM_DN a, int dofree, int Rkind, SEXP dn, Rboolean transp)
{
/* FIXME: should also have args  (int uploST, char *diag) */
    SEXP ans;
    char *cl = ""; /* -Wall */
    int *dims, ntot;

    PROTECT(dn); // <-- no longer protected in caller

#define DOFREE_de_MAYBE				\
    if (dofree > 0) cholmod_free_dense(&a, &c);	\
    else if (dofree < 0) Free(a);

    switch(a->xtype) {		/* determine the class of the result */
/* CHOLMOD_PATTERN never happens because cholmod_dense can't :
 *     case CHOLMOD_PATTERN:
 * 	cl = "ngeMatrix"; break;
 */
    case CHOLMOD_REAL:
	switch(Rkind) { /* -1: special for this function! */
	case -1: cl = "ngeMatrix"; break;
	case 0:	 cl = "dgeMatrix"; break;
	case 1:	 cl = "lgeMatrix"; break;
	default:
	    DOFREE_de_MAYBE;
	    error(_("unknown 'Rkind'"));
	}
	break;
    case CHOLMOD_COMPLEX:
	cl = "zgeMatrix"; break;
    default:
	DOFREE_de_MAYBE;
	error(_("unknown xtype"));
    }

    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cl)));
				/* allocate and copy common slots */
    dims = INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2));
    if(transp) {
	dims[1] = a->nrow; dims[0] = a->ncol;
    } else {
	dims[0] = a->nrow; dims[1] = a->ncol;
    }
    ntot = dims[0] * dims[1];
    if (a->d == a->nrow) { /* copy data slot -- always present in dense(!) */
	if (a->xtype == CHOLMOD_REAL) {
	    int i, *m_x;
	    double *ansx, *a_x = (double *) a->x;
	    switch(Rkind) {
	    case 0:
		ansx = REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, ntot));
		if(transp) {
		    // Inspired from R's do_transpose() in .../R/src/main/array.c :
		    int i,j, nrow = a->nrow, len = ntot, l_1 = len-1;
		    for (i = 0, j = 0; i < len; i++, j += nrow) {
			if (j > l_1) j -= l_1;
			ansx[i] = a_x[j];
		    }
		} else {
		    Memcpy(ansx, a_x, ntot);
		}
		break;
	    case -1: /* nge*/
	    case 1:  /* lge*/
		m_x = LOGICAL(ALLOC_SLOT(ans, Matrix_xSym, LGLSXP, ntot));
		if(transp) {
		    // Inspired from R's do_transpose() in .../R/src/main/array.c :
		    int i,j, nrow = a->nrow, len = ntot, l_1 = len-1;
		    for (i = 0, j = 0; i < len; i++, j += nrow) {
			if (j > l_1) j -= l_1;
			m_x[i] = a_x[j];
		    }
		} else {
		    for (i=0; i < ntot; i++)
			m_x[i] = ISNAN(a_x[i]) ? NA_LOGICAL : (a_x[i] != 0);
		}
		break;
	    }
	}
	else if (a->xtype == CHOLMOD_COMPLEX) {
	    DOFREE_de_MAYBE;
	    error(_("complex sparse matrix code not yet written"));
/*	Memcpy(COMPLEX(ALLOC_SLOT(ans, Matrix_xSym, CPLXSXP, ntot)), */
/*	       (complex *) a->x, ntot); */
	}
    } else {
	DOFREE_de_MAYBE;
	error(_("code for cholmod_dense with holes not yet written"));
    }

    DOFREE_de_MAYBE;
    if (dn != R_NilValue)
	SET_SLOT(ans, Matrix_DimNamesSym, duplicate(dn));
    UNPROTECT(2);
    return ans;
}

/**
 * Copy the contents of a to a matrix object and, optionally, free a
 * or free both a and its pointer to its contents.
 *
 * @param a cholmod_dense structure to be converted {already REAL for original l..CMatrix}
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 * @param dn either R_NilValue or an SEXP suitable for the Dimnames slot.
 *
 * @return SEXP containing a copy of a as a matrix object
 */
SEXP chm_dense_to_matrix(CHM_DN a, int dofree, SEXP dn)
{
#define CHM_DENSE_TYPE						\
    SEXPTYPE typ;						\
    /* determine the class of the result */			\
    typ = (a->xtype == CHOLMOD_PATTERN) ? LGLSXP :		\
	((a->xtype == CHOLMOD_REAL) ? REALSXP :			\
	 ((a->xtype == CHOLMOD_COMPLEX) ? CPLXSXP : NILSXP));	\
    if (typ == NILSXP) {					\
	DOFREE_de_MAYBE;					\
	error(_("unknown xtype"));				\
    }

    PROTECT(dn);
    CHM_DENSE_TYPE;

    SEXP ans = PROTECT(allocMatrix(typ, a->nrow, a->ncol));

#define CHM_DENSE_COPY_DATA						\
    if (a->d == a->nrow) {	/* copy data slot if present */		\
	if (a->xtype == CHOLMOD_REAL)					\
	    Memcpy(REAL(ans), (double *) a->x, a->nrow * a->ncol);	\
	else if (a->xtype == CHOLMOD_COMPLEX) {				\
	    DOFREE_de_MAYBE;						\
	    error(_("complex sparse matrix code not yet written"));	\
/* 	Memcpy(COMPLEX(ALLOC_SLOT(ans, Matrix_xSym, CPLXSXP, a->nnz)), */ \
/* 	       (complex *) a->x, a->nz); */				\
	} else if (a->xtype == CHOLMOD_PATTERN) {			\
	    DOFREE_de_MAYBE;						\
	    error(_("don't know if a dense pattern matrix makes sense")); \
	}								\
    } else {								\
	DOFREE_de_MAYBE;						\
	error(_("code for cholmod_dense with holes not yet written"));	\
    }

    CHM_DENSE_COPY_DATA;

    DOFREE_de_MAYBE;
    if (dn != R_NilValue)
        setAttrib(ans, R_DimNamesSymbol, duplicate(dn));
    UNPROTECT(2);
    return ans;
}

/**
 * Copy the contents of a to a numeric R object and, optionally, free a
 * or free both a and its pointer to its contents.
 *
 * @param a cholmod_dense structure to be converted
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 *
 * @return SEXP containing a copy of a  in the sense of  as.vector(a)
 */
SEXP chm_dense_to_vector(CHM_DN a, int dofree)
{
    CHM_DENSE_TYPE;

    SEXP ans = PROTECT(allocVector(typ, a->nrow * a->ncol));
    CHM_DENSE_COPY_DATA;
    DOFREE_de_MAYBE;
    UNPROTECT(1);
    return ans;
}

CHM_DN numeric_as_chm_dense(CHM_DN ans, double *v, int nr, int nc)
{
    ans->d = ans->nrow = nr;
    ans->ncol = nc;
    ans->nzmax = nr * nc;
    ans->x = (void *) v;
    ans->xtype = CHOLMOD_REAL;
    ans->dtype = CHOLMOD_DOUBLE;
    return ans;
}

/**
 * Populate ans with the pointers from x and modify its scalar
 * elements accordingly. Note that later changes to the contents of
 * ans will change the contents of the SEXP.
 *
 * In most cases this function is called through the macro AS_CHM_FR.
 * It is unusual to call it directly.
 *
 * @param ans an CHM_FR object
 * @param x pointer to an object that inherits from CHMfactor
 *
 * @return ans containing pointers to the slots of x.
 */
CHM_FR as_cholmod_factor(CHM_FR ans, SEXP x)
{
    static const char *valid[] = { MATRIX_VALID_CHMfactor, ""};
    int *type = INTEGER(GET_SLOT(x, install("type"))),
	ctype = Matrix_check_class_etc(x, valid);
    SEXP tmp;

    if (ctype < 0) error(_("invalid class of object to as_cholmod_factor"));
    memset(ans, 0, sizeof(cholmod_factor)); /* zero the struct */

    ans->itype = CHOLMOD_INT;	/* characteristics of the system */
    ans->dtype = CHOLMOD_DOUBLE;
    ans->z = (void *) NULL;
    ans->xtype = (ctype < 2) ? CHOLMOD_REAL : CHOLMOD_PATTERN;

    ans->ordering = type[0];	/* unravel the type */
    ans->is_ll = (type[1] ? 1 : 0);
    ans->is_super = (type[2] ? 1 : 0);
    ans->is_monotonic = (type[3] ? 1 : 0);
				/* check for consistency */
    if ((!(ans->is_ll)) && ans->is_super)
	error(_("Supernodal LDL' decomposition not available"));
    if ((!type[2]) ^ (ctype % 2))
	error(_("Supernodal/simplicial class inconsistent with type flags"));
				/* slots always present */
    tmp = GET_SLOT(x, Matrix_permSym);
    ans->minor = ans->n = LENGTH(tmp); ans->Perm = INTEGER(tmp);
    ans->ColCount = INTEGER(GET_SLOT(x, install("colcount")));
    ans->z = ans->x = (void *) NULL;
    if (ctype < 2) {
	tmp = GET_SLOT(x, Matrix_xSym);
	ans->x = REAL(tmp);
    }
    if (ans->is_super) {	/* supernodal factorization */
	ans->xsize = LENGTH(tmp);
	ans->maxcsize = type[4]; ans->maxesize = type[5];
	ans->i = (int*)NULL;
	tmp = GET_SLOT(x, install("super"));
	ans->nsuper = LENGTH(tmp) - 1; ans->super = INTEGER(tmp);
	/* Move these checks to the CHMfactor_validate function */
	if (ans->nsuper < 1)
	    error(_("Number of supernodes must be positive when is_super is TRUE"));
	tmp = GET_SLOT(x, install("pi"));
	if (LENGTH(tmp) != ans->nsuper + 1)
	    error(_("Lengths of super and pi must be equal"));
	ans->pi = INTEGER(tmp);
	tmp = GET_SLOT(x, install("px"));
	if (LENGTH(tmp) != ans->nsuper + 1)
	    error(_("Lengths of super and px must be equal"));
	ans->px = INTEGER(tmp);
	tmp = GET_SLOT(x, install("s"));
	ans->ssize = LENGTH(tmp); ans->s = INTEGER(tmp);
    } else {
	ans->nzmax = LENGTH(tmp);
	ans->p = INTEGER(GET_SLOT(x, Matrix_pSym));
	ans->i = INTEGER(GET_SLOT(x, Matrix_iSym));
	ans->nz = INTEGER(GET_SLOT(x, install("nz")));
	ans->next = INTEGER(GET_SLOT(x, install("nxt")));
	ans->prev = INTEGER(GET_SLOT(x, install("prv")));
    }
    if (!cholmod_check_factor(ans, &c))
	error(_("failure in as_cholmod_factor"));
    return ans;
}


/**
 * Copy the contents of f to an appropriate CHMfactor object and,
 * optionally, free f or free both f and its pointer to its contents.
 *
 * @param f cholmod_factor object to be converted
 * @param dofree 0 - don't free a; > 0 cholmod_free a; < 0 Free a
 *
 * @return SEXP containing a copy of a
 */
SEXP chm_factor_to_SEXP(CHM_FR f, int dofree)
{
    SEXP ans;
    int *dims, *type;
    char *class = (char*) NULL;	/* -Wall */

#define DOFREE_MAYBE					\
    if(dofree) {					\
	if (dofree > 0) cholmod_free_factor(&f, &c);	\
	else /* dofree < 0 */ Free(f);			\
    }

    if(!chm_factor_ok(f)) {
	DOFREE_MAYBE;
	error(_("CHOLMOD factorization was unsuccessful"));
	// error(_("previous CHOLMOD factorization was unsuccessful"));
    }

    switch(f->xtype) {
    case CHOLMOD_REAL:
	class = f->is_super ? "dCHMsuper" : "dCHMsimpl";
	break;
    case CHOLMOD_PATTERN:
	class = f->is_super ? "nCHMsuper" : "nCHMsimpl";
	break;
    default:
	DOFREE_MAYBE;
	error(_("f->xtype of %d not recognized"), f->xtype);
    }

    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(class)));
    dims = INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2));
    dims[0] = dims[1] = f->n;
				/* copy component of known length */
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_permSym, INTSXP, f->n)),
	   (int*)f->Perm, f->n);
    Memcpy(INTEGER(ALLOC_SLOT(ans, install("colcount"), INTSXP, f->n)),
	   (int*)f->ColCount, f->n);
    type = INTEGER(ALLOC_SLOT(ans, install("type"), INTSXP, f->is_super ? 6 : 4));
    type[0] = f->ordering; type[1] = f->is_ll;
    type[2] = f->is_super; type[3] = f->is_monotonic;
    if (f->is_super) {
	type[4] = f->maxcsize; type[5] = f->maxesize;
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("super"), INTSXP, f->nsuper + 1)),
	       (int*)f->super, f->nsuper+1);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("pi"), INTSXP, f->nsuper + 1)),
	       (int*)f->pi, f->nsuper + 1);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("px"), INTSXP, f->nsuper + 1)),
	       (int*)f->px, f->nsuper + 1);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("s"), INTSXP, f->ssize)),
	       (int*)f->s, f->ssize);
	Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, f->xsize)),
	       (double*)f->x, f->xsize);
    } else {
	Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP, f->nzmax)),
	       (int*)f->i, f->nzmax);
	Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_pSym, INTSXP, f->n + 1)),
	       (int*)f->p, f->n + 1);
	Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, f->nzmax)),
	       (double*)f->x, f->nzmax);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("nz"), INTSXP, f->n)),
	       (int*)f->nz, f->n);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("nxt"), INTSXP, f->n + 2)),
	       (int*)f->next, f->n + 2);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("prv"), INTSXP, f->n + 2)),
	       (int*)f->prev, f->n + 2);

    }
    DOFREE_MAYBE;
    UNPROTECT(1);
    return ans;
}
#undef DOFREE_MAYBE

/**
 * Drop the (unit) diagonal entries from a cholmod_sparse matrix
 *
 * @param chx   cholmod_sparse matrix.
 *              Note that the matrix "slots" are modified _in place_
 * @param uploT integer code (= +/- 1) indicating if chx is
 *              upper (+1) or lower (-1) triangular
 * @param do_realloc  Rboolean indicating, if a cholmod_sprealloc() should
 *              finalize the procedure; not needed, e.g. when the
 *              result is converted to a SEXP immediately afterwards.
 */
void chm_diagN2U(CHM_SP chx, int uploT, Rboolean do_realloc)
{
    int i, n = chx->nrow, nnz = (int)cholmod_nnz(chx, &c),
	n_nnz = nnz - n, /* the new nnz : we will have removed  n entries */
	i_to = 0, i_from = 0;

    if(chx->ncol != n)
	error(_("chm_diagN2U(<non-square matrix>): nrow=%d, ncol=%d"),
	      n, chx->ncol);

    if (!chx->sorted || !chx->packed) cholmod_sort(chx, &c);
				/* dimensions and nzmax */

#define _i(I) (   (int*) chx->i)[I]
#define _x(I) ((double*) chx->x)[I]
#define _p(I) (   (int*) chx->p)[I]

    /* work by copying from i_from to i_to ==> MUST i_to <= i_from */

    if(uploT == 1) { /* "U" : upper triangular */

	for(i = 0; i < n; i++) { /* looking at i-th column */
	    int j, n_i = _p(i+1) - _p(i); /* = #{entries} in this column */

	    /* 1) copy all but the last _above-diagonal_ column-entries: */
	    for(j = 1; j < n_i; j++, i_to++, i_from++) {
		_i(i_to) = _i(i_from);
		_x(i_to) = _x(i_from);
	    }

	    /* 2) drop the last column-entry == diagonal entry */
	    i_from++;
	}
    }
    else if(uploT == -1) { /* "L" : lower triangular */

	for(i = 0; i < n; i++) { /* looking at i-th column */
	    int j, n_i = _p(i+1) - _p(i); /* = #{entries} in this column */

	    /* 1) drop the first column-entry == diagonal entry */
	    i_from++;

	    /* 2) copy the other _below-diagonal_ column-entries: */
	    for(j = 1; j < n_i; j++, i_to++, i_from++) {
		_i(i_to) = _i(i_from);
		_x(i_to) = _x(i_from);
	    }
	}
    }
    else {
	error(_("chm_diagN2U(x, uploT = %d): uploT should be +- 1"), uploT);
    }

    /* the column pointers are modified the same in both cases :*/
    for(i=1; i <= n; i++)
	_p(i) -= i;

#undef _i
#undef _x
#undef _p

    if(do_realloc) /* shorten (i- and x-slots from nnz to n_nnz */
	cholmod_reallocate_sparse(n_nnz, chx, &c);
    return;
}

/* Placeholders; TODO: use checks above (search "CHMfactor_validate"): */

SEXP CHMfactor_validate(SEXP obj) /* placeholder */
{
    return ScalarLogical(1);
}

SEXP CHMsimpl_validate(SEXP obj) /* placeholder */
{
    return ScalarLogical(1);
}

SEXP CHMsuper_validate(SEXP obj) /* placeholder */
{
    return ScalarLogical(1);
}

