
#ifndef MATRIX_TPMATRIX_H
#define MATRIX_TPMATRIX_H

#include <R_ext/Lapack.h>
#include "Mutils.h"

SEXP dtpMatrix_validate(SEXP obj);
SEXP dtpMatrix_norm(SEXP obj, SEXP type);
SEXP dtpMatrix_rcond(SEXP obj, SEXP type);
SEXP dtpMatrix_addDiag(SEXP x, SEXP d);
SEXP dtpMatrix_getDiag(SEXP x);
SEXP ltpMatrix_getDiag(SEXP x);
SEXP dtpMatrix_setDiag(SEXP x, SEXP d);
SEXP ltpMatrix_setDiag(SEXP x, SEXP d);
SEXP dtpMatrix_solve(SEXP a);
SEXP dtpMatrix_matrix_solve(SEXP a, SEXP b);
SEXP dtpMatrix_as_dtrMatrix(SEXP from);
SEXP dgeMatrix_dtpMatrix_mm(SEXP x, SEXP y);
SEXP dtpMatrix_matrix_mm(SEXP x, SEXP y, SEXP right, SEXP trans);

#endif /* MATRIX_TPMATRIX_H */
