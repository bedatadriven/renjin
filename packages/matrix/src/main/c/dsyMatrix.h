#ifndef MATRIX_SYMATRIX_H
#define MATRIX_SYMATRIX_H

#include "Mutils.h"
#include "R_ext/Lapack.h"

SEXP dsyMatrix_as_dspMatrix(SEXP from);
SEXP dsyMatrix_as_matrix(SEXP from, SEXP keep_dimnames);
SEXP dsyMatrix_matrix_mm(SEXP a, SEXP b, SEXP rt);
SEXP dsyMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dsyMatrix_norm(SEXP obj, SEXP type);
SEXP dsyMatrix_rcond(SEXP obj, SEXP type);
SEXP dsyMatrix_solve(SEXP a);
SEXP dsyMatrix_trf(SEXP x);
double get_norm_sy(SEXP obj, const char *typstr);

#endif
