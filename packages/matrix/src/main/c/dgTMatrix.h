#ifndef MATRIX_TRIPLET_H
#define MATRIX_TRIPLET_H

#include "Mutils.h"

SEXP xTMatrix_validate(SEXP x);
SEXP dgTMatrix_to_dgeMatrix(SEXP x);
SEXP lgTMatrix_to_lgeMatrix(SEXP x);
SEXP dgTMatrix_to_matrix(SEXP x);
SEXP lgTMatrix_to_matrix(SEXP x);


#endif
