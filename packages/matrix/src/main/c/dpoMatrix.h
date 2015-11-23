#ifndef MATRIX_POMATRIX_H
#define MATRIX_POMATRIX_H

#include <R_ext/Lapack.h>
#include "Mutils.h"

SEXP dpoMatrix_rcond(SEXP obj, SEXP type);
SEXP dpoMatrix_validate(SEXP obj);
SEXP dpoMatrix_solve(SEXP a);
SEXP dpoMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dpoMatrix_dgeMatrix_solve(SEXP a, SEXP b);
SEXP dpoMatrix_chol(SEXP x);
double get_norm_sy(SEXP obj, const char *typstr);

#endif
