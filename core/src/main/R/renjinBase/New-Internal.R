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


gc <- function(verbose = getOption("verbose"),	reset=FALSE) {

    # This is implemented as a NO OP in Renjin.
    # While it is possible to force the JVM to stop and run
    # garbage collection, it is very UNLIKELY to yield any
    # performance benefit because the JVM does its job very well
    # without intervention, and generally in parallel, while calling
    # Runtime.gc() stops the world to run an exhaustive collection.

    res <- matrix(numeric(14), 2L, 7L,
		  dimnames = list(c("Ncells","Vcells"),
		  c("used", "(Mb)", "gc trigger", "(Mb)",
		    "limit (Mb)", "max used", "(Mb)")))
    if(all(is.na(res[, 5L]))) res[, -5L] else res
}

gcinfo <- function(verbose) FALSE

gctorture <- function(on=TRUE) {
    warning("Renjin does not implement the gc torture mode.")
    FALSE
}

gctorture2 <- function(step, wait = step, inhibit_release = FALSE) {
    warning("Renjin does not implement the gc torture mode.")
    FALSE
}

# overwrite l10n_info in version 3.5.3, because it calls the internal l10n_info:
l10n_info <- function() {
    list(MBCS = TRUE, `UTF-8` = TRUE, `Latin-1` = FALSE)
}


# lengths
# Introduced in R-3.2.0, see the release notes at http://cran.r-project.org/src/base/NEWS
# Fixed in R-3.2.1 to work (trivially) on atomic vectors.
lengths <- function(x, use.names = TRUE) {
    if (!isTRUE(use.names)) x <- unname(x)
    sapply(x, length)
}

nchar <- function(x, type = "chars", allowNA = FALSE, keepNA = NA) {
    if (is.factor(x)) stop("'nchar()' requires a character vector")

    res <- .Internal(nchar(x, type, allowNA))

    if ((is.na(keepNA) || isTRUE(keepNA)) && anyNA(x)) {
        res[which(is.na(x))] <- NA_integer_
    }

    res
}
