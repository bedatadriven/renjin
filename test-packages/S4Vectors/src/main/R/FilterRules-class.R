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
### FilterRules objects
### -------------------------------------------------------------------------

setClassUnion("expressionORfunction", c("expression", "function"))

setClass("FilterRules", representation(active = "logical"),
         prototype(elementType = "expressionORfunction"),
         contains = "SimpleList")

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessors.
###

setGeneric("active", function(x) standardGeneric("active"))

setMethod("active", "FilterRules", function(x) {
  a <- x@active
  names(a) <- names(x)
  a
})

setGeneric("active<-", signature="x",
    function(x, value) standardGeneric("active<-")
)

setReplaceMethod("active", "FilterRules", function(x, value) {
  if (is.numeric(value)) {
    value <- as.integer(value)[!is.na(value)]
    if (any(value < 1) || any(value > length(x)))
      stop("filter index out of range")
    value <- names(x)[value]
  }
  if (is.character(value)) {
    value <- value[!is.na(value)] ## NA's are dropped
    filterNames <- names(x)
    if (length(filterNames) == 0)
      stop("there are no filter names")
    if (any(!(value %in% filterNames)))
      stop("'value' contains invalid filter names")
    x@active <- filterNames %in% value
    x
  } else if (is.logical(value)) {
    nfilters <- length(x)
    if (length(value) > nfilters)
      stop("length of 'value' must not be greater than that of 'filters'")
    if (anyMissing(value))
      stop("'value' cannot contain NA's")
    if (nfilters && (nfilters %% length(value) != 0))
      stop("number of filters not a multiple of 'value' length")
    x@active <- rep(value, length.out = nfilters)
    x
  } else stop("unsupported type of 'value'")
})

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor.
###

FilterRules.parseRule <- function(expr) {
  if (is.character(expr)) {
    expr <- try(parse(text = expr, srcfile = NULL), silent = TRUE)
    if (is.character(expr))
      stop("failed to parse filter expression: ", expr)
    expr
  } else if (is.language(expr) || is.logical(expr))
    as.expression(expr)
  else if (is.function(expr))
    as(expr, "FilterClosure")
  else stop("would not evaluate to logical: ", expr)
}

## takes logical expressions, character vectors, or functions to parse

FilterRules <- function(exprs = list(), ..., active = TRUE) {
  exprs <- c(as.list(substitute(list(...)))[-1L], exprs)
  if (length(names(exprs)) == 0) {
    funs <- as.logical(sapply(exprs, is.function))
    nonfuns <- exprs[!funs]
    names(nonfuns) <- unlist(lapply(nonfuns, deparse))
    chars <- as.logical(sapply(nonfuns, is.character))
    names(nonfuns)[chars] <- unlist(nonfuns[chars])
    names(exprs)[!funs] <- names(nonfuns)
  }
  exprs <- lapply(exprs, FilterRules.parseRule)

  active <- rep(active, length.out = length(exprs))

  if (!is.logical(active) || anyMissing(active))
    stop("'active' must be logical without any missing values")
  if (length(active) > length(exprs))
    stop("length of 'active' is greater than number of rules")
  if (length(exprs) && length(exprs) %% length(active) > 0)
    stop("number of rules must be a multiple of length of 'active'")

  ans <- new_SimpleList_from_list("FilterRules", exprs,
                                  active = active)
  validObject(ans)
  ans
}

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting.
###

setReplaceMethod("[[", "FilterRules",
                 function(x, i, j, ..., value)
                 {
                   if (!missing(j) || length(list(...)) > 0)
                     warning("arguments beyond 'i' ignored")
                   if (missing(i))
                     stop("subscript is missing")
                   rule <- FilterRules.parseRule(value)
                   x <- callNextMethod(x, i, value = rule)
                   if (is.numeric(i) && is.character(value))
                     names(x)[i] <- value
                   active <- x@active ## in case we expanded
                   names(active) <- names(x)[seq_along(active)]
                   active[[i]] <- TRUE
                   names(active) <- NULL
                   x@active <- active
                   names(x) <- make.names(names(x), unique = TRUE)
                   x
                 })

