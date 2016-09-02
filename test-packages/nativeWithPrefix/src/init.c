#include <Rinternals.h>
#include <R_ext/Rdynload.h>

SEXP mysum(SEXP vector) {
    double sum;
    int i;
    for(i=0;i<LENGTH(vector);++i) {
        sum += REAL(vector)[i];
    }
    return ScalarReal(sum);
}

static const R_CallMethodDef callMethods[] = {
    {"mysum",     (DL_FUNC) &mysum,  1},
    { NULL, NULL, 0 }
};


R_init_nativeWithPrefix(DllInfo *dll) {
    R_registerRoutines(dll, NULL, callMethods, NULL, NULL);
    R_useDynamicSymbols(dll, FALSE);
}

