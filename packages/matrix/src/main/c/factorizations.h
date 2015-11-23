#ifndef MATRIX_FACTORS_H
#define MATRIX_FACTORS_H

#include "Mutils.h"

SEXP MatrixFactorization_validate(SEXP obj);
SEXP LU_validate(SEXP obj);
SEXP BunchKaufman_validate(SEXP obj);
SEXP pBunchKaufman_validate(SEXP obj);
SEXP Cholesky_validate(SEXP obj);
SEXP pCholesky_validate(SEXP obj);
#ifdef _Matrix_has_SVD_
SEXP SVD_validate(SEXP obj);
#endif
SEXP LU_expand(SEXP x);

#endif
