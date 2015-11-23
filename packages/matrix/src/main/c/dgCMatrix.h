#ifndef MATRIX_DGCMATRIX_H
#define MATRIX_DGCMATRIX_H

#include <R_ext/BLAS.h>
#include "Mutils.h"
#include "cs_utils.h"

SEXP xCMatrix_validate(SEXP x);
SEXP xRMatrix_validate(SEXP x);
SEXP compressed_to_TMatrix(SEXP x, SEXP colP);
SEXP compressed_non_0_ij(SEXP x, SEXP colP);
SEXP R_to_CMatrix(SEXP x);
SEXP dgCMatrix_colSums(SEXP x, SEXP NArm, SEXP spRes, SEXP trans, SEXP means);
SEXP igCMatrix_colSums(SEXP x, SEXP NArm, SEXP spRes, SEXP trans, SEXP means);
SEXP lgCMatrix_colSums(SEXP x, SEXP NArm, SEXP spRes, SEXP trans, SEXP means);
SEXP ngCMatrix_colSums(SEXP x, SEXP NArm, SEXP spRes, SEXP trans, SEXP means);

/* SEXP dgCMatrix_lusol(SEXP x, SEXP y); */
SEXP dgCMatrix_qrsol(SEXP x, SEXP y, SEXP ord);
SEXP dgCMatrix_cholsol(SEXP x, SEXP y);
SEXP dgCMatrix_QR(SEXP Ap, SEXP order, SEXP keep_dimnames);
#ifdef Matrix_with_SPQR
SEXP dgCMatrix_SPQR(SEXP Ap, SEXP ordering, SEXP econ, SEXP tol);
#endif
SEXP dgCMatrix_LU(SEXP Ap, SEXP orderp, SEXP tolp, SEXP error_on_sing, SEXP keep_dimnames);
SEXP dgCMatrix_matrix_solve(SEXP Ap, SEXP bp, SEXP give_sparse);

#endif
