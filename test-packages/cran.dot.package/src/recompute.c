

#include <R.h>
#include <Rdefines.h>
#include <Rinternals.h>

SEXP recompute(SEXP x) {
    return ScalarInteger(LENGTH(x));
}