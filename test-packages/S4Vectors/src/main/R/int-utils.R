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
### Some low-level utility functions to operate on integer vectors
### -------------------------------------------------------------------------
###
### Unless stated otherwise, the functions defined in this file are not
### exported.
###


anyMissingOrOutside <- function(x, lower = -.Machine$integer.max,
                                   upper = .Machine$integer.max)
{
    if (!is.integer(x))
        stop("'x' must be an integer vector")
    if (!is.integer(lower))
        lower <- as.integer(lower)
    if (!is.integer(upper))
        upper <- as.integer(upper)
    .Call2("Integer_any_missing_or_outside",
           x, lower, upper,
           PACKAGE="S4Vectors")
}

### Returns 'sum(x)', or an error if 'x' contains NAs or negative values or if
### an integer overflow occurs while summing.
sumNonNegInts <- function(x)
    .Call2("Integer_sum_non_neg_vals", x, PACKAGE="S4Vectors")

### Equivalent to (but much faster than):
###
###   diff(c(0L, x))
###
### except that NAs are not supported.
diffWithInitialZero <- function(x)
{
    if (!is.integer(x))
        stop("'x' must be an integer vector")
    .Call2("Integer_diff_with_0", x, PACKAGE="S4Vectors")
}

### Equivalent to (but much faster than):
###
###   diff(c(x, last))
###
### except that NAs are not supported.
diffWithLast <- function(x, last)
{
  if (!is.integer(x))
    stop("'x' must be an integer vector")
  if (!isSingleInteger(last))
    stop("'last' must be a single, non-NA integer")
  .Call2("Integer_diff_with_last", x, last, PACKAGE="S4Vectors")
}

### x: integer vector.
### breakpoints: vector of positions on 'x' in increasing order.
### Equivalent to (but 10x faster than):
###     sum(relist(x, PartitioningByEnd(breakpoints)))
### Also equivalent to (but 200x faster than):
###     f <- rep(factor(seq_along(breakpoints)), diff(c(0L, breakpoints)))
###     vapply(split(x, f, drop=FALSE), sum, integer(1), USE.NAMES=FALSE)
groupsum <- function(x, breakpoints)
{   
    if (last_or(breakpoints, 0L) != length(x))
        stop("invalid 'breakpoints' argument")
    diffWithInitialZero(cumsum(x)[breakpoints])
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Fast ordering/comparing of integer pairs.
###

.normargIntegerOrFactor <- function(arg, argname)
{
    if (is.factor(arg))
        arg <- as.integer(arg)
    else if (is(arg, "Rle") &&
             (is(runValue(arg), "integer") || is(runValue(arg), "factor")))
        arg <- as.integer(arg)
    else if (!is.integer(arg))
        stop("'", argname, "' must be an integer vector or factor")
    arg
}

.normargMethod <- function(method=c("auto", "quick", "hash"), a_len)
{
    method <- match.arg(method)
    if (method == "auto") {
        if (a_len <= 2^29)
            method <- "hash"
        else
            method <- "quick"
    }
    method
}

pcompareIntegerPairs <- function(a1, b1, a2, b2)
{
    a1 <- .normargIntegerOrFactor(a1, "a1")
    b1 <- .normargIntegerOrFactor(b1, "b1")
    if (length(a1) != length(b1))
        stop("'a1' and 'b1' must have the same length")
    a2 <- .normargIntegerOrFactor(a2, "a2")
    b2 <- .normargIntegerOrFactor(b2, "b2")
    if (length(a2) != length(b2))
        stop("'a2' and 'b2' must have the same length")
    .Call2("Integer_pcompare2", a1, b1, a2, b2, PACKAGE="S4Vectors")
}

sortedIntegerPairs <- function(a, b, decreasing=FALSE, strictly=FALSE)
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    .Call2("Integer_sorted2", a, b, decreasing, strictly, PACKAGE="S4Vectors")
}

### Exported!
orderIntegerPairs <- function(a, b, decreasing=FALSE)
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    #.Call2("Integer_order2", a, b, decreasing, PACKAGE="S4Vectors")
    base::order(a, b, decreasing=decreasing)
}

