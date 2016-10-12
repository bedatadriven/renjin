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
### Rle objects
### -------------------------------------------------------------------------
###

setClass("Rle",
         representation(values = "vectorORfactor",
                        lengths = "integer"),
         prototype = prototype(values = logical()),
         contains = "Vector",
         validity = function(object)
         {
             msg <- NULL
             run_values <- runValue(object)
             run_lengths <- runLength(object)
             if (length(run_values) != length(run_lengths))
                 msg <- c(msg, "run values and run lengths must have the same length")
             if (!all(run_lengths > 0L))
                 msg <- c(msg, "all run lengths must be positive")
             ## TODO: Fix the following test.
             #if (length(run_lengths) >= 2 && is.atomic(run_values)
             #      && any(run_values[-1L] == run_values[-length(run_values)]))
             #    msg <- c(msg, "consecutive runs must have different values")
             if (is.null(msg)) TRUE else msg
         })

 
### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Getters
###

setGeneric("runLength", signature = "x",
           function(x) standardGeneric("runLength"))
setMethod("runLength", "Rle", function(x) x@lengths)
 
setMethod("length", "Rle", function(x) sum(runLength(x)))

setGeneric("runValue", signature = "x",
           function(x) standardGeneric("runValue"))
setMethod("runValue", "Rle", function(x) x@values)

setGeneric("nrun", signature = "x", function(x) standardGeneric("nrun"))
setMethod("nrun", "Rle", function(x) length(runLength(x)))

setMethod("start", "Rle", function(x) .Call2("Rle_start", x, PACKAGE="S4Vectors"))
setMethod("end", "Rle", function(x) .Call2("Rle_end", x, PACKAGE="S4Vectors"))
setMethod("width", "Rle", function(x) runLength(x))


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor
###

### Low-level constructor.
new_Rle <- function(values=logical(0), lengths=integer(0), check=TRUE)
{
    stopifnot(is(values, "vectorORfactor"))
    stopifnot(is.numeric(lengths))
    if (!is.integer(lengths))
        lengths <- as.integer(lengths)
    if (!isTRUEorFALSE(check))
        stop("'check' must be TRUE or FALSE")
    .Call2("Rle_constructor", values, lengths, check, 0L, PACKAGE="S4Vectors")
}

setGeneric("Rle", signature="values",
    function(values=logical(0), lengths=integer(0)) standardGeneric("Rle")
)

setMethod("Rle", "ANY",
    function(values=logical(0), lengths=integer(0)) new_Rle(values, lengths)
)

