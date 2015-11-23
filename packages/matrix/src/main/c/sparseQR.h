#ifndef MATRIX_SPARSEQR_H
#define MATRIX_SPARSEQR_H

#include "Mutils.h"
#include "cs_utils.h"
#include "chm_common.h"

SEXP sparseQR_validate(SEXP x);
SEXP sparseQR_qty(SEXP qr, SEXP y, SEXP trans, SEXP keep_dimnames);
SEXP sparseQR_coef(SEXP qr, SEXP y);
SEXP sparseQR_resid_fitted(SEXP qr, SEXP y, SEXP resid);

#endif
