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
### DataFrame objects
### -------------------------------------------------------------------------

## A data.frame-like interface for S4 objects that implement length() and `[`

## NOTE: Normal data.frames always have rownames (sometimes as integers),
## but we allow the rownames to be NULL for efficiency. This means that we
## need to store the number of rows (nrows).
setClass("DataFrame",
         representation(
                        rownames = "characterORNULL",
                        nrows = "integer"
                        ),
         prototype(rownames = NULL,
                   nrows = 0L,
                   listData = structure(list(), names = character())),
         contains = c("DataTable", "SimpleList"))

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessor methods.
###

setMethod("nrow", "DataFrame", function(x) x@nrows)

setMethod("ncol", "DataFrame", function(x) length(x))

setMethod("rownames", "DataFrame",
          function(x, do.NULL = TRUE, prefix = "row")
          {
            rn <- x@rownames
            if (is.null(rn) && !do.NULL) {
              nr <- NROW(x)
              if (nr > 0L)
                rn <- paste(prefix, seq_len(nr), sep = "")
              else
                rn <- character(0L)
            }
            rn
          })

setMethod("colnames", "DataFrame",
          function(x, do.NULL = TRUE, prefix = "col")
          {
            if (!identical(do.NULL, TRUE)) warning("do.NULL arg is ignored ",
                "in this method")
            cn <- names(x@listData)
            if (!is.null(cn))
                return(cn)
            if (length(x@listData) != 0L)
                stop("DataFrame object with NULL colnames, please fix it ",
                     "with colnames(x) <- value")
            return(character(0))
          })

setReplaceMethod("rownames", "DataFrame",
                 function(x, value)
                 {
                   if (!is.null(value)) {
                     if (anyMissing(value))
                       stop("missing values not allowed in rownames")
                     if (length(value) != nrow(x))
                       stop("invalid rownames length")
                     if (anyDuplicated(value))
                       stop("duplicate rownames not allowed")
                     if (!is(value, "XStringSet"))
                       value <- as.character(value)
                   }
                   x@rownames <- value
                   x
                 })

setReplaceMethod("colnames", "DataFrame",
                 function(x, value)
                 {
                   if (!is.character(value))
                       stop("'value' must be a character vector ",
                            "in colnames(x) <- value")
                   if (length(value) > length(x))
                     stop("more column names than columns")
                   names(x) <- value
                   x
                 })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Validity.
###


.valid.DataFrame.dim <- function(x)
{
  nr <- dim(x)[1L]
  if (!length(nr) == 1)
    return("length of 'nrows' slot must be 1")
  if (nr < 0)
    return("number of rows must be non-negative")
  NULL
}

.valid.DataFrame.rownames <- function(x)
{
  if (is.null(rownames(x)))
    return(NULL)
  if (length(rownames(x)) != nrow(x))
    return("number of row names and number of rows differ")
  NULL
}

.valid.DataFrame.names <- function(x)
{
  ## DataFrames with no columns can have NULL column name
  if (is.null(names(x)) && ncol(x) != 0)
    return("column names should not be NULL")
  if (length(names(x)) != ncol(x))
    return("number of columns and number of column names differ")
  NULL
}

.valid.DataFrame <- function(x)
{
  c(.valid.DataFrame.dim(x),
    .valid.DataFrame.rownames(x),
    .valid.DataFrame.names(x))
}

setValidity2("DataFrame", .valid.DataFrame)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor.
###