.matchIntegerPairs_quick <- function(a1, b1, a2, b2, nomatch=NA_integer_)
{
    .Call2("Integer_match2_quick",
           a1, b1, a2, b2, nomatch,
           PACKAGE="S4Vectors")
}

.matchIntegerPairs_hash <- function(a1, b1, a2, b2, nomatch=NA_integer_)
{
    .Call2("Integer_match2_hash",
           a1, b1, a2, b2, nomatch,
           PACKAGE="S4Vectors")
}

### Exported!
matchIntegerPairs <- function(a1, b1, a2, b2, nomatch=NA_integer_,
                              method=c("auto", "quick", "hash"))
{
    a1 <- .normargIntegerOrFactor(a1, "a1")
    b1 <- .normargIntegerOrFactor(b1, "b1")
    if (length(a1) != length(b1))
        stop("'a1' and 'b1' must have the same length")
    a2 <- .normargIntegerOrFactor(a2, "a2")
    b2 <- .normargIntegerOrFactor(b2, "b2")
    if (length(a2) != length(b2))
        stop("'a2' and 'b2' must have the same length")
    if (!isSingleNumberOrNA(nomatch))
        stop("'nomatch' must be a single number or NA")
    if (!is.integer(nomatch))
        nomatch <- as.integer(nomatch)
    method <- .normargMethod(method, length(a2))
    if (method == "quick") {
        ans <- .matchIntegerPairs_quick(a1, b1, a2, b2, nomatch=nomatch)
    } else {
        ans <- .matchIntegerPairs_hash(a1, b1, a2, b2, nomatch=nomatch)
    }
    ans
}

.selfmatchIntegerPairs_quick <- function(a, b)
{
    .Call2("Integer_selfmatch2_quick", a, b, PACKAGE="S4Vectors")
}

### Author: Martin Morgan
.selfmatchIntegerPairs_hash <- function(a, b)
{
    .Call2("Integer_selfmatch2_hash", a, b, PACKAGE="S4Vectors")
}

### Exported!
selfmatchIntegerPairs <- function(a, b, method=c("auto", "quick", "hash"))
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    if (length(a) != length(b))
        stop("'a' and 'b' must have the same length")
    method <- .normargMethod(method, length(a))
    if (method == "quick") {
        ans <- .selfmatchIntegerPairs_quick(a, b)
    } else {
        ans <- .selfmatchIntegerPairs_hash(a, b)
    }
    ans
}

### Exported!
###
### For 'a' and 'b' integer vectors of equal length with no NAs,
### 'duplicatedIntegerPairs(a, b)' is equivalent to (but much faster than):
###
###   duplicated(cbind(a, b))
###
### For efficiency reasons, we don't support (and don't even check) for NAs.
duplicatedIntegerPairs <- function(a, b,
                                   fromLast=FALSE,
                                   method=c("auto", "quick", "hash"))
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    if (length(a) != length(b))
        stop("'a' and 'b' must have the same length")
    if (!isTRUEorFALSE(fromLast))
        stop("'fromLast' must be TRUE or FALSE")
    if (length(a) == 0L)
        return(logical(0L))
    if (length(a) == 1L)
        return(FALSE)
    ## This is a temporary (and inefficient) workaround until "quick"
    ## and "hash" methods can natively support fromLast=TRUE.
    ## TODO: Add support for fromLast=TRUE to "quick" and "hash" methods.
    if (fromLast)
        return(rev(duplicatedIntegerPairs(rev(a), rev(b), method=method)))
    sm <- selfmatchIntegerPairs(a, b, method=method)
    sm != seq_len(length(sm))
}

