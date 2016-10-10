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
### Common operations on Rle objects
### -------------------------------------------------------------------------
###

 
### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Group generic methods
###

.sumprodRle <- function(e1, e2, na.rm = FALSE)
{
    n1 <- length(e1)
    n2 <- length(e2)
    if (n1 == 0 || n2 == 0) {
        ends <- integer(0)
        which1 <- integer(0)
        which2 <- integer(0)
    } else {
        n <- max(n1, n2)
        if (max(n1, n2) %% min(n1, n2) != 0)
            warning("longer object length is not a multiple of shorter object length")
        if (n1 < n)
            e1 <- rep(e1, length.out = n)
        if (n2 < n)
            e2 <- rep(e2, length.out = n)
        # ends <- sort(unique(c(end(e1), end(e2))))
        ends <- sortedMerge(end(e1), end(e2))
        which1 <- findIntervalAndStartFromWidth(ends, runLength(e1))[["interval"]]
        which2 <- findIntervalAndStartFromWidth(ends, runLength(e2))[["interval"]]
    }
    lengths <- diffWithInitialZero(ends)
    values <- runValue(e1)[which1] * runValue(e2)[which2]
    sum(lengths * values, na.rm = na.rm)
}

setMethod("Ops", signature(e1 = "Rle", e2 = "Rle"),
          function(e1, e2)
          {
              n1 <- length(e1)
              n2 <- length(e2)
              if (n1 == 0 || n2 == 0) {
                  ends <- integer(0)
                  which1 <- integer(0)
                  which2 <- integer(0)
              } else {
                  n <- max(n1, n2)
                  if (max(n1, n2) %% min(n1, n2) != 0)
                      warning("longer object length is not a multiple of shorter object length")
                  if (n1 < n)
                      e1 <- rep(e1, length.out = n)
                  if (n2 < n)
                      e2 <- rep(e2, length.out = n)
                  # ends <- sort(unique(c(end(e1), end(e2))))
                  ends <- sortedMerge(end(e1), end(e2))
                  which1 <- findIntervalAndStartFromWidth(ends, runLength(e1))[["interval"]]
                  which2 <- findIntervalAndStartFromWidth(ends, runLength(e2))[["interval"]]
              }
              new_Rle(callGeneric(runValue(e1)[which1], runValue(e2)[which2]),
                      diffWithInitialZero(ends),
                      check=FALSE)
          })

setMethod("Ops", signature(e1 = "Rle", e2 = "vector"),
          function(e1, e2) callGeneric(e1, Rle(e2)))

setMethod("Ops", signature(e1 = "vector", e2 = "Rle"),
          function(e1, e2) callGeneric(Rle(e1), e2))

setMethod("Math", "Rle",
          function(x)
              switch(.Generic,
                     cumsum =
                     {
                         whichZero <- which(runValue(x) == 0)
                         widthZero <- runLength(x)[whichZero]
                         startZero <- cumsum(c(1L, runLength(x)))[whichZero]
                         y <- x
                         y@lengths[y@values == 0] <- 1L
                         values <- cumsum(as.vector(y))
                         lengths <- rep.int(1L, length(values))
                         lengths[startZero - c(0L, cumsum(head(widthZero, -1) - 1L))] <- widthZero
                         new_Rle(values, lengths, check=FALSE)
                     },
                     cumprod =
                     {
                         whichOne <- which(runValue(x) == 0)
                         widthOne <- runLength(x)[whichOne]
                         startOne <- cumsum(c(1L, runLength(x)))[whichOne]
                         y <- x
                         y@lengths[y@values == 0] <- 1L
                         values <- cumprod(as.vector(y))
                         lengths <- rep.int(1L, length(values))
                         lengths[startOne - c(0L, cumsum(head(widthOne, -1) - 1L))] <- widthOne
                         new_Rle(values, lengths, check=FALSE)
                     },
                     new_Rle(callGeneric(runValue(x)),
                             runLength(x), check=FALSE)))

setMethod("Math2", "Rle",
          function(x, digits)
          {
              if (missing(digits))
                  digits <- ifelse(.Generic == "round", 0, 6)
              new_Rle(callGeneric(runValue(x), digits = digits),
                      runLength(x), check=FALSE)
          })

