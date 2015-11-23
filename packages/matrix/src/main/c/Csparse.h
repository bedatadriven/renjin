
#ifndef MATRIX_CSPARSE_H
#define MATRIX_CSPARSE_H

#include "Mutils.h"

Rboolean isValid_Csparse(SEXP x);
SEXP Csp_dense_products(SEXP a, SEXP b,
			Rboolean transp_a, Rboolean transp_b, Rboolean transp_ans);

SEXP Csparse_band(SEXP x, SEXP k1, SEXP k2);
SEXP Csparse_Csparse_prod(SEXP a, SEXP b, SEXP bool_arith);
SEXP Csparse_Csparse_crossprod(SEXP a, SEXP b, SEXP trans, SEXP bool_arith);
SEXP Csparse_crossprod(SEXP x, SEXP trans, SEXP triplet, SEXP bool_arith);
SEXP Csparse_dense_crossprod(SEXP a, SEXP b, SEXP transp);
SEXP Csparse_dense_prod     (SEXP a, SEXP b, SEXP transp);
SEXP Csparse_diagU2N(SEXP x);
SEXP Csparse_diagN2U(SEXP x);
SEXP Csparse_drop(SEXP x, SEXP tol);
SEXP Csparse_horzcat(SEXP x, SEXP y);
SEXP Csparse_submatrix(SEXP x, SEXP i, SEXP j);
SEXP dCsparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value);
SEXP lCsparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value);
SEXP iCsparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value);
SEXP nCsparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value);
SEXP zCsparse_subassign(SEXP x, SEXP i_, SEXP j_, SEXP value);
SEXP Csparse_symmetric_to_general(SEXP x);
SEXP Csparse_general_to_symmetric(SEXP x, SEXP uplo, SEXP sym_dmns);
SEXP Csparse_MatrixMarket(SEXP x, SEXP fname);
SEXP Csparse_sort (SEXP x);
SEXP Csparse_to_Tsparse(SEXP x, SEXP tri);
SEXP Csparse_to_tCsparse(SEXP x, SEXP uplo, SEXP diag);
SEXP Csparse_to_tTsparse(SEXP x, SEXP uplo, SEXP diag);
SEXP Csparse_to_dense(SEXP x, SEXP symm_or_tri);
SEXP Csparse2nz           (SEXP x, Rboolean tri);
SEXP Csparse_to_nz_pattern(SEXP x, SEXP tri);
SEXP nz_pattern_to_Csparse(SEXP x, SEXP res_kind);
SEXP nz2Csparse           (SEXP x, enum x_slot_kind r_kind);
SEXP Csparse_to_matrix(SEXP x, SEXP chk, SEXP symm);
SEXP Csparse_to_vector(SEXP x);
SEXP Csparse_transpose(SEXP x, SEXP tri);
SEXP Csparse_validate (SEXP x);
SEXP Csparse_validate2(SEXP x, SEXP maybe_modify);
SEXP Csparse_validate_(SEXP x, Rboolean maybe_modify);
SEXP Csparse_vertcat(SEXP x, SEXP y);

SEXP Rsparse_validate(SEXP x);

SEXP diag_tC_ptr(int n, int *x_p, double *x_x, Rboolean is_U, int *perm,
		 SEXP resultKind);
SEXP diag_tC(SEXP obj, SEXP resultKind);

// FIXME: these are nowhere used (are they?)
SEXP create_Csparse(char* cls, int* i, int* j, int* p, int np,
		    void* x, int nnz, int* dims, SEXP dimnames,
		    int index1);
#define DG_I_J(i, j, x, nnz) create_Csparse("dgCMatrix", i, j, (int*)NULL, 0, (void*)x, nnz, (int*)NULL, R_NilValue, 1)
#define NG_I_J(i, j, nnz) create_Csparse("ngCMatrix", i, j, (int*)NULL, 0, (void*)NULL, nnz, (int*)NULL, R_NilValue, 1)
#define DG_I_P(i, p, np, x, nnz) create_Csparse("dgCMatrix", i, (int*)NULL, p, np, (void*)x, nnz, (int*)NULL, R_NilValue, 1)
#define NG_I_P(i, p, np, nnz) create_Csparse("ngCMatrix", i, (int*)NULL, p, np, (void*)NULL, nnz, (int*)NULL, R_NilValue, 1)

#endif