DataFrame <- function(..., row.names = NULL, check.names = TRUE)
{
  ## build up listData, with names from arguments
  if (!isTRUEorFALSE(check.names))
    stop("'check.names' must be TRUE or FALSE")
  nr <- 0
  listData <- list(...)
  varlist <- vector("list", length(listData))
  if (length(listData) > 0) {
    dotnames <- names(listData)
    if (is.null(dotnames)) {
      emptynames <- rep.int(TRUE, length(listData))
    } else {
      emptynames <- !nzchar(dotnames)
    }
    if (any(emptynames)) {
      qargs <- as.list(substitute(list(...)))[-1L]
      dotvalues <- sapply(qargs[emptynames], function(arg) deparse(arg)[1L])
      names(listData)[emptynames] <- dotvalues
    }
    varnames <- as.list(names(listData))
    nrows <- ncols <- integer(length(varnames))
    for (i in seq_along(listData)) {
      element <- try(as(listData[[i]], "DataFrame"), silent = TRUE)
      if (inherits(element, "try-error"))
        stop("cannot coerce class \"", class(listData[[i]]),
             "\" to a DataFrame")
      nrows[i] <- nrow(element)
      ncols[i] <- ncol(element)
      varlist[[i]] <- as.list(element, use.names = FALSE)
      if (!is(listData[[i]], "AsIs")) {
        if (((length(dim(listData[[i]])) > 1) || (ncol(element) > 1)))
          {
            if (emptynames[i])
              varnames[[i]] <- colnames(element)
            else
              varnames[[i]] <- paste(varnames[[i]], colnames(element), sep = ".")
          } else if (is.list(listData[[i]]) && length(names(listData[[i]])))
            varnames[[i]] <- names(element)
      }
      if (is.null(row.names))
        row.names <- rownames(element)
    }
    nr <- max(nrows)
    for (i in which((nrows > 0L) & (nrows < nr) & (nr %% nrows == 0L))) {
      recycle <- rep(seq_len(nrows[i]), length.out = nr)
      varlist[[i]] <- lapply(varlist[[i]], `[`, recycle, drop=FALSE)
      nrows[i] <- nr
    }
    if (!all(nrows == nr))
      stop("different row counts implied by arguments")
    varlist <- unlist(varlist, recursive = FALSE, use.names = FALSE)
    nms <- unlist(varnames[ncols > 0L])
    if (check.names)
      nms <- make.names(nms, unique = TRUE)
    names(varlist) <- nms
  } else names(varlist) <- character(0)

  if (!is.null(row.names)) {
    if (anyMissing(row.names))
      stop("missing values in 'row.names'")
    if (length(varlist) && length(row.names) != nr)
      stop("invalid length of row names")
    if (anyDuplicated(row.names))
      stop("duplicate row names")
    row.names <- as.character(row.names)
  }

  new2("DataFrame", listData=varlist, rownames=row.names,
       nrows=as.integer(max(nr, length(row.names))), check=FALSE)
}

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting.
###

setReplaceMethod("[[", "DataFrame",
                 function(x, i, j,..., value)
                 {
                   nrx <- nrow(x)
                   lv <- NROW(value)
                   if (!missing(j) || length(list(...)) > 0)
                     warning("arguments beyond 'i' ignored")
                   if (missing(i))
                     stop("subscript is missing")
                   if (!is.character(i) && !is.numeric(i))
                     stop("invalid subscript type")
                   if (length(i) < 1L)
                     stop("attempt to select less than one element")
                   if (length(i) > 1L)
                     stop("attempt to select more than one element")
                   if (is.numeric(i) && (i < 1L || i > ncol(x) + 1L))
                     stop("subscript out of bounds")
                   if (!is.null(value) && (nrx != lv)) {
                     if ((nrx == 0) || (lv == 0) || (nrx %% lv != 0))
                       stop(paste(lv, "elements in value to replace",
                                  nrx, "elements"))
                     else
                       value <- rep(value, length.out = nrx)
                   }
                   callNextMethod(x, i, value=value)
                 })

setMethod("extractROWS", "DataFrame",
    function(x, i)
    {
        i <- normalizeSingleBracketSubscript(i, x, exact=FALSE, as.NSBS=TRUE)
        slot(x, "listData", check=FALSE) <-
            lapply(structure(seq_len(ncol(x)), names=names(x)),
                   function(j) extractROWS(x[[j]], i))
        slot(x, "nrows", check=FALSE) <- length(i)
        if (!is.null(rownames(x))) {
            slot(x, "rownames", check=FALSE) <-
                make.unique(extractROWS(rownames(x), i))
        }
        x
    }
)