setMethod("[", "FilterRules",
          function(x, i, j, ..., drop)
          {
            if (!missing(j) || length(list(...)) > 0)
              stop("invalid subsetting")
            if (!missing(i)) {
              x@active <- setNames(setNames(x@active, names(x))[i], NULL)
              x <- callNextMethod(x, i)
            }
            x
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Validity.
###

.valid.FilterRules.active <- function(x) {
  if (length(active(x)) != length(x))
    "length of 'active' must match length of 'filters'"
  else if (!identical(names(active(x)), names(x)))
    "names of 'active' must match those of 'filters'"
  else if (anyMissing(active(x)))
    "'active' cannot contain NA's"
  else NULL
}

.valid.FilterRules.rules <- function(x) {
  unlist(lapply(x, function(rule) {
    if (is.function(rule) && length(formals(rule)) < 1)
      "function rule must take at least one parameter"
    else NULL
  }))
}

.valid.FilterRules <- function(x)
  c(.valid.FilterRules.active(x), .valid.FilterRules.rules(x))

setValidity2("FilterRules", .valid.FilterRules)

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining
###

setMethod("append", c("FilterRules", "FilterRules"),
          function(x, values, after=length(x))
          {
            if (!isSingleNumber(after))
              stop("'after' must be a single number")
            ans <-
              FilterRules(append(as.list(x, use.names = TRUE),
                                 as.list(values, use.names = TRUE),
                                 after = after))
            active(ans) <-
              structure(append(active(x), active(values), after),
                        names = names(ans))
            mcols(ans) <- rbind(mcols(x), mcols(values))
            ans
          })

setMethod("c", "FilterRules",
          function(x, ..., recursive = FALSE) {
            if (!identical(recursive, FALSE))
              stop("\"c\" method for FilterRules objects ",
                   "does not support the 'recursive' argument")
            if (missing(x))
              args <- unname(list(...))
            else
              args <- unname(list(x, ...))
            args <- lapply(args, as, "FilterRules")              
            ans <-
              FilterRules(unlist(lapply(args,
                                        function(x) {
                                          elts <- as.list(x)
                                          names(elts) <- names(x)
                                          elts
                                        }), recursive = FALSE))
            active(ans) <-
              structure(unlist(lapply(args, active), use.names = FALSE),
                        names = names(ans))
            mcols(ans) <- do.call(rbind, lapply(args, mcols))
            ans
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Evaluating
###

setMethod("eval", signature(expr="FilterRules", envir="ANY"),
          function(expr, envir = parent.frame(),
                   enclos = if(is.list(envir) || is.pairlist(envir))
                   parent.frame() else baseenv())
          {
            result <- rep.int(TRUE, NROW(envir))
            rules <- as.list(expr)[active(expr)]
            for (i in seq_along(rules)) {
              rule <- rules[[i]]
              val <- tryCatch(if (is.expression(rule))
                                  eval(rule, envir, enclos)
                              else rule(envir),
                              error = function(e) {
                                  stop("Filter '", names(rules)[i], "' failed: ",
                                       e$message)
                              })
              if (is(val, "Rle"))
                val <- as.vector(val)
              if (!is.logical(val))
                stop("filter rule evaluated to non-logical: ",
                     names(rules)[i])
              if ((NROW(envir) == 0L && length(val) > 0L) ||
                  (NROW(envir) > 0L && length(val) == 0L) || 
                  (NROW(envir) > 0L &&
                   (max(NROW(envir), length(val)) %%
                    min(NROW(envir), length(val)) != 0)))
                stop("filter rule evaluated to inconsistent length: ",
                     names(rule)[i])
              if (length(rules) > 1L)
                envir <- extractROWS(envir, val)
              result[result] <- val
            }
            result
          })

setGeneric("evalSeparately",
           function(expr, envir = parent.frame(),
                    enclos = if (is.list(envir) ||
                      is.pairlist(envir)) parent.frame() else baseenv(),
                    ...)
           standardGeneric("evalSeparately"))

setMethod("evalSeparately", "FilterRules",
          function(expr, envir = parent.frame(),
                   enclos = if (is.list(envir) ||
                     is.pairlist(envir)) parent.frame() else baseenv(),
                   serial = FALSE)
          {
            if (!isTRUEorFALSE(serial))
              stop("'serial' must be TRUE or FALSE")
            inds <- seq_len(length(expr))
            names(inds) <- names(expr)
            passed <- rep.int(TRUE, NROW(envir))
            m <- do.call(cbind, lapply(inds, function(i) {
              result <- eval(expr[i], envir = envir, enclos = enclos)
              if (serial) {
                envir <<- subset(envir, .(result))
                passed[passed] <<- result
                passed
              } else result
            }))
            FilterMatrix(matrix = m, filterRules = expr)
          })

setGeneric("subsetByFilter",
           function(x, filter, ...) standardGeneric("subsetByFilter"))

setMethod("subsetByFilter", c("ANY", "FilterRules"), function(x, filter) {
  extractROWS(x, eval(filter, x))
})

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Summary
###

setMethod("summary", "FilterRules",
          function(object, subject, serial = FALSE, discarded = FALSE,
                   percent = FALSE)
          {
            if (!isTRUEorFALSE(serial))
              stop("'serial' must be TRUE or FALSE")
            mat <- evalSeparately(object, subject, serial = serial)
            summary(mat, discarded = discarded, percent = percent)
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### FilterRule closures
###

setClass("FilterClosure", contains = "function")

setClass("GenericFilterClosure", contains = "FilterClosure")

setClass("StandardGenericFilterClosure",
         contains = c("GenericFilterClosure", "standardGeneric"))

setAs("standardGeneric", "FilterClosure", function(from) {
          new("StandardGenericFilterClosure", from)
      })

setAs("function", "FilterClosure", function(from) {
          new("FilterClosure", from)
      })

setGeneric("params", function(x, ...) standardGeneric("params"))

setMethod("params", "FilterClosure", 
          function(x) {
            as.list(environment(x))
          })

setMethod("show", "FilterClosure", function(object) {
  p <- params(object)
  cat("filter (",
      paste(names(p), "=", sapply(p, deparse, control = NULL),
            collapse = ", "),
      ")\n", sep = "")
  print(body(object))
})

### ------------------------------------------------------------------------- 
### FilterMatrix: coordinates results from multiple filters 
###

.valid.FilterMatrix <- function(object)
{
  c(if (!is.logical(object))
      "values must be logical",
    if (!is.null(names(filterRules)))
      "filterRules must not be named",
    if (length(object@filterRules) != ncol(object))
      "length(filterRules) must equal ncol(object)") 
}

setClass("FilterMatrix", representation(filterRules = "FilterRules"),
         contains = "matrix",
         validity = .valid.FilterMatrix)

setGeneric("filterRules", function(x, ...) standardGeneric("filterRules"))

setMethod("filterRules", "FilterMatrix", function(x) {
  setNames(x@filterRules, colnames(x))
})

setMethod("[", "FilterMatrix", function(x, i, j, ..., drop = TRUE) {
  if (!missing(i))
    i <- as.vector(i)
  if (!missing(j))
    j <- as.vector(j)
  ans <- callNextMethod()
  if (is.matrix(ans)) {
    filterRules <- filterRules(x)
    if (!missing(j))
      filterRules <- filterRules[j]
    ans <- FilterMatrix(matrix = ans, filterRules = filterRules)
  }
  ans
})

setMethod("rbind", "FilterMatrix", function(..., deparse.level = 1) {
  args <- list(...)
  ans <- do.call(rbind, lapply(args, as, "matrix"))
  rulesList <- lapply(args, filterRules)
  if (any(!sapply(rulesList, identical, rulesList[[1]])))
    stop("cannot rbind filter matrices with non-identical rule sets")
  FilterMatrix(matrix = ans, filterRules = rulesList[[1]])
})

setMethod("cbind", "FilterMatrix", function(..., deparse.level = 1) {
  args <- list(...)
  ans <- do.call(cbind, lapply(args, as, "matrix"))
  rules <- do.call(c, lapply(args, function(x) x@filterRules))
  FilterMatrix(matrix = ans, filterRules = rules)
})

FilterMatrix <- function(matrix, filterRules) {
  stopifnot(ncol(matrix) == length(filterRules))  
  if (is.null(colnames(matrix)))
    colnames(matrix) <- names(filterRules)
  else if (!is.null(names(filterRules)) &&
           !identical(names(filterRules), colnames(matrix)))
    stop("if names(filterRules) and colnames(matrix) are both not NULL,",
         " the names must match")
  names(filterRules) <- NULL
  new("FilterMatrix", matrix, filterRules = filterRules)
}

setMethod("show", "FilterMatrix", function(object) {
  cat(class(object), " (", nrow(object), " x ", ncol(object), ")\n", sep = "")
  mat <- makePrettyMatrixForCompactPrinting(object, function(x) x@.Data)
  print(mat, quote = FALSE, right = TRUE)
})

setMethod("summary", "FilterMatrix",
          function(object, discarded = FALSE, percent = FALSE)
          {
            if (!isTRUEorFALSE(discarded))
              stop("'discarded' must be TRUE or FALSE")
            if (!isTRUEorFALSE(percent))
              stop("'percent' must be TRUE or FALSE")
            counts <- c("<initial>" = nrow(object), colSums(object),
                        "<final>" = sum(rowSums(object) == ncol(object)))
            if (discarded) {
              counts <- nrow(object) - counts
            }
            if (percent) {
              round(counts / nrow(object), 3)
            } else counts
          })
