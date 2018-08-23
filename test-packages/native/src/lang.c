
#include "mynative.h"


// Tests handling of SET_TYPEOF()
// Adapted from Rsamtools

#define NEW_CALL(S, T, NAME, ENV, N)            \
    PROTECT(S = T = allocList(N));              \
    SET_TYPEOF(T, LANGSXP);                     \
    SETCAR(T, findFun(install(NAME), ENV));     \
    T = CDR(T)
#define CSET_CDR(T, NAME, VALUE)                \
    SETCAR(T, VALUE);                           \
    SET_TAG(T, install(NAME));                  \
    T = CDR(T)
#define CEVAL_TO(S, ENV, GETS)                  \
    GETS = eval(S, ENV);                        \
    UNPROTECT(1)


SEXP Cinvokeupper(SEXP string)
{
    SEXP xstringset, s, t, nmspc, result;
    PROTECT(nmspc = R_BaseEnv);
    NEW_CALL(s, t, "toupper", nmspc, 2);
    CSET_CDR(t, "x", string);
    CEVAL_TO(s, nmspc, result);
    UNPROTECT(2);
    return result;
}