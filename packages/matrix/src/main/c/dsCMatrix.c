#include "dsCMatrix.h"

static int chk_nm(const char *nm, int perm, int LDL, int super)
{
    if (strlen(nm) != 11) return 0;
    if (strcmp(nm + 3, "Cholesky")) return 0;
    if (super  > 0 && nm[0] != 'S') return 0;
    if (super == 0 && nm[0] != 's') return 0;
    if (perm  > 0 && nm[1] != 'P') return 0;
    if (perm == 0 && nm[1] != 'p') return 0;
    if (LDL  > 0 && nm[2] != 'D') return 0;
    if (LDL == 0 && nm[2] != 'd') return 0;
    return 1;
}

SEXP R_chkName_Cholesky(SEXP nm, SEXP perm, SEXP LDL, SEXP super)
{
    return ScalarLogical(chk_nm(CHAR(asChar(nm)), asLogical(perm),
				asLogical(LDL), asLogical(super)));
}

// must be called with 'nm' a string of length 11
static void chm_factor_name(char* nm, int perm, int LDL, int super) {
    if (strlen(nm) != 11) {
	error(_("chm_factor_name(): did not get string of length 11"));
	return;
    }
    nm[0] = (super > 0) ? 'S' : 's';
    nm[1] = (perm == 0) ? 'p' : 'P';
    nm[2] = (LDL  == 0) ? 'd' : 'D';
    return;
}

// must be called with 'nm' a string of length 11
SEXP R_chm_factor_name(SEXP perm, SEXP LDL, SEXP super)
{
    char nm[12] = "...Cholesky";// 11 + final \0
    chm_factor_name(nm, asLogical(perm), asLogical(LDL), asLogical(super));
    return mkString(nm);
}



/**
 * Return a CHOLMOD copy of the cached Cholesky decomposition with the
 * required perm, LDL and super attributes.  If Imult is nonzero,
 * update the numeric values before returning.
 *
 * If no cached copy is available then evaluate one, cache it (for
 * zero Imult), and return a copy.
 *
 * @param Ap     dsCMatrix object
 * @param perm   integer indicating if permutation is required (!= 0),
 *               forbidden (0) [not yet: or optional (<0)]
 * @param LDL    integer indicating if the LDL' form is required (>0),
 *               forbidden (0) or optional (<0)
 * @param super  integer indicating if the supernodal form is required (>0),
 *               forbidden (0) or optional (<0)
 * @param Imult  numeric multiplier of I in  |A + Imult * I|
 */
static CHM_FR
internal_chm_factor(SEXP Ap, int perm, int LDL, int super, double Imult)
{
    SEXP facs = GET_SLOT(Ap, Matrix_factorSym);
    SEXP nms = getAttrib(facs, R_NamesSymbol);
    CHM_FR L;
    CHM_SP A = AS_CHM_SP__(Ap);
    R_CheckStack();

    CHM_store_common();		/* save settings from c */
    if (LENGTH(facs)) {
	for (int i = 0; i < LENGTH(nms); i++) { /* look for a match in cache */
	    if (chk_nm(CHAR(STRING_ELT(nms, i)), perm, LDL, super)) {
		L = AS_CHM_FR(VECTOR_ELT(facs, i));
		R_CheckStack();
		/* copy the factor so later it can safely be cholmod_free'd */
		L = cholmod_copy_factor(L, &c);
		if (Imult) cholmod_factorize_p(A, &Imult, (int*)NULL, 0, L, &c);
		return L;
	    }
	}
    }
    /* Else:  No cached factor - create one */

    c.final_ll = (LDL == 0) ? 1 : 0;
    c.supernodal = (super > 0) ? CHOLMOD_SUPERNODAL :
	((super < 0) ? CHOLMOD_AUTO :
	 /* super == 0 */ CHOLMOD_SIMPLICIAL);

    if (perm) {			/* obtain fill-reducing permutation */
	L = cholmod_analyze(A, &c);
    } else {			/* require identity permutation */
	c.nmethods = 1; c.method[0].ordering = CHOLMOD_NATURAL; c.postorder = FALSE;
	// *_restore_*() below or in R_cholmod_error() will restore c.<foo>
	L = cholmod_analyze(A, &c);
    }
    if (!cholmod_factorize_p(A, &Imult, (int*)NULL, 0 /*fsize*/, L, &c))
	// have never seen this, rather R_cholmod_error(status, ..) is called :
	error(_("Cholesky factorization failed; unusually, please report to Matrix-authors"));

    if (!Imult) {		/* cache the factor */
	if(!chm_factor_ok(L)) {
	    cholmod_free_factor(&L, &c);// <- do not leak!
	    CHM_restore_common();
	    error(_("internal_chm_factor: Cholesky factorization failed"));
	}

	/* now that we allow (super, LDL) to be "< 0", be careful :*/
	if(super < 0) super = L->is_super ? 1 : 0;
	if(LDL < 0)   LDL   = L->is_ll    ? 0 : 1;

	char fnm[12] = "...Cholesky";// 11 + final \0
	chm_factor_name(fnm, perm, LDL, super);

	set_factors(Ap, chm_factor_to_SEXP(L, 0), fnm);
    }
    CHM_restore_common();
    return L;
}

