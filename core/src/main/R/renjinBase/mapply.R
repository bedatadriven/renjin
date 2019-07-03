#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#


mapply <- function(FUN,..., MoreArgs = NULL, SIMPLIFY = TRUE, USE.NAMES = TRUE)
{
    FUN <- match.fun(FUN)
    dots <- list(...)

    # Renjin's implementation of the 'mapply' primitive requires the 'environment()' argument:
    answer <- .Internal(mapply(FUN, dots, MoreArgs, environment()))

    if (USE.NAMES && length(dots)) {
	if (is.null(names1 <- names(dots[[1L]])) && is.character(dots[[1L]]))
	    names(answer) <- dots[[1L]]
	else if (!is.null(names1))
	    names(answer) <- names1
    }
    if(!isFALSE(SIMPLIFY) && length(answer))
	simplify2array(answer, higher = (SIMPLIFY == "array"))
    else answer
}

