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
### Common operations on List objects
### -------------------------------------------------------------------------
###


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Looping on List objects
###

setMethod("lapply", "List",
          function(X, FUN, ...)
          {
              FUN <- match.fun(FUN)
              ii <- seq_len(length(X))
              names(ii) <- names(X)
              lapply(ii, function(i) FUN(X[[i]], ...))
          })

.sapplyDefault <- base::sapply
environment(.sapplyDefault) <- topenv()
setMethod("sapply", "List", .sapplyDefault)

setGeneric("endoapply", signature = "X",
           function(X, FUN, ...) standardGeneric("endoapply"))

setMethod("endoapply", "list",
          function(X, FUN, ...) lapply(X = X, FUN = match.fun(FUN), ...))

setMethod("endoapply", "data.frame",
          function(X, FUN, ...)
          as.data.frame(lapply(X = X, FUN = match.fun(FUN), ...)))

setMethod("endoapply", "List",
          function(X, FUN, ...) {
              elementTypeX <- elementType(X)
              FUN <- match.fun(FUN)
              for (i in seq_len(length(X))) {
                  elt <- FUN(X[[i]], ...)
                  if (!extends(class(elt), elementTypeX))
                      stop("'FUN' must return elements of class ", elementTypeX)
                  X[[i]] <- elt
              }
              X
          })

setGeneric("revElements", signature="x",
    function(x, i) standardGeneric("revElements")
)

### These 2 methods explain the concept of revElements() but they are not
### efficient because they loop over the elements of 'x[i]'.
### There is a fast method for CompressedList objects though.
setMethod("revElements", "list",
    function(x, i)
    {
        x[i] <- lapply(x[i], revROWS)
        x
    }
)

setMethod("revElements", "List",
    function(x, i)
    {
        x[i] <- endoapply(x[i], revROWS)
        x
    }
)

setGeneric("mendoapply", signature = "...",
           function(FUN, ..., MoreArgs = NULL) standardGeneric("mendoapply"))

setMethod("mendoapply", "list", function(FUN, ..., MoreArgs = NULL)
          mapply(FUN = FUN, ..., MoreArgs = MoreArgs, SIMPLIFY = FALSE))

setMethod("mendoapply", "data.frame", function(FUN, ..., MoreArgs = NULL)
          as.data.frame(mapply(FUN = match.fun(FUN), ..., MoreArgs = MoreArgs,
                               SIMPLIFY = FALSE)))

setMethod("mendoapply", "List",
          function(FUN, ..., MoreArgs = NULL) {
              X <- list(...)[[1L]]
              elementTypeX <- elementType(X)
              FUN <- match.fun(FUN)
              listData <-
                mapply(FUN = FUN, ..., MoreArgs = MoreArgs, SIMPLIFY = FALSE)
              for (i in seq_len(length(listData))) {
                  if (!extends(class(listData[[i]]), elementTypeX))
                      stop("'FUN' must return elements of class ", elementTypeX)
                  X[[i]] <- listData[[i]]
              }
              X
          })

### Element-wise c() for list-like objects.
### This is a fast mapply(c, ..., SIMPLIFY=FALSE) but with the following
### differences:
###   1) pc() ignores the supplied objects that are NULL.
###   2) pc() does not recycle its arguments. All the supplied objects must
###      have the same length.
###   3) If one of the supplied objects is a List object, then pc() returns a
###      List object.
###   4) pc() always returns a homogenous list or List object, that is, an
###      object where all the list elements have the same type.
pc <- function(...)
{
    args <- unname(list(...))
    args <- args[!sapply_isNULL(args)]
    if (length(args) == 0L)
        return(list())
    if (length(args) == 1L)
        return(args[[1L]])
    args_NROWS <- elementNROWS(args)
    if (!all(args_NROWS == args_NROWS[[1L]]))
        stop("all the objects to combine must have the same length")

    ans_as_List <- any(vapply(args, is, logical(1), "List", USE.NAMES=FALSE))
    SPLIT.FUN <- if (ans_as_List) IRanges::splitAsList else split
    ans_unlisted <- do.call(c, lapply(args, unlist, use.names=FALSE))
    f <- structure(unlist(lapply(args, quick_togroup), use.names=FALSE),
                   levels=as.character(seq_along(args[[1L]])),
                   class="factor")
    setNames(SPLIT.FUN(ans_unlisted, f), names(args[[1L]]))
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Functional programming methods
###

### Copy+pasted to disable forced as.list() coercion
.ReduceDefault <- function(f, x, init, right = FALSE, accumulate = FALSE) 
{
    mis <- missing(init)
    len <- length(x)
    if (len == 0L) 
        return(if (mis) NULL else init)
    f <- match.fun(f)
#    if (!is.vector(x) || is.object(x)) 
#        x <- as.list(x)
    ind <- seq_len(len)
    if (mis) {
        if (right) {
            init <- x[[len]]
            ind <- ind[-len]
        }
        else {
            init <- x[[1L]]
            ind <- ind[-1L]
        }
    }
    if (!accumulate) {
        if (right) {
            for (i in rev(ind)) init <- f(x[[i]], init)
        }
        else {
            for (i in ind) init <- f(init, x[[i]])
        }
        init
    }
    else {
        len <- length(ind) + 1L
        out <- vector("list", len)
        if (mis) {
            if (right) {
                out[[len]] <- init
                for (i in rev(ind)) {
                    init <- f(x[[i]], init)
                    out[[i]] <- init
                }
            }
            else {
                out[[1L]] <- init
                for (i in ind) {
                    init <- f(init, x[[i]])
                    out[[i]] <- init
                }
            }
        }
        else {
            if (right) {
                out[[len]] <- init
                for (i in rev(ind)) {
                    init <- f(x[[i]], init)
                    out[[i]] <- init
                }
            }
            else {
                for (i in ind) {
                    out[[i]] <- init
                    init <- f(init, x[[i]])
                }
                out[[len]] <- init
            }
        }
        if (all(lengths(out) == 1L)) 
            out <- unlist(out, recursive = FALSE)
        out
    }
}

setMethod("Reduce", "List", .ReduceDefault)

### Presumably to avoid base::lapply coercion to list.
.FilterDefault <- base::Filter
environment(.FilterDefault) <- topenv()
setMethod("Filter", "List", .FilterDefault)

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Evaluating.
###

setMethod("within", "List",
          function(data, expr, ...)
          {
            ## cannot use active bindings here, as they break for replacement
            e <- list2env(as.list(data))
            ##e <- as.env(data)
            safeEval(substitute(expr), e, top_prenv(expr))
            l <- mget(ls(e), e)
            l <- l[!sapply(l, is.null)]
            nD <- length(del <- setdiff(names(data), (nl <- names(l))))
            for (nm in nl)
              data[[nm]] <- l[[nm]]
            for (nm in del)
              data[[nm]] <- NULL
            data
          })

setMethod("do.call", c("ANY", "List"),
          function (what, args, quote = FALSE, envir = parent.frame()) {
            args <- as.list(args)
            callGeneric()
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Factors.
###

droplevels.List <- function(x, ...) droplevels(x, ...)
.droplevels.List <- function(x, except = NULL) 
{
  ix <- vapply(x, Has(levels), logical(1L))
  ix[except] <- FALSE
  x[ix] <- lapply(x[ix], droplevels)
  x
}

setMethod("droplevels", "List", .droplevels.List)