### For 'a' and 'b' integer vectors of equal length with no NAs,
### 'runEndsOfIntegerPairs(a, b)' finds the runs of identical rows in
### 'cbind(a, b)' and returns the indices of the last row in each run.
### In other words, it's equivalent to (but much faster than):
###
###   cumsum(runLength(Rle(paste(a, b, sep="|"))))
###
### Note that, if the rows in 'cbind(a, b)' are already sorted, then
### 'runEndsOfIntegerPairs(a, b)' returns the indices of the unique rows.
### In other words, 'runEndsOfIntegerPairs()' could be used to efficiently
### extract the unique pairs of integers from a presorted set of pairs.
### However, at the moment (April 2011) using 'duplicatedIntegerPairs()'
### is still faster than using 'runEndsOfIntegerPairs()' for finding the
### duplicated or unique pairs of integers in a presorted set of pairs.
### But this only because 'runEndsOfIntegerPairs()' is not as fast as it
### could/should be (an all-in-C implementation would probably solve this).
###
### For efficiency reasons, we don't support (and don't even check) for NAs.
### TODO: What happens if 'a' and 'b' don't have the same length? Shouldn't
### we check for that?
runEndsOfIntegerPairs <- function(a, b)
{
    not_same_as_prev <- diffWithInitialZero(a) != 0L |
                        diffWithInitialZero(b) != 0L
    if (length(not_same_as_prev) == 0L)
        return(integer())
    which(c(not_same_as_prev[-1L], TRUE))
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Fast ordering/comparing of integer quadruplets.
###

sortedIntegerQuads <- function(a, b, c, d, decreasing=FALSE, strictly=FALSE)
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    c <- .normargIntegerOrFactor(c, "c")
    d <- .normargIntegerOrFactor(d, "d")
    .Call2("Integer_sorted4", a, b, c, d, decreasing, strictly,
           PACKAGE="S4Vectors")
}

### Exported!
orderIntegerQuads <- function(a, b, c, d, decreasing=FALSE)
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    c <- .normargIntegerOrFactor(c, "c")
    d <- .normargIntegerOrFactor(d, "d")
    #.Call2("Integer_order4", a, b, c, d, decreasing, PACKAGE="S4Vectors")
    base::order(a, b, c, d, decreasing=decreasing)
}

.matchIntegerQuads_quick <- function(a1, b1, c1, d1, a2, b2, c2, d2,
                                     nomatch=NA_integer_)
{
    .Call2("Integer_match4_quick",
           a1, b1, c1, d1, a2, b2, c2, d2, nomatch,
           PACKAGE="S4Vectors")
}

.matchIntegerQuads_hash <- function(a1, b1, c1, d1, a2, b2, c2, d2,
                                    nomatch=NA_integer_)
{
    .Call2("Integer_match4_hash",
           a1, b1, c1, d1, a2, b2, c2, d2, nomatch,
           PACKAGE="S4Vectors")
}

### Exported!
matchIntegerQuads <- function(a1, b1, c1, d1, a2, b2, c2, d2,
                              nomatch=NA_integer_,
                              method=c("auto", "quick", "hash"))
{
    a1 <- .normargIntegerOrFactor(a1, "a1")
    b1 <- .normargIntegerOrFactor(b1, "b1")
    c1 <- .normargIntegerOrFactor(c1, "c1")
    d1 <- .normargIntegerOrFactor(d1, "d1")
    if (length(a1) != length(b1) ||
        length(b1) != length(c1) ||
        length(c1) != length(d1))
        stop("'a1', 'b1', 'c1' and 'd1' must have the same length")
    a2 <- .normargIntegerOrFactor(a2, "a2")
    b2 <- .normargIntegerOrFactor(b2, "b2")
    c2 <- .normargIntegerOrFactor(c2, "c2")
    d2 <- .normargIntegerOrFactor(d2, "d2")
    if (length(a2) != length(b2) ||
        length(b2) != length(c2) ||
        length(c2) != length(d2))
        stop("'a2', 'b2', 'c2' and 'd2' must have the same length")
    if (!isSingleNumberOrNA(nomatch))
        stop("'nomatch' must be a single number or NA")
    if (!is.integer(nomatch))
        nomatch <- as.integer(nomatch)
    method <- .normargMethod(method, length(a2))
    if (method == "quick") {
        ans <- .matchIntegerQuads_quick(a1, b1, c1, d1, a2, b2, c2, d2,
                                        nomatch=nomatch)
    } else {
        ans <- .matchIntegerQuads_hash(a1, b1, c1, d1, a2, b2, c2, d2,
                                       nomatch=nomatch)
    }
    ans
}

