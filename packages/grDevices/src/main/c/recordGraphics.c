/*
 *  R : A Computer Language for Statistical Data Analysis
 *  Copyright (C) 2001-2015  The R Core Team.
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
#include <config.h>
#endif

#include <Defn.h>
#include <Internal.h>
#include <R_ext/GraphicsEngine.h>
#include <R_ext/Applic.h>	/* pretty() */
#include <Rmath.h>

# include <rlocale.h>

#define _(String) String

 /* Adapted from src/main/engine.c */



/****************************************************************
 * do_recordGraphics
 *
 * A ".Internal" R function
 *
 ****************************************************************
 */

SEXP attribute_hidden recordGraphics(SEXP code, SEXP list, SEXP parentenv)
{
    SEXP x, xptr, evalenv, retval;
    pGEDevDesc dd = GEcurrentDevice();
    Rboolean record = dd->recordGraphics;
    /*
     * This function can be run under three conditions:
     *
     *   (i) a top-level call to do_recordGraphics.
     *       In this case, call != R_NilValue and
     *       dd->recordGraphics = TRUE
     *       [so GErecording() returns TRUE]
     *
     *   (ii) a nested call to do_recordGraphics.
     *        In this case, call != R_NilValue but
     *        dd->recordGraphics = FALSE
     *        [so GErecording() returns FALSE]
     *
     *   (iii) a replay of the display list
     *         In this case, call == R_NilValue and
     *         dd->recordGraphics = FALSE
     *         [so GErecording() returns FALSE]
     */
    /*
     * First arg is an expression, second arg is a list, third arg is an env
     */

    if (!isLanguage(code))
	error(_("'expr' argument must be an expression"));
    if (TYPEOF(list) != VECSXP)
	error(_("'list' argument must be a list"));
    if (isNull(parentenv)) {
	error(_("use of NULL environment is defunct"));
	parentenv = R_BaseEnv;
    } else
    if (!isEnvironment(parentenv))
	error(_("'env' argument must be an environment"));
    /*
     * This conversion of list to env taken from do_eval
     */
    PROTECT(x = VectorToPairList(list));
    for (xptr = x ; xptr != R_NilValue ; xptr = CDR(xptr))
	SET_NAMED(CAR(xptr) , 2);
    /*
     * The environment passed in as the third arg is used as
     * the parent of the new evaluation environment.
     */
    PROTECT(evalenv = NewEnvironment(R_NilValue, x, parentenv));
    dd->recordGraphics = FALSE;
    PROTECT(retval = eval(code, evalenv));
    /*
     * If there is an error or user-interrupt in the above
     * evaluation, dd->recordGraphics is set to TRUE
     * on all graphics devices (see GEonExit(); called in errors.c)
     */
    dd->recordGraphics = record;

    /* TODO(renjin):
       this was:
       GErecording(call, dd)

    if (dd->recordGraphics) {
        if (!GEcheckState(dd))
            error(_("invalid graphics state"));
        GErecordGraphicOperation(op, args, dd);
        }
        */
    UNPROTECT(3);
    return retval;
}