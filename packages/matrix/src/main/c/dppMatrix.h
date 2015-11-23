#ifndef MATRIX_PPMATRIX_H
#define MATRIX_PPMATRIX_H

#include <R_ext/Lapack.h>
#include "Mutils.h"
#include "dspMatrix.h"

SEXP dppMatrix_rcond(SEXP obj, SEXP type);
SEXP dppMatrix_validate(SEXP obj);
SEXP dppMatrix_solve(SEXP a);
SEXP dppMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dppMatrix_chol(SEXP x);
double get_norm_sp(SEXP obj, const char *typstr);

#endif
