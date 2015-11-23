#include "Mutils.h"
#include "abIndex.h"
#include "chm_common.h"
#include "CHMfactor.h"
#include "Csparse.h"
#include "Tsparse.h"
#include "dense.h"
#include "dgCMatrix.h"
#include "dgTMatrix.h"
#include "dgeMatrix.h"
#include "dpoMatrix.h"
#include "dppMatrix.h"
#include "dsCMatrix.h"
#include "TMatrix_as.h"
#include "dspMatrix.h"
#include "dsyMatrix.h"
#include "dtCMatrix.h"
#include "dtTMatrix.h"
#include "dtrMatrix.h"
#include "dtpMatrix.h"
#include "factorizations.h"
#include "ldense.h"
#include "lgCMatrix.h"
#include "sparseQR.h"
#include <R_ext/Rdynload.h>

#include "Syms.h"

#define CALLDEF(name, n)  {#name, (DL_FUNC) &name, n}
#define EXTDEF(name, n)   {#name, (DL_FUNC) &name, n}


static R_CallMethodDef CallEntries[] = {
    CALLDEF(BunchKaufman_validate, 1),
    CALLDEF(pBunchKaufman_validate, 1),
    CALLDEF(CHMfactor_to_sparse, 1),
    CALLDEF(CHMfactor_solve, 3),
    CALLDEF(CHMfactor_spsolve, 3),
    CALLDEF(CHMfactor_ldetL2, 1),
    CALLDEF(CHMfactor_ldetL2up, 3),
    CALLDEF(CHMfactor_update, 3),
    CALLDEF(CHMfactor_updown,3),
    CALLDEF(destructive_CHM_update, 3),
    CALLDEF(Cholesky_validate, 1),
    CALLDEF(Csparse_Csparse_prod, 3),
    CALLDEF(Csparse_Csparse_crossprod, 4),
    CALLDEF(Csparse_MatrixMarket, 2),
    CALLDEF(Csparse_band, 3),
    CALLDEF(Csparse_crossprod, 4),
    CALLDEF(Csparse_dense_crossprod, 3),
    CALLDEF(Csparse_dense_prod, 3),
    CALLDEF(Csparse_diagN2U, 1),
    CALLDEF(Csparse_diagU2N, 1),
    CALLDEF(Csparse_drop, 2),
    CALLDEF(Csparse_horzcat, 2),
    CALLDEF(Csparse_sort, 1),
    CALLDEF(Csparse_to_Tsparse, 2),
    CALLDEF(Csparse_to_tCsparse, 3),
    CALLDEF(Csparse_to_tTsparse, 3),
    CALLDEF(Csparse_to_dense, 2),
    CALLDEF(Csparse_to_nz_pattern, 2),
    CALLDEF(Csparse_to_matrix, 3),
    CALLDEF(Csparse_to_vector, 1),
    CALLDEF(Csparse_submatrix, 3),
    CALLDEF(dCsparse_subassign, 4),
    CALLDEF(lCsparse_subassign, 4),
    CALLDEF(iCsparse_subassign, 4),
    CALLDEF(nCsparse_subassign, 4),
    CALLDEF(zCsparse_subassign, 4),
    CALLDEF(Csparse_general_to_symmetric, 3),
    CALLDEF(Csparse_symmetric_to_general, 1),
    CALLDEF(Csparse_transpose, 2),
    CALLDEF(Csparse_validate, 1),
    CALLDEF(Csparse_validate2, 2),
    CALLDEF(Csparse_vertcat, 2),
    CALLDEF(pCholesky_validate, 1),
    CALLDEF(Rsparse_validate, 1),
    CALLDEF(diag_tC, 2),
    CALLDEF(LU_expand, 1),
    CALLDEF(LU_validate, 1),
    CALLDEF(MatrixFactorization_validate, 1),
    CALLDEF(Matrix_expand_pointers, 1),
    CALLDEF(R_rbind2_vector, 2),
    CALLDEF(R_all0, 1),
    CALLDEF(R_any0, 1),
    CALLDEF(R_to_CMatrix, 1),
#ifdef _Matrix_has_SVD_
    CALLDEF(SVD_validate, 1),
#endif
    CALLDEF(Tsparse_validate, 1),
    CALLDEF(Tsparse_diagU2N, 1),
    CALLDEF(Tsparse_to_Csparse, 2),
    CALLDEF(Tsparse_to_tCsparse, 3),
    CALLDEF(compressed_to_TMatrix, 2),
    CALLDEF(compressed_non_0_ij, 2),
    CALLDEF(dense_to_Csparse, 1),
    CALLDEF(nz_pattern_to_Csparse, 2),
    CALLDEF(dense_nonpacked_validate, 1),
    CALLDEF(dense_band, 3),
    CALLDEF(dense_to_symmetric, 3),
    CALLDEF(ddense_symmpart, 1),
    CALLDEF(ddense_skewpart, 1),
    CALLDEF(dimNames_validate, 1),
    CALLDEF(Dim_validate, 2),
    CALLDEF(dMatrix_validate, 1),

    CALLDEF(dgCMatrix_LU, 5),
    CALLDEF(dgCMatrix_QR, 3),
#ifdef Matrix_with_SPQR
    CALLDEF(dgCMatrix_SPQR, 4),
#endif
    CALLDEF(dgCMatrix_colSums, 5),
    CALLDEF(igCMatrix_colSums, 5),
    CALLDEF(lgCMatrix_colSums, 5),
    CALLDEF(ngCMatrix_colSums, 5),
    CALLDEF(dgCMatrix_cholsol, 2),
    /* CALLDEF(dgCMatrix_lusol, 2), */
    CALLDEF(dgCMatrix_matrix_solve, 3),
    CALLDEF(dgCMatrix_qrsol, 3),
    CALLDEF(dgTMatrix_to_dgeMatrix, 1),
    CALLDEF(lgTMatrix_to_lgeMatrix, 1),
    CALLDEF(dgTMatrix_to_matrix, 1),
    CALLDEF(lgTMatrix_to_matrix, 1),
    CALLDEF(dgeMatrix_LU, 2),
    CALLDEF(dgeMatrix_Schur, 3),
    CALLDEF(dgeMatrix_colsums, 4),
    CALLDEF(dgeMatrix_crossprod, 2),
    CALLDEF (geMatrix_crossprod, 2),
    CALLDEF(dgeMatrix_determinant, 2),
    CALLDEF(dgeMatrix_dgeMatrix_crossprod, 3),
    CALLDEF (geMatrix_geMatrix_crossprod, 3),
    CALLDEF(dgeMatrix_matrix_mm, 3),
    CALLDEF (geMatrix_matrix_mm, 3),
    CALLDEF(dgeMatrix_matrix_solve, 2),
    CALLDEF(dgeMatrix_dtpMatrix_mm, 2),
    CALLDEF(dgeMatrix_exp, 1),
    CALLDEF(dgeMatrix_addDiag, 2),
    CALLDEF(dgeMatrix_getDiag, 1),
    CALLDEF(lgeMatrix_getDiag, 1),
    CALLDEF(dgeMatrix_setDiag, 2),
    CALLDEF(lgeMatrix_setDiag, 2),
    CALLDEF(dgeMatrix_matrix_crossprod, 3),
    CALLDEF (geMatrix_matrix_crossprod, 3),
    CALLDEF(dgeMatrix_norm, 2),
    CALLDEF(dgeMatrix_rcond, 2),
    CALLDEF(dgeMatrix_solve, 1),
    CALLDEF(dgeMatrix_validate, 1),
    CALLDEF(dpoMatrix_chol, 1),
    CALLDEF(dpoMatrix_dgeMatrix_solve, 2),
    CALLDEF(dpoMatrix_matrix_solve, 2),
    CALLDEF(dpoMatrix_rcond, 2),
    CALLDEF(dpoMatrix_solve, 1),
    CALLDEF(dpoMatrix_validate, 1),
    CALLDEF(dppMatrix_chol, 1),
    CALLDEF(dppMatrix_matrix_solve, 2),
    CALLDEF(dppMatrix_rcond, 2),
    CALLDEF(dppMatrix_solve, 1),
    CALLDEF(dppMatrix_validate, 1),
    CALLDEF(R_chkName_Cholesky, 4),
    CALLDEF(R_chm_factor_name, 3),
    CALLDEF(dsCMatrix_Cholesky, 5),
    CALLDEF(dsCMatrix_LDL_D, 3),
    CALLDEF(dsCMatrix_chol, 2),
    CALLDEF(dsCMatrix_Csparse_solve, 3),
    CALLDEF(dsCMatrix_matrix_solve,  3),
    CALLDEF(dsCMatrix_to_dgTMatrix, 1),
    CALLDEF(dsTMatrix_as_dgTMatrix, 1),
    CALLDEF(lsTMatrix_as_lgTMatrix, 1),
    CALLDEF(nsTMatrix_as_ngTMatrix, 1),
    CALLDEF(dsTMatrix_as_dsyMatrix, 1),
    CALLDEF(lsTMatrix_as_lsyMatrix, 1),
    CALLDEF(nsTMatrix_as_nsyMatrix, 1),
    CALLDEF(dsyMatrix_as_dspMatrix, 1),
    CALLDEF(dsyMatrix_as_matrix, 2),
    CALLDEF(dsyMatrix_matrix_mm, 3),
    CALLDEF(dsyMatrix_matrix_solve, 2),
    CALLDEF(dsyMatrix_norm, 2),
    CALLDEF(dsyMatrix_rcond, 2),
    CALLDEF(dsyMatrix_solve, 1),
    CALLDEF(dsyMatrix_trf, 1),
    CALLDEF(dspMatrix_as_dsyMatrix, 1),
    CALLDEF(dspMatrix_matrix_mm, 2),
    CALLDEF(dspMatrix_matrix_solve, 2),
    CALLDEF(dspMatrix_norm, 2),
    CALLDEF(dspMatrix_rcond, 2),
    CALLDEF(dspMatrix_solve, 1),
    CALLDEF(dspMatrix_trf, 1),
    CALLDEF(dspMatrix_validate, 1),
    CALLDEF(dspMatrix_getDiag, 1),
    CALLDEF(lspMatrix_getDiag, 1),
    CALLDEF(dspMatrix_setDiag, 2),
    CALLDEF(lspMatrix_setDiag, 2),
/*     CALLDEF(dtCMatrix_solve, 1), */
    CALLDEF(dtCMatrix_matrix_solve, 3),
    CALLDEF(dtCMatrix_sparse_solve, 2),
    CALLDEF(dtTMatrix_as_dtrMatrix, 1),
    CALLDEF(ltTMatrix_as_ltrMatrix, 1),
    CALLDEF(ntTMatrix_as_ntrMatrix, 1),
    CALLDEF(dtpMatrix_as_dtrMatrix, 1),
    CALLDEF(dtpMatrix_addDiag, 2),
    CALLDEF(dtpMatrix_getDiag, 1),
    CALLDEF(ltpMatrix_getDiag, 1),
    CALLDEF(dtpMatrix_setDiag, 2),
    CALLDEF(ltpMatrix_setDiag, 2),
    CALLDEF(dtpMatrix_matrix_mm, 4),
    CALLDEF(dtpMatrix_matrix_solve, 2),
    CALLDEF(dtpMatrix_norm, 2),
    CALLDEF(dtpMatrix_rcond, 2),
    CALLDEF(dtpMatrix_solve, 1),
    CALLDEF(dtpMatrix_validate, 1),
    CALLDEF(dtrMatrix_as_dtpMatrix, 1),
    CALLDEF(dtrMatrix_as_matrix, 2),
    CALLDEF(dtrMatrix_matrix_mm,    4),
    CALLDEF(dtrMatrix_dtrMatrix_mm, 4),
    CALLDEF(dtrMatrix_chol2inv, 1),
    CALLDEF(dtrMatrix_addDiag, 2),
    CALLDEF(dtrMatrix_getDiag, 1),
    CALLDEF(ltrMatrix_getDiag, 1),
    CALLDEF(dtrMatrix_setDiag, 2),
    CALLDEF(ltrMatrix_setDiag, 2),
    CALLDEF(dtrMatrix_matrix_solve, 2),
    CALLDEF(dtrMatrix_norm, 2),
    CALLDEF(dtrMatrix_rcond, 2),
    CALLDEF(dtrMatrix_solve, 1),
    CALLDEF(dup_mMatrix_as_dgeMatrix, 1),
    CALLDEF(dup_mMatrix_as_geMatrix, 1),

    /* for dgC* _and_ lgC* : */
    CALLDEF(xCMatrix_validate, 1),
    CALLDEF(xRMatrix_validate, 1),
    CALLDEF(xTMatrix_validate, 1),
    CALLDEF(tCMatrix_validate, 1),
    CALLDEF(tRMatrix_validate, 1),
    CALLDEF(tTMatrix_validate, 1),

    CALLDEF(lapack_qr, 2),

    CALLDEF(lgC_to_matrix, 1),
    CALLDEF(ngC_to_matrix, 1),

    CALLDEF(lspMatrix_as_lsyMatrix, 2),
    CALLDEF(lsyMatrix_as_lspMatrix, 2),
    CALLDEF(lsyMatrix_as_lgeMatrix, 2),
    CALLDEF(ltpMatrix_as_ltrMatrix, 2),
    CALLDEF(ltrMatrix_as_lgeMatrix, 2),
    CALLDEF(ltrMatrix_as_ltpMatrix, 2),

    CALLDEF(lsq_dense_Chol, 2),
    CALLDEF(lsq_dense_QR, 2),
    CALLDEF(sparseQR_validate, 1),
    CALLDEF(sparseQR_qty, 4),
    CALLDEF(sparseQR_coef, 2),
    CALLDEF(sparseQR_resid_fitted, 3),
    CALLDEF(triangularMatrix_validate, 1),
    CALLDEF(symmetricMatrix_validate, 1),
    CALLDEF(R_symmetric_Dimnames, 1),

/* still simple placeholders, but already used in ../R/AllClass.R : */
    CALLDEF(CHMfactor_validate, 1),
    CALLDEF(CHMsimpl_validate, 1),
    CALLDEF(CHMsuper_validate, 1),

    CALLDEF(CHM_set_common_env, 1),

    CALLDEF(inv_permutation, 3),
    CALLDEF(m_encodeInd,  4),
    CALLDEF(m_encodeInd2, 5),

    CALLDEF(Matrix_rle_i, 2),
    CALLDEF(Matrix_rle_d, 2),

    CALLDEF(R_set_factors, 4),

    CALLDEF(get_SuiteSparse_version, 0),
    {NULL, NULL, 0}
};

