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
### Hits objects
### -------------------------------------------------------------------------
###


### Vector of hits between a set of left nodes and a set of right nodes.
setClass("Hits",
    contains="Vector",
    representation(
        from="integer",    # integer vector of length N
        to="integer",      # integer vector of length N
        nLnode="integer",  # single integer: number of Lnodes ("left nodes")
        nRnode="integer"   # single integer: number of Rnodes ("right nodes")
    ),
    prototype(
        nLnode=0L,
        nRnode=0L
    )
)

### A SelfHits object is a Hits object where the left and right nodes are
### identical.
setClass("SelfHits", contains="Hits")

### Hits objects where the hits are sorted by query. Coercion from
### SortedByQueryHits to List takes advantage of this and is very fast.
setClass("SortedByQueryHits", contains="Hits")
setClass("SortedByQuerySelfHits", contains=c("SelfHits", "SortedByQueryHits"))


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### parallelSlotNames()
###

### Combine the new parallel slots with those of the parent class. Make sure
### to put the new parallel slots *first*.
setMethod("parallelSlotNames", "Hits",
    function(x) c("from", "to", callNextMethod())
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessors
###

setGeneric("from", function(x, ...) standardGeneric("from"))
setMethod("from", "Hits", function(x) x@from)

setGeneric("to", function(x, ...) standardGeneric("to"))
setMethod("to", "Hits", function(x) x@to)

setGeneric("nLnode", function(x, ...) standardGeneric("nLnode"))
setMethod("nLnode", "Hits", function(x) x@nLnode)

setGeneric("nRnode", function(x, ...) standardGeneric("nRnode"))
setMethod("nRnode", "Hits", function(x) x@nRnode)

setGeneric("nnode", function(x, ...) standardGeneric("nnode"))
setMethod("nnode", "SelfHits", function(x) nLnode(x))

setGeneric("countLnodeHits", function(x, ...) standardGeneric("countLnodeHits"))

.count_Lnode_hits <- function(x) tabulate(from(x), nbins=nLnode(x))
setMethod("countLnodeHits", "Hits", .count_Lnode_hits)

setGeneric("countRnodeHits", function(x, ...) standardGeneric("countRnodeHits"))

.count_Rnode_hits <- function(x) tabulate(to(x), nbins=nRnode(x))
setMethod("countRnodeHits", "Hits", .count_Rnode_hits)

### query/subject API
queryHits <- function(x, ...) from(x, ...)
subjectHits <- function(x, ...) to(x, ...)
queryLength <- function(x, ...) nLnode(x, ...)
subjectLength <- function(x, ...) nRnode(x, ...)
countQueryHits <- function(x, ...) countLnodeHits(x, ...)
countSubjectHits <- function(x, ...) countRnodeHits(x, ...)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Validity
###

.valid.Hits.nnode <- function(nnode, side)
{
    if (!isSingleInteger(nnode) || nnode < 0L) {
        msg <- wmsg("'n", side, "node(x)' must be a single non-negative ",
                    "integer")
        return(msg)
    }
    if (!is.null(attributes(nnode))) {
        msg <- wmsg("'n", side, "node(x)' must be a single integer with ",
                    "no attributes")
        return(msg)
    }
    NULL
}

.valid.Hits.from_or_to <- function(from_or_to, nnode, what, side)
{
    if (!(is.integer(from_or_to) && is.null(attributes(from_or_to)))) {
        msg <- wmsg("'", what, "' must be an integer vector ",
                    "with no attributes")
        return(msg)
    }
    if (anyMissingOrOutside(from_or_to, 1L, nnode)) {
        msg <- wmsg("'", what, "' must contain non-NA values ",
                    ">= 1 and <= 'n", side, "node(x)'")
        return(msg)
    }
    NULL
}

.valid.Hits <- function(x)
{
    c(.valid.Hits.nnode(nLnode(x), "L"),
      .valid.Hits.nnode(nRnode(x), "R"),
      .valid.Hits.from_or_to(from(x), nLnode(x), "from(x)", "L"),
      .valid.Hits.from_or_to(to(x), nRnode(x), "to(x)", "R"))
}

setValidity2("Hits", .valid.Hits)

.valid.SelfHits <- function(x)
{
    if (nLnode(x) != nRnode(x))
        return("'nLnode(x)' and 'nRnode(x)' must be equal")
    NULL
}

setValidity2("SelfHits", .valid.SelfHits)

.valid.SortedByQueryHits <- function(x)
{
    if (isNotSorted(from(x)))
        return("'queryHits(x)' must be sorted")
    NULL
}

setValidity2("SortedByQueryHits", .valid.SortedByQueryHits)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructors
###

### Very low-level constructor. Doesn't try to sort the hits by query.
.new_Hits <- function(Class, from, to, nLnode, nRnode, mcols)
{
    new2(Class, from=from, to=to, nLnode=nLnode, nRnode=nRnode,
                elementMetadata=mcols,
                check=TRUE)
}

### Low-level constructor. Sort the hits by query if Class extends
### SortedByQueryHits.
new_Hits <- function(Class, from=integer(0), to=integer(0),
                            nLnode=0L, nRnode=0L,
                            mcols=NULL)
{
    if (!isSingleString(Class))
        stop("'Class' must be a single character string")
    if (!extends(Class, "Hits"))
        stop("'Class' must be the name of a class that extends Hits")

    if (!(is.numeric(from) && is.numeric(to)))
        stop("'from' and 'to' must be integer vectors")
    if (!is.integer(from))
        from <- as.integer(from)
    if (!is.integer(to))
        to <- as.integer(to)

    if (!(isSingleNumber(nLnode) && isSingleNumber(nRnode)))
        stop("'nLnode' and 'nRnode' must be single integers")
    if (!is.integer(nLnode))
        nLnode <- as.integer(nLnode)
    if (!is.integer(nRnode))
        nRnode <- as.integer(nRnode)

    if (!(is.null(mcols) || is(mcols, "DataFrame")))
        stop("'mcols' must be NULL or a DataFrame object")

    if (!extends(Class, "SortedByQueryHits")) {
        ## No need to sort the hits by query.
        ans <- .new_Hits(Class, from, to, nLnode, nRnode, mcols)
        return(ans)
    }

    ## Sort the hits by query.
    if (!is.null(mcols)) {
        revmap_envir <- new.env(parent=emptyenv())
    } else {
        revmap_envir <- NULL
    }
    ans <- .Call2("Hits_new", Class, from, to, nLnode, nRnode, revmap_envir,
                              PACKAGE="S4Vectors")
    if (!is.null(mcols)) {
        if (nrow(mcols) != length(ans))
            stop("length of supplied metadata columns ",
                 "must equal number of hits")
        if (exists("revmap", envir=revmap_envir)) {
            revmap <- get("revmap", envir=revmap_envir)
            mcols <- mcols[revmap, , drop=FALSE]
        }
        mcols(ans) <- mcols
    }
    ans
}

.make_mcols <- function(...)
{
    if (nargs() == 0L)
        return(NULL)
    DataFrame(..., check.names=FALSE)
}

### 2 high-level constructors.

Hits <- function(from=integer(0), to=integer(0), nLnode=0L, nRnode=0L, ...,
                 sort.by.query=FALSE)
{
    if (!isTRUEorFALSE(sort.by.query))
        stop("'sort.by.query' must be TRUE or FALSE")
    Class <- if (sort.by.query) "SortedByQueryHits" else "Hits"
    mcols <- .make_mcols(...)
    new_Hits(Class, from, to, nLnode, nRnode, mcols)
}

SelfHits <- function(from=integer(0), to=integer(0), nnode=0L, ...,
                     sort.by.query=FALSE)
{
    if (!isTRUEorFALSE(sort.by.query))
        stop("'sort.by.query' must be TRUE or FALSE")
    Class <- if (sort.by.query) "SortedByQuerySelfHits" else "SelfHits"
    mcols <- .make_mcols(...)
    new_Hits(Class, from, to, nnode, nnode, mcols)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Conversion from old to new internal representation
###

setMethod("updateObject", "Hits",
    function(object, ..., verbose=FALSE)
    {
        if (is(try(object@queryHits, silent=TRUE), "try-error"))
            return(object)
        ans <- new_Hits("SortedByQueryHits", object@queryHits,
                                             object@subjectHits,
                                             object@queryLength,
                                             object@subjectLength,
                                             object@elementMetadata)
        ans@metadata <- object@metadata
        ans
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion
###

.from_Hits_to_SortedByQueryHits <- function(from)
{
    new_Hits("SortedByQueryHits", from(from), to(from),
                                  nLnode(from), nRnode(from),
                                  mcols(from))
}
setAs("Hits", "SortedByQueryHits", .from_Hits_to_SortedByQueryHits)

setMethod("as.matrix", "Hits",
    function(x)
    {
        ans <- cbind(from=from(x), to=to(x))
        if (is(x, "SortedByQueryHits"))
            colnames(ans) <- c("queryHits", "subjectHits")
        ans
    }
)

setMethod("as.table", "Hits", .count_Lnode_hits)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Subsetting
###

### The "extractROWS" method for Vector objects doesn't test the validity of
### the result so we override it.
setMethod("extractROWS", "SortedByQueryHits",
    function(x, i)
    {
        ans <- callNextMethod()
        pbs <- validObject(ans, test=TRUE)
        if (is.character(pbs))
            stop(wmsg("Problem(s) found when testing validity of ", class(ans),
                      " object returned by subsetting operation: ",
                      paste0(pbs, collapse=", "), ". Make sure to use a ",
                      "subscript that results in a valid ", class(ans),
                      " object."))
        ans
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Displaying
###

setMethod("classNameForDisplay", "Hits", function(x) "Hits")
setMethod("classNameForDisplay", "SelfHits", function(x) "SelfHits")

.make_naked_matrix_from_Hits <- function(x)
{
    x_len <- length(x)
    x_mcols <- mcols(x)
    x_nmc <- if (is.null(x_mcols)) 0L else ncol(x_mcols)
    ans <- cbind(from=as.character(from(x)),
                 to=as.character(to(x)))
    if (is(x, "SortedByQueryHits"))
        colnames(ans) <- c("queryHits", "subjectHits")
    if (x_nmc > 0L) {
        tmp <- do.call(data.frame, c(lapply(x_mcols, showAsCell),
                                     list(check.names=FALSE)))
        ans <- cbind(ans, `|`=rep.int("|", x_len), as.matrix(tmp))
    }
    ans
}

showHits <- function(x, margin="", print.classinfo=FALSE,
                                   print.nnode=FALSE)
{
    x_class <- class(x)
    x_len <- length(x)
    x_mcols <- mcols(x)
    x_nmc <- if (is.null(x_mcols)) 0L else ncol(x_mcols)
    cat(classNameForDisplay(x), " object with ",
        x_len, " hit", ifelse(x_len == 1L, "", "s"),
        " and ",
        x_nmc, " metadata column", ifelse(x_nmc == 1L, "", "s"),
        ":\n", sep="")
    out <- makePrettyMatrixForCompactPrinting(x, .make_naked_matrix_from_Hits)
    if (print.classinfo) {
        .COL2CLASS <- c(
            from="integer",
            to="integer"
        )
        if (is(x, "SortedByQueryHits"))
            names(.COL2CLASS) <- c("queryHits", "subjectHits")
        classinfo <- makeClassinfoRowForCompactPrinting(x, .COL2CLASS)
        ## A sanity check, but this should never happen!
        stopifnot(identical(colnames(classinfo), colnames(out)))
        out <- rbind(classinfo, out)
    }
    if (nrow(out) != 0L)
        rownames(out) <- paste0(margin, rownames(out))
    ## We set 'max' to 'length(out)' to avoid the getOption("max.print")
    ## limit that would typically be reached when 'showHeadLines' global
    ## option is set to Inf.
    print(out, quote=FALSE, right=TRUE, max=length(out))
    if (print.nnode) {
        cat(margin, "-------\n", sep="")
        if (is(x, "SortedByQueryHits")) {
            cat(margin, "queryLength: ", nLnode(x),
                " / subjectLength: ", nRnode(x), "\n", sep="")
        } else {
            if (is(x, "SelfHits")) {
                cat(margin, "nnode: ", nnode(x), "\n", sep="")
            } else {
                cat(margin, "nLnode: ", nLnode(x),
                    " / nRnode: ", nRnode(x), "\n", sep="")
            }
        }
    }
}

setMethod("show", "Hits",
    function(object)
        showHits(object, margin="  ", print.classinfo=TRUE,
                                      print.nnode=TRUE)
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Combining
###
### Note that supporting "extractROWS" and "c" makes "replaceROWS" (and thus
### "[<-") work out-of-the-box!
###

### 'Class' must be "Hits" or the name of a concrete Hits subclass.
### 'objects' must be a list of Hits objects.
### Returns an instance of class 'Class'.
combine_Hits_objects <- function(Class, objects,
                                 use.names=TRUE, ignore.mcols=FALSE)
{
    if (!isSingleString(Class))
        stop("'Class' must be a single character string")
    if (!extends(Class, "Hits"))
        stop("'Class' must be the name of a class that extends Hits")
    if (!is.list(objects))
        stop("'objects' must be a list")
    if (!isTRUEorFALSE(use.names))
        stop("'use.names' must be TRUE or FALSE")
    ### TODO: Support 'use.names=TRUE'.
    if (use.names)
        stop("'use.names=TRUE' is not supported yet")
    if (!isTRUEorFALSE(ignore.mcols))
        stop("'ignore.mcols' must be TRUE or FALSE")

    NULL_idx <- which(sapply_isNULL(objects))
    if (length(NULL_idx) != 0L)
        objects <- objects[-NULL_idx]
    if (length(objects) == 0L)
        return(new(Class))

    ## TODO: Implement (in C) fast 'elementIs(objects, class)' in S4Vectors
    ## that does 'sapply(objects, is, class, USE.NAMES=FALSE)', and use it
    ## here. 'elementIs(objects, "NULL")' should work and be equivalent to
    ## 'elementIsNull(objects)'.
    if (!all(sapply(objects, is, Class, USE.NAMES=FALSE)))
        stop("the objects to combine must be ", Class, " objects (or NULLs)")
    objects_names <- names(objects)
    names(objects) <- NULL  # so lapply(objects, ...) below returns an
                            # unnamed list

    ## Combine "nLnode" slots.
    nLnode_slots <- lapply(objects, function(x) x@nLnode)
    ans_nLnode <- unlist(nLnode_slots, use.names=FALSE)

    ## Combine "nRnode" slots.
    nRnode_slots <- lapply(objects, function(x) x@nRnode)
    ans_nRnode <- unlist(nRnode_slots, use.names=FALSE)

    if (!(all(ans_nLnode == ans_nLnode[[1L]]) &&
          all(ans_nRnode == ans_nRnode[[1L]])))
        stop(wmsg("the objects to combine are incompatible Hits objects ",
                  "by number of left and/or right nodes"))
    ans_nLnode <- ans_nLnode[[1L]]
    ans_nRnode <- ans_nRnode[[1L]]

    ## Combine "from" slots.
    from_slots <- lapply(objects, function(x) x@from)
    ans_from <- unlist(from_slots, use.names=FALSE)

    ## Combine "to" slots.
    to_slots <- lapply(objects, function(x) x@to)
    ans_to <- unlist(to_slots, use.names=FALSE)

    ## Combine "mcols" slots.
    if (ignore.mcols) {
        ans_mcols <- NULL
    } else {
        ans_mcols <- do.call(S4Vectors:::rbind_mcols, objects)
    }

    ## Make 'ans' and return it.
    .new_Hits(Class, ans_from, ans_to, ans_nLnode, ans_nRnode, ans_mcols)
}

setMethod("c", "Hits",
    function (x, ..., ignore.mcols=FALSE, recursive=FALSE)
    {
        if (!identical(recursive, FALSE))
            stop("\"c\" method for Hits objects ",
                 "does not support the 'recursive' argument")
        if (missing(x)) {
            objects <- list(...)
            x <- objects[[1L]]
        } else {
            objects <- list(x, ...)
        }
        combine_Hits_objects(class(x), objects,
                             use.names=FALSE,
                             ignore.mcols=ignore.mcols)
    }
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### selectHits()
###

selectHits <- function(x, select=c("all", "first", "last", "arbitrary",
                                   "count"))
{
    if (!is(x, "Hits"))
        stop("'x' must be a Hits object")
    select <- match.arg(select)
    if (select == "all")
        return(x)
    .Call2("select_hits", from(x), to(x), nLnode(x), select,
                          PACKAGE="S4Vectors")
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### revmap()
###

### NOT exported (but used in IRanges).
### TODO: Move revmap() generic from AnnotationDbi to S4Vectors, and make this
### the "revmap" method for SortedByQueryHits objects.
### Note that:
###   - If 'x' is a valid SortedByQueryHits object (i.e. the hits in it are
###     sorted by query), then 'revmap_Hits(x)' returns a SortedByQueryHits
###     object where hits are "fully sorted" i.e. sorted by query first and
###     then by subject.
###   - Because revmap_Hits() reorders the hits by query, doing
###     'revmap_Hits(revmap_Hits(x))' brings back 'x' but with the hits in it
###     now "fully sorted".
revmap_Hits <- function(x)
    new_Hits(class(x), to(x), from(x), nRnode(x), nLnode(x), mcols(x))

### FIXME: Replace this with "revmap" method for Hits objects.
setMethod("t", "Hits", revmap_Hits)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Remap the left and/or right nodes of a Hits object.
###

### Returns 'arg' as a NULL, an integer vector, or a factor.
.normarg_nodes.remapping <- function(arg, side, old.nnode)
{
    if (is.null(arg))
        return(arg)
    if (!is.factor(arg)) {
        if (!is.numeric(arg))
            stop("'" , side, "nodes.remappping' must be a vector ",
                 "of integers")
        if (!is.integer(arg))
            arg <- as.integer(arg)
    }
    if (length(arg) != old.nnode)
        stop("'" , side, "nodes.remapping' must be of length 'n",
             side, "node(x)'")
    arg
}

.normarg_new.nnode <- function(arg, side, map)
{
    if (!isSingleNumberOrNA(arg))
        stop("'new.n", side, "node' must be a single number or NA")
    if (!is.integer(arg))
        arg <- as.integer(arg)
    if (is.null(map))
        return(arg)
    if (is.factor(map)) {
        if (is.na(arg))
            return(nlevels(map))
        if (arg < nlevels(map))
            stop("supplied 'new.n", side, "node' must ",
                 "be >= 'nlevels(", side, "nodes.remapping)'")
        return(arg)
    }
    if (is.na(arg))
        stop("'new.n", side, "node' must be specified when ",
             "'" , side, "s.remapping' is specified and is not a factor")
    arg
}

remapHits <- function(x, Lnodes.remapping=NULL, new.nLnode=NA,
                         Rnodes.remapping=NULL, new.nRnode=NA,
                         with.counts=FALSE)
{
    if (!is(x, "SortedByQueryHits"))
        stop("'x' must be a SortedByQueryHits object")
    Lnodes.remapping <- .normarg_nodes.remapping(Lnodes.remapping, "L",
                                                 nLnode(x))
    new.nLnode <- .normarg_new.nnode(new.nLnode, "L", Lnodes.remapping)
    Rnodes.remapping <- .normarg_nodes.remapping(Rnodes.remapping, "R",
                                                 nRnode(x))
    new.nRnode <- .normarg_new.nnode(new.nRnode, "R", Rnodes.remapping)
    if (!isTRUEorFALSE(with.counts))
        stop("'with.counts' must be TRUE or FALSE")
    x_from <- from(x)
    if (is.null(Lnodes.remapping)) {
        if (is.na(new.nLnode))
            new.nLnode <- nLnode(x)
    } else {
        if (is.factor(Lnodes.remapping))
            Lnodes.remapping <- as.integer(Lnodes.remapping)
        if (anyMissingOrOutside(Lnodes.remapping, 1L, new.nLnode))
            stop(wmsg("'Lnodes.remapping' cannot contain NAs, or values that ",
                      "are < 1, or > 'new.nLnode'"))
        x_from <- Lnodes.remapping[x_from]
    }
    x_to <- to(x)
    if (is.null(Rnodes.remapping)) {
        if (is.na(new.nRnode))
            new.nRnode <- nRnode(x)
    } else {
        if (is.factor(Rnodes.remapping))
            Rnodes.remapping <- as.integer(Rnodes.remapping)
        if (anyMissingOrOutside(Rnodes.remapping, 1L, new.nRnode))
            stop(wmsg("'Rnodes.remapping' cannot contain NAs, or values that ",
                      "are < 1, or > 'new.nRnode'"))
        x_to <- Rnodes.remapping[x_to]
    }
    x_mcols <- mcols(x)
    add_counts <- function(counts) {
        if (is.null(x_mcols))
            return(DataFrame(counts=counts))
        if ("counts" %in% colnames(x_mcols))
            warning("'x' has a \"counts\" metadata column, replacing it")
        x_mcols$counts <- counts
        x_mcols
    }
    if (is.null(Lnodes.remapping) && is.null(Rnodes.remapping)) {
        if (with.counts) {
            counts <- rep.int(1L, length(x))
            x_mcols <- add_counts(counts)
        }
    } else {
        sm <- selfmatchIntegerPairs(x_from, x_to)
        if (with.counts) {
            counts <- tabulate(sm, nbins=length(sm))
            x_mcols <- add_counts(counts)
            keep_idx <- which(counts != 0L)
        } else {
            keep_idx <- which(sm == seq_along(sm))
        }
        x_from <- x_from[keep_idx]
        x_to <- x_to[keep_idx]
        x_mcols <- extractROWS(x_mcols, keep_idx)
    }
    new_Hits(class(x), x_from, x_to, new.nLnode, new.nRnode, x_mcols)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### SelfHits methods
###
### TODO: Make isSelfHit() and isRedundantHit() generic functions with
### methods for SelfHits objects.
###

### A "self hit" is an edge from a node to itself. For example, the 2nd hit
### in the SelfHits object below is a self hit (from 3rd node to itself):
###     SelfHits(c(3, 3, 3, 4, 4), c(2:4, 2:3), 4)
isSelfHit <- function(x)
{
    if (!is(x, "SelfHits"))
        stop("'x' must be a SelfHits object")
    from(x) == to(x)
}

### When there is more than 1 edge between 2 given nodes (regardless of
### orientation), the extra edges are considered to be "redundant hits". For
### example, hits 3, 5, 7, and 8, in the SelfHits object below are redundant
### hits:
###     SelftHits(c(3, 3, 3, 3, 3, 4, 4, 4), c(3, 2:4, 2, 2:3, 2), 4, 4)
### Note that this is regardless of the orientation of the edge so hit 7 (edge
### 4-3) is considered to be redundant with hit 4 (edge 3-4).
isRedundantHit <- function(x)
{
    if (!is(x, "SelfHits"))
        stop("'x' must be a SelfHits object")
    duplicatedIntegerPairs(pmin.int(from(x), to(x)),
                           pmax.int(from(x), to(x)))
}

### Specialized constructor.
### Return a SortedByQuerySelfHits object.
### About 10x faster and uses 4x less memory than my first attempt in pure
### R below.
### NOT exported.
makeAllGroupInnerHits <- function(group.sizes, hit.type=0L)
{
    if (!is.integer(group.sizes))
        stop("'group.sizes' must be an integer vector")
    if (!isSingleNumber(hit.type))
        stop("'hit.type' must be a single integer")
    if (!is.integer(hit.type))
        hit.type <- as.integer(hit.type)
    .Call2("make_all_group_inner_hits", group.sizes, hit.type,
           PACKAGE="S4Vectors")
}

### Return a SortedByQuerySelfHits object.
### NOT exported.
### TODO: Remove this.
makeAllGroupInnerHits.old <- function(GS)
{
    NG <- length(GS)  # nb of groups
    ## First Element In group i.e. first elt associated with each group.
    FEIG <- cumsum(c(1L, GS[-NG]))
    GSr <- c(0L, GS[-NG])
    CGSr2 <- cumsum(GSr * GSr)
    GS2 <- GS * GS
    nnode <- sum(GS)  # length of original vector (i.e. before grouping)

    ## Original Group Size Assignment i.e. group size associated with each
    ## element in the original vector.
    OGSA <- rep.int(GS, GS)  # is of length 'nnode'
    ans_from <- rep.int(seq_len(nnode), OGSA)
    NH <- length(ans_from)  # same as sum(GS2)

    ## Hit Group Assignment i.e. group associated with each hit.
    HGA <- rep.int(seq_len(NG), GS2)
    ## Hit Group Size Assignment i.e. group size associated with each hit.
    HGSA <- GS[HGA]
    ans_to <- (0:(NH-1L) - CGSr2[HGA]) %% GS[HGA] + FEIG[HGA]
    SelfHits(ans_from, ans_to, nnode, sort.by.query=TRUE)
}