setMethod("Rle", "Rle",
    function(values=logical(0), lengths=integer(0))
    {
        if (!missing(lengths))
            stop(wmsg("'lengths' cannot be supplied when calling Rle() ",
                      "on an Rle object"))
        values
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Setters
###

setGeneric("runLength<-", signature="x",
           function(x, value) standardGeneric("runLength<-"))
setReplaceMethod("runLength", "Rle",
                 function(x, value) Rle(runValue(x), value))
         
setGeneric("runValue<-", signature="x",
           function(x, value) standardGeneric("runValue<-"))
setReplaceMethod("runValue", "Rle",
                 function(x, value) Rle(value, runLength(x)))


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion
###

setAs("vector", "Rle", function(from) Rle(from))
setAs("logical", "Rle", function(from) Rle(from))
setAs("integer", "Rle", function(from) Rle(from))
setAs("numeric", "Rle", function(from) Rle(from))
setAs("complex", "Rle", function(from) Rle(from))
setAs("character", "Rle", function(from) Rle(from))
setAs("raw", "Rle", function(from) Rle(from))
setAs("factor", "Rle", function(from) Rle(from))

setAs("Rle", "vector", function(from) as.vector(from))
setAs("Rle", "logical", function(from) as.logical(from))
setAs("Rle", "integer", function(from) as.integer(from))
setAs("Rle", "numeric", function(from) as.numeric(from))
setAs("Rle", "complex", function(from) as.complex(from))
setAs("Rle", "character", function(from) as.character(from))
setAs("Rle", "raw", function(from) as.raw(from))
setAs("Rle", "factor", function(from) as.factor(from))
setAs("Rle", "list", function(from) as.list(from))
setAs("Rle", "data.frame", function(from) as.data.frame(from))

as.vector.Rle <- function(x, mode)
  rep.int(as.vector(runValue(x), mode), runLength(x))
setMethod("as.vector", "Rle", as.vector.Rle)
setMethod("as.factor", "Rle", function(x) rep.int(as.factor(runValue(x)), runLength(x)))

asFactorOrFactorRle <- function(x) {
  if (is(x, "Rle")) {
    runValue(x) <- as.factor(runValue(x))
    x
  } else {
    as.factor(x)
  }
}

.as.list.Rle <- function(x) as.list(as.vector(x))
setMethod("as.list", "Rle", .as.list.Rle)

setGeneric("decode", function(x, ...) standardGeneric("decode"))
setMethod("decode", "ANY", identity)

decodeRle <- function(x) rep.int(runValue(x), runLength(x))
setMethod("decode", "Rle", decodeRle)

.as.data.frame.Rle <- function(x, row.names=NULL, optional=FALSE, ...)
{
    value <- decodeRle(x)
    as.data.frame(value, row.names=row.names,
                  optional=optional, ...)
}
setMethod("as.data.frame", "Rle", .as.data.frame.Rle)

getStartEndRunAndOffset <- function(x, start, end) {
    .Call2("Rle_getStartEndRunAndOffset", x, start, end, PACKAGE="S4Vectors")
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting workhorses
###
### These are the low-level functions that do the real work of subsetting an
### Rle object. The final coercion to class(x) is to make sure that they act
### like an endomorphism on objects that belong to a subclass of Rle (the
### VariantAnnotation package defines Rle subclasses).
### Note that they drop the metadata columns!
###

extract_range_from_Rle <- function(x, start, end)
{
    ans <- .Call2("Rle_extract_range", x, start, end, PACKAGE="S4Vectors")
    as(ans, class(x))  # so the function is an endomorphism
}

.normarg_method <- function(method)
{
    if (!(isSingleNumber(method) && method >= 0 && method <= 3))
        stop("'method' must be a single integer between 0 and 3")
    if (!is.integer(method))
        method <- as.integer(method)
    method
}

### Used in GenomicRanges.
map_ranges_to_runs <- function(run_lens, start, width, method=0L)
{
    method <- .normarg_method(method)
    .Call2("ranges_to_runs_mapper", run_lens, start, width, method,
                                    PACKAGE="S4Vectors")
}

### NOT exported but used in IRanges package (by "extractROWS" method with
### signature Rle,RangesNSBS).
extract_ranges_from_Rle <- function(x, start, width, method=0L, as.list=FALSE)
{
    method <- .normarg_method(method)
    if (!isTRUEorFALSE(as.list))
        stop("'as.list' must be TRUE or FALSE")
    ans <- .Call2("Rle_extract_ranges", x, start, width, method, as.list,
                                        PACKAGE="S4Vectors")
    ## The function must act like an endomorphism.
    x_class <- class(x)
    if (!as.list)
        return(as(ans, x_class))
    ## 'ans' is a list of Rle instances.
    if (x_class == "Rle")
        return(ans)
    lapply(ans, as, x_class)
}

### TODO: Optimize this, maybe by implementing a simpler version of .Call
### entry point "Rle_extract_ranges" that takes 1 integer vector instead of 2.
extract_positions_from_Rle <- function(x, i)
{
    if (!is.integer(i))
        stop("'i' must be an integer vector")
    extract_ranges_from_Rle(x, i, rep.int(1L, length(i)))
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting
###

setMethod("extractROWS", c("Rle", "ANY"),
    function (x, i) 
    {
        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        callGeneric()
    }
)

setMethod("extractROWS", c("Rle", "RangeNSBS"),
    function(x, i)
    {
        range <- i@subscript
        range_start <- range[[1L]]
        range_end <- range[[2L]]
        ans <- extract_range_from_Rle(x, range_start, range_end)
        mcols(ans) <- extractROWS(mcols(x), i)
        ans
    }
)

setMethod("extractROWS", c("Rle", "NSBS"),
    function(x, i)
    {
        ans <- extract_positions_from_Rle(x, as.integer(i))
        mcols(ans) <- extractROWS(mcols(x), i)
        ans
    }
)

setMethod("[", "Rle",
    function(x, i, j, ..., drop=getOption("dropRle", default=FALSE))
    {
        if (!missing(j) || length(list(...)) > 0)
            stop("invalid subsetting")
        if (!missing(i))
            x <- extractROWS(x, i)
        if (drop)
            x <- decodeRle(x)
        x
    }
)

### The replaced elements in 'x' must get their metadata columns from 'value'.
### See this thread on bioc-devel:
###   https://stat.ethz.ch/pipermail/bioc-devel/2015-November/008319.html
setMethod("replaceROWS", "Rle",
    function(x, i, value)
    {
        ## FIXME: Right now, the subscript 'i' is turned into an IRanges
        ## object so we need stuff that lives in the IRanges package for this
        ## to work. This is ugly/hacky and needs to be fixed (thru a redesign
        ## of this method).
        if (!requireNamespace("IRanges", quietly=TRUE))
            stop("Couldn't load the IRanges package. You need to install ",
                 "the IRanges\n  package in order to replace values in ",
                 "an Rle object.")

        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        lv <- length(value)
        if (lv != 1L) {
            ans <- Rle(replaceROWS(decodeRle(x), i, as.vector(value)))
            mcols(ans) <- replaceROWS(mcols(x), i, mcols(value))
            return(ans)
        }

        ## From here, 'value' is guaranteed to be of length 1.

        ## TODO: Maybe make this the coercion method from NSBS to Ranges.
        if (is(i, "RangesNSBS")) {
            ir <- i@subscript
        } else {
            ir <- as(as.integer(i), "IRanges")
        }
        ir <- IRanges::reduce(ir)
        if (length(ir) == 0L)
            return(x)

        isFactorRle <- is.factor(runValue(x))
        value <- normalizeSingleBracketReplacementValue(value, x)
        value2 <- as.vector(value)
        if (isFactorRle) {
            value2 <- factor(value2, levels=levels(x))
            dummy_value <- factor(levels(x), levels=levels(x))
        }
        if (anyMissingOrOutside(start(ir), 1L, length(x)) ||
            anyMissingOrOutside(end(ir), 1L, length(x)))
            stop("some ranges are out of bounds")

        valueWidths <- width(ir)
        ir <- IRanges::gaps(ir, start=1, end=length(x))
        k <- length(ir)
        start <- start(ir)
        end <- end(ir)

        info <- getStartEndRunAndOffset(x, start, end)
        runStart <- info[["start"]][["run"]]
        offsetStart <- info[["start"]][["offset"]]
        runEnd <- info[["end"]][["run"]]
        offsetEnd <- info[["end"]][["offset"]]

        if ((length(ir) == 0L) || (start(ir)[1L] != 1L)) {
            k <- k + 1L
            runStart <- c(1L, runStart)
            offsetStart <- c(0L, offsetStart)
            runEnd <- c(0L, runEnd)
            offsetEnd <- c(0L, offsetEnd)
        } 
        if ((length(ir) > 0L) && (end(ir[length(ir)]) != length(x))) {
            k <- k + 1L
            runStart <- c(runStart, 1L)
            offsetStart <- c(offsetStart, 0L)
            runEnd <- c(runEnd, 0L)
            offsetEnd <- c(offsetEnd, 0L)
        }

        subseqs <- vector("list", length(valueWidths) + k)
        if (k > 0L) {
            if (isFactorRle) {
                subseqs[seq(1L, length(subseqs), by=2L)] <-
                    lapply(seq_len(k), function(i) {
                           ans <- .Call2("Rle_window_aslist",
                                         x, runStart[i], runEnd[i],
                                         offsetStart[i], offsetEnd[i],
                                         PACKAGE="S4Vectors")
                           ans[["values"]] <- dummy_value[ans[["values"]]]
                           ans})
            } else {
                subseqs[seq(1L, length(subseqs), by=2L)] <-
                    lapply(seq_len(k), function(i)
                           .Call2("Rle_window_aslist",
                                  x, runStart[i], runEnd[i],
                                  offsetStart[i], offsetEnd[i],
                                  PACKAGE="S4Vectors"))
            }
        }
        if (length(valueWidths) > 0L) {
            subseqs[seq(2L, length(subseqs), by=2L)] <-
                lapply(seq_len(length(valueWidths)), function(i)
                       list(values=value2,
                            lengths=valueWidths[i]))
        }
        values <- unlist(lapply(subseqs, "[[", "values"))
        if (isFactorRle)
            values <- dummy_value[values]
        ans <- Rle(values, unlist(lapply(subseqs, "[[", "lengths")))
        mcols(ans) <- replaceROWS(mcols(x), i, mcols(value))
        ans
    }
)

setReplaceMethod("[", "Rle",
    function(x, i, j,..., value)
    {
        if (!missing(j) || length(list(...)) > 0L)
            stop("invalid subsetting")
        i <- normalizeSingleBracketSubscript(i, x, as.NSBS=TRUE)
        li <- length(i)
        if (li == 0L) {
            ## Surprisingly, in that case, `[<-` on standard vectors does not
            ## even look at 'value'. So neither do we...
            return(x)
        }
        lv <- length(value)
        if (lv == 0L)
            stop("replacement has length zero")
        replaceROWS(x, i, value)
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting an object by an Rle subscript.
###
### See R/subsetting-utils.R for more information.
###

setClass("RleNSBS",      # not exported
    contains="NSBS",
    representation(
        subscript="Rle"  # integer-Rle
    ),
    prototype(
        ## Calling Rle(integer(0)) below causes the following error at
        ## installation time:
        ##     Error in .Call(.NAME, ..., PACKAGE = PACKAGE) : 
        ##       "Rle_constructor" not available for .Call() for package
        ##       "S4Vectors"
        ##     Error : unable to load R code in package ‘S4Vectors’
        ##     ERROR: lazy loading failed for package ‘S4Vectors’
        #subscript=Rle(integer(0))
        subscript=new2("Rle", values=integer(0),
                              lengths=integer(0),
                              check=FALSE)
    )
)

### Construction methods.
### Supplied arguments are trusted so we don't check them!

setMethod("NSBS", "Rle",
    function(i, x, exact=TRUE, upperBoundIsStrict=TRUE)
    {
        x_NROW <- NROW(x)
        i_vals <- runValue(i)
        if (is.logical(i_vals) && length(i_vals) != 0L) {
            if (anyMissing(i_vals))
                stop("subscript contains NAs")
            if (length(i) < x_NROW)
                i <- rep(i, length.out=x_NROW)
            ## The coercion method from Rle to NormalIRanges is defined in the
            ## IRanges package.
            if (requireNamespace("IRanges", quietly=TRUE)) {
                i <- as(i, "NormalIRanges")
                ## This will call the "NSBS" method for Ranges objects defined
                ## in the IRanges package and return a RangesNSBS, or
                ## RangeNSBS, or NativeNSBS object.
                return(callGeneric())
            }
            warning(wmsg(
                "Couldn't load the IRanges package. Installing this package ",
                "will enable efficient subsetting by a logical-Rle object ",
                "so is higly recommended."
            ))
            i <- which(i)
            return(callGeneric())  # will return a NativeNSBS object
        }
        i_vals <- as.integer(NSBS(i_vals, x,
                                  exact=exact,
                                  upperBoundIsStrict=upperBoundIsStrict))
        runValue(i) <- i_vals
        new2("RleNSBS", subscript=i,
                        upper_bound=x_NROW,
                        upper_bound_is_strict=upperBoundIsStrict,
                        check=FALSE)
    }
)

### Other methods.

setMethod("as.integer", "RleNSBS", function(x) decodeRle(x@subscript))

setMethod("length", "RleNSBS", function(x) length(x@subscript))

setMethod("anyDuplicated", "RleNSBS",
    function(x, incomparables=FALSE, ...) anyDuplicated(x@subscript)
)

setMethod("isStrictlySorted", "RleNSBS",
    function(x) isStrictlySorted(x@subscript)
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting an Rle object by an Rle subscript.
###

### Simplified version of rep.int() for Rle objects. Handles only the case
### where 'times' has the length of 'x'.
.rep_times_Rle <- function(x, times)
{
    breakpoints <- end(x)
    if (length(times) != last_or(breakpoints, 0L))
        stop("invalid 'times' argument")
    runLength(x) <- groupsum(times, breakpoints)
    x
}

setMethod("extractROWS", c("Rle", "RleNSBS"),
    function(x, i)
    {
        rle <- i@subscript
        .rep_times_Rle(extractROWS(x, runValue(rle)), runLength(rle))
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other subsetting-related operations
###

### S3/S4 combo for rev.Rle
rev.Rle <- function(x)
{
    x@values <- rev(runValue(x))
    x@lengths <- rev(runLength(x))
    x
}
setMethod("rev", "Rle", rev.Rle)

setMethod("rep.int", "Rle",
    function(x, times)
    {
        if (!is.numeric(times))
            stop("invalid 'times' argument")
        if (!is.integer(times))
            times <- as.integer(times)
        if (anyMissingOrOutside(times, 0L))
            stop("invalid 'times' argument")

        x_len <- length(x)
        times_len <- length(times)
        if (times_len == x_len)
            return(.rep_times_Rle(x, times))
        if (times_len != 1L)
            stop("invalid 'times' argument")
        ans <- Rle(rep.int(runValue(x), times),
                   rep.int(runLength(x), times))
        as(ans, class(x))  # rep.int() must act like an endomorphism
    }
)

setMethod("rep", "Rle",
          function(x, times, length.out, each)
          {
              usedEach <- FALSE
              if (!missing(each) && length(each) > 0) {
                  each <- as.integer(each[1L])
                  if (!is.na(each)) {
                      if (each < 0)
                          stop("invalid 'each' argument")
                      usedEach <- TRUE
                      if (each == 0)
                          x <- new2(class(x), values=runValue(x)[0L],
                                              check=FALSE)
                      else
                          x@lengths <- each[1L] * runLength(x)
                  }
              }
              if (!missing(length.out) && length(length.out) > 0) {
                  n <- length(x)
                  length.out <- as.integer(length.out[1L])
                  if (!is.na(length.out)) {
                      if (length.out == 0) {
                          x <- new2(class(x), values=runValue(x)[0L],
                                              check=FALSE)
                      } else if (length.out < n) {
                          x <- window(x, 1, length.out)
                      } else if (length.out > n) {
                          if (n == 0) {
                              x <- Rle(rep(runValue(x), length.out=1),
                                       length.out)
                          } else {
                              x <-
                                window(rep.int(x, ceiling(length.out / n)),
                                       1, length.out)
                          }
                      }
                  }
              } else if (!missing(times)) {
                  if (usedEach && length(times) != 1)
                      stop("invalid 'times' argument")
                  x <- rep.int(x, times)
              }
              x
          })


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining.
###

setMethod("c", "Rle", 
          function(x, ..., recursive = FALSE)
          {
              args <- lapply(unname(list(x, ...)), Rle)
              args <- args[sapply(args, length) > 0]
              if (length(args) == 0L)
                  return(x)
              ans_values <- unlist(lapply(args, slot, "values"))
              ans_lengths <- unlist(lapply(args, slot, "lengths"))
              Rle(ans_values, ans_lengths)
          })

setMethod("append", c("Rle", "vector"),
          function (x, values, after = length(x)) {
              append(x, Rle(values), after)
          })

setMethod("append", c("vector", "Rle"),
          function (x, values, after = length(x)) {
              append(Rle(x), values, after)
          })

### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Other methods.
###

setMethod("%in%", "Rle",
          function(x, table)
              new_Rle(runValue(x) %in% table, runLength(x), check=FALSE))

setGeneric("findRun", signature = "vec",
           function(x, vec) standardGeneric("findRun"))

setMethod("findRun", signature = c(vec = "Rle"),
          function(x, vec) {
            runs <- findIntervalAndStartFromWidth(as.integer(x),
                                         runLength(vec))[["interval"]]
            runs[x == 0 | x > length(vec)] <- NA
            runs
          })

setMethod("is.na", "Rle",
          function(x)
              new_Rle(is.na(runValue(x)), runLength(x), check=FALSE))

setMethod("anyNA", "Rle",
          function(x)
              anyNA(runValue(x)))

setMethod("is.unsorted", "Rle",
          function(x, na.rm = FALSE, strictly = FALSE)
          {
              ans <- is.unsorted(runValue(x), na.rm = na.rm, strictly = strictly)
              if (strictly && !ans)
                  ans <- any(runLength(x) > 1L)
              ans
          })

setMethod("match", c("ANY", "Rle"),
    function(x, table, nomatch=NA_integer_, incomparables=NULL)
    {
        m <- match(x, runValue(table), incomparables=incomparables)
        ans <- start(table)[m]
        ## 'as.integer(nomatch)[1L]' seems to mimic how base::match() treats
        ## the 'nomatch' argument.
        nomatch <- as.integer(nomatch)[1L]
        if (!is.na(nomatch))
            ans[is.na(ans)] <- nomatch
        ans
    }
)

setMethod("match", c("Rle", "ANY"),
    function(x, table, nomatch=NA_integer_, incomparables=NULL)
    {
        x_run_lens <- runLength(x)
        x <- runValue(x)
        m <- callGeneric()
        Rle(m, x_run_lens)
    }
)

setMethod("match", c("Rle", "Rle"),
    function(x, table, nomatch=NA_integer_, incomparables=NULL)
    {
        x_run_lens <- runLength(x)
        x <- runValue(x)
        m <- callGeneric()
        Rle(m, x_run_lens)
    }
)

### FIXME: Remove in R 3.3
setMethod("order", "Rle",
    function(..., nalast=TRUE, decreas=FALSE, method=c("shell", "radix"))
    {
        args <- lapply(unname(list(...)), decodeRle)
        do.call(order, c(args, list(nalast=nalast,
                                    decreas=decreas,
                                    method=method)))
    }
)

.sort.Rle <- function(x, decreas=FALSE, nalast=NA, ...)
{
    if (is.na(nalast)) {
        if (anyMissing(runValue(x)))
            x <- x[!is.na(x)]
    }
    ord <- base::order(runValue(x), nalast=nalast, decreas=decreas)
    new_Rle(runValue(x)[ord], runLength(x)[ord], check=FALSE)
}
setMethod("sort", "Rle", .sort.Rle)

setMethod("xtfrm", "Rle", function(x) {
    initialize(x, values=xtfrm(runValue(x)))
})

setMethod("rank", "Rle", function (x, na.last = TRUE,
                                   ties.method = c("average", "first", 
                                     "random", "max", "min"))
          {
              ties.method <- match.arg(ties.method)
              if (ties.method == "min" || ties.method == "first") {
                  callNextMethod()
              } else {
                  x <- as.vector(x)
                  ans <- callGeneric()
                  if (ties.method %in% c("average", "max", "min")) {
                      Rle(ans)
                  } else {
                      ans
                  }
              }
          })

setMethod("table", "Rle", 
    function(...)
    {
        ## Currently only 1 Rle is supported. An approach for multiple 
        ## Rle's could be disjoin(), findRun() to find matches, then 
        ## xtabs(length ~ value ...).
        x <- sort(list(...)[[1L]]) 
        if (is.factor(runValue(x))) {
            dn <- levels(x)
            tab <- integer(length(dn))
            tab[dn %in% runValue(x)] <- runLength(x)
            dims <- length(dn)
        } else {
            dn <- as.character(runValue(x)) 
            tab <- runLength(x) 
            dims <- nrun(x)
        }
        ## Adjust 'dn' for consistency with base::table
        if (length(dn) == 0L)
            dn <- NULL
        dn <- list(dn)
        names(dn) <- .list.names(...) 
        y <- array(tab, dims, dimnames=dn)
        class(y) <- "table"
        y 
    }
)

.list.names <- function(...) {
    l <- as.list(substitute(list(...)))[-1L]
    deparse.level <- 1 
    nm <- names(l)
    fixup <- if (is.null(nm))
        seq_along(l)
    else nm == ""
    dep <- vapply(l[fixup], function(x) switch(deparse.level +
        1, "", if (is.symbol(x)) as.character(x) else "",
        deparse(x, nlines = 1)[1L]), "")
    if (is.null(nm))
        dep
    else {
        nm[fixup] <- dep
        nm
    }
}

.duplicated.Rle <- function(x, incomparables=FALSE, fromLast=FALSE)
    stop("no \"duplicated\" method for Rle objects yet, sorry")
setMethod("duplicated", "Rle", .duplicated.Rle)

.unique.Rle <- function(x, incomparables=FALSE, ...)
    unique(runValue(x), incomparables=incomparables, ...)
setMethod("unique", "Rle", .unique.Rle)

### S3/S4 combo for anyDuplicated.Rle
anyDuplicated.Rle <- function(x, incomparables=FALSE, ...)
    all(runLength(x) == 1L) && anyDuplicated(runValue(x))
setMethod("anyDuplicated", "Rle", anyDuplicated.Rle)

setMethod("isStrictlySorted", "Rle",
    function(x)  all(runLength(x) == 1L) && isStrictlySorted(runValue(x))
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Set methods
###
### The return values of these do not have any duplicated values, so
### it would obviously be more efficient to return plain vectors. That
### might violate user expectations though.
###

setMethod("union", c("Rle", "Rle"), function(x, y) {
  Rle(union(runValue(x), runValue(y)))
})

setMethod("union", c("ANY", "Rle"), function(x, y) {
  Rle(union(as.vector(x), runValue(y)))
})

setMethod("union", c("Rle", "ANY"), function(x, y) {
  Rle(union(runValue(x), as.vector(y)))
})

setMethod("intersect", c("Rle", "Rle"), function(x, y) {
  Rle(intersect(runValue(x), runValue(y)))
})

setMethod("intersect", c("ANY", "Rle"), function(x, y) {
  Rle(intersect(as.vector(x), runValue(y)))
})

setMethod("intersect", c("Rle", "ANY"), function(x, y) {
  Rle(intersect(runValue(x), as.vector(y)))
})

setMethod("setdiff", c("Rle", "Rle"), function(x, y) {
  Rle(setdiff(runValue(x), runValue(y)))
})

setMethod("setdiff", c("ANY", "Rle"), function(x, y) {
  Rle(setdiff(as.vector(x), runValue(y)))
})

setMethod("setdiff", c("Rle", "ANY"), function(x, y) {
  Rle(setdiff(runValue(x), as.vector(y)))
})


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### The "show" method
###

setMethod("show", "Rle",
          function(object)
          {
              lo <- length(object)
              nr <- nrun(object)
              halfWidth <- getOption("width") %/% 2L
              cat(classNameForDisplay(runValue(object)),
                  "-Rle of length ", lo, " with ", nr,
                  ifelse(nr == 1, " run\n", " runs\n"), sep = "")
              first <- max(1L, halfWidth)
              showMatrix <-
                rbind(as.character(head(runLength(object), first)),
                      as.character(head(runValue(object), first)))
              if (nr > first) {
                  last <- min(nr - first, halfWidth)
                  showMatrix <-
                    cbind(showMatrix,
                          rbind(as.character(tail(runLength(object), last)),
                                as.character(tail(runValue(object), last))))
              }
              if (is.character(runValue(object))) {
                  showMatrix[2L,] <-
                    paste("\"", showMatrix[2L,], "\"", sep = "")
              }
              showMatrix <- format(showMatrix, justify = "right")
              cat(labeledLine("  Lengths", showMatrix[1L,], count = FALSE))
              cat(labeledLine("  Values ", showMatrix[2L,], count = FALSE))
              if (is.factor(runValue(object)))
                  cat(labeledLine("Levels", levels(object)))
          })

setMethod("showAsCell", "Rle", function(object) as.vector(object))

