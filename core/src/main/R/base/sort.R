#  File src/library/base/R/sort.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

sort <- function(x, decreas = FALSE, ...)
{
    if(!is.logical(decreas) || length(decreas) != 1L)
        stop("'decreasing' must be a length-1 logical vector.\nDid you intend to set 'partial'?")
    UseMethod("sort")
}

sort.default <- function(x, decreas = FALSE, nalast = NA, ...)
{
    ## The first case includes factors.
    if(is.object(x)) x[order(x, nalast = nalast, decreas = decreas)]
    else sort.int(x, nalast = nalast, decreas = decreas, ...)
}

sort.int <-
    function(x, partial = NULL, nalast = NA, decreas = FALSE,
             method = c("shell", "quick"), index.return = FALSE)
{
    if(isfact <- is.factor(x)) {
        if(index.return) stop("'index.return' only for non-factors")
	lev <- levels(x)
	nlev <- nlevels(x)
 	isord <- is.ordered(x)
        x <- c(x)
    } else if(!is.atomic(x))
        stop("'x' must be atomic")

    if(has.na <- any(ina <- is.na(x))) {
        nas <- x[ina]
        x <-  x[!ina]
    }
    if(index.return && !is.na(nalast))
        stop("'index.return' only for 'na.last = NA'")
    if(!is.null(partial)) {
        if(index.return || decreas || isfact || !missing(method))
	    stop("unsupported options for partial sorting")
        if(!all(is.finite(partial))) stop("non-finite 'partial'")
        y <- if(length(partial) <= 10L) {
            partial <- .Internal(qsort(partial, FALSE))
            .Internal(psort(x, partial))
        } else if (is.double(x)) .Internal(qsort(x, FALSE))
        else .Internal(sort(x, FALSE))
    }
    else {
        nms <- names(x)
        method <- if(is.numeric(x)) match.arg(method) else "shell"
        switch(method,
               "quick" = {
                   if(!is.null(nms)) {
                       if(decreas) x <- -x
                       y <- .Internal(qsort(x, TRUE))
                       if(decreas) y$x <- -y$x
                       names(y$x) <- nms[y$ix]
                       if (!index.return) y <- y$x
                   } else {
                       if(decreas) x <- -x
                       y <- .Internal(qsort(x, index.return))
                       if(decreas)
                           if(index.return) y$x <- -y$x else y <- -y
                   }
               },
               "shell" = {
                   if(index.return || !is.null(nms)) {
                       o <- sort.list(x, decreas = decreas)
                       y <- if (index.return) list(x = x[o], ix = o) else x[o]
                       ## names(y) <- nms[o] # pointless!
                   }
                   else
                       y <- .Internal(sort(x, decreas))
               })
    }
    if(!is.na(nalast) && has.na)
	y <- if(!nalast) c(nas, y) else c(y, nas)
    if(isfact)
        y <- (if (isord) ordered else factor)(y, levels=seq_len(nlev),
                                              labels=lev)
    y
}

order <- function(..., nalast = TRUE, decreas = FALSE, method = c("shell", "radix"))
{
    z <- list(...)
cat("\n100, ")
    method <- if (missing(method)) "shell" else "shell"

    if(any(unlist(lapply(z, is.object)))) {
    cat("104, ")
        z <- lapply(z, function(x) if(is.object(x)) as.vector(xtfrm(x)) else x)
        if(method == "radix" || !is.na(nalast))
            return(do.call("order", c(z, nalast = nalast,
                                      decreas = decreas,
                                      method = method)))
    } else if(method != "radix" && !is.na(nalast)) {
    cat("111, ")
        return(.Internal(order(nalast, decreas, method, ...)))
    }
cat("114, ")
    if (method == "radix") {
        decreas <- rep_len(as.logical(decreas), length(z))
        return(.Internal(radixsort(nalast, decreas, FALSE, TRUE, ...)))
    }
cat("119, ")
    ## na.last = NA case: remove nas
    if(any(diff((l.z <- lengths(z)) != 0L)))
        stop("argument lengths differ")
    na <- vapply(z, is.na, rep.int(NA, l.z[1L]))
cat("123, ")
    ok <- if(is.matrix(na)) rowSums(na) == 0L else !any(na)
cat("125, ")
    if(all(!ok)) return(integer())
cat("127, ")
    z[[1L]][!ok] <- NA
cat("129, ")
    if (is.na(nalast)) nalast <- TRUE
    ans <- do.call("order", c(z, list(nalast=nalast, decreas = decreas, method=method) ) )
    ans[ok[ans]]
}

sort.list <- function(x, partial = NULL, nalast = TRUE, decreas = FALSE,
                      method = c("shell", "quick", "radix"))
{
    method <- match.arg(method)
    if(!is.atomic(x))
        stop("'x' must be atomic for 'sort.list'\nHave you called 'sort' on a list?")
    if(!is.null(partial))
        .NotYetUsed("partial != NULL")
    if(method == "quick") {
        if(is.factor(x)) x <- as.integer(x) # sort the internal codes
        if(is.numeric(x))
            return(sort(x, nalast = nalast, decreas = decreas,
                        method = "quick", index.return = TRUE)$ix)
        else stop("method=\"quick\" is only for numeric 'x'")
    }
    if(method == "radix") {
        if(!typeof(x) == "integer") # do want to allow factors here
            stop("method=\"radix\" is only for integer 'x'")
        if(is.na(nalast))
            return(.Internal(radixsort(x[!is.na(x)], TRUE, decreas)))
        else
            return(.Internal(radixsort(x, nalast, decreas)))
    }
    ## method == "shell"
    if(is.na(nalast)) .Internal(order(TRUE, decreas, method, x[!is.na(x)]))
    else .Internal(order(nalast, decreas, method, x))
}


## xtfrm is now primitive
## xtfrm <- function(x) UseMethod("xtfrm")
xtfrm.default <- function(x)
    if(is.numeric(x)) unclass(x) else as.vector(rank(x, ties.method="min", na.last="keep"))
xtfrm.factor <- function(x) as.integer(x) # primitive, so needs a wrapper
xtfrm.Surv <- function(x)
    if(ncol(x) == 2L) order(x[,1L], x[,2L]) else order(x[,1L], x[,2L], x[,3L]) # needed by 'party'
xtfrm.AsIs <- function(x)
{
    if(length(cl<- class(x)) > 1) oldClass(x) <- cl[-1L]
    NextMethod("xtfrm")
}

.gt <- function(x, i, j)
{
    xi <- x[i]; xj <- x[j]
    if (xi == xj) 0L else if(xi > xj) 1L else -1L;
}

.gtn <- function(x, strictly)
{
    n <- length(x)
    if(strictly) all(x[-1L] > x[-n]) else all(x[-1L] >= x[-n])
}
