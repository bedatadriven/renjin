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


startsWith <- function(x, prefix)
{
    if(!is.character(x) || !is.character(prefix)) {
        stop("non-character object(s)")
    }

    if( length(x) == 0 || length(prefix) == 0) {
        return(logical(0))
    }

    substr(x, 1, nchar(prefix)) == prefix
}

endsWith <- function(x, suffix)
{
    if(!is.character(x) || !is.character(suffix)) {
        stop("non-character object(s)")
    }

    if( length(x) == 0 || length(suffix) == 0) {
        return(logical(0))
    }

    substr(x, nchar(x)-nchar(suffix)+1, nchar(x)) == suffix
}

strrep <-
function(x, times)
{
    if(!is.character(x)) x <- as.character(x)
    times <- as.integer(times)

    mapply(x, times, SIMPLIFY = TRUE, USE.NAMES = FALSE, FUN = function(x, times) {
        if(times < 0) {
            error("invalid 'times' value")
        }
        paste(rep.int(x, times), collapse = "")
    })
}
