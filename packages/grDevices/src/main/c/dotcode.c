/*
 *  R : A Computer Language for Statistical Data Analysis
 *  Copyright (C) 1995  Robert Gentleman and Ross Ihaka
 *  Copyright (C) 1997--2014  The R Core Team
 *  Copyright (C) 2003	      The R Foundation
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

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif

#define R_USE_SIGNALS 1
#include <Defn.h>
#include <Internal.h>
#include <ctype.h> /* for tolower */
#include <string.h>
#include <errno.h>

#include <Rmath.h>


#ifndef max
#define max(a, b) ((a > b)?(a):(b))
#endif

#include <R_ext/GraphicsEngine.h>

SEXP attribute_hidden do_Externalgr(SEXP call, SEXP op, SEXP args, SEXP env)
{
    // Since we are being invoked by .External2, skip the first argument
    // which is our function name
    args = CDR(args);

    SEXP retval;
    pGEDevDesc dd = GEcurrentDevice();
    Rboolean record = dd->recordGraphics;
    dd->recordGraphics = FALSE;
    PROTECT(retval = do_External(call, op, args, env));
    dd->recordGraphics = record;
    if (GErecording(call, dd)) { // which is record && call != R_NilValue
	if (!GEcheckState(dd))
	    errorcall(call, _("invalid graphics state"));
	GErecordGraphicOperation(op, args, dd);
    }
    UNPROTECT(1);
    return retval;
}


SEXP attribute_hidden do_dotcallgr(SEXP call, SEXP op, SEXP args, SEXP env)
{
    // Since we are being invoked by .Call, skip the first argument
    // which is our function name
    args = CDR(args);

    SEXP retval;
    pGEDevDesc dd = GEcurrentDevice();
    Rboolean record = dd->recordGraphics;
    dd->recordGraphics = FALSE;
    PROTECT(retval = do_dotcall(call, op, args, env));
    dd->recordGraphics = record;
    if (GErecording(call, dd)) {
	if (!GEcheckState(dd))
	    errorcall(call, _("invalid graphics state"));
	GErecordGraphicOperation(op, args, dd);
    }
    UNPROTECT(1);
    return retval;
}