
#include "mynative.h"

SEXP mysum(SEXP vector) {
    
    int n = LENGTH(vector);
    double *x = REAL(vector);
    double sum = 0;
    int i;
    for(i=0;i<n;++i) {
        sum += x[i];
    }
    return ScalarReal(sum);
}   