setMethod("Summary", "Rle",
    function(x, ..., na.rm = FALSE)
    {
        switch(.Generic,
        all =, any =, min =, max =, range =
            callGeneric(runValue(x), ..., na.rm=na.rm),
        sum = 
            withCallingHandlers({
                sum(runValue(x) * runLength(x), ..., na.rm=na.rm)
            }, warning=function(warn) {
                msg <- conditionMessage(warn)
                exp <- gettext("integer overflow - use sum(as.numeric(.))",
                               domain="R")
                if (msg == exp) {
                    msg <- sub("sum\\(as.numeric\\(.\\)\\)",
                               "runValue(.) <- as.numeric(runValue(.))", msg)
                    warning(simpleWarning(msg, conditionCall(warn)))
                    invokeRestart("muffleWarning")
                } else {
                    warn
                }
            }), 
        prod = prod(runValue(x) ^ runLength(x), ..., na.rm=na.rm))
    }
) 

setMethod("Complex", "Rle",
          function(z)
              new_Rle(callGeneric(runValue(z)), runLength(z), check=FALSE))

### S3/S4 combo for summary.Rle
summary.Rle <- function(object, ..., digits=max(3, getOption("digits") - 3)) 
{
    value <-
        if (is.logical(runValue(object))) 
            c(ValueMode = "logical", {
                tb <- table(object, exclude = NULL)
                if (!is.null(n <- dimnames(tb)[[1L]]) && any(iN <- is.na(n)))
                    dimnames(tb)[[1L]][iN] <- "NA's"
                tb
            })
        else if (is.numeric(runValue(object))) {
            nas <- is.na(object)
            object <- object[!nas]
            qq <- quantile(object)
            qq <- signif(c(qq[1L:3L], mean(object), qq[4L:5L]), digits)
            names(qq) <-
                c("Min.", "1st Qu.", "Median", "Mean", "3rd Qu.", "Max.")
            if (any(nas)) 
                c(qq, `NA's` = sum(nas))
            else
                qq
        }
        else
            c(Length = length(object),
              Class = class(object),
              ValueMode = mode(runValue(object)))
    class(value) <- c("summaryDefault", "table")
    value
}
setMethod("summary", "Rle", summary.Rle)

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other logical data methods
###

setMethod("!", "Rle",
          function(x)
              new_Rle(!runValue(x), runLength(x), check=FALSE))

setMethod("which", "Rle",
          function(x, arr.ind = FALSE) {
              if (!is.logical(runValue(x)))
                  stop("argument to 'which' is not logical")
              ok <- runValue(x)
              ok[is.na(ok)] <- FALSE
              from <- start(x)[ok]
              to <- end(x)[ok]
              if (length(from) == 0)
                  integer(0)
              else mseq(from, to)
          })

setMethod("which.max", "Rle",
          function(x) {
            start(x)[which.max(runValue(x))]
          })

## base::ifelse works fine for S4 'test', but not for S4 yes/no
.ifelse_generic_defunct_msg <- c(
    "  The \"ifelse\" methods for Rle objects are defunct. Please use",
    "\n\n      as(ifelse(test, as.vector(yes), as.vector(no)), \"Rle\")",
    "\n\n  instead."
)
setMethod("ifelse", c(yes = "Rle"), function(test, yes, no) 
            .Defunct(msg=.ifelse_generic_defunct_msg))
setMethod("ifelse", c(no = "Rle"), function(test, yes, no) 
            .Defunct(msg=.ifelse_generic_defunct_msg))
setMethod("ifelse", c(yes = "Rle", no = "Rle"), function(test, yes, no) 
            .Defunct(msg=.ifelse_generic_defunct_msg))

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other numerical data methods
###

diff.Rle <- function(x, ...) diff(x, ...)
.diff.Rle <- function(x, lag = 1, differences = 1)
{
    if (!isSingleNumber(lag) || lag < 1L ||
        !isSingleNumber(differences) || differences < 1L) 
        stop("'lag' and 'differences' must be integers >= 1")
    lag <- as.integer(lag)
    differences <- as.integer(differences)
    if (lag * differences >= length(x))
        return(Rle(vector(class(runValue(x)))))
    for (i in seq_len(differences)) {
        n <- length(x)
        x <- window(x, 1L + lag, n) - window(x, 1L, n - lag)
    }
    x
}
setMethod("diff", "Rle", .diff.Rle)

.psummary.Rle <- function(FUN, ..., MoreArgs = NULL) {
    args <- list(...)
    ends <- end(args[[1L]])
    if (length(args) > 1) {
        for (i in 2:length(args))
            ends <- sortedMerge(ends, end(args[[i]]))
    }
    new_Rle(do.call(FUN,
                c(lapply(args,
                         function(x) {
                             runs <- findIntervalAndStartFromWidth(ends,
                                         runLength(x))[["interval"]]
                             runValue(x)[runs]
                         }),
                 MoreArgs)),
            diffWithInitialZero(ends), check=FALSE)
}

