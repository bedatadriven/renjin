
#include "mylink.h"

SEXP Cmydsum2(SEXP vector) {

    DL_FUNC fun = R_GetCCallable("native", "Cmydsum");

    SEXP res = fun(vector);
    double sum = REAL(res)[0];

    return ScalarReal(sum);
}