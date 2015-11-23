#ifndef MATRIX_SPMATRIX_H
#define MATRIX_SPMATRIX_H

#include "dgeMatrix.h"
#include "R_ext/Lapack.h"

SEXP dspMatrix_validate(SEXP obj);
double get_norm_sp(SEXP obj, const char *typstr);
SEXP dspMatrix_norm(SEXP obj, SEXP type);
SEXP dspMatrix_rcond(SEXP obj, SEXP type);
SEXP dspMatrix_solve(SEXP a);
SEXP dspMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dspMatrix_getDiag(SEXP x);
SEXP lspMatrix_getDiag(SEXP x);
SEXP dspMatrix_setDiag(SEXP x, SEXP d);
SEXP lspMatrix_setDiag(SEXP x, SEXP d);
SEXP dspMatrix_as_dsyMatrix(SEXP from);
SEXP dspMatrix_matrix_mm(SEXP a, SEXP b);
SEXP dspMatrix_trf(SEXP x);

#endif
