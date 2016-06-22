


#include "mynative.h"

SEXP _secret(SEXP vector) {
    return ScalarReal(LENGTH(vector) * 42);
}   