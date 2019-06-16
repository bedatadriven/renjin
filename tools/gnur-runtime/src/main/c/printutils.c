/*
 *  R : A Computer Language for Statistical Data Analysis
 *  Copyright (C) 1999--2017  The R Core Team
 *  Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
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


/* =========
 * Printing:
 * =========
 *
 * All printing in R is done via the functions Rprintf and REprintf
 * or their (v) versions Rvprintf and REvprintf.
 * These routines work exactly like (v)printf(3).  Rprintf writes to
 * ``standard output''.	 It is redirected by the sink() function,
 * and is suitable for ordinary output.	 REprintf writes to
 * ``standard error'' and is useful for error messages and warnings.
 * It is not redirected by sink().
 *
 *  See ./format.c  for the  format_FOO_  functions which provide
 *	~~~~~~~~~~  the	 length, width, etc.. that are used here.
 *  See ./print.c  for do_printdefault, do_prmatrix, etc.
 *
 *
 * Here, the following UTILITIES are provided:
 *
 * The utilities EncodeLogical, EncodeInteger, EncodeReal
 * and EncodeString can be used to convert R objects to a form suitable
 * for printing.  These print the values passed in a formatted form
 * or, in the case of NA values, an NA indicator.  EncodeString takes
 * care of printing all the standard ANSI escapes \a, \t \n etc.
 * so that these appear in their backslash form in the string.	There
 * is also a routine called Rstrlen which computes the length of the
 * string in its escaped rather than literal form.
 *
 * Finally there is a routine called EncodeElement which will encode
 * a single R-vector element.  This is used in deparse and write.table.
 */

/* if ESC_BARE_QUOTE is defined, " in an unquoted string is replaced
   by \".  " in a quoted string is always replaced by \". */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <Defn.h>
#include <Rmath.h>
#include <Print.h>
#include <R_ext/RS.h>
#include <Rconnections.h>

#include "RBufferUtils.h"


#if !defined(__STDC_ISO_10646__) && (defined(__APPLE__) || defined(__FreeBSD__))
/* This may not be 100% true (see the comment in rlocales.h),
   but it seems true in normal locales */
# define __STDC_ISO_10646__
#endif

#ifdef Win32
#include <trioremap.h>
#endif
#ifndef min
#define min(a, b) (((a)<(b))?(a):(b))
#endif



int attribute_hidden IndexWidth(R_xlen_t n)
{
    return (int) (log10(n + 0.5) + 1);
}

void attribute_hidden VectorIndex(R_xlen_t i, int w)
{
/* print index label "[`i']" , using total width `w' (left filling blanks) */
    Rprintf("%*s[%ld]", w-IndexWidth(i)-2, "", i);
}
