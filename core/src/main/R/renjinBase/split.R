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

split.default <- function(x, f, drop = FALSE, sep = ".", lex.order = FALSE, ...)
{
    if(!missing(...)) .NotYetUsed(deparse(...), error = FALSE)

    .split <- function(x, f) {
        if (length(f) > length(x)) f <- f[seq_along(x)]
        ff <- levels(f)[f]
        if (length(ff) < length(x)) ff <- rep_len(ff, length(x))
        sapply(levels(f), function(level) x[sapply(ff, identical, level)], simplify = FALSE)
    }

    if (is.list(f))
	f <- interaction(f, drop = drop, sep = sep, lex.order = lex.order)
    else if (!is.factor(f)) f <- as.factor(f) # docs say as.factor
    else if (drop) f <- factor(f) # drop extraneous levels
    storage.mode(f) <- "integer"  # some factors have had double in the past
    if (is.null(attr(x, "class")))
	return(.split(x, f))
    ## else
    lf <- levels(f)
    y <- vector("list", length(lf))
    names(y) <- lf
    ind <- .split(seq_along(x), f)
    for(k in lf) y[[k]] <- x[ind[[k]]]
    y
}