setMethod("pmax", "Rle", function(..., na.rm = FALSE)
            .psummary.Rle(pmax, ..., MoreArgs = list(na.rm = na.rm)))

setMethod("pmin", "Rle", function(..., na.rm = FALSE)
            .psummary.Rle(pmin, ..., MoreArgs = list(na.rm = na.rm)))

setMethod("pmax.int", "Rle", function(..., na.rm = FALSE)
            .psummary.Rle(pmax.int, ..., MoreArgs = list(na.rm = na.rm)))

setMethod("pmin.int", "Rle", function(..., na.rm = FALSE)
            .psummary.Rle(pmin.int, ..., MoreArgs = list(na.rm = na.rm)))

### S3/S4 combo for mean.Rle
.mean.Rle <- function(x, na.rm = FALSE)
{
    if (is.integer(runValue(x)))
        runValue(x) <- as.double(runValue(x))
    if (na.rm)
        n <- length(x) - sum(runLength(x)[is.na(runValue(x))])
    else
        n <- length(x)
    sum(x, na.rm = na.rm) / n
}
mean.Rle <- function(x, ...) .mean.Rle(x, ...)
setMethod("mean", "Rle", .mean.Rle)

setMethod("var", signature = c(x = "Rle", y = "missing"),
          function(x, y = NULL, na.rm = FALSE, use)
          {
              if (na.rm)
                  n <- length(x) - sum(runLength(x)[is.na(runValue(x))])
              else
                  n <- length(x)
              centeredValues <- runValue(x) - mean(x, na.rm = na.rm)
              sum(runLength(x) * centeredValues * centeredValues,
                  na.rm = na.rm) / (n - 1)
          })

setMethod("var", signature = c(x = "Rle", y = "Rle"),
          function(x, y = NULL, na.rm = FALSE, use)
          {
              # Direct change to slots for fast computation
              x@values <- runValue(x) - mean(x, na.rm = na.rm)
              y@values <- runValue(y) - mean(y, na.rm = na.rm)
              z <- x * y
              if (na.rm)
                  n <- length(z) - sum(runLength(z)[is.na(runValue(z))])
              else
                  n <- length(z)
              sum(z, na.rm = na.rm) / (n - 1)
          })

setMethod("cov", signature = c(x = "Rle", y = "Rle"),
          function(x, y = NULL, use = "everything",
                   method = c("pearson", "kendall", "spearman"))
          {
              use <-
                match.arg(use,
                          c("all.obs", "complete.obs", "pairwise.complete.obs",
                            "everything", "na.or.complete"))
              method <- match.arg(method)
              if (method != "pearson")
                  stop("only 'pearson' method is supported for Rle objects")
              na.rm <-
                use %in% c("complete.obs", "pairwise.complete.obs", "na.or.complete")
              if (use == "all.obs" && (anyMissing(x) || anyMissing(y)))
                  stop("missing observations in cov/cor")
              var(x, y, na.rm = na.rm)
          })

setMethod("cor", signature = c(x = "Rle", y = "Rle"),
          function(x, y = NULL, use = "everything",
                   method = c("pearson", "kendall", "spearman"))
          {
              use <-
                match.arg(use,
                          c("all.obs", "complete.obs", "pairwise.complete.obs",
                            "everything", "na.or.complete"))
              method <- match.arg(method)
              if (method != "pearson")
                  stop("only 'pearson' method is supported for Rle objects")
              na.rm <-
                use %in% c("complete.obs", "pairwise.complete.obs", "na.or.complete")
              isMissing <- is.na(x) | is.na(y)
              if (any(isMissing)) {
                  if (use == "all.obs") {
                      stop("missing observations in cov/cor")
                  } else if (na.rm) {
                      x <- x[!isMissing]
                      y <- y[!isMissing]
                  }
              }
              # Direct change to slots for fast computation
              x@values <- runValue(x) - mean(x, na.rm = na.rm)
              y@values <- runValue(y) - mean(y, na.rm = na.rm)
              .sumprodRle(x, y, na.rm = na.rm) /
                  (sqrt(sum(runLength(x) * runValue(x) * runValue(x),
                            na.rm = na.rm)) *
                   sqrt(sum(runLength(y) * runValue(y) * runValue(y),
                            na.rm = na.rm)))
         })

