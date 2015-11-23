#ifndef MATRIX_TRS_H
#define MATRIX_TRS_H

#include "Mutils.h"

SEXP dsTMatrix_as_dsyMatrix(SEXP x);
SEXP lsTMatrix_as_lsyMatrix(SEXP x);
SEXP nsTMatrix_as_nsyMatrix(SEXP x);

SEXP dtTMatrix_as_dtrMatrix(SEXP x);
SEXP ltTMatrix_as_ltrMatrix(SEXP x);
SEXP ntTMatrix_as_ntrMatrix(SEXP x);

SEXP dsTMatrix_as_dgTMatrix(SEXP x);
SEXP lsTMatrix_as_lgTMatrix(SEXP x);
SEXP nsTMatrix_as_ngTMatrix(SEXP x);

#endif
