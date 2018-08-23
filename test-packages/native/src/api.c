
#include <Rinternals.h>
#include <string.h>

SEXP call_asChar(SEXP p) {
  return ScalarString(asChar(p));
}

SEXP call_copyMatrix(SEXP m) {
  SEXP copy = allocMatrix(TYPEOF(m), nrows(m), ncols(m));
  
  copyMatrix(copy, m, FALSE);
  
  return copy;
}

SEXP test_CHAR_NULL() {
  const char * na = CHAR(NA_STRING);
  Rprintf("na = %s\n", na);
  Rprintf("na = %p\n", na);
  Rprintf("NA_STRING = %p\n", NA_STRING);
  
  return ScalarInteger(strncmp(na, "foo", 3));
}