setMethod("sd", signature = c(x = "Rle"),
          function(x, na.rm = FALSE) sqrt(var(x, na.rm = na.rm)))

### S3/S4 combo for median.Rle
### FIXME: code duplication needed for S3 / S4 dispatch
### drop NA's here, so dropRle==TRUE allows x[FALSE][NA] in median.default
median.Rle <- function(x, na.rm = FALSE)
{
    if (na.rm)
        x <- x[!is.na(x)]
    oldOption <- getOption("dropRle")
    options("dropRle" = TRUE)
    on.exit(options("dropRle" = oldOption))
    NextMethod("median", na.rm=FALSE)
}
setMethod("median", "Rle", 
    function(x, na.rm = FALSE)
{
    if (na.rm)
        x <- x[!is.na(x)]
    oldOption <- getOption("dropRle")
    options("dropRle" = TRUE)
    on.exit(options("dropRle" = oldOption))
    callNextMethod(x=x, na.rm=FALSE)
})

quantile.Rle <- 
    function(x, probs = seq(0, 1, 0.25), na.rm = FALSE, names = TRUE,
             type = 7, ...)
{
    if (na.rm)
        x <- x[!is.na(x)]
    oldOption <- getOption("dropRle")
    options("dropRle" = TRUE)
    on.exit(options("dropRle" = oldOption))
    NextMethod("quantile", na.rm=FALSE)
}

setMethod("mad", "Rle",
          function(x, center = median(x), constant = 1.4826, na.rm = FALSE,
                   low = FALSE, high = FALSE)
          {
              if (na.rm)
                  x <- x[!is.na(x)]
              oldOption <- getOption("dropRle")
              options("dropRle" = TRUE)
              on.exit(options("dropRle" = oldOption))
              callNextMethod(x=x, center=center, constant=constant,
                             na.rm=FALSE, low=FALSE, high=FALSE)
          })

setMethod("IQR", "Rle",
          function(x, na.rm = FALSE)
              diff(quantile(x, c(0.25, 0.75), na.rm = na.rm, names = FALSE)))

setMethod("smoothEnds", "Rle", function(y, k = 3)
          {
              oldOption <- getOption("dropRle")
              options("dropRle" = TRUE)
              on.exit(options("dropRle" = oldOption))
              callNextMethod(y = y, k = k)
          })

setGeneric("runmean", signature="x",
           function(x, k, endrule = c("drop", "constant"), ...)
               standardGeneric("runmean"))

setMethod("runmean", "Rle",
          function(x, k, endrule = c("drop", "constant"), na.rm = FALSE)
          {
              sums <- runsum(x, k, endrule, na.rm)
              if (na.rm) {
                  d <- Rle(rep(1L, length(x)))
                  d[is.na(x)] <- 0L 
                  sums / runsum(d, k, endrule, na.rm)
              } else {
                  sums / k
              }
          })

setMethod("runmed", "Rle",
          function(x, k, endrule = c("median", "keep", "drop", "constant"),
                   algorithm = NULL, print.level = 0)
          {
              if (!all(is.finite(as.vector(x))))
                  stop("NA/NaN/Inf not supported in runmed,Rle-method")
              endrule <- match.arg(endrule)
              n <- length(x)
              k <- normargRunK(k = k, n = n, endrule = endrule)
              i <- (k + 1L) %/% 2L
              ans <- runq(x, k = k, i = i)
              if (endrule == "constant") {
                  runLength(ans)[1L] <- runLength(ans)[1L] + (i - 1L)
                  runLength(ans)[nrun(ans)] <-
                    runLength(ans)[nrun(ans)] + (i - 1L)
              } else if (endrule != "drop") {
                  ans <- c(head(x, i - 1L), ans, tail(x, i - 1L))
                  if (endrule == "median") {
                      ans <- smoothEnds(ans, k = k)
                  }
              }
              ans
          })

setGeneric("runsum", signature="x",
           function(x, k, endrule = c("drop", "constant"), ...)
               standardGeneric("runsum"))

setMethod("runsum", "Rle",
          function(x, k, endrule = c("drop", "constant"), na.rm = FALSE)
          {
              endrule <- match.arg(endrule)
              n <- length(x)
              k <- normargRunK(k = k, n = n, endrule = endrule)
              ans <- .Call2("Rle_runsum", x, as.integer(k), as.logical(na.rm), 
                            PACKAGE="S4Vectors")
              if (endrule == "constant") {
                  j <- (k + 1L) %/% 2L
                  runLength(ans)[1L] <- runLength(ans)[1L] + (j - 1L)
                  runLength(ans)[nrun(ans)] <-
                    runLength(ans)[nrun(ans)] + (j - 1L)
              }
              ans
          })

