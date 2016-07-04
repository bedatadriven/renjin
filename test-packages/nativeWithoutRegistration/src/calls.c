
#include <R.h>
#include <Rinternals.h>


SEXP _dotCall(SEXP vector) {
    return ScalarReal(LENGTH(vector) * 42);
}   

void _dotC(int *x) {
    x[0] = 3333;
}