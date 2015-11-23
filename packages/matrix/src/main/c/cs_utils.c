#include "cs_utils.h"

/* Borrowed from one of Tim Davis' examples in the CSparse Demo directory */
/* 1 if A is square & upper tri., -1 if square & lower tri., 0 otherwise */
static int is_sym (cs *A)
{
    int is_upper, is_lower, j, p, n = A->n, m = A->m, *Ap = A->p, *Ai = A->i ;
    if (m != n) return (0) ;
    is_upper = 1 ;
    is_lower = 1 ;
    for (j = 0 ; j < n ; j++)
    {
	for (p = Ap [j] ; p < Ap [j+1] ; p++)
	{
	    if (Ai [p] > j) is_upper = 0 ;
	    if (Ai [p] < j) is_lower = 0 ;
	}
    }
    return (is_upper ? 1 : (is_lower ? -1 : 0)) ;
}


/**
 * Create an identity matrix of size n as a cs struct.  The structure
 * must be freed with cs_free by the caller.
 *
 * @param n size of identity matrix to construct.
 *
 * @return pointer to a cs object that contains the identity matrix.
 */
static CSP csp_eye(int n)
{
    CSP eye = cs_spalloc(n, n, n, 1, 0);
    int *ep = eye->p, *ei = eye->i;
    double *ex = eye->x;

    if (n <= 0) error(_("csp_eye argument n must be positive"));
    eye->nz = -1;		/* compressed column storage */
    for (int j = 0; j < n; j++) {
	ep[j] = ei[j] = j;
	ex[j] = 1;
    }
    eye->nzmax = ep[n] = n;
    return eye;
}

/**
 * Create a cs object with the contents of x.  Typically called via  AS_CSP()
 *
 * @param ans pointer to a cs struct.  This is allocated in the caller
 *  so it is easier to keep track of where it should be freed - in many
 *  applications the memory can be allocated with alloca and
 *  automatically freed on exit from the caller.
 * @param x pointer to an object that inherits from CsparseMatrix
 * @param check_Udiag boolean - should a check for (and consequent
 *  expansion of) a unit diagonal be performed.
 *
 * @return pointer to a cs object that contains pointers
 * to the slots of x.
 */
cs *Matrix_as_cs(cs *ans, SEXP x, Rboolean check_Udiag)
{
    static const char *valid[] = {"dgCMatrix", "dtCMatrix", ""};
    /* had also "dsCMatrix", but that only stores one triangle */
    int *dims, ctype = Matrix_check_class_etc(x, valid);
    SEXP islot;

    if (ctype < 0) error(_("invalid class of 'x' in Matrix_as_cs(a, x)"));
				/* dimensions and nzmax */
    dims = INTEGER(GET_SLOT(x, Matrix_DimSym));
    ans->m = dims[0]; ans->n = dims[1];
    islot = GET_SLOT(x, Matrix_iSym);
    ans->nz = -1;		/* indicates compressed column storage */
    ans->nzmax = LENGTH(islot);
    ans->i = INTEGER(islot);
    ans->p = INTEGER(GET_SLOT(x, Matrix_pSym));
    ans->x = REAL(GET_SLOT(x, Matrix_xSym));

    if(check_Udiag && ctype == 1 && (*diag_P(x) == 'U')) { /* diagU2N(.) : */
	int n = dims[0];
	CSP I_n = csp_eye(n);

	/* tmp := 1*ans + 1*eye -- result is newly allocated in cs_add(): */
	CSP tmp = cs_add(ans, I_n, 1., 1.), t2;
	int nz = (tmp->p)[n];

	/* double transpose trick to sort the columns */
	cs_spfree(I_n);
	t2 = cs_transpose(tmp, 1); /* transpose including values */
	cs_spfree(tmp);
	tmp = cs_transpose(t2, 1);
	cs_spfree(t2);

	/* content(ans) := content(tmp) : */
	ans->nzmax = nz;
	/* The ans "slots" were pointers to x@ <slots>; all need new content now: */
	ans->p = Memcpy((   int*) R_alloc(n+1, sizeof(int)),
			(   int*) tmp->p, n+1);
	ans->i = Memcpy((   int*) R_alloc(nz, sizeof(int)),
			(   int*) tmp->i, nz);
	ans->x = Memcpy((double*) R_alloc(nz, sizeof(double)),
			(double*) tmp->x, nz);

	cs_spfree(tmp);
    }
    return ans;
}