setGeneric("runwtsum", signature="x",
           function(x, k, wt, endrule = c("drop", "constant"), ...)
               standardGeneric("runwtsum"))

setMethod("runwtsum", "Rle",
          function(x, k, wt, endrule = c("drop", "constant"), na.rm = FALSE)
          {
              endrule <- match.arg(endrule)
              n <- length(x)
              k <- normargRunK(k = k, n = n, endrule = endrule)
              ans <-
                .Call2("Rle_runwtsum", x, as.integer(k), as.numeric(wt),
                      as.logical(na.rm), PACKAGE="S4Vectors")
              if (endrule == "constant") {
                  j <- (k + 1L) %/% 2L
                  runLength(ans)[1L] <- runLength(ans)[1L] + (j - 1L)
                  runLength(ans)[nrun(ans)] <-
                    runLength(ans)[nrun(ans)] + (j - 1L)
              }
              ans
          })

setGeneric("runq", signature="x",
           function(x, k, i, endrule = c("drop", "constant"), ...)
               standardGeneric("runq"))

setMethod("runq", "Rle",
          function(x, k, i, endrule = c("drop", "constant"), na.rm = FALSE)
          {
              endrule <- match.arg(endrule)
              n <- length(x)
              k <- normargRunK(k = k, n = n, endrule = endrule)
              ans <-
                .Call2("Rle_runq", x, as.integer(k), as.integer(i), 
                      as.logical(na.rm), PACKAGE="S4Vectors")
              if (endrule == "constant") {
                  j <- (k + 1L) %/% 2L
                  runLength(ans)[1L] <- runLength(ans)[1L] + (j - 1L)
                  runLength(ans)[nrun(ans)] <-
                    runLength(ans)[nrun(ans)] + (j - 1L)
              }
              ans
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other character data methods
###

setMethod("nchar", "Rle",
    function(x, type="chars", allowNA=FALSE, keepNA=NA)
        new_Rle(nchar(runValue(x), type=type, allowNA=allowNA, keepNA=keepNA),
                runLength(x), check=FALSE)
)

setMethod("substr", "Rle",
          function(x, start, stop)
          {
              if (is.factor(runValue(x))) {
                  levels(x) <- substr(levels(x), start = start, stop = stop)
              } else {
                  runValue(x) <- substr(runValue(x), start = start, stop = stop)
              }
              x
          })
setMethod("substring", "Rle",
          function(text, first, last = 1000000L)
          {
              if (is.factor(runValue(text))) {
                  levels(text) <-
                    substring(levels(text), first = first, last = last)
              } else {
                  runValue(text) <-
                    substring(runValue(text), first = first, last = last)
              }
              text
          })

setMethod("chartr", c(old = "ANY", new = "ANY", x = "Rle"),
          function(old, new, x)
          {
              if (is.factor(runValue(x))) {
                  levels(x) <- chartr(old = old, new = new, levels(x))
              } else {
                  runValue(x) <- chartr(old = old, new = new, runValue(x))
              }
              x
          })
setMethod("tolower", "Rle",
          function(x) {
              if (is.factor(runValue(x))) {
                  levels(x) <- tolower(levels(x))
              } else {
                  runValue(x) <- tolower(runValue(x))
              }
              x
          })
setMethod("toupper", "Rle",
          function(x)
          {
              if (is.factor(runValue(x))) {
                  levels(x) <- toupper(levels(x))
              } else {
                  runValue(x) <- toupper(runValue(x))
              }
              x
          })

setMethod("sub", signature = c(pattern = "ANY", replacement = "ANY", x = "Rle"),
          function(pattern, replacement, x, ignore.case = FALSE,
                   perl = FALSE, fixed = FALSE, useBytes = FALSE)
          {
              if (is.factor(runValue(x))) {
                  levels(x) <-
                    sub(pattern = pattern, replacement = replacement,
                        x = levels(x), ignore.case = ignore.case,
                        perl = perl, fixed = fixed, useBytes = useBytes)
              } else {
                  runValue(x) <-
                    sub(pattern = pattern, replacement = replacement,
                        x = runValue(x), ignore.case = ignore.case,
                        perl = perl, fixed = fixed, useBytes = useBytes)
              }
              x
          })
setMethod("gsub", signature = c(pattern = "ANY", replacement = "ANY", x = "Rle"),
          function(pattern, replacement, x, ignore.case = FALSE,
                   perl = FALSE, fixed = FALSE, useBytes = FALSE)
          {
              if (is.factor(runValue(x))) {
                  levels(x) <-
                    gsub(pattern = pattern, replacement = replacement,
                         x = levels(x), ignore.case = ignore.case,
                         perl = perl, fixed = fixed, useBytes = useBytes)
              } else {
                  runValue(x) <-
                    gsub(pattern = pattern, replacement = replacement,
                         x = runValue(x), ignore.case = ignore.case,
                         perl = perl, fixed = fixed, useBytes = useBytes)
              }
              x
          })

.pasteTwoRles <- function(e1, e2, sep = " ", collapse = NULL)
{
    n1 <- length(e1)
    n2 <- length(e2)
    if (n1 == 0 || n2 == 0) {
        ends <- integer(0)
        which1 <- integer(0)
        which2 <- integer(0)
    } else {
        n <- max(n1, n2)
        if (max(n1, n2) %% min(n1, n2) != 0)
            warning("longer object length is not a multiple of shorter object length")
        if (n1 < n)
            e1 <- rep(e1, length.out = n)
        if (n2 < n)
            e2 <- rep(e2, length.out = n)
        # ends <- sort(unique(c(end(e1), end(e2))))
        ends <- sortedMerge(end(e1), end(e2))
        which1 <- findIntervalAndStartFromWidth(ends, runLength(e1))[["interval"]]
        which2 <- findIntervalAndStartFromWidth(ends, runLength(e2))[["interval"]]
    }
    if (is.null(collapse) &&
        is.factor(runValue(e1)) && is.factor(runValue(e2))) {
        levelsTable <-
          expand.grid(levels(e2), levels(e1), KEEP.OUT.ATTRS = FALSE,
                      stringsAsFactors = FALSE)
        values <-
          structure((as.integer(runValue(e1)[which1]) - 1L) * nlevels(e2) +
                    as.integer(runValue(e2)[which2]),
                    levels =
                    paste(levelsTable[[2L]], levelsTable[[1L]], sep = sep),
                    class = "factor")
    } else {
        values <-
          paste(runValue(e1)[which1], runValue(e2)[which2], sep = sep,
                collapse = collapse)
    }
    new_Rle(values, diffWithInitialZero(ends), check=FALSE)
}

setMethod("paste", "Rle",
          function(..., sep = " ", collapse = NULL)
          {
              args <- list(...)
              ans <- args[[1L]]
              if (length(args) > 1) {
                  for (i in 2:length(args)) {
                      ans <-
                        .pasteTwoRles(ans, args[[i]], sep = sep,
                                      collapse = collapse)
                  }
              }
              ans
          })

setMethod("grepl", c("ANY", "Rle"),
          function(pattern, x, ignore.case = FALSE, perl = FALSE,
                   fixed = FALSE, useBytes = FALSE) {
              v <- grepl(pattern, runValue(x), ignore.case, perl, fixed,
                         useBytes)
              Rle(v, runLength(x))
          })

setMethod("grep", c("ANY", "Rle"),
          function(pattern, x, ignore.case = FALSE, perl = FALSE, value = FALSE,
                   fixed = FALSE, useBytes = FALSE, invert = FALSE) {
              if (isTRUE(value)) {
                  v <- grep(pattern, x, ignore.case, perl, value=TRUE, fixed,
                            useBytes, invert)
                  Rle(v, runLength(x))
              } else { # obviously inefficient
                  Rle(callNextMethod())
              }
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other factor data methods
###

### S3/S4 combo for levels.Rle
levels.Rle <- function(x) levels(runValue(x))
setMethod("levels", "Rle", levels.Rle)

setReplaceMethod("levels", "Rle",
                 function(x, value) {
                     levels(x@values) <- value
                     if (anyDuplicated(value))
                         x <- new_Rle(runValue(x), runLength(x), check=FALSE)
                     x
                 })

droplevels.Rle <- function(x, ...) droplevels(x, ...)
.droplevels.Rle <- function(x) {
  if (!is.factor(runValue(x))) {
    stop("levels can only be dropped when runValue(x) is a factor")
  }
  runValue(x) <- droplevels(runValue(x))
  x
}
setMethod("droplevels", "Rle", .droplevels.Rle)

