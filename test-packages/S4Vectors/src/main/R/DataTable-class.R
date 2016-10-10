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
### DataTable objects
### -------------------------------------------------------------------------
###
### DataTable is an API only (i.e. virtual class with no slots) for
### accessing objects with a rectangular shape like DataFrame or
### RangedData objects.  It mimics the API for standard data.frame
### objects, except derivatives do not necessarily behave as a list of
### columns.
###


setClass("DataTable", representation("VIRTUAL"))


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessors.
###

setMethod("NROW", "DataTable", function(x) nrow(x))

setMethod("NCOL", "DataTable", function(x) ncol(x))

setMethod("dim", "DataTable", function(x) c(nrow(x), ncol(x)))

setGeneric("ROWNAMES", function(x) standardGeneric("ROWNAMES"))

setMethod("ROWNAMES", "ANY",
    function (x) if (length(dim(x)) != 0L) rownames(x) else names(x)
)

setMethod("ROWNAMES", "DataTable", function(x) rownames(x))

setMethod("dimnames", "DataTable",
          function(x) {
            list(rownames(x), colnames(x))
          })

setReplaceMethod("dimnames", "DataTable",
                 function(x, value)
                 {
                   if (!is.list(value))
                     stop("replacement value must be a list")
                   rownames(x) <- value[[1L]]
                   colnames(x) <- value[[2L]]
                   x
                 })


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting.
###

head.DataTable <- head.matrix
setMethod("head", "DataTable", head.DataTable)

tail.DataTable <- tail.matrix
setMethod("tail", "DataTable", tail.DataTable)

setMethod("subset", "DataTable",
          function(x, subset, select, drop = FALSE, ...) 
          {
              i <- evalqForSubset(subset, x, ...)
              j <- evalqForSelect(select, x, ...)
              x[i, j, drop=drop]
          })

setMethod("na.omit", "DataTable",
          function(object, ...) {
            attr(object, "row.names") <- rownames(object)
            object.omit <- stats:::na.omit.data.frame(object)
            attr(object.omit, "row.names") <- NULL
            object.omit
          })

setMethod("na.exclude", "DataTable",
          function(object, ...) {
            attr(object, "row.names") <- rownames(object)
            object.ex <- stats:::na.exclude.data.frame(object)
            attr(object.ex, "row.names") <- NULL
            object.ex
          })

setMethod("is.na", "DataTable", function(x) {
  na <- do.call(cbind, lapply(seq(ncol(x)), function(xi) is.na(x[[xi]])))
  rownames(na) <- rownames(x)
  na
})

setMethod("complete.cases", "DataTable", function(...) {
  args <- list(...)
  if (length(args) == 1) {
    x <- args[[1L]]
    rowSums(is.na(x)) == 0
  } else complete.cases(args[[1L]]) & do.call(complete.cases, args[-1L])
})


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Transforming.
###

transform.DataTable <- function(`_data`, ...) {
  exprs <- as.list(substitute(list(...))[-1L])
  if (any(names(exprs) == "")) {
    stop("all arguments in '...' must be named")
  }
  ## elements in '...' can originate from different environments
  env <- setNames(top_prenv_dots(...), names(exprs))
  for (colName in names(exprs)) { # for loop allows inter-arg dependencies
    `_data`[[colName]] <- safeEval(exprs[[colName]], `_data`, env[[colName]])
  }
  `_data`
}

setMethod("transform", "DataTable", transform.DataTable)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining.
###

setMethod("cbind", "DataTable", function(..., deparse.level=1)
          stop("missing 'cbind' method for DataTable class ",
               class(list(...)[[1L]])))

setMethod("rbind", "DataTable", function(..., deparse.level=1)
          stop("missing 'rbind' method for DataTable class ",
               class(list(...)[[1L]])))

setMethod("cbind2", c("ANY", "DataTable"), function(x, y, ...) {
  x <- as(x, "DataFrame")
  cbind(x, y, ...)
})

setMethod("cbind2", c("DataTable", "ANY"), function(x, y, ...) {
  y <- as(y, "DataFrame")
  cbind(x, y, ...)
})

setMethod("cbind2", c("DataTable", "DataTable"), function(x, y, ...) {
  x <- as(x, "DataFrame")
  y <- as(y, "DataFrame")
  cbind(x, y, ...)
})

setMethod("rbind2", c("ANY", "DataTable"), function(x, y, ...) {
  x <- as(x, "DataFrame")
  rbind(x, y, ...)
})

setMethod("rbind2", c("DataTable", "ANY"), function(x, y, ...) {
  y <- as(y, "DataFrame")
  rbind(x, y, ...)
})

setMethod("rbind2", c("DataTable", "DataTable"), function(x, y, ...) {
  x <- as(x, "DataFrame")
  y <- as(y, "DataFrame")
  rbind(x, y, ...)
})