setMethod("[", "DataFrame",
    function(x, i, j, ..., drop=TRUE)
    {
        if (!isTRUEorFALSE(drop))
            stop("'drop' must be TRUE or FALSE")
        if (length(list(...)) > 0L)
            warning("parameters in '...' not supported")

        ## We do list-style subsetting when [ was called with no ','.
        ## NOTE: matrix-style subsetting by logical matrix not supported.
        list_style_subsetting <- (nargs() - !missing(drop)) < 3L
        if (list_style_subsetting || !missing(j)) {
            if (list_style_subsetting) {
                if (!missing(drop))
                    warning("'drop' argument ignored by list-style subsetting")
                if (missing(i))
                    return(x)
                j <- i
            }
            if (!is(j, "Ranges")) {
                xstub <- setNames(seq_along(x), names(x))
                j <- normalizeSingleBracketSubscript(j, xstub)
            }
            new_listData <- extractROWS(x@listData, j)
            new_mcols <- extractROWS(mcols(x), j)
            x <- initialize(x, listData=new_listData,
                               elementMetadata=new_mcols)
            if (anyDuplicated(names(x)))
                names(x) <- make.names(names(x))
            if (list_style_subsetting)
                return(x)
        }
        if (!missing(i))
            x <- extractROWS(x, i)
        if (missing(drop))  # drop by default if only one column left
            drop <- ncol(x) == 1L
        if (drop) {
            ## one column left
            if (ncol(x) == 1L)
                return(x[[1L]])
            ## one row left
            if (nrow(x) == 1L)
                return(as(x, "list"))
        }
        x
    }
)

setMethod("replaceROWS", "DataFrame",
    function(x, i, value)
    {
        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        x_ncol <- ncol(x)
        value_ncol <- ncol(value)
        if (value_ncol > x_ncol)
            stop("provided ", value_ncol, " variables ",
                 "to replace ", x_ncol, " variables")
        slot(x, "listData", check=FALSE) <-
            lapply(structure(seq_len(ncol(x)), names=names(x)),
                   function(j)
                       replaceROWS(x[[j]], i,
                                   value[[((j - 1L) %% value_ncol) + 1L]]))
        x
    }
)