/**
 * Copy the contents of a to an appropriate CsparseMatrix object and,
 * optionally, free a or free both a and the pointers to its contents.
 *
 * @param a matrix to be converted
 * @param cl the name of the S4 class of the object to be generated
 * @param dofree 0 - don't free a; > 0 cs_free a; < 0 Free a
 * @param dn either R_NilValue or an SEXP suitable for the Dimnames slot.
 *
 * @return SEXP containing a copy of a
 */
/* FIXME:  Change API : May need object,  not just class name 'cl' */
SEXP Matrix_cs_to_SEXP(cs *a, char *cl, int dofree, SEXP dn)
{
    static const char *valid[] = {"dgCMatrix", "dsCMatrix", "dtCMatrix", ""};
    int ctype = Matrix_check_class(cl, valid);

    if (ctype < 0)
	error(_("invalid class of object to %s"), "Matrix_cs_to_SEXP");
    SEXP ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cl)));
				/* allocate and copy common slots */
    int *dims = INTEGER(ALLOC_SLOT(ans, Matrix_DimSym, INTSXP, 2));
    PROTECT(dn); // <- as in chm_sparse_to_SEXP()
    dims[0] = a->m; dims[1] = a->n;
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_pSym, INTSXP, a->n + 1)),
	   a->p, a->n + 1);
    int nz = a->p[a->n];
    Memcpy(INTEGER(ALLOC_SLOT(ans, Matrix_iSym, INTSXP, nz)), a->i, nz);
    Memcpy(REAL(ALLOC_SLOT(ans, Matrix_xSym, REALSXP, nz)), a->x, nz);
    if (ctype > 0) { /* dsC or dtC */
	int uplo = is_sym(a);
	if (!uplo)
	    error(_("cs matrix not compatible with class '%s'"), valid[ctype]);
	if (ctype == 2) /* dtC* */
	    SET_SLOT(ans, Matrix_diagSym, mkString("N"));
	SET_SLOT(ans, Matrix_uploSym, mkString(uplo < 0 ? "L" : "U"));
    }
    if (dofree > 0) cs_spfree(a);
    if (dofree < 0) Free(a);
    if (dn != R_NilValue)
	SET_SLOT(ans, Matrix_DimNamesSym, duplicate(dn));
    UNPROTECT(2);
    return ans;
}

#if 0 				/* unused ------------------------------------*/
/*  -------------------------------------*/

/**
 * Populate a css object with the contents of x.
 *
 * @param ans pointer to a csn object
 * @param x pointer to an object of class css_LU or css_QR.
 *
 * @return pointer to a cs object that contains pointers
 * to the slots of x.
 */
css *Matrix_as_css(css *ans, SEXP x)
{
    char *cl = class_P(x);
    static const char *valid[] = {"css_LU", "css_QR", ""};
    int *nz = INTEGER(GET_SLOT(x, install("nz"))),
	ctype = Matrix_check_class(cl, valid);

    if (ctype < 0)
	error(_("invalid class of object to %s"), "Matrix_as_css");
    ans->q = INTEGER(GET_SLOT(x, install("Q")));
    ans->m2 = nz[0]; ans->lnz = nz[1]; ans->unz = nz[2];
    switch(ctype) {
    case 0:			/* css_LU */
	ans->pinv = (int *) NULL;
	ans->parent = (int *) NULL;
	ans->cp = (int *) NULL;
	break;
    case 1:			/* css_QR */
	ans->pinv = INTEGER(GET_SLOT(x, install("Pinv")));
	ans->parent = INTEGER(GET_SLOT(x, install("parent")));
	ans->cp = INTEGER(GET_SLOT(x, install("cp")));
	break;
    default:
	error(_("invalid class of object to %s"), "Matrix_as_css");
    }
    return ans;
}

/**
 * Populate a csn object with the contents of x.
 *
 * @param ans pointer to a csn object
 * @param x pointer to an object of class csn_LU or csn_QR.
 *
 * @return pointer to a cs object that contains pointers
 * to the slots of x.
 */
