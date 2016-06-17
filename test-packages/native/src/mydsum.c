
#include "mynative.h"

SEXP Cmydsum(SEXP vector) {

    DL_FUNC fun = R_GetCCallable("native", "Cmysum");

    SEXP res = fun(vector);
    double sum = REAL(res)[0];

    return ScalarReal(sum);
}