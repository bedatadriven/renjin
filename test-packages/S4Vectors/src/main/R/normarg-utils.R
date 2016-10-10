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
### Utility functions for checking/fixing user-supplied arguments
### -------------------------------------------------------------------------


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### For checking only.
###

isTRUEorFALSE <- function(x)
{
    is.logical(x) && length(x) == 1L && !is.na(x)
}

isSingleInteger <- function(x)
{
    is.integer(x) && length(x) == 1L && !is.na(x)
}

isSingleNumber <- function(x)
{
    is.numeric(x) && length(x) == 1L && !is.na(x)
}

isSingleString <- function(x)
{
    is.character(x) && length(x) == 1L && !is.na(x)
}

### We want these functions to return TRUE when passed an NA of whatever type.
isSingleNumberOrNA <- function(x)
{
    is.atomic(x) && length(x) == 1L && (is.numeric(x) || is.na(x))
}

isSingleStringOrNA <- function(x)
{
    is.atomic(x) && length(x) == 1L && (is.character(x) || is.na(x))
}

### NOT exported.
anyMissing <- function(x) .Call2("anyMissing", x, PACKAGE="S4Vectors")

### NOT exported.
isNumericOrNAs <- function(x)
{
    is.numeric(x) || (is.atomic(x) && is.vector(x) && all(is.na(x)))
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Vertical/horiontal recycling of a vector-like/list-like object.
###

### Vertical recycling (of any vector-like object).
### NOT exported.
V_recycle <- function(x, skeleton, x_what="x", skeleton_what="skeleton")
{
    x_NROW <- NROW(x)
    skeleton_len <- length(skeleton)
    if (x_NROW == skeleton_len)
        return(x)
    if (x_NROW > skeleton_len && x_NROW != 1L)
        stop(wmsg(
            "'NROW(", x_what, ")' is greater than ",
            "'length(", skeleton_what, ")'"
        ))
    if (x_NROW == 0L)
        stop(wmsg(
            "'NROW(", x_what, ")' is 0 but ",
            "'length(", skeleton_what, ")' is not"
        ))
    if (skeleton_len %% x_NROW != 0L)
        warning(wmsg(
            "'length(", skeleton_what, ")' is not a multiple of ",
            "'NROW(", x_what, ")'"
        ))
    idx <- rep(seq_len(x_NROW), length.out=skeleton_len)
    extractROWS(x, idx)
}

### Horizontal recycling (of a list-like object only).
### NOT exported.
H_recycle <- function(x, skeleton, x_what="x", skeleton_what="skeleton",
                      more_blahblah=NA)
{
    stopifnot(is.list(x) || is(x, "List"))
    stopifnot(is.list(skeleton) || is(skeleton, "List"))
    x_len <- length(x)
    skeleton_len <- length(skeleton)
    stopifnot(x_len == skeleton_len)

    x_what2 <- paste0("some list elements in '", x_what, "'")
    if (!is.na(more_blahblah))
        x_what2 <- paste0(x_what2, " (", more_blahblah, ")")

    x_eltNROWS <- unname(elementNROWS(x))
    skeleton_eltNROWS <- unname(elementNROWS(skeleton))
    idx <- which(x_eltNROWS != skeleton_eltNROWS)
    if (length(idx) == 0L)
        return(x)

    longer_idx <- which(x_eltNROWS > skeleton_eltNROWS)
    shorter_idx <- which(x_eltNROWS < skeleton_eltNROWS)
    if (length(longer_idx) == 0L && length(shorter_idx) == 0L)
        return(x)
    if (length(longer_idx) != 0L) {
        if (max(x_eltNROWS[longer_idx]) >= 2L)
            stop(wmsg(
                x_what2, " are longer than their corresponding ",
                "list element in '", skeleton_what, "'"
            ))
    }
    if (length(shorter_idx) != 0L) {
        tmp <- x_eltNROWS[shorter_idx]
        if (min(tmp) == 0L)
            stop(wmsg(
                x_what2, " are of length 0, but their corresponding ",
                "list element in '", skeleton_what, "' is not"
            ))
        if (max(tmp) >= 2L)
            stop(wmsg(
                x_what2, " are shorter than their corresponding ",
                "list element in '", skeleton_what, "', but have ",
                "a length >= 2. \"Horizontal\" recycling only supports ",
                "list elements of length 1 at the moment."
            ))
    }

    ## From here 'x[idx]' is guaranteed to contain list elements of length 1.

    ## We use an "unlist => stretch => relist" algo to perform the horizontal
    ## recycling. Because of this, the returned value is not necessary of the
    ## same class as 'x' (e.g. can be an IntegerList if 'x' is an ordinary
    ## list of integers and 'skeleton' a List object).
    unlisted_x <- unlist(x, use.names=FALSE)
    times <- rep.int(1L, length(unlisted_x))
    idx2 <- cumsum(x_eltNROWS)[idx]
    times[idx2] <- skeleton_eltNROWS[idx]
    unlisted_ans <- rep.int(unlisted_x, times)
    ans <- relist(unlisted_ans, skeleton)
    names(ans) <- names(x)
    ans
}

### Performs first vertical then horizontal recycling (of a list-like object
### only).
### NOT exported.
VH_recycle <- function(x, skeleton, x_what="x", skeleton_what="skeleton",
                       more_blahblah=NA)
{
    x <- V_recycle(x, skeleton, x_what=x_what, skeleton_what=skeleton_what)
    H_recycle(x, skeleton, x_what=x_what, skeleton_what=skeleton_what,
                           more_blahblah=more_blahblah)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### More recycling of a vector-like object.
###
### TODO: This section needs to be cleaned. Some of the stuff in it is
### redundant with and superseded by V_recycle() and/or H_recycle() (defined
### in the previous section).
###

### NOT exported.
### recycleVector() vs rep(x, length.out=length):
###   - The former seems a little bit faster (1.5x - 2x).
###   - The former will issue a warning that "number of items to replace is not
###     a multiple of replacement length". The latter will always remain silent.
recycleVector <- function(x, length.out)
{
    if (length(x) == length.out) {
        x
    } else {
        ans <- vector(storage.mode(x), length.out)
        ans[] <- x
        ans
    }
}

### Must always drop the names of 'arg'.
recycleArg <- function(arg, argname, length.out)
{
    if (length.out == 0L) {
        if (length(arg) > 1L)
            stop("invalid length for '", argname, "'")
        if (length(arg) == 1L && is.na(arg))
            stop("'", argname, "' contains NAs")
        return(recycleVector(arg, length.out))  # drops the names
    }
    if (length(arg) == 0L)
        stop("'", argname, "' has no elements")
    if (length(arg) > length.out)
        stop("'", argname, "' is longer than 'x'")
    if (anyMissing(arg))
        stop("'", argname, "' contains NAs")
    if (length(arg) < length.out)
        arg <- recycleVector(arg, length.out)  # drops the names
    else
        arg <- unname(arg)
    arg
}

recycleIntegerArg <- function(arg, argname, length.out)
{
    if (!is.numeric(arg))
        stop("'", argname, "' must be a vector of integers")
    if (!is.integer(arg))
        arg <- as.integer(arg)
    recycleArg(arg, argname, length.out)
}

recycleNumericArg <- function(arg, argname, length.out)
{
    if (!is.numeric(arg))
        stop("'", argname, "' must be a numeric vector")
    recycleArg(arg, argname, length.out)
}

recycleLogicalArg <- function(arg, argname, length.out)
{
    if (!is.logical(arg))
        stop("'", argname, "' must be a logical vector")
    recycleArg(arg, argname, length.out)
}

recycleCharacterArg <- function(arg, argname, length.out)
{
    if (!is.character(arg))
        stop("'", argname, "' must be a character vector")
    recycleArg(arg, argname, length.out)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Normalization of replacement values
###

### NOT exported.
normalize_names_replacement_value <- function(value, x)
{
    if (is.null(value))
        return(NULL)
    value <- as.character(value)
    value_len <- length(value)
    x_len <- length(x)
    if (value_len > x_len)
        stop(wmsg("attempt to set too many names (", value_len, ") ",
                  "on ", class(x), " object of length ", x_len))
    if (value_len < x_len) {
        ## We pad with NA's to mimic what 'names(x) <- value' does on an
        ## ordinary vector.
        value <- c(value, rep.int(NA_character_, x_len - value_len))
    }
    value
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Fold a vector-like object.
###

### We use a signature in the style of IRanges::successiveIRanges() or
### IRanges::successiveViews().
### The current implementation should be fast enough if length(x)/circle.length
### is small (i.e. < 10 or 20). This will actually be the case for the typical
### usecase which is the calculation of "circular coverage vectors", that is,
### we use fold() on the "linear coverage vector" to turn it into a "circular
### coverage vector" of length 'circle.length' where 'circle.length' is the
### length of the circular sequence.  
fold <- function(x, circle.length, from=1)
{
    if (typeof(x) != "S4" && !is.numeric(x) && !is.complex(x))
        stop("'x' must be a vector-like object with elements that can be added")
    if (!isSingleNumber(circle.length))
        stop("'circle.length' must be a single integer")
    if (!is.integer(circle.length))
        circle.length <- as.integer(circle.length)
    if (circle.length <= 0L)
        stop("'circle.length' must be positive")
    if (!isSingleNumber(from))
        stop("'from' must be a single integer")
    if (!is.integer(from))
        from <- as.integer(from)
    from <- 1L + (from - 1L) %% circle.length
    if (typeof(x) == "S4") {
        ans <- as(rep.int(0L, circle.length), class(x))
        if (length(ans) != circle.length)
            stop("don't know how to handle 'x' of class ", class(x))
    } else {
        ans <- vector(typeof(x), length=circle.length)
    }
    if (from > length(x)) {
        ## Nothing to fold
        jj <- seq_len(length(x)) + circle.length - from + 1L
        ans[jj] <- x
        return(ans)
    }
    if (from > 1L) {
        ii <- seq_len(from - 1L)
        jj <- ii + circle.length - from + 1L
        ans[jj] <- x[ii]
    }
    max_from <- length(x) - circle.length + 1L
    while (from <= max_from) {
        ii <- from:(from+circle.length-1L)
        ans[] <- ans[] + x[ii]
        from <- from + circle.length
    }
    if (from > length(x))
        return(ans)
    ii <- from:length(x)
    jj <- ii - from + 1L
    ans[jj] <- ans[jj] + x[ii]
    ans
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other non exported normarg* functions.
###

### NOT exported.
normargSingleStartOrNA <- function(start)
{
    if (!isSingleNumberOrNA(start))
        stop("'start' must be a single integer or NA")
    if (!is.integer(start))
        start <- as.integer(start)
    start
}

### NOT exported.
normargSingleEndOrNA <- function(end)
{
    if (!isSingleNumberOrNA(end))
        stop("'end' must be a single integer or NA")
    if (!is.integer(end)) 
        end <- as.integer(end)
    end
}

### NOT exported.
normargUseNames <- function(use.names)
{
    if (is.null(use.names))
        return(TRUE)
    if (!isTRUEorFALSE(use.names))
        stop("'use.names' must be TRUE or FALSE")
    use.names
}

### NOT exported.
normargRunK <- function(k, n, endrule)
{
    if (!is.numeric(k))
        stop("'k' must be a numeric vector")
    if (k < 0)
        stop("'k' must be positive")
    if ((endrule != "drop") && (k %% 2 == 0)) {
        k <- 1L + 2L * (k %/% 2L)
        warning(paste("'k' must be odd when 'endrule != \"drop\"'!",
                      "Changing 'k' to ", k))
    }
    if (k > n) {
        k <- 1L + 2L * ((n - 1L) %/% 2L)
        warning("'k' is bigger than 'n'! Changing 'k' to ", k)
    }
    as.integer(k)
}

### NOT exported.
normargSubset2_iOnly <-
    function(x, i, j, ..., .conditionPrefix=character())
{
    if (!missing(j) || length(list(...)) > 0)
        warning(.conditionPrefix, "arguments beyond 'i' ignored")
    if (missing(i))
        stop(.conditionPrefix, "subscript 'i' is missing")
    if (!is.character(i) && !is.numeric(i))
        stop(.conditionPrefix, "invalid subscript 'i' type")
    if (length(i) < 1L)
        stop(.conditionPrefix, "attempt to select less than one element")
    if (length(i) > 1L)
        stop(.conditionPrefix, "attempt to select more than one element")
    if (is.numeric(i) && (i < 1L || i > length(x)+1))
        stop(.conditionPrefix, "subscript 'i' out of bounds")
    if (is.character(i)) {
        i <- match(i, names(x))
        if (is.na(i))
            i <- length(x) + 1L
    }
    i
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Miscellaneous.
###

### NOT exported.
numeric2integer <- function(x)
{
    if (is.numeric(x) && !is.integer(x)) as.integer(x) else x
}

### NOT exported.
extraArgsAsList <- function(.valid.argnames, ...)
{
    args <- list(...)
    argnames <- names(args)
    if (length(args) != 0L
        && (is.null(argnames) || any(argnames %in% c("", NA))))
        stop("all extra arguments must be named")
    if (!is.null(.valid.argnames) && !all(argnames %in% .valid.argnames))
        stop("valid extra argument names are ",
             paste("'", .valid.argnames, "'", sep="", collapse=", "))
    if (anyDuplicated(argnames))
        stop("argument names must be unique")
    args
}