setReplaceMethod("[", "DataFrame",
                 function(x, i, j, ..., value)
                 {
                   if (length(list(...)) > 0)
                     warning("parameters in '...' not supported")
                   useI <- FALSE
                   newrn <- newcn <- NULL
                   if (nargs() < 4) {
                     if (missing(i)) {
                       j2 <- seq_len(ncol(x))
                     } else {
                       if (length(i) == 1) {
                         if (is.logical(i) == 1 && i)
                             i <- rep(i, ncol(x))
                       }
                       xstub <- setNames(seq_along(x), names(x))
                       j2 <- normalizeSingleBracketSubscript(i, xstub,
                                                             allow.append=TRUE)
                       if (is.character(i))
                           newcn <- i[j2 > ncol(x)]
                     }
                   } else {
                     if (missing(i)) {
                       i2 <- seq_len(nrow(x))
                     } else {
                       useI <- TRUE
                       i2 <- normalizeSingleBracketSubscript(i, x,
                                                             allow.append=TRUE)
                       if (is.character(i))
                           newrn <- i[i2 > nrow(x)]
                     }
                     if (missing(j)) {
                       j2 <- seq_len(ncol(x))
                     } else {
                       xstub <- setNames(seq_along(x), names(x))
                       j2 <- normalizeSingleBracketSubscript(j, xstub,
                                                             allow.append=TRUE)
                       if (is.character(j))
                           newcn <- j[j2 > ncol(x)]
                     }
                     i <- i2
                   }
                   j <- j2
                   if (!length(j)) # nothing to replace
                     return(x)
                   if (is(value, "list") || is(value, "List")) {
                     null <- vapply(value, is.null, logical(1L))
                     if (any(null)) { ### FIXME: data.frame handles gracefully
                       stop("NULL elements not allowed in list value")
                     }
                     value <- as(value, "DataFrame")
                   }
                   if (!is(value, "DataFrame")) {
                     if (useI)
                       li <- length(i)
                     else
                       li <- nrow(x)
                     lv <- length(value)
                     if (lv > 0L && li != lv) {
                       if (li %% lv != 0)
                         stop(paste(lv, "rows in value to replace",
                                    li, " rows"))
                       else
                         value <- rep(value, length.out = li)
                     }
                     ## come up with some default row and col names
                     if (!length(newcn) && max(j) > length(x)) {
                       newcn <- paste("V", seq.int(length(x) + 1L, max(j)),
                                      sep = "")
                       if (length(newcn) != sum(j > length(x)))
                         stop("new columns would leave holes after ",
                              "existing columns")
                     }
                     if (useI) {
                       if (length(newrn) == 0L && li > 0L && max(i) > nrow(x))
                         newrn <- as.character(seq.int(nrow(x) + 1L, max(i)))
                       if (length(x@listData[j][[1]]) == 0L)
                         x@listData[j] <- list(rep(NA, nrow(x)))
                       x@listData[j] <-
                         lapply(x@listData[j], function(y) {y[i] <- value; y})
                     } else {
                       if (is.null(value))
                         x@listData[j] <- NULL
                       else x@listData[j] <- list(value)
                     }
                   } else {
                     vc <- seq_len(ncol(value))
                     if (ncol(value) > length(j))
                       stop("ncol(x[j]) < ncol(value)")
                     if (ncol(value) < length(j))
                       vc <- rep(vc, length.out = length(j))
                     if (useI)
                       li <- length(i)
                     else
                       li <- nrow(x)
                     nrv <- nrow(value)
                     if (li != nrv) {
                       if ((li == 0) || (li %% nrv != 0))
                         stop(paste(nrv, "rows in value to replace",
                                    li, " rows"))
                       else
                         value <-
                           value[rep(seq_len(nrv), length.out = li), ,
                                 drop=FALSE]
                     }
                     ## attempt to derive new row and col names from value
                     if (!length(newcn) && max(j) > length(x)) {
                       newcn <- rep(names(value), length.out = length(j))
                       newcn <- newcn[j > length(x)]
                     }
                     if (useI) {
                       if (length(newrn) == 0L && li > 0L && max(i) > nrow(x)) {
                         if (!is.null(rownames(value))) {
                           newrn <- rep(rownames(value), length.out = length(i))
                           newrn <- newrn[i > nrow(x)]
                         } else newrn <-
                           as.character(seq.int(nrow(x) + 1L, max(i)))
                       }
                       for (k in seq_len(length(j))) {
                         if (j[k] > length(x))
                           v <- NULL
                         else v <- x@listData[[j[k]]]
                         rv <- value[[vc[k]]]
                         if (length(dim(rv)) == 2)
                           v[i,] <- rv
                         else v[i] <- if (is.null(v)) rv else as(rv, class(v))
                         x@listData[[j[k]]] <- v
                       }
                     } else {
                       if (is.logical(j)) {
                         for (k in seq_len(length(j)))
                           x@listData[[k]] <- value[[vc[k]]]
                       } else {
                         for (k in seq_len(length(j)))
                           x@listData[[j[k]]] <- value[[vc[k]]]
                       }
                     }
                   }
                   ## update row and col names, making them unique
                   if (length(newcn)) {
                     oldcn <- head(colnames(x), length(x) - length(newcn))
                     colnames(x) <- make.unique(c(oldcn, newcn))
                     if (!is.null(mcols(x)))
                       mcols(x)[tail(names(x),length(newcn)),] <-
                         DataFrame(NA)
                   }
                   if (length(newrn)) {
                     notj <- setdiff(seq_len(ncol(x)), j)
                     x@listData[notj] <-
                       lapply(x@listData[notj],
                              function(y) c(y, rep(NA, length(newrn))))
                     x@rownames <- make.unique(c(rownames(x), newrn))
                   }
                   x@nrows <- length(x[[1]]) # we should always have a column
                   x
                 })

hasNonDefaultMethod <- function(f, signature) {
  any(selectMethod(f, signature)@defined != "ANY")
}

