#include "R.h"
#include <Rdefines.h>


void F77_NAME(hygfx)(double *a, double *b, double *c, double* z, double *resr);

SEXP hyp2f1zhangjin(SEXP a, SEXP b, SEXP c, SEXP x)
{
    int i,n;
    double *xa, *xb, *xc, *xx, *xresr;
    SEXP resr;

    n = LENGTH(a);
    PROTECT(a = AS_NUMERIC(a));
    PROTECT(b = AS_NUMERIC(b));
    PROTECT(c = AS_NUMERIC(c));
    PROTECT(x = AS_NUMERIC(x));
    PROTECT(resr = NEW_NUMERIC(n));
    xa = NUMERIC_POINTER(a);
    xb = NUMERIC_POINTER(b);
    xc = NUMERIC_POINTER(c);
    xx = NUMERIC_POINTER(x);
    xresr = NUMERIC_POINTER(resr);

    for (i = 0; i < n; i++)
    {
        F77_CALL(hygfx)(&xa[i], &xb[i], &xc[i], &xx[i], &xresr[i]);
        if(n % 1000 == 0) R_CheckUserInterrupt();
	}
    UNPROTECT(5);
    return(resr);
}


