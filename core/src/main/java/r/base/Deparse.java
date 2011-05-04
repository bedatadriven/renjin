/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import r.lang.SEXP;

public class Deparse {

  /**
   * {
    opts <- pmatch(as.character(control), c("all", "keepInteger",
        "quoteExpressions", "showAttributes", "useSource", "warnIncomplete",
        "delayPromises", "keepNA", "S_compatible"))
    if (any(is.na(opts)))
        stop(sprintf(ngettext(as.integer(sum(is.na(opts))), "deparse option %s is not recognized",
            "deparse options %s are not recognized"), paste(sQuote(control[is.na(opts)]),
            collapse = ", ")), call. = FALSE, domain = NA)
    if (any(opts == 1L))
        opts <- unique(c(opts[opts != 1L], 2L, 3L, 4L, 5L, 6L,
            8L))

   */


  public static String deparse(SEXP exp, int widthCutoff, boolean backTick, int options, int nlines) {

    // TODO: really implement!
    return exp.toString();

  }

}
