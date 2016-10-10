#include "S4Vectors.h"

static SEXP _top_prenv(SEXP promise, SEXP env) {
  while(TYPEOF(promise) == PROMSXP) {
    env = PRENV(promise);
    promise = PREXPR(promise);
  }
  return env;
}

/*
 * --- .Call ENTRY POINT ---
 * Gets the top environment associated with a (nested) promise.
 */
SEXP top_prenv(SEXP nm, SEXP env)
{
  SEXP promise = findVar(nm, env);
  return _top_prenv(promise, env);
}

/*
 * --- .Call ENTRY POINT ---
 * Gets the top environment associated with each promise in '...'
 */
SEXP top_prenv_dots(SEXP env)
{
  SEXP dots = findVar(R_DotsSymbol, env);
  SEXP ans = allocVector(VECSXP, length(dots));
  if (TYPEOF(dots) == DOTSXP) {
    int i = 0;
    for (SEXP p = dots; p != R_NilValue; p = CDR(p)) {
      SET_VECTOR_ELT(ans, i++, _top_prenv(CAR(p), env));
    }
  }
  return ans;
}
