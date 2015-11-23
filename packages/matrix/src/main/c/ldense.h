#ifndef MATRIX_LDENSE_H
#define MATRIX_LDENSE_H

#include "Mutils.h"

SEXP lspMatrix_as_lsyMatrix(SEXP from, SEXP kind);
SEXP lsyMatrix_as_lspMatrix(SEXP from, SEXP kind);
SEXP lsyMatrix_as_lgeMatrix(SEXP from, SEXP kind);
SEXP ltpMatrix_as_ltrMatrix(SEXP from, SEXP kind);
SEXP ltrMatrix_as_ltpMatrix(SEXP from, SEXP kind);
SEXP ltrMatrix_as_lgeMatrix(SEXP from, SEXP kind);

#endif


