/*
 *  R : A Computer Language for Statistical Data Analysis
 *  Copyright (C) 1995, 1996, 1997  Robert Gentleman and Ross Ihaka
 *  Copyright (C) 2000-2016	    The R Core Team
 *  Copyright (C) 2005		    The R Foundation
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.R-project.org/Licenses/
 */

/* ===========================
 * Abridged for Renjin
 * ===========================
 *
 * A few functions are included here that are required by base
 * package or CRAN packages
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

/* Note: gcc -peantic may warn in several places about C99 features
   as extensions.
   This was a very-long-standing GCC bug, http://gcc.gnu.org/PR7263
   The system <complex.h> header can work around it: some do.
   It should have been resolved (after a decade) in 2012.
*/

#if defined(HAVE_CTANH) && !defined(HAVE_WORKING_CTANH)
#undef HAVE_CTANH
#endif

#if 0
/* For testing substitute fns */
#undef HAVE_CARG
#undef HAVE_CABS
#undef HAVE_CPOW
#undef HAVE_CEXP
#undef HAVE_CLOG
#undef HAVE_CSQRT
#undef HAVE_CSIN
#undef HAVE_CCOS
#undef HAVE_CTAN
#undef HAVE_CASIN
#undef HAVE_CACOS
#undef HAVE_CATAN
#undef HAVE_CSINH
#undef HAVE_CCOSH
#undef HAVE_CTANH
#endif

#ifdef __SUNPRO_C
/* segfaults in Solaris Studio 12.3 */
#undef HAVE_CPOW
#endif

#include <Defn.h>		/* -> ../include/R_ext/Complex.h */
#include <Internal.h>
#include <Rmath.h>

#include "arithmetic.h"		/* complex_*  */
#include <complex.h>
#include "Rcomplex.h"		/* I, SET_C99_COMPLEX, toC99 */
#include <R_ext/Itermacros.h>


/* interval at which to check interrupts, a guess */
#define NINTERRUPT 10000000



/* used in format.c and printutils.c */
#define MAX_DIGITS 22
void attribute_hidden z_prec_r(Rcomplex *r, const Rcomplex *x, double digits)
{
    double m = 0.0, m1, m2;
    int dig, mag;

    r->r = x->r; r->i = x->i;
    m1 = fabs(x->r); m2 = fabs(x->i);
    if(R_FINITE(m1)) m = m1;
    if(R_FINITE(m2) && m2 > m) m = m2;
    if (m == 0.0) return;
    if (!R_FINITE(digits)) {
	if(digits > 0) return; else {r->r = r->i = 0.0; return ;}
    }
    dig = (int)floor(digits+0.5);
    if (dig > MAX_DIGITS) return; else if (dig < 1) dig = 1;
    mag = (int)floor(log10(m));
    dig = dig - mag - 1;
    if (dig > 306) {
	double pow10 = 1.0e4;
	digits = (double)(dig - 4);
	r->r = fround(pow10 * x->r, digits)/pow10;
	r->i = fround(pow10 * x->i, digits)/pow10;
    } else {
	digits = (double)(dig);
	r->r = fround(x->r, digits);
	r->i = fround(x->i, digits);
    }
}
