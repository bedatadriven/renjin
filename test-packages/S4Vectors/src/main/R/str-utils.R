#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

### =========================================================================
### Some utility functions to operate on strings
### -------------------------------------------------------------------------


### NOT exported
capitalize <- function(x)
{
    substring(x, 1L, 1L) <- toupper(substring(x, 1L, 1L))
    x
}

### NOT exported
### Reduce size of each input string by keeping only its head and tail
### separated by 3 dots. Each returned strings is guaranteed to have a number
### characters <= width.
sketchStr <- function(x, width=23) 
{
    if (!is.character(x))
        stop("'x' must be a character vector")
    if (!isSingleNumber(width))
        stop("'width' must be a single integer")
    if (!is.integer(width))
        width <- as.integer(width)
    if (width < 7L) 
        width <- 7L
    x_nchar <- nchar(x, type="width")
    idx <- which(x_nchar > width)
    if (length(idx) != 0L) {
        xx <- x[idx]
        xx_nchar <- x_nchar[idx]
        w1 <- (width - 2L) %/% 2L
        w2 <- (width - 3L) %/% 2L
        x[idx] <- paste0(substr(xx, start=1L, stop=w1),
                         "...",
                         substr(xx, start=xx_nchar - w2 + 1L, stop=xx_nchar))
    }
    x
}

setGeneric("unstrsplit", signature="x",
    function(x, sep="") standardGeneric("unstrsplit")
)

setMethod("unstrsplit", "list",
    function(x, sep="") .Call2("unstrsplit_list", x, sep, PACKAGE="S4Vectors")
)

setMethod("unstrsplit", "character",
    function(x, sep="") x
)

### Safe alternative to 'strsplit(x, NULL, fixed=TRUE)[[1L]]'.
safeExplode <- function(x)
{
    if (!isSingleString(x))
        stop("'x' must be a single string")
    .Call2("safe_strexplode", x, PACKAGE="S4Vectors")
}

### strsplitAsListOfIntegerVectors(x) is an alternative to:
###   lapply(strsplit(x, ",", fixed=TRUE), as.integer)
### except that:
###  - strsplit() accepts NAs, we don't (raise an error);
###  - as.integer() introduces NAs by coercion (with a warning), we don't
###    (raise an error);
###  - as.integer() supports "inaccurate integer conversion in coercion"
###    when the value to coerce is > INT_MAX (then it's coerced to INT_MAX),
###    we don't (raise an error);
###  - as.integer() will coerce non-integer values (e.g. 10.3) to an int
###    by truncating them, we don't (raise an error).
### When it fails, strsplit_as_list_of_ints() will print a detailed parse
### error message.
### It's also faster and uses much less memory. E.g. it's 8x faster and uses
### < 1 Mb versus > 60 Mb on the character vector 'biginput' created with:
###   library(rtracklayer)
###   session <- browserSession()
###   genome(session) <- "hg18"
###   query <- ucscTableQuery(session, "UCSC Genes")
###   tx <- getTable(query)
###   biginput <- c(tx$exonStarts, tx$exonEnds)  # 133606 elements
strsplitAsListOfIntegerVectors <- function(x, sep=",")
{
    if (!is.character(x))
        stop("'x' must be a character vector")
    if (!isSingleString(sep) || nchar(sep) != 1L)
        stop("'sep' must be a string containing just one single-byte character")
    ans <- .Call2("strsplit_as_list_of_ints", x, sep, PACKAGE="S4Vectors")
    names(ans) <- names(x)
    ans
}

### svn.time() returns the time in Subversion format, e.g.:
###   "2007-12-07 10:03:15 -0800 (Fri, 07 Dec 2007)"
### The -0800 part will be adjusted if daylight saving time is in effect.
### TODO: Find a better home for this function.
svn.time <- function() .Call2("svn_time", PACKAGE="S4Vectors")