hasS3Method <- function(f, signature) {
  !is.null(getS3method(f, signature, optional=TRUE))
}

droplevels.DataFrame <- function(x, except=NULL) {
  canDropLevels <- function(xi) {
    hasNonDefaultMethod(droplevels, class(xi)) ||
      hasS3Method("droplevels", class(xi))
  }
  drop.levels <- vapply(x, canDropLevels, NA)
  if (!is.null(except)) 
    drop.levels[except] <- FALSE
  x@listData[drop.levels] <- lapply(x@listData[drop.levels], droplevels)
  x
}
setMethod("droplevels", "DataFrame", droplevels.DataFrame)

setMethod("rep", "DataFrame", function(x, ...) {
  x[rep(seq_len(nrow(x)), ...),,drop=FALSE]
})

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion.
###

## Break DataFrame into a normal R data.frame
setAs("DataFrame", "data.frame",
      function(from) {
        as.data.frame(from, optional=TRUE)
      })

injectIntoScope <- function(x, ...) {
  nms <- sapply(tail(substitute(list(...)), -1), deparse)
  environment(x) <- list2env(setNames(list(...), nms), parent = environment(x))
  x
}

.as.data.frame.DataFrame <- function(x, row.names=NULL, optional=FALSE, ...)
{
    if (length(list(...)))
        warning("Arguments in '...' ignored")
    l <- as(x, "list")
    if (is.null(row.names))
        row.names <- rownames(x)
    if (!length(l) && is.null(row.names))
        row.names <- seq_len(nrow(x))
    l <- lapply(l,
                function(y) {
                    if (is(y, "SimpleList") || is(y, "CompressedList"))
                        y <- as.list(y)
                    if (is.list(y))
                        y <- I(y)
                    y
                })
    IRanges.data.frame <- injectIntoScope(data.frame, as.data.frame)
    do.call(IRanges.data.frame,
            c(l, list(row.names=row.names),
              check.names=!optional, stringsAsFactors=FALSE))
}
setMethod("as.data.frame", "DataFrame", .as.data.frame.DataFrame)

setMethod("as.matrix", "DataFrame", function(x) {
  if (length(x) == 0L)
    m <- matrix(logical(), nrow = nrow(x), ncol = 0L)
  else m <- do.call(cbind, as.list(x))
  rownames(m) <- rownames(x)
  m
})

## take data.frames to DataFrames
setAs("data.frame", "DataFrame",
      function(from) {
        rn <- attributes(from)[["row.names"]]
        if (is.integer(rn))
          rn <- NULL
        nr <- nrow(from)
### FIXME: this should be:
        ## from <- as.list(from)
### But unclass() causes deep copy
        attr(from, "row.names") <- NULL
        class(from) <- NULL
        new2("DataFrame", listData=from, nrows=nr, rownames=rn, check=FALSE)
      })

setAs("table", "DataFrame",
      function(from) {
        df <- as.data.frame(from)
        factors <- sapply(df, is.factor)
        factors[1] <- FALSE
        do.call(DataFrame, c(df[1], lapply(df[factors], Rle), df["Freq"]))
      })

setOldClass(c("xtabs", "table"))
setAs("xtabs", "DataFrame",
      function(from) {
        class(from) <- "table"
        as(from, "DataFrame")
      })

.defaultAsDataFrame <- function(from) {
  if (length(dim(from)) == 2L) {
    df <- as.data.frame(from)
    if (0L == ncol(from))
      ## colnames on matrix with 0 columns are 'NULL'
      names(df) <- character()
    as(df, "DataFrame")
  } else {
    row.names <- if (!anyDuplicated(names(from))) names(from) else NULL
    new2("DataFrame", listData = setNames(list(from), "X"),
         nrows = length(from), rownames = row.names, check=FALSE)
  }
}

setAs("ANY", "DataFrame", .defaultAsDataFrame)

.VectorAsDataFrame <- function(from) {
  ans <- .defaultAsDataFrame(from)
  if (!is.null(mcols(from))) {
    ans <- cbind(ans, mcols(from))
  }
  ans
}

