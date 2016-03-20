

      
#include <R.h>
#include <Rinternals.h>
#include <R_ext/Random.h>

static int count = 0;

SEXP counter() {
    return ScalarInteger(count++);
}

SEXP mySample() {
    return ScalarReal(unif_rand());
}