SEXP dsCMatrix_chol(SEXP x, SEXP pivot)
{
    int pivP = asLogical(pivot);
    CHM_FR L = internal_chm_factor(x, pivP, /*LDL = */ 0, /* super = */ 0,
				   /* Imult = */ 0.);
    CHM_SP R, Rt;
    SEXP ans;

    Rt = cholmod_factor_to_sparse(L, &c);
    R = cholmod_transpose(Rt, /*values*/ 1, &c);
    cholmod_free_sparse(&Rt, &c);
    ans = PROTECT(chm_sparse_to_SEXP(R, 1/*do_free*/, 1/*uploT*/, 0/*Rkind*/,
				     "N"/*diag*/, GET_SLOT(x, Matrix_DimNamesSym)));

    if (pivP) {
	SEXP piv = PROTECT(allocVector(INTSXP, L->n));
	int *dest = INTEGER(piv), *src = (int*)L->Perm;

	for (int i = 0; i < L->n; i++) dest[i] = src[i] + 1;
	setAttrib(ans, install("pivot"), piv);
	setAttrib(ans, install("rank"), ScalarInteger((size_t) L->minor));
	UNPROTECT(1);
    }
    cholmod_free_factor(&L, &c);
    UNPROTECT(1);
    return ans;
}

SEXP dsCMatrix_Cholesky(SEXP Ap, SEXP perm, SEXP LDL, SEXP super, SEXP Imult)
{
    int iSuper = asLogical(super),
	iPerm  = asLogical(perm),
	iLDL   = asLogical(LDL);

    /* When parameter is set to  NA  in R, let CHOLMOD choose */
    if(iSuper == NA_LOGICAL)	iSuper = -1;
    /* if(iPerm  == NA_LOGICAL)	iPerm  = -1; */
    if(iLDL   == NA_LOGICAL)	iLDL   = -1;
    SEXP r = chm_factor_to_SEXP(internal_chm_factor(Ap, iPerm, iLDL, iSuper,
						    asReal(Imult)),
				1 /* dofree */);
    return r;
}

/**
 * Fast version of getting at the diagonal matrix D of the
 * (generalized) simplicial Cholesky LDL' decomposition of a
 * (sparse symmetric) dsCMatrix.
 *
 * @param Ap  symmetric CsparseMatrix
 * @param permP  logical indicating if permutation is allowed
 * @param resultKind an (SEXP) string indicating which kind of result
 *        is desired.
 *
 * @return SEXP containing either the vector diagonal entries of D,
 *         or just  sum_i D[i], prod_i D[i] or  sum_i log(D[i]).
 */
SEXP dsCMatrix_LDL_D(SEXP Ap, SEXP permP, SEXP resultKind)
{
    CHM_FR L;
    SEXP ans;
    L = internal_chm_factor(Ap, asLogical(permP),
			    /*LDL*/ 1, /*super*/0, /*Imult*/0.);
    // ./Csparse.c :
    ans = PROTECT(diag_tC_ptr(L->n,
			      L->p,
			      L->x,
			      /* is_U = */ FALSE,
			      L->Perm,
			      resultKind));
    cholmod_free_factor(&L, &c);
    UNPROTECT(1);
    return(ans);
}

// using cholmod_spsolve() --> sparse result
SEXP dsCMatrix_Csparse_solve(SEXP a, SEXP b, SEXP LDL)
{
    int iLDL = asLogical(LDL);
    // When parameter is set to  NA  in R, let CHOLMOD choose
    if(iLDL == NA_LOGICAL) iLDL = -1;

    CHM_FR L = internal_chm_factor(a, /*perm*/-1, iLDL, /*super*/-1, /*Imult*/0.);
    if(!chm_factor_ok(L)) {
	cholmod_free_factor(&L, &c);
	return R_NilValue;// == "CHOLMOD factorization failed"
    }
    CHM_SP cb = AS_CHM_SP(b), cx;
    R_CheckStack();

    cx = cholmod_spsolve(CHOLMOD_A, L, cb, &c);
    cholmod_free_factor(&L, &c);
    return chm_sparse_to_SEXP(cx, /*do_free*/ 1, /*uploT*/ 0,
			      /*Rkind*/ 0, /*diag*/ "N",
			      /*dimnames = */ R_NilValue);
}

// using cholmod_solve() --> dense result
SEXP dsCMatrix_matrix_solve(SEXP a, SEXP b, SEXP LDL)
{
    int iLDL = asLogical(LDL);
    // When parameter is set to  NA  in R, let CHOLMOD choose
    if(iLDL == NA_LOGICAL) iLDL = -1;

    CHM_FR L = internal_chm_factor(a, /*perm*/-1, iLDL, /*super*/-1, /*Imult*/0.);
    if(!chm_factor_ok(L)) {
	cholmod_free_factor(&L, &c);
	return R_NilValue;// == "CHOLMOD factorization failed"
    }

    CHM_DN cx, cb = AS_CHM_DN(PROTECT(mMatrix_as_dgeMatrix(b)));
    R_CheckStack();
    cx = cholmod_solve(CHOLMOD_A, L, cb, &c);
    cholmod_free_factor(&L, &c);
    UNPROTECT(1);
    return chm_dense_to_SEXP(cx, 1, 0, /*dimnames = */ R_NilValue, /* transp: */ FALSE);
}

/* Needed for printing dsCMatrix objects */
/* FIXME: Create a more general version of this operation: also for lsC, (dsR?),..
*         e.g. make  compressed_to_dgTMatrix() in ./dgCMatrix.c work for dsC */
SEXP dsCMatrix_to_dgTMatrix(SEXP x)
{
    CHM_SP A = AS_CHM_SP__(x);
    CHM_SP Afull = cholmod_copy(A, /*stype*/ 0, /*mode*/ 1, &c);
    CHM_TR At = cholmod_sparse_to_triplet(Afull, &c);
    R_CheckStack();

    if (!A->stype)
	error(_("Non-symmetric matrix passed to dsCMatrix_to_dgTMatrix"));
    cholmod_free_sparse(&Afull, &c);
    return chm_triplet_to_SEXP(At, 1, /*uploT*/ 0, /*Rkind*/ 0, "",
			       GET_SLOT(x, Matrix_DimNamesSym));
}