## overriding the default inheritance-based coercion from methods package
setAs("SimpleList", "DataFrame", .VectorAsDataFrame)
setAs("Vector", "DataFrame", .VectorAsDataFrame)

## note that any element named 'row.names' will be interpreted differently
## is this a bug or a feature?
setAs("list", "DataFrame",
      function(from) {
        do.call(DataFrame, c(from, check.names = FALSE))
      })

setAs("NULL", "DataFrame", function(from) as(list(), "DataFrame"))

### FIXME: only exists due to annoying S4 warning due to its caching of
### coerce methods.
setAs("integer", "DataFrame",
      function(from) {
        selectMethod("coerce", c("vector", "DataFrame"))(from)
      })

setAs("AsIs", "DataFrame",
      function(from) {
        df <- new2("DataFrame", nrows = NROW(from), check=FALSE)
        df[[1]] <- from
        df
      })

setAs("ANY", "AsIs", function(from) I(from))

setAs("ANY", "DataTableORNULL", function(from) as(from, "DataFrame"))

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining.
###

cbind.DataFrame <- function(..., deparse.level = 1) {
  ans <- DataFrame(...)
  mcols(ans) <- rbind_mcols(...)
  ans
}

setMethod("cbind", "DataFrame", cbind.DataFrame)

rbind.DataFrame <- function(..., deparse.level = 1) {
  do.call(rbind, lapply(list(...), as, "DataFrame"))
}

setMethod("rbind", "DataFrame", function(..., deparse.level=1) {
  args <- list(...)
  hasrows <- unlist(lapply(args, nrow), use.names=FALSE) > 0L
  hascols <- unlist(lapply(args, ncol), use.names=FALSE) > 0L

  if (!any(hasrows | hascols)) {
    return(DataFrame())
  } else if (!any(hasrows)) {
    return(args[[which(hascols)[1L]]])
  } else if (sum(hasrows) == 1) {
    return(args[[which(hasrows)]])
  } else {
    args <- args[hasrows]
  }

  df <- args[[1L]]

  for (i in 2:length(args)) {
    if (ncol(df) != ncol(args[[i]]))
      stop("number of columns for arg ", i, " do not match those of first arg")
    if (!identical(colnames(df), colnames(args[[i]])))
      stop("column names for arg ", i, " do not match those of first arg")
  }

  if (ncol(df) == 0) {
    ans <- DataFrame()
    ans@nrows <- sum(unlist(lapply(args, nrow), use.names=FALSE))
  } else {
    cols <- lapply(colnames(df), function(cn) {
      cols <- lapply(args, `[[`, cn)
      isRle <- vapply(cols, is, logical(1L), "Rle")
      if (any(isRle) && !all(isRle)) { # would fail dispatch to c,Rle
        cols[isRle] <- lapply(cols[isRle], decodeRle)
      }
      isFactor <- vapply(cols, is.factor, logical(1L))
      if (any(isFactor)) {
        cols <- lapply(cols, as.factor)
        levs <- unique(unlist(lapply(cols, levels), use.names=FALSE))
        cols <- lapply(cols, factor, levs)
      }
      rectangular <- length(dim(cols[[1]])) == 2L
      if (rectangular) {
        combined <- do.call(rbind, unname(cols))
      } else {
        combined <- do.call(c, unname(cols))
      }
      if (any(isFactor))
        combined <- structure(combined, class="factor", levels=levs)
      combined
    })
    names(cols) <- colnames(df)
    ans <- new2("DataFrame", listData = cols, nrows = NROW(cols[[1]]),
                             check = FALSE)
  }

  rn <- unlist(lapply(args, rownames), use.names=FALSE)
  if (!is.null(rn)) {
    if (length(rn) != nrow(ans)) {
      rn <- NULL
    } else if (anyDuplicated(rn))
      rn <- make.unique(rn, sep = "")
  }
  rownames(ans) <- rn

  if (!is.null(mcols(df))) {
    df_mcols <- mcols(df)
    if (all(sapply(args, function(x) identical(mcols(x), df_mcols))))
      mcols(ans) <- df_mcols
  }

  ans
})

