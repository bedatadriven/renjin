
#ifndef R_R_H
#define R_R_H

#ifndef USING_R
# define USING_R
#endif

#ifndef NO_C_HEADERS
#include <stdlib.h>
#include <stdio.h>  /* Used by several packages, remove in due course */
#include <limits.h> /* for INT_MAX */
#include <math.h>
#endif


#include <Rconfig.h>
#include <R_ext/Arith.h>      /* R_FINITE, ISNAN, ... */
#include <R_ext/Boolean.h>    /* Rboolean type */
//#include <R_ext/Complex.h>    /* Rcomplex type */
//#include <R_ext/Constants.h>  /* PI, DOUBLE_EPS, etc */
//#include <R_ext/Error.h>      /* error and warning */
//#include <R_ext/Memory.h>     /* R_alloc and S_alloc */
//#include <R_ext/Print.h>      /* Rprintf etc */
//#include <R_ext/Random.h>     /* RNG interface */
//#include <R_ext/Utils.h>      /* sort routines et al */
#include <R_ext/RS.h>
/* for PROBLEM ... Calloc, Realloc, Free, Memcpy, F77_xxxx */

typedef double Sfloat;
typedef int Sint;
#define SINT_MAX INT_MAX
#define SINT_MIN INT_MIN



#endif