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
### SimpleList objects
### -------------------------------------------------------------------------

setClass("SimpleList",
         contains="List",
         representation(
                        listData="list"
                        )
         )

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessor methods.
###

setMethod("length", "SimpleList", function(x) length(as.list(x)))

setMethod("names", "SimpleList", function(x) names(as.list(x)))

setReplaceMethod("names", "SimpleList",
                 function(x, value) {
                     names(x@listData) <- value
                     x
                 })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor.
###

### Low-level. NOT exported.
### Stuff to put in elementMetadata slot can be passed either with
###   new_SimpleList_from_list(..., elementMetadata=somestuff)
### or with
###   new_SimpleList_from_list(..., mcols=somestuff)
### The latter is the new recommended form.
new_SimpleList_from_list <- function(Class, x, ..., mcols)
{
    if (!extends(Class, "SimpleList"))
        stop("class ", Class, " must extend SimpleList")
    if (!is.list(x))
        stop("'x' must be a list")
    if (is.array(x)) { # drop any unwanted dimensions
        tmp_names <- names(x)
        dim(x) <- NULL # clears the names
        names(x) <- tmp_names
    }
    class(x) <- "list"
    ans_elementType <- elementType(new(Class))
    if (!all(sapply(x, function(xi) extends(class(xi), ans_elementType))))
        stop("all elements in 'x' must be ", ans_elementType, " objects")
    if (missing(mcols))
        return(new2(Class, listData=x, ..., check=FALSE))
    new2(Class, listData=x, ..., elementMetadata=mcols, check=FALSE)
}

SimpleList <- function(...)
{
    args <- list(...)
    ## The extends(class(x), "list") test is NOT equivalent to is.list(x) or
    ## to is(x, "list") or to inherits(x, "list"). Try for example with
    ## x <- data.frame() or x <- matrix(list()). We use the former below
    ## because it seems to closely mimic what the methods package uses for
    ## checking the "listData" slot of the SimpleList object that we try to
    ## create later with new(). For example if we were using is.list() instead
    ## of extends(), the test would pass on matrix(list()) but new() then would
    ## fail with the following message:
    ## Error in validObject(.Object) : 
    ##   invalid class “SimpleList” object: invalid object for slot "listData"
    ##   in class "SimpleList": got class "matrix", should be or extend class
    ##   "list"
    if (length(args) == 1L && extends(class(args[[1L]]), "list"))
        args <- args[[1L]]
    new2("SimpleList", listData=args, check=FALSE)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Validity.
###

.valid.SimpleList.listData <- function(x)
{
    elementTypeX <- elementType(x)
    if (!all(sapply(as.list(x),
                    function(xi) extends(class(xi), elementTypeX))))
        return(paste("the 'listData' slot must be a list containing",
                     elementTypeX, "objects"))
    NULL
}
.valid.SimpleList <- function(x)
{
    c(.valid.SimpleList.listData(x))
}
setValidity2("SimpleList", .valid.SimpleList)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### classNameForDisplay()
###

setMethod("classNameForDisplay", "SimpleList",
    function(x) sub("^Simple", "", class(x))
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting.
###

setMethod("extractROWS", "SimpleList",
    function(x, i)
    {
        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        ans_listData <- extractROWS(x@listData, i)
        ans_elementMetadata <- extractROWS(x@elementMetadata, i)
        BiocGenerics:::replaceSlots(x,
            listData=ans_listData,
            elementMetadata=ans_elementMetadata)
    }
)

### The replaced elements in 'x' must get their metadata columns from 'value'.
### See this thread on bioc-devel:
###   https://stat.ethz.ch/pipermail/bioc-devel/2015-November/008319.html
setMethod("replaceROWS", "SimpleList",
    function(x, i, value)
    {
        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        ans_listData <- replaceROWS(x@listData, i, value@listData)
        ans_elementMetadata <- replaceROWS(x@elementMetadata, i,
                                           value@elementMetadata)
        BiocGenerics:::replaceSlots(x,
            listData=ans_listData,
            elementMetadata=ans_elementMetadata)
    }
)

setMethod("getListElement", "SimpleList",
    function(x, i, exact=TRUE)
        getListElement(x@listData, i, exact=exact)
)

setMethod("setListElement", "SimpleList",
    function(x, i, value)
    {
        x@listData[[i]] <- value
        x
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining.
###

## NOTE: while the 'c' function does not have an 'x', the generic does
## c() is a primitive, so 'x' can be missing; dispatch is by position,
## although sometimes this does not work so well, so it's best to keep
## names off the parameters whenever feasible.

#setMethod("c", "SimpleList",
#          function(x, ..., recursive = FALSE) {
#              slot(x, "listData") <-
#                do.call(c, lapply(unname(list(x, ...)), as.list))
#              if (!is.null(mcols(x)))
#                mcols(x) <- rbind.mcols(x, ...)
#              x
#          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Looping.
###

setMethod("lapply", "SimpleList",
          function(X, FUN, ...)
              lapply(as.list(X), FUN = FUN, ...))

setMethod("endoapply", "SimpleList",
          function(X, FUN, ...) {
              FUN <- match.fun(FUN)
              listData <- lapply(X, FUN = FUN, ...)
              elementTypeX <- elementType(X)
              if (!all(sapply(listData,
                              function(Xi) extends(class(Xi), elementTypeX))))
                  stop("all results must be of class '", elementTypeX, "'")
              slot(X, "listData", check=FALSE) <- listData
              X
          })

setMethod("mendoapply", "SimpleList",
          function(FUN, ..., MoreArgs = NULL) {
              X <- list(...)[[1L]]
              elementTypeX <- elementType(X)
              FUN <- match.fun(FUN)
              listData <-
                mapply(FUN = FUN, ..., MoreArgs = MoreArgs, SIMPLIFY = FALSE)
              if (!all(sapply(listData,
                              function(Xi) extends(class(Xi), elementTypeX))))
                  stop("all results must be of class '", elementTypeX, "'")
              slot(X, "listData", check=FALSE) <- listData
              X
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion.
###

.as.list.SimpleList <- function(x, use.names=TRUE)
{
    if (!isTRUEorFALSE(use.names))
        stop("'use.names' must be TRUE or FALSE")
    ans <- x@listData
    if (!use.names)
        names(ans) <- NULL
    ans
}
setMethod("as.list", "SimpleList", .as.list.SimpleList)

setAs("ANY", "SimpleList", function(from) {
  coerceToSimpleList(from)
})

setAs("list", "List", function(from) {
  coerceToSimpleList(from)
})

coerceToSimpleList <- function(from, element.type, ...) {
  if (missing(element.type)) {
    if (is(from, "List"))
      element.type <- from@elementType
    else if (is.list(from))
      element.type <- listElementType(from)
    else element.type <- class(from)
  }
  SimpleListClass <- listClassName("Simple", element.type)
  if (!is(from, SimpleListClass)) {
    listData <- as.list(from)
    if (!is.null(element.type))
      listData <- lapply(listData, coercerToClass(element.type), ...)
    new_SimpleList_from_list(SimpleListClass, listData)
  } else {
    from
  }
}

