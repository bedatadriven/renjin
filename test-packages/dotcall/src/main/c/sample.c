


#include "proto.h"

SEXP mysample(SEXP countSexp) 
{
    
    int count = *INTEGER(countSexp);
    
    SEXP result = allocVector(REALSXP, count);

    double *p = REAL(result);
    
    GetRNGstate();

    int i;
    for(i=0;i<count;++i) {
        p[i] = ceil(100.0 * unif_rand());
    }
    
    PutRNGstate();
    
    return result;
}