csn *Matrix_as_csn(csn *ans, SEXP x)
{
    static const char *valid[] = {"csn_LU", "csn_QR", ""};
    int ctype = Matrix_check_class(class_P(x), valid);

    if (ctype < 0)
	error(_("invalid class of object to %s"), "Matrix_as_csn");
    ans->U = Matrix_as_cs(GET_SLOT(x, install("U")));
    ans->L = Matrix_as_cs(GET_SLOT(x, install("L")));
    switch(ctype) {
    case 0:
	ans->B = (double*) NULL;
	ans->pinv = INTEGER(GET_SLOT(x, install("Pinv")));
	break;
    case 1:
	ans->B = REAL(GET_SLOT(x, Matrix_betaSym));
	ans->pinv = (int*) NULL;
	break;
    default:
	error(_("invalid class of object to %s"), "Matrix_as_csn");
    }
    return ans;
}

/**
 * Copy the contents of S to a css_LU or css_QR object and,
 * optionally, free S or free both S and the pointers to its contents.
 *
 * @param a css object to be converted
 * @param cl the name of the S4 class of the object to be generated
 * @param dofree 0 - don't free a; > 0 cs_free a; < 0 Free a
 * @param m number of rows in original matrix
 * @param n number of columns in original matrix
 *
 * @return SEXP containing a copy of S
 */
SEXP Matrix_css_to_SEXP(css *S, char *cl, int dofree, int m, int n)
{
    SEXP ans;
    static const char *valid[] = {"css_LU", "css_QR", ""};
    int *nz, ctype = Matrix_check_class(cl, valid);

    if (ctype < 0)
	error(_("Inappropriate class cl='%s' in Matrix_css_to_SEXP(S, cl, ..)"),
	      cl);
    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cl)));
				/* allocate and copy common slots */
    Memcpy(INTEGER(ALLOC_SLOT(ans, install("Q"), INTSXP, n)), S->q, n);
    nz = INTEGER(ALLOC_SLOT(ans, install("nz"), INTSXP, 3));
    nz[0] = S->m2; nz[1] = S->lnz; nz[2] = S->unz;
    switch(ctype) {
    case 0:
	break;
    case 1:
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("Pinv"), INTSXP, m)),
	       S->pinv, m);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("parent"), INTSXP, n)),
	       S->parent, n);
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("cp"), INTSXP, n)),
	       S->cp, n);
	break;
    default:
	error(_("Inappropriate class cl='%s' in Matrix_css_to_SEXP(S, cl, ..)"),
	      cl);
    }
    if (dofree > 0) cs_sfree(S);
    if (dofree < 0) Free(S);
    UNPROTECT(1);
    return ans;
}

/**
 * Copy the contents of N to a csn_LU or csn_QR object and,
 * optionally, free N or free both N and the pointers to its contents.
 *
 * @param a csn object to be converted
 * @param cl the name of the S4 class of the object to be generated
 * @param dofree 0 - don't free a; > 0 cs_free a; < 0 Free a
 * @param dn either R_NilValue or an SEXP suitable for the Dimnames slot. FIXME (L,U!)
 *
 * @return SEXP containing a copy of S
 */
SEXP Matrix_csn_to_SEXP(csn *N, char *cl, int dofree, SEXP dn)
{
    SEXP ans;
    static const char *valid[] = {"csn_LU", "csn_QR", ""};
    int ctype = Matrix_check_class(cl, valid), n = (N->U)->n;

    if (ctype < 0)
	error(_("Inappropriate class cl='%s' in Matrix_csn_to_SEXP(S, cl, ..)"),
	      cl);
    ans = PROTECT(NEW_OBJECT(MAKE_CLASS(cl)));
				/* allocate and copy common slots */
    /* FIXME: Use the triangular matrix classes for csn_LU */
    SET_SLOT(ans, install("L"),	/* these are free'd later if requested */
	     Matrix_cs_to_SEXP(N->L, "dgCMatrix", 0, dn)); // FIXME: dn
    SET_SLOT(ans, install("U"),
	     Matrix_cs_to_SEXP(N->U, "dgCMatrix", 0, dn)); // FIXME: dn
    switch(ctype) {
    case 0:
	Memcpy(INTEGER(ALLOC_SLOT(ans, install("Pinv"), INTSXP, n)),
	       N->pinv, n);
	break;
    case 1:
	Memcpy(REAL(ALLOC_SLOT(ans, Matrix_betaSym, REALSXP, n)),
	       N->B, n);
	break;
    default:
	error(_("Inappropriate class cl='%s' in Matrix_csn_to_SEXP(S, cl, ..)"),
	      cl);
    }
    if (dofree > 0) cs_nfree(N);
    if (dofree < 0) {
	Free(N->L); Free(N->U); Free(N);
    }
    UNPROTECT(1);
    return ans;
}

#endif	/* unused */
