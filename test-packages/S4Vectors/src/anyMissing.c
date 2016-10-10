/***************************************************************************
 Public methods:
 anyMissing(SEXP x)

 TO DO: Support list():s too.

 Copyright Henrik Bengtsson, 2007
 **************************************************************************/

/* Include R packages */
#include <Rdefines.h>


SEXP anyMissing(SEXP x) {
  SEXP ans;
  int n, ii;

  PROTECT(ans = allocVector(LGLSXP, 1));
  LOGICAL(ans)[0] = 0;

  n = length(x);

  /* anyMissing() on zero-length objects should always return FALSE,
     just like any(double(0)). */
  if (n == 0) {
    UNPROTECT(1);
    return(ans);
  }

  switch (TYPEOF(x)) {
    case REALSXP:
      for (ii=0; ii < n; ii++) {
        if ISNAN(REAL(x)[ii]) {
          LOGICAL(ans)[0] = 1;
          break; 
        }
      }
      break;

    case INTSXP:
      for (ii=0; ii < n; ii++) {
        if (INTEGER(x)[ii] == NA_INTEGER) {
          LOGICAL(ans)[0] = 1;
          break; 
        }
      }
      break;

    case LGLSXP:
      for (ii=0; ii < n; ii++) {
        if (LOGICAL(x)[ii] == NA_LOGICAL) {
          LOGICAL(ans)[0] = 1;
          break; 
        }
      }
      break;

    case CPLXSXP:
      for (ii=0; ii < n; ii++) {
        if (ISNAN(COMPLEX(x)[ii].r) || ISNAN(COMPLEX(x)[ii].i)) {
          LOGICAL(ans)[0] = 1;
          break; 
        }
      }
      break;

    case STRSXP:
      for (ii=0; ii < n; ii++) {
        if (STRING_ELT(x, ii) == NA_STRING) {
          LOGICAL(ans)[0] = 1;
          break; 
        }
      }
      break; 

    case RAWSXP: 
      /* no such thing as a raw NA */
      break; 
    default:
      break; 
			/*
      warningcall(call, _("%s() applied to non-vector of type '%s'"), 
                  "anyMissing", type2char(TYPEOF(x)));
			*/
  } /* switch() */

  UNPROTECT(1); /* ans */

  return(ans);
} // anyMissing()


/***************************************************************************
 HISTORY:
 2007-08-14 [HB]
  o Created using do_isna() in src/main/coerce.c as a template.
 **************************************************************************/