setMethod("merge", c("DataTable", "DataTable"), function(x, y, ...) {
  as(merge(as(x, "data.frame"), as(y, "data.frame"), ...), class(x))
})
setMethod("merge", c("data.frame", "DataTable"), function(x, y, ...) {
  as(merge(x, as(y, "data.frame"), ...), class(y))
})
setMethod("merge", c("DataTable", "data.frame"), function(x, y, ...) {
  as(merge(as(x, "data.frame"), y, ...), class(x))
})

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Summary methods.
###

### S3/S4 combo for duplicated.DataTable
duplicated.DataTable <- function(x, incomparables=FALSE, fromLast=FALSE, ...)
{
    duplicated(as(x, "data.frame"),
               incomparables=incomparables, fromLast=fromLast, ...)
}

setMethod("duplicated", "DataTable", duplicated.DataTable)

### S3/S4 combo for unique.DataTable
unique.DataTable <- unique.data.frame
setMethod("unique", "DataTable", unique.DataTable)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion
###

setGeneric("as.env", function(x, ...) standardGeneric("as.env"))

setMethod("as.env", "NULL", function(x, enclos, tform = identity) {
  new.env(parent=enclos)
})

addSelfRef <- function(x, env) {
  env$.. <- x
  env
}

setMethod("as.env", "DataTable",
          function(x, enclos = parent.frame(2), tform = identity) {
              env <- new.env(parent = enclos)
              lapply(colnames(x),
                     function(col) {
                         colFun <- function() {
                             val <- tform(x[[col]])
                             rm(list=col, envir=env)
                             assign(col, val, env)
                             val
                         }
                         makeActiveBinding(col, colFun, env)
                     })
              addSelfRef(x, env)
          })

as.data.frame.DataTable <- function(x, row.names=NULL, optional=FALSE, ...) {
    as.data.frame(x, row.names=NULL, optional=optional, ...)
}
setMethod("as.data.frame", "DataTable",
          function(x, row.names=NULL, optional=FALSE, ...) {
              as.data.frame(as(x, "DataFrame"),
                            row.names=row.names, optional=optional, ...)
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### The "show" method.
###

setMethod("show", "DataTable",
          function(object)
          {
              nhead <- get_showHeadLines()
              ntail <- get_showTailLines()
              nr <- nrow(object)
              nc <- ncol(object)
              cat(class(object), " with ",
                  nr, ifelse(nr == 1, " row and ", " rows and "),
                  nc, ifelse(nc == 1, " column\n", " columns\n"),
                  sep = "")
              if (nr > 0 && nc > 0) {
                  nms <- rownames(object)
                  if (nr <= (nhead + ntail + 1L)) {
                      out <-
                        as.matrix(format(as.data.frame(
                                         lapply(object, showAsCell),
                                         optional = TRUE)))
                      if (!is.null(nms))
                          rownames(out) <- nms
                  } else {
                      out <-
                        rbind(as.matrix(format(as.data.frame(
                                               lapply(object, function(x)
                                                      showAsCell(head(x, nhead))),
                                               optional = TRUE))),
                              rbind(rep.int("...", nc)),
                              as.matrix(format(as.data.frame(
                                               lapply(object, function(x) 
                                                      showAsCell(tail(x, ntail))),
                                               optional = TRUE))))
                  rownames(out) <- .rownames(nms, nr, nhead, ntail) 
                  }
                  classinfo <-
                    matrix(unlist(lapply(object, function(x) {
                        paste0("<", classNameForDisplay(x)[1],
                               ">")
                    }), use.names = FALSE), nrow = 1,
                           dimnames = list("", colnames(out)))
                  out <- rbind(classinfo, out)
                  print(out, quote = FALSE, right = TRUE)
              }
          })

.rownames <- function(nms, nrow, nhead, ntail)
{
    p1 <- ifelse (nhead == 0, 0L, 1L)
    p2 <- ifelse (ntail == 0, 0L, ntail-1L)
    s1 <- s2 <- character(0)

    if (is.null(nms)) {
        if (nhead > 0) 
            s1 <- paste0(as.character(p1:nhead))
        if (ntail > 0) 
            s2 <- paste0(as.character((nrow-p2):nrow))
    } else { 
        if (nhead > 0) 
            s1 <- paste0(head(nms, nhead))
        if (ntail > 0) 
            s2 <- paste0(tail(nms, ntail))
    }
    c(s1, "...", s2)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Statistical routines
###

setMethod("xtabs", signature(data = "DataTable"),
          function(formula = ~., data, subset, na.action, exclude = c(NA, NaN),
                   drop.unused.levels = FALSE)
          {
            data <- as(data, "data.frame")
            callGeneric()
          })

setMethod("table", "DataTable", function(...) {
  table(as.list(cbind(...)))
})

## TODO: lm, glm, loess, ...


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Row-level and column-level summary
###

### Turn the row/col-level summarization function defined in base R into S4
### generics.
setGeneric("rowSums", signature="x")
setGeneric("colSums", signature="x")
setGeneric("rowMeans", signature="x")
setGeneric("colMeans", signature="x")

