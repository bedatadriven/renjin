
#include <R.h>
#include <Rinternals.h>


SEXP myFunction(SEXP vector) {
    return ScalarReal(LENGTH(vector) * 42);
}   
