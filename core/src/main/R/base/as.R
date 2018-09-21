#  File src/library/base/R/as.R
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

as.single <- function(x,...) UseMethod("as.single")
as.single.default <- function(x,...)
    structure(.Internal(as.numeric(x)), Csingle=TRUE)

# as.character is now internal.  The default method remains here to
# preserve the semantics that for a call with an object argument
# dispatching is done first on as.character and then on as.vector.
as.character.default <- function(x,...) as.vector(x, "character")
as.character <- function(x, ...) UseMethod("as.character")

as.expression <- function(x,...) UseMethod("as.expression")
as.expression.default <- function(x,...) .Internal(as.expression(x))

as.list <- function(x,...) UseMethod("as.list")
## This if() avoid dispatch on methods for as.vector.
as.list.default <- function (x, ...)
    if (typeof(x) == "list") x else as.vector(x, "list")

as.list.function <- function (x, ...) c(formals(x), list(body(x)))

## FIXME:  Really the above  as.vector(x, "list")  should work for data.frames!
as.list.data.frame <- function(x,...) {
    x <- unclass(x)
    attr(x,"row.names") <- NULL
    x
}

as.list.environment <- function(x, all.names=FALSE, ...)
    .Internal(env2list(x, all.names))

as.vector <- function(x, mode) {
    UseMethod("as.vector")
}

as.vector.default <- function(x, mode = "any") {

    if(mode == "any") {
        if(is.atomic(x)) {
            attributes(x) <- NULL
            return(x)
        } else if(is.list(x) || is.pairlist(x) || is.call(x)) {
            return(x)
        } else {
            stop(sprintf("cannot coerce type '%s' to vector of type 'any'", typeof(x)))
        }
    }

    switch(mode,
        logical = as.logical(x),
        raw = as.raw(x),
        integer = as.integer(x),
        double = as.numeric(x),
        numeric = as.numeric(x),
        character = .Internal(as.character(x)),
        complex = as.complex(x),
        expression = .Internal(as.expression(x)),
        list = .Internal(as.list(x)),
        pairlist = .Internal(as.pairlist(x)),
        name = .Internal(as.symbol(x)),
        symbol = .Internal(as.symbol(x)),
        stop(sprintf("Invalid 'mode' argument: %s", mode)))
}


as.matrix <- function(x, ...) UseMethod("as.matrix")
as.matrix.default <- function(x, ...) {
    if (is.matrix(x)) x
    else
	array(x, c(length(x), 1L),
	      if(!is.null(names(x))) list(names(x), NULL) else NULL)
}
as.null <- function(x,...) UseMethod("as.null")
as.null.default <- function(x,...) NULL

as.function <- function(x,...) UseMethod("as.function")
as.function.default <- function (x, envir = parent.frame(), ...)
    if (is.function(x)) x else .Internal(as.function.default(x, envir))

as.array <- function(x, ...) UseMethod("as.array")
as.array.default <- function(x, ...)
{
    if(is.array(x)) return(x)
    n <- names(x)
    dim(x) <- length(x)
    if(length(n)) dimnames(x) <- list(n)
    return(x)
}

as.symbol <- function(x) as.vector(x, "symbol")
as.name <- as.symbol

## as.call <- function(x) stop("type call cannot be assigned")
as.qr <- function(x) stop("you cannot be serious")
