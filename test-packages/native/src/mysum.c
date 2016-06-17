
#include "mynative.h"

SEXP Cmysum(SEXP vector) {
    
    int n = LENGTH(vector);
    double *x = REAL(vector);
    double sum = 1;
    int i;
    for(i=0;i<n;++i) {
        sum += x[i];
    }

    return ScalarReal(sum);
}