.selfmatchIntegerQuads_quick <- function(a, b, c, d)
{
    .Call2("Integer_selfmatch4_quick", a, b, c, d, PACKAGE="S4Vectors")
}

.selfmatchIntegerQuads_hash <- function(a, b, c, d)
{
    .Call2("Integer_selfmatch4_hash", a, b, c, d, PACKAGE="S4Vectors")
}

### Exported!
selfmatchIntegerQuads <- function(a, b, c, d,
                                  method=c("auto", "quick", "hash"))
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    c <- .normargIntegerOrFactor(c, "c")
    d <- .normargIntegerOrFactor(d, "d")
    if (length(a) != length(b) ||
        length(b) != length(c) ||
        length(c) != length(d))
        stop("'a', 'b', 'c' and 'd' must have the same length")
    method <- .normargMethod(method, length(a))
    if (method == "quick") {
        ans <- .selfmatchIntegerQuads_quick(a, b, c, d)
    } else {
        ans <- .selfmatchIntegerQuads_hash(a, b, c, d)
    }
    ans
}

### Exported!
duplicatedIntegerQuads <- function(a, b, c, d,
                                   fromLast=FALSE,
                                   method=c("auto", "quick", "hash"))
{
    a <- .normargIntegerOrFactor(a, "a")
    b <- .normargIntegerOrFactor(b, "b")
    c <- .normargIntegerOrFactor(c, "c")
    d <- .normargIntegerOrFactor(d, "d")
    if (length(a) != length(b) ||
        length(b) != length(c) ||
        length(c) != length(d))
        stop("'a', 'b', 'c' and 'd' must have the same length")
    if (!isTRUEorFALSE(fromLast))
        stop("'fromLast' must be TRUE or FALSE")
    if (length(a) == 0L)
        return(logical(0L))
    if (length(a) == 1L)
        return(FALSE)
    ## This is a temporary (and inefficient) workaround until "quick"
    ## and "hash" methods can natively support fromLast=TRUE.
    ## TODO: Add support for fromLast=TRUE to "quick" and "hash" methods.
    if (fromLast)
        return(rev(duplicatedIntegerQuads(rev(a), rev(b), rev(c), rev(d),
                                          method=method)))
    sm <- selfmatchIntegerQuads(a, b, c, d, method=method)
    sm != seq_len(length(sm))
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### tabulate2()
###
### An enhanced version of base::tabulate() that: (1) handles integer weights
### (NA and negative weights are OK), and (2) throws an error if 'strict' is
### TRUE and if 'x' contains NAs or values not in the [1, 'nbins'] interval.
### Unlike with base::tabulate(), 'nbins' needs to be specified (no default
### value). Also for now, it only works if 'x' is an integer vector.
###

tabulate2 <- function(x, nbins, weight=1L, strict=FALSE)
{
    if (!is.integer(x))
        stop("'x' must be an integer vector")
    if (!isSingleNumber(nbins))
        stop("'nbins' must be a single integer")
    if (!is.integer(nbins))
        nbins <- as.integer(nbins)
    if (!is.integer(weight))
        stop("'weight' must be an integer vector")
    if (!isTRUEorFALSE(strict))
        stop("'strict' must be TRUE or FALSE")
    .Call2("Integer_tabulate2", x, nbins, weight, strict, PACKAGE="S4Vectors")
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Bitwise operations.
###
### The bitwise operations in this section don't treat the integer NA (aka
### NA_integer_) in any particular way: at the C level an NA_integer_ is
### just a 32-bit pattern like any other int in C.
###

makePowersOfTwo <- function(nbit)
{
    if (!isSingleInteger(nbit) || nbit < 0L)
        stop("'nbit' must be a single non-negative integer")
    if (nbit == 0L)
        return(integer(0))
    as.integer(cumprod(c(1L, rep.int(2L, nbit-1L))))
}

### Returns an integer matrix with 'length(x)' rows and 'length(bitpos)' cols.
explodeIntBits <- function(x, bitpos=1:32)
{
    if (!is.integer(x))
        stop("'x' must be an integer vector")
    if (!is.integer(bitpos))
        stop("'bitpos' must be an integer vector")
    ## Old implementation: not very efficient and also broken on NAs and
    ## negative integers!
    #if (length(bitpos) == 0L)
    #    return(matrix(nrow=length(x), ncol=0L))
    #nbit <- max(bitpos)
    #if (is.na(nbit) || min(bitpos) <= 0L)
    #    stop("'bitpos' must contain potive values only")
    #ans <- matrix(nrow=length(x), ncol=nbit)
    #for (i in seq_len(ncol(ans))) {
    #    ans[ , i] <- x %% 2L
    #    x <- x %/% 2L
    #}
    #ans[ , bitpos, drop=FALSE]
    .Call2("Integer_explode_bits", x, bitpos, PACKAGE="S4Vectors")
}

### FIXME: Broken if ncol(x) = 32.
implodeIntBits <- function(x)
{
    if (!is.matrix(x))
        stop("'x' must be a matrix")
    tx <- t(x)
    data <- tx * makePowersOfTwo(nrow(tx))
    ## In some circumstances (e.g. if 'tx' has 0 col), the "dim" attribute
    ## gets lost during the above multiplication.
    if (is.null(dim(data)))
        dim(data) <- dim(tx)
    as.integer(colSums(data))
}

intbitsNOT <- function(x)
{
    stop("not yet implemented")  # fix implodeIntBits() first!
    xbits <- explodeIntBits(x)
    implodeIntBits(!xbits)
}

intbitsAND <- function(x, y)
{
    stop("not yet implemented")  # fix implodeIntBits() first!
    xbits <- explodeIntBits(x)
    ybits <- explodeIntBits(y)
    implodeIntBits(xbits & ybits)
}

intbitsOR <- function(x, y)
{
    stop("not yet implemented")  # fix implodeIntBits() first!
    xbits <- explodeIntBits(x)
    ybits <- explodeIntBits(y)
    implodeIntBits(xbits | ybits)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Others.
###

sortedMerge <- function(x, y)
    .Call2("Integer_sorted_merge", x, y, PACKAGE="S4Vectors")

mseq <- function(from, to)
{
    if (!is.integer(from))
        from <- as.integer(from)
    if (!is.integer(to))
        to <- as.integer(to)
    .Call2("Integer_mseq", from, to, PACKAGE="S4Vectors")
}

fancy_mseq <- function(lengths, offset=0L, rev=FALSE)
{
    if (!is.integer(lengths))
        lengths <- as.integer(lengths)
    if (!is.integer(offset))
        offset <- as.integer(offset)
    if (!is.logical(rev))
        stop("'rev' must be a logical vector")
    #unlist(lapply(seq_len(length(lengths)),
    #              function(i) {
    #                  tmp <- seq_len(lengths[i]) + offset[i]
    #                  if (rev[i])
    #                      tmp <- rev(tmp)
    #                  tmp
    #              }))
    .Call2("Integer_fancy_mseq", lengths, offset, rev, PACKAGE="S4Vectors")
}

make_XYZxyz_to_XxYyZz_subscript <- function(N)
    as.vector(matrix(seq_len(2L * N), nrow=2L, byrow=TRUE))

findIntervalAndStartFromWidth <- function(x, width)
    .Call2("findIntervalAndStartFromWidth", x, width, PACKAGE="S4Vectors")

### Reverse an injection from 1:M to 1:N.
### The injection is represented by an integer vector of length M (eventually
### with NAs). Fundamental property:
###
###   reverseIntegerInjection(reverseIntegerInjection(injection, N), M)
###
### is the identity function.
### Can be used to efficiently reverse the result of a call to 'order()'.
reverseIntegerInjection <- function(injection, N)
{
    M <- length(injection)
    ans <- rep.int(NA_integer_, N)
    is_not_na <- !is.na(injection)
    ans[injection[is_not_na]] <- seq_len(M)[is_not_na]
    ans
}

