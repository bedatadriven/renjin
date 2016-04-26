
// Headers from "native" test package
#include "mynative.h"


SEXP mydoublesum(SEXP x) {
    
    SEXP sumSexp = mysum(x);
    double sum = REAL(sumSexp)[0];
    
    return ScalarReal(sum * 2.0);
}