#ifndef MATRIX_TSPARSE_H
#define MATRIX_TSPARSE_H

#include "Mutils.h"

SEXP Tsparse_validate(SEXP x);
SEXP Tsparse_diagU2N(SEXP x);
SEXP Tsparse_to_Csparse(SEXP x, SEXP tri);
SEXP Tsparse_to_tCsparse(SEXP x, SEXP uplo, SEXP diag);

#endif
