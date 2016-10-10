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
### Pairs objects
### -------------------------------------------------------------------------
###
### Two parallel vectors. Could result from "dereferencing" a Hits.
###

setClass("Pairs",
         contains="Vector",
         representation(first="ANY",
                        second="ANY",
                        NAMES="characterORNULL"),
         prototype(first=logical(0L),
                   second=logical(0L),
                   elementMetadata=DataFrame()))

setValidity2("Pairs", .valid.Pairs)

.valid.Pairs <- function(object) {
    c(if (length(object@first) != length(object@second))
          "'first' and 'second' must have the same length",
      if (!is.null(object@NAMES) &&
          length(object@NAMES) != length(object@first))
          "'NAMES', if not NULL, must have the same length as object"
      )
}

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessors
###

setGeneric("first", function(x, ...) standardGeneric("first"))
setGeneric("second", function(x, ...) standardGeneric("second"))

setMethod("first", "Pairs", function(x) x@first)
setMethod("second", "Pairs", function(x) x@second)

setGeneric("first<-", function(x, ..., value) standardGeneric("first<-"),
           signature="x")
setGeneric("second<-", function(x, ..., value) standardGeneric("second<-"),
           signature="x")

setReplaceMethod("first", "Pairs", function(x, value) {
                     x@first <- value
                     x
                 })
setReplaceMethod("second", "Pairs", function(x, value) {
                     x@second <- value
                     x
                 })

setMethod("names", "Pairs", function(x) x@NAMES)
setReplaceMethod("names", "Pairs", function(x, value) {
                     x@NAMES <- value
                     x
                 })

setMethod("length", "Pairs", function(x) length(first(x)))

setMethod("parallelSlotNames", "Pairs", function(x)
    c("first", "second", "NAMES", callNextMethod()))

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor
###

Pairs <- function(first, second, ..., names = NULL, hits = NULL) {
    if (!is.null(hits)) {
        stopifnot(is(hits, "Hits"),
                  queryLength(hits) == length(first),
                  subjectLength(hits) == length(second))
        first <- first[queryHits(hits)]
        second <- second[subjectHits(hits)]
    }
    stopifnot(length(first) == length(second),
              is.null(names) || length(names) == length(first))
    if (!missing(...)) {
        elementMetadata <- DataFrame(...)
    } else {
        elementMetadata <- make_zero_col_DataFrame(length(first))
    }
    new("Pairs", first=first, second=second, NAMES=names,
                 elementMetadata=elementMetadata)
}

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Comparison
### 

setMethod("match", c("Pairs", "Pairs"),
          function(x, table, nomatch = NA_integer_, incomparables = NULL, ...) {
              hits <- intersect(findMatches(first(x), first(table),
                                            incomparables=incomparables, ...),
                                findMatches(second(x), second(table),
                                            incomparables=incomparables, ...))
              ans <- selectHits(hits, "first")
              if (!identical(nomatch, NA_integer_)) {
                  ans[is.na(ans)] <- nomatch
              }
              ans
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coerce
### 
### We use 'zipup' and 'zipdown' because '(un)zip' already taken by utils.
###

setGeneric("zipup", function(x, y, ...) standardGeneric("zipup"))

setMethod("zipup", c("ANY", "ANY"), function(x, y) {
              stopifnot(length(x) == length(y))
              linear <- append(x, y)
              collate_subscript <- make_XYZxyz_to_XxYyZz_subscript(length(x))
              linear <- linear[collate_subscript]
              names <- if (!is.null(names(x))) names(x) else names(y)
              p <- IRanges::PartitioningByWidth(rep(2L, length(x)), names=names)
              relist(linear, p)
          })

setMethod("zipup", c("Pairs", "missing"), function(x, y, ...) {
              zipped <- zipup(first(x), second(x), ...)
              names(zipped) <- names(x)
              mcols(zipped) <- mcols(x)
              zipped
          })

setGeneric("zipdown", function(x, ...) standardGeneric("zipdown"))

setMethod("zipdown", "ANY", function(x) {
              stopifnot(all(lengths(x) == 2L))
              p <- IRanges::PartitioningByEnd(x)
              v <- unlist(x, use.names=FALSE)
              Pairs(v[start(p)], v[end(p)], names=names(x))
          })

setMethod("zipdown", "List", function(x) {
              unzipped <- callNextMethod()
              mcols(unzipped) <- mcols(x)
              unzipped
          })

setAs("Pairs", "DataFrame", function(from) {
          df <- DataFrame(first=first(from), second=second(from),
                          mcols(from), check.names=FALSE)
          df$names <- names(from)
          df
      })

setMethod("as.data.frame", "Pairs",
          function (x, row.names = NULL, optional = FALSE, ...) {
              as.data.frame(as(x, "DataFrame"), optional=optional,
                            row.names=row.names, ...)
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Show
###

.makeNakedMatFromPairs <- function(x) {
    x_len <- length(x)
    x_mcols <- mcols(x)
    x_nmc <- if (is.null(x_mcols)) 
                 0L
             else ncol(x_mcols)
    ans <- cbind(first = showAsCell(first(x)),
                 second = showAsCell(second(x)))
    if (x_nmc > 0L) {
        tmp <- do.call(data.frame, c(lapply(x_mcols, showAsCell), 
                                     list(check.names = FALSE)))
        ans <- cbind(ans, `|` = rep.int("|", x_len), as.matrix(tmp))
    }
    ans
}

showPairs <- function(x, margin = "", print.classinfo = FALSE) {
    x_class <- class(x)
    x_len <- length(x)
    x_mcols <- mcols(x)
    x_nmc <- if (is.null(x_mcols)) 
                 0L
             else ncol(x_mcols)
    cat(x_class, " object with ", x_len, " pair",
        ifelse(x_len ==  1L, "", "s"), " and ", x_nmc, " metadata column",
        ifelse(x_nmc == 1L, "", "s"), ":\n", sep = "")
    out <- makePrettyMatrixForCompactPrinting(x, .makeNakedMatFromPairs)
    if (print.classinfo) {
        .COL2CLASS <- c(first = class(first(x)), second = class(second(x)))
        classinfo <- makeClassinfoRowForCompactPrinting(x, .COL2CLASS)
        stopifnot(identical(colnames(classinfo), colnames(out)))
        out <- rbind(classinfo, out)
    }
    if (nrow(out) != 0L) 
        rownames(out) <- paste0(margin, rownames(out))
    print(out, quote = FALSE, right = TRUE, max = length(out))
}

setMethod("show", "Pairs", function(object) {
              showPairs(object, margin = "  ", print.classinfo = TRUE)
          })
