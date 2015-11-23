#ifndef CS_UTILS_H
#define CS_UTILS_H

#include "cs.h"
#include "Mutils.h"

typedef cs  *CSP ;

CSP Matrix_as_cs(CSP ans, SEXP x, Rboolean check_Udiag);
SEXP Matrix_cs_to_SEXP(CSP A, char *cl, int dofree, SEXP dn);

#define AS_CSP(x)   Matrix_as_cs((CSP)alloca(sizeof(cs)), x, TRUE)
#define AS_CSP__(x) Matrix_as_cs((CSP)alloca(sizeof(cs)), x, FALSE)

#if 0				/* unused */
css *Matrix_as_css(css *ans, SEXP x);
csn *Matrix_as_csn(csn *ans, SEXP x);
SEXP Matrix_css_to_SEXP(css *S, char *cl, int dofree, int m, int n);
SEXP Matrix_csn_to_SEXP(csn *N, char *cl, int dofree);
#endif

#endif
