#ifndef MATRIX_TRMATRIX_H
#define MATRIX_TRMATRIX_H

#include <R_ext/Lapack.h>
#include "Mutils.h"

SEXP dtrMatrix_norm(SEXP obj, SEXP type);
SEXP dtrMatrix_rcond(SEXP obj, SEXP type);
SEXP dtrMatrix_solve(SEXP a);
SEXP dtrMatrix_chol2inv(SEXP a);
SEXP dtrMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dtrMatrix_matrix_mm   (SEXP a, SEXP b, SEXP right, SEXP trans);
SEXP dtrMatrix_dtrMatrix_mm(SEXP a, SEXP b, SEXP right, SEXP trans);
SEXP dtrMatrix_as_dgeMatrix(SEXP from);
SEXP dtrMatrix_as_matrix(SEXP from, SEXP keep_dimnames);
SEXP dtrMatrix_as_dtpMatrix(SEXP from);
SEXP dtrMatrix_addDiag(SEXP x, SEXP d);
SEXP dtrMatrix_getDiag(SEXP x);
SEXP ltrMatrix_getDiag(SEXP x);
SEXP dtrMatrix_setDiag(SEXP x, SEXP d);
SEXP ltrMatrix_setDiag(SEXP x, SEXP d);

#endif