static const R_ExternalMethodDef ExtEntries[] = {
    EXTDEF(Mmatrix, 7),
    {NULL, NULL, 0}
};

void
#ifdef HAVE_VISIBILITY_ATTRIBUTE
__attribute__ ((visibility ("default")))
#endif
R_init_Matrix(DllInfo *dll)
{
    R_registerRoutines(dll, NULL, CallEntries, NULL, ExtEntries);
    R_useDynamicSymbols(dll, FALSE);

/* These are callable from other packages' C code: */

#define RREGDEF(name)  R_RegisterCCallable("Matrix", #name, (DL_FUNC) name)

    RREGDEF(Csparse_diagU2N);

    RREGDEF(as_cholmod_dense);
    RREGDEF(as_cholmod_factor);
    RREGDEF(as_cholmod_sparse);
    RREGDEF(as_cholmod_triplet);
    RREGDEF(chm_factor_to_SEXP);
    RREGDEF(chm_factor_ldetL2);
    RREGDEF(chm_factor_update);
    RREGDEF(chm_sparse_to_SEXP);
    RREGDEF(chm_triplet_to_SEXP);

    RREGDEF(cholmod_aat);
    RREGDEF(cholmod_add);
    RREGDEF(cholmod_allocate_dense);
    RREGDEF(cholmod_allocate_sparse);
    RREGDEF(cholmod_allocate_triplet);
    RREGDEF(cholmod_analyze);
    RREGDEF(cholmod_analyze_p);
    RREGDEF(cholmod_band_inplace);
    RREGDEF(cholmod_change_factor);
    RREGDEF(cholmod_copy);
    RREGDEF(cholmod_copy_dense);
    RREGDEF(cholmod_copy_factor);
    RREGDEF(cholmod_copy_sparse);
    RREGDEF(cholmod_dense_to_sparse);
    RREGDEF(cholmod_factor_to_sparse);
    RREGDEF(cholmod_factorize);
    RREGDEF(cholmod_factorize_p);
    RREGDEF(cholmod_finish);
    RREGDEF(cholmod_free_dense);
    RREGDEF(cholmod_free_factor);
    RREGDEF(cholmod_free_sparse);
    RREGDEF(cholmod_free_triplet);
    RREGDEF(cholmod_nnz);
    RREGDEF(cholmod_scale);
    RREGDEF(cholmod_sdmult);
    RREGDEF(cholmod_solve);
    RREGDEF(cholmod_solve2);
    RREGDEF(cholmod_sort);
    RREGDEF(cholmod_sparse_to_dense);
    RREGDEF(cholmod_sparse_to_triplet);
    RREGDEF(cholmod_speye);
    RREGDEF(cholmod_spsolve);
    RREGDEF(cholmod_ssmult);
    RREGDEF(cholmod_start);
    RREGDEF(cholmod_submatrix);
    RREGDEF(cholmod_transpose);
    RREGDEF(cholmod_triplet_to_sparse);
    RREGDEF(cholmod_vertcat);
    RREGDEF(cholmod_updown);

    RREGDEF(dpoMatrix_chol);
    RREGDEF(numeric_as_chm_dense);

    R_cholmod_start(&c);
//    R_cholmod_l_start(&cl);

    Matrix_betaSym = install("beta");
    Matrix_DimNamesSym = install("Dimnames");
    Matrix_DimSym = install("Dim");
    Matrix_diagSym = install("diag");
    Matrix_factorSym = install("factors");
    Matrix_iSym = install("i");
    Matrix_jSym = install("j");
    Matrix_lengthSym = install("length");
    Matrix_pSym = install("p");
    Matrix_permSym = install("perm");
    Matrix_uploSym = install("uplo");
    Matrix_xSym = install("x");
    Matrix_VSym = install("V");

    Matrix_NS = R_FindNamespace(mkString("Matrix"));
    if(Matrix_NS == R_UnboundValue)
	error(_("missing 'Matrix' namespace: should never happen"));

#ifdef DEBUG_Matrix
    if(isEnvironment(Matrix_NS))
	Rprintf("Matrix_NS: %s\n",
		CHAR(asChar(eval(lang2(install("format"),Matrix_NS),
				 R_GlobalEnv))));
    else
#else
    if(!isEnvironment(Matrix_NS))
#endif
	error(_("Matrix namespace not determined correctly"));
}

void R_unload_Matrix(DllInfo *dll)
{
    cholmod_finish(&c);
}
