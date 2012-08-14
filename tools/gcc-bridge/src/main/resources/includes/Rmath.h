
#ifndef RMATH_H
#define RMATH_H

/* Note that on some systems we need to include math.h before the
   defines below, to avoid redefining ftrunc */
#ifndef NO_C_HEADERS
# include <math.h>
#endif


#define R_EXTERN extern

R_EXTERN double NA_REAL;


#endif