### Define Methods that can be inherited for all subclasses

### Idea: Coercion between *VIRTUAL* classes -- as() chooses "closest" classes
### ----  should also work e.g. for  dense-triangular --> sparse-triangular !

##-> see als ./dMatrix.R, ./ddenseMatrix.R  and  ./lMatrix.R

setAs("ANY", "sparseMatrix", function(from) as(from, "CsparseMatrix"))

## If people did not use xtabs(), but table():
setAs("table", "sparseMatrix", function(from) {
    if(length(dim(from)) != 2L)
        stop("only 2-dimensional tables can be directly coerced to sparse matrices")
    as(unclass(from), "CsparseMatrix")
})

setAs("sparseMatrix", "generalMatrix", as_gSparse)

setAs("sparseMatrix", "symmetricMatrix", as_sSparse)

setAs("sparseMatrix", "triangularMatrix", as_tSparse)

spMatrix <- function(nrow, ncol,
                     i = integer(), j = integer(), x = numeric())
{
    dim <- c(as.integer(nrow), as.integer(ncol))
    ## The conformability of (i,j,x) with itself and with 'dim'
    ## is checked automatically by internal "validObject()" inside new(.):
    kind <- .M.kind(x)
    new(paste0(kind, "gTMatrix"), Dim = dim,
        x = if(kind == "d") as.double(x) else x,
        ## our "Tsparse" Matrices use  0-based indices :
        i = as.integer(i - 1L),
        j = as.integer(j - 1L))
}

sparseMatrix <- function(i = ep, j = ep, p, x, dims, dimnames,
                         symmetric = FALSE, index1 = TRUE,
                         giveCsparse = TRUE, check = TRUE, use.last.ij = FALSE)
{
  ## Purpose: user-level substitute for most  new(<sparseMatrix>, ..) calls
  ## Author: Douglas Bates, Date: 12 Jan 2009, based on Martin's version
    if((m.i <- missing(i)) + (m.j <- missing(j)) + (m.p <- missing(p)) != 1)
        stop("exactly one of 'i', 'j', or 'p' must be missing from call")
    if(!m.p) {
        p <- as.integer(p)
        if((lp <- length(p)) < 1 || p[1] != 0 || any((dp <- p[-1] - p[-lp]) < 0))
            stop("'p' must be a non-decreasing vector (0, ...)")
        ep <- rep.int(seq_along(dp), dp)
    }
    ## i and j are now both defined (via default = ep).  Make them 1-based indices.
    i1 <- as.logical(index1)[1]
    i <- as.integer(i + !(m.i || i1))
    j <- as.integer(j + !(m.j || i1))

    ## "minimal dimensions" from (i,j,p); no warnings from empty i or j :
    dims.min <- suppressWarnings(c(max(i), max(j)))
    if(anyNA(dims.min)) stop("NA's in (i,j) are not allowed")
    if(missing(dims)) {
        dims <- dims.min
    } else { ## check dims
        stopifnot(all(dims >= dims.min))
        dims <- as.integer(dims)
    }
    sx <- if(symmetric) {
        if(dims[1] != dims[2])
            stop("symmetric matrix must be square")
        "s"
    } else "g"
    isPat <- missing(x) ## <-> patter"n" Matrix
    kx <- if(isPat) "n" else .M.kind(x)
    r <- new(paste0(kx, sx, "TMatrix"))
    r@Dim <- dims
    if(symmetric && all(i >= j)) r@uplo <- "L" # else "U", the default
    if(!isPat) {
	if(kx == "d" && !is.double(x)) x <- as.double(x)
	if(length(x) != (n <- length(i))) { ## recycle
	    if(length(x) != 1 && n %% length(x) != 0)
		warning("length(i) is not a multiple of length(x)")
	    x <- rep_len(x, n)
	}
        if(use.last.ij && (id <- anyDuplicated(cbind(i,j), fromLast=TRUE))) {
            i <- i[-id]
            j <- j[-id]
            x <- x[-id]
            if(any(idup <- duplicated(cbind(i,j), fromLast=TRUE))) {
                ndup <- -which(idup)
                i <- i[ndup]
                j <- j[ndup]
                x <- x[ndup]
            }
        }
	r@x <- x
    }
    r@i <- i - 1L
    r@j <- j - 1L
    if(!missing(dimnames))
	r@Dimnames <- .fixupDimnames(dimnames)
    if(check) validObject(r)
    if(giveCsparse) as(r, "CsparseMatrix") else r
}

## "graph" coercions -- this needs the graph package which is currently
##  -----               *not* required on purpose
## Note: 'undirected' graph <==> 'symmetric' matrix

## Use 'graph::'  as it is not impoted into Matrix, and may only be loaded, not attached:

## Add some utils that may no longer be needed in future versions of the 'graph' package
graph.has.weights <- function(g) "weight" %in% names(graph::edgeDataDefaults(g))

graph.non.1.weights <- function(g) any(unlist(graph::edgeData(g, attr = "weight")) != 1)

graph.wgtMatrix <- function(g)
{
    ## Purpose: work around "graph" package's  as(g, "matrix") bug
    ## ----------------------------------------------------------------------
    ## Arguments: g: an object inheriting from (S4) class "graph"
    ## ----------------------------------------------------------------------
    ## Author: Martin Maechler, based on Seth Falcon's code;  Date: 12 May 2006

    ## MM: another buglet for the case of  "no edges":
    if(graph::numEdges(g) == 0) {
      p <- length(nd <- graph::nodes(g))
      return( matrix(0, p,p, dimnames = list(nd, nd)) )
    }

    ## Usual case, when there are edges:
    if(has.w <- graph.has.weights(g)) {
        ## graph.non.1.weights(g) :
        w <- unlist(graph::edgeData(g, attr = "weight"))
        has.w <- any(w != 1)
    } ## now 'has.w' is TRUE  iff  there are weights != 1
    ## now 'has.w' is TRUE  iff  there are weights != 1
    m <- as(g, "matrix")
    ## now is a 0/1 - matrix (instead of 0/wgts) with the 'graph' bug
    if(has.w) { ## fix it if needed
        tm <- t(m)
        tm[tm != 0] <- w
        t(tm)
    }
    else m
}


setAs("graphAM", "sparseMatrix",
      function(from) {
	  symm <- graph::edgemode(from) == "undirected" && isSymmetric(from@adjMat)
	  ## This is only ok if there are no weights...
	  if(graph.has.weights(from)) {
	      as(graph.wgtMatrix(from),
		 if(symm) "dsTMatrix" else "dgTMatrix")
	  }
	  else { ## no weights: 0/1 matrix -> logical
	      as(as(from, "matrix"),
		 if(symm) "nsTMatrix" else "ngTMatrix")
	  }
      })

setAs("graph", "CsparseMatrix",
      function(from) as(as(from, "graphNEL"), "CsparseMatrix"))
setAs("graph", "Matrix", function(from) as(from, "CsparseMatrix"))

setAs("graphNEL", "CsparseMatrix",
      function(from) as(as(from, "TsparseMatrix"), "CsparseMatrix"))

graph2T <- function(from, use.weights =
		    graph.has.weights(from) && graph.non.1.weights(from)) {
    nd <- graph::nodes(from); dnms <- list(nd,nd)
    dm <- rep.int(length(nd), 2)
    edge2i <- function(e) {
	## return (0-based) row indices 'i'
	rep.int(0:(dm[1]-1L), lengths(e))
    }

    if(use.weights) {
	eWts <- graph::edgeWeights(from); names(eWts) <- NULL
	i <- edge2i(eWts)
	To <- unlist(lapply(eWts, names))
	j <- as.integer(match(To,nd)) - 1L # columns indices (0-based)
	## symm <- symm && <weights must also be symmetric>: improbable
	## if(symm) new("dsTMatrix", .....) else
	new("dgTMatrix", i = i, j = j, x = unlist(eWts), Dim = dm, Dimnames = dnms)
    }
    else { ## no weights: 0/1 matrix -> logical
	edges <- lapply(from@edgeL[nd], "[[", "edges")
	symm <- graph::edgemode(from) == "undirected"
	if(symm)# each edge appears twice; keep upper triangle only
	    edges <- lapply(seq_along(edges), function(i) {e <- edges[[i]]; e[e >= i]})
	i <- edge2i(edges)
	j <- as.integer(unlist(edges)) - 1L # column indices (0-based)
	## if(symm) {			# symmetric: ensure upper triangle
	##     tmp <- i
	##     flip <- i > j
	##     i[flip] <- j[flip]
	##     j[flip] <- tmp[flip]
	##     new("nsTMatrix", i = i, j = j, Dim = dm, Dimnames = dnms, uplo = "U")
	## } else {
	##     new("ngTMatrix", i = i, j = j, Dim = dm, Dimnames = dnms)
	## }
	new(if(symm) "nsTMatrix" else "ngTMatrix",
	    i = i, j = j, Dim = dm, Dimnames = dnms)# uplo = "U" is default
    }
}
setAs("graphNEL", "TsparseMatrix", function(from) graph2T(from))

setAs("sparseMatrix", "graph", function(from) as(from, "graphNEL"))
setAs("sparseMatrix", "graphNEL",
      ## since have specific method for Tsparse below, 'from' is *not*,
      ## i.e. do not need to "uniquify" the T* matrix:
      function(from) T2graph(as(from, "TsparseMatrix"), need.uniq=FALSE))
setAs("TsparseMatrix", "graphNEL", function(from) T2graph(from))

T2graph <- function(from, need.uniq = is_not_uniqT(from), edgemode = NULL) {
    d <- dim(from)
    if(d[1] != d[2])
	stop("only square matrices can be used as incidence matrices for graphs")
    n <- d[1]
    if(n == 0) return(new("graphNEL"))
    if(is.null(rn <- dimnames(from)[[1]]))
	rn <- as.character(1:n)
    if(need.uniq) ## Need to 'uniquify' the triplets!
	from <- uniqTsparse(from)

    if(is.null(edgemode))
        edgemode <-
            if(isSymmetric(from)) { # either "symmetricMatrix" or otherwise
                ##-> undirected graph: every edge only once!
                if(!is(from, "symmetricMatrix")) {
                    ## a general matrix which happens to be symmetric
                    ## ==> remove the double indices
                    from <- tril(from)
                }
                "undirected"
            } else {
                "directed"
            }
    ## every edge is there only once, either upper or lower triangle
    ft1 <- cbind(rn[from@i + 1L], rn[from@j + 1L])
    graph::ftM2graphNEL(ft1, W = if(.hasSlot(from,"x")) as.numeric(from@x), ## else NULL
			V = rn, edgemode=edgemode)
}


### Subsetting -- basic things (drop = "missing") are done in ./Matrix.R

### FIXME : we defer to the "*gT" -- conveniently, but not efficient for gC !

## [dl]sparse -> [dl]gT   -- treat both in one via superclass
##                        -- more useful when have "z" (complex) and even more

setMethod("[", signature(x = "sparseMatrix", i = "index", j = "missing",
			 drop = "logical"),
	  function (x, i,j, ..., drop) {
	      Matrix.msg("sp[i,m,l] : nargs()=",nargs(), .M.level = 2)
	      cld <- getClassDef(class(x))
	      na <- nargs()
	      x <- if(na == 4) as(x, "TsparseMatrix")[i, , drop=drop]
	      else if(na == 3) as(x, "TsparseMatrix")[i, drop=drop]
	      else ## should not happen
		  stop("Matrix-internal error in <sparseM>[i,,d]; please report")
              ##
	      ## try_as(x, c(cl, sub("T","C", viaCl)))
	      if(is(x, "Matrix") && extends(cld, "CsparseMatrix"))
		  as(x, "CsparseMatrix") else x
	  })

setMethod("[", signature(x = "sparseMatrix", i = "missing", j = "index",
			 drop = "logical"),
	  function (x,i,j, ..., drop) {
	      Matrix.msg("sp[m,i,l] : nargs()=",nargs(), .M.level = 2)
	      cld <- getClassDef(class(x))
##> why should this be needed; can still happen in <Tsparse>[..]:
##>	      if(!extends(cld, "generalMatrix")) x <- as(x, "generalMatrix")
##	      viaCl <- paste0(.M.kind(x, cld), "gTMatrix")

	      x <- as(x, "TsparseMatrix")[, j, drop=drop]
##simpler than x <- callGeneric(x = as(x, "TsparseMatrix"), j=j, drop=drop)
	      if(is(x, "Matrix") && extends(cld, "CsparseMatrix"))
		  as(x, "CsparseMatrix") else x
	  })

setMethod("[", signature(x = "sparseMatrix",
			 i = "index", j = "index", drop = "logical"),
	  function (x, i, j, ..., drop) {
	      Matrix.msg("sp[i,i,l] : nargs()=",nargs(), .M.level = 2)
	      cld <- getClassDef(class(x))
	      ## be smart to keep symmetric indexing of <symm.Mat.> symmetric:
##>	      doSym <- (extends(cld, "symmetricMatrix") &&
##>			length(i) == length(j) && all(i == j))
##> why should this be needed; can still happen in <Tsparse>[..]:
##>	      if(!doSym && !extends(cld, "generalMatrix"))
##>		  x <- as(x, "generalMatrix")
##	      viaCl <- paste0(.M.kind(x, cld),
##			      if(doSym) "sTMatrix" else "gTMatrix")
	      x <- as(x, "TsparseMatrix")[i, j, drop=drop]
	      if(is(x, "Matrix") && extends(cld, "CsparseMatrix"))
		  as(x, "CsparseMatrix") else x
	  })

### "[<-" : -----------------

## setReplaceMethod("[", .........)
## -> ./Tsparse.R
## &  ./Csparse.R  & ./Rsparse.R {those go via Tsparse}

## x[] <- value :
setReplaceMethod("[", signature(x = "sparseMatrix", i = "missing", j = "missing",
				value = "ANY"),## double/logical/...
	  function (x, i,j,..., value) {
	      if(all0(value)) { # be faster
		  cld <- getClassDef(class(x))
		  x <- diagU2N(x, cl = cld)
		  for(nm in intersect(nsl <- names(cld@slots),
				      c("x", "i","j", "factors")))
		      length(slot(x, nm)) <- 0L
		  if("p" %in% nsl)
		      x@p <- rep.int(0L, ncol(x)+1L)
	      } else { ## typically non-sense: assigning to full sparseMatrix
		  x[TRUE] <- value
	      }
	      x
	  })

## Do not use as.vector() (see ./Matrix.R ) for sparse matrices :
setReplaceMethod("[", signature(x = "sparseMatrix", i = "missing", j = "ANY",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		     callGeneric(x=x, , j=j, value = as(value, "sparseVector")))

setReplaceMethod("[", signature(x = "sparseMatrix", i = "ANY", j = "missing",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		     if(nargs() == 3)
			 callGeneric(x=x, i=i, value = as(value, "sparseVector"))
		     else
			 callGeneric(x=x, i=i, , value = as(value, "sparseVector")))

setReplaceMethod("[", signature(x = "sparseMatrix", i = "ANY", j = "ANY",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		 callGeneric(x=x, i=i, j=j, value = as(value, "sparseVector")))



### --- print() and show() methods ---

.formatSparseSimple <- function(m, asLogical=FALSE, digits=NULL,
				col.names, note.dropping.colnames = TRUE,
				dn=dimnames(m))
{
    stopifnot(is.logical(asLogical))
    if(asLogical)
	cx <- array("N", dim(m), dimnames=dn)
    else { ## numeric (or --not yet implemented-- complex):
	cx <- apply(m, 2, format, digits=digits)
	if(is.null(dim(cx))) {# e.g. in	1 x 1 case
	    dim(cx) <- dim(m)
	    dimnames(cx) <- dn
	} else ## workaround bug in apply() which has lost row names:
	    if(getRversion() < "3.2" && !is.null(names(dn))) {
		if(is.null(dimnames(cx)))
		    dimnames(cx) <- dn
		else
		    names(dimnames(cx)) <- names(dn)
            }
    }
    if (missing(col.names))
	col.names <- {
	    if(!is.null(cc <- getOption("sparse.colnames")))
		cc
	    else if(is.null(dn[[2]]))
		FALSE
	    else { # has column names == dn[[2]]
		ncol(m) < 10
	    }
	}
    if(identical(col.names, FALSE))
	cx <- emptyColnames(cx, msg.if.not.empty = note.dropping.colnames)
    else if(is.character(col.names)) {
	stopifnot(length(col.names) == 1)
	cn <- col.names
	switch(substr(cn, 1,3),
	       "abb" = {
		   iarg <- as.integer(sub("^[^0-9]*", '', cn))
		   colnames(cx) <- abbreviate(colnames(cx), minlength = iarg)
	       },
	       "sub" = {
		   iarg <- as.integer(sub("^[^0-9]*", '', cn))
		   colnames(cx) <- substr(colnames(cx), 1, iarg)
	       },
	       stop(gettextf("invalid 'col.names' string: %s", cn), domain=NA))
    }
    ## else: nothing to do for col.names == TRUE
    cx
}## .formatSparseSimple


### NB: Want this to work also for logical or numeric traditional matrix 'x':
formatSparseM <- function(x, zero.print = ".", align = c("fancy", "right"),
                          m = as(x,"matrix"), asLogical=NULL, digits=NULL,
                          cx, iN0, dn = dimnames(m))
{
    cld <- getClassDef(class(x))
    if(is.null(asLogical)) {
        binary <- extends(cld,"nsparseMatrix") || extends(cld, "indMatrix")# -> simple T / F
        asLogical <- { binary || extends(cld,"lsparseMatrix") ||
                       extends(cld,"matrix") && is.logical(x) }
					# has NA and (non-)structural FALSE
    }
    if(missing(cx))
        cx <- .formatSparseSimple(m, asLogical=asLogical, digits=digits, dn=dn)
    if(is.null(d <- dim(cx))) {# e.g. in 1 x 1 case
	d <- dim(cx) <- dim(m)
	dimnames(cx) <- dn
    }
    if(missing(iN0))
	iN0 <- 1L + .Call(m_encodeInd, non0ind(x, cld), di = d, FALSE, FALSE)
    ## ne <- length(iN0)

    if(asLogical) {
        cx[m] <- "|"
        if(!extends(cld, "sparseMatrix"))
            x <- as(x,"sparseMatrix")
        if(anyFalse(x@x)) { ## any (x@x == FALSE)
            ## Careful for *non-sorted* Tsparse, e.g. from U-diag
            if(extends(cld, "TsparseMatrix")) {
                ## have no "fast  uniqTsparse():
                x <- as(x, "CsparseMatrix")
                cld <- getClassDef(class(x))
            }
            F. <- is0(x@x)              # the 'FALSE' ones
### FIXME: have  iN0 already above -- *really* need the following ??? --FIXME--
            ij <- non0.i(x, cld, uniqT=FALSE)
            if(extends(cld, "symmetricMatrix")) {
                ## also get "other" triangle
                notdiag <- ij[,1] != ij[,2] # but not the diagonals again
                ij <- rbind(ij, ij[notdiag, 2:1], deparse.level=0)
                F. <-	  c(F., F.[notdiag])
            }
            iN0 <- 1L + .Call(m_encodeInd, ij, di = d, FALSE, FALSE)
            cx[iN0[F.]] <- ":" # non-structural FALSE (or "o", "," , "-" or "f")?
        }
    }
    else if(match.arg(align) == "fancy" && !is.integer(m)) {
        fi <- apply(m, 2, format.info) ## fi[3,] == 0  <==> not expo.

        ## now 'format' the zero.print by padding it with ' ' on the right:
        ## case 1: non-exponent:  fi[2,] + as.logical(fi[2,] > 0)
        ## the column numbers of all 'zero' entries -- (*large*)
        cols <- 1L + (0:(prod(d)-1L))[-iN0] %/% d[1]
        pad <-
            ifelse(fi[3,] == 0,
                   fi[2,] + as.logical(fi[2,] > 0),
                   ## exponential:
                   fi[2,] + fi[3,] + 4)
        ## now be efficient ; sprintf() is relatively slow
        ## and pad is much smaller than 'cols'; instead of "simply"
        ## zero.print <- sprintf("%-*s", pad[cols] + 1, zero.print)
        if(any(doP <- pad > 0)) {       #
            ## only pad those that need padding - *before* expanding
            z.p.pad <- rep.int(zero.print, length(pad))
            z.p.pad[doP] <- sprintf("%-*s", pad[doP] + 1, zero.print)
            zero.print <- z.p.pad[cols]
        }
        else
            zero.print <- rep.int(zero.print, length(cols))
    } ## else "right" : nothing to do
    cx[-iN0] <- zero.print
    cx
}## formatSparseM()

## utility used inside sparseMatrix print()ing which might be useful
## outside the Matrix package:
formatSpMatrix <- function(x, digits = NULL, # getOption("digits"),
                           maxp = 1e9, # ~ 1/2 * .Machine$integer.max, ## getOption("max.print"),
                           cld = getClassDef(class(x)), zero.print = ".",
                           col.names, note.dropping.colnames = TRUE,
                           align = c("fancy", "right"))
{
    stopifnot(extends(cld, "sparseMatrix"))
    validObject(x) # have seen seg.faults for invalid objects
    d <- dim(x)
    if(extends(cld, "triangularMatrix") && x@diag == "U") {
	if(extends(cld, "CsparseMatrix"))
	    x <- .Call(Csparse_diagU2N, x)
	else if(extends(cld, "TsparseMatrix"))
	    x <- .Call(Tsparse_diagU2N, x)
	else {
	    kind <- .M.kind(x, cld)
	    x <- .Call(Tsparse_diagU2N,
		       as(as(x, paste0(kind, "Matrix")), "TsparseMatrix"))
	    cld <- getClassDef(class(x))
	}
    }
    ## TODO?  Could note it is *unit*-diagonal, e.g., by using "I" instead of "1" ?

    if(prod(d) > maxp) { # "Large" => will be "cut"
        ## only coerce to dense that part which won't be cut :
        nr <- maxp %/% d[2]
	m <- as(x[1:max(1, nr), ,drop=FALSE], "matrix")
    } else {
        m <- as(x, "matrix")
    }
    dn <- dimnames(m) ## will be === dimnames(cx)
    binary <- extends(cld,"nsparseMatrix") || extends(cld, "indMatrix") # -> simple T / F
    logi <- binary || extends(cld,"lsparseMatrix") # has NA and (non-)structural FALSE
    cx <- .formatSparseSimple(m, asLogical = logi, digits=digits,
                              col.names=col.names,
                              note.dropping.colnames=note.dropping.colnames, dn=dn)
    if(is.logical(zero.print))
	zero.print <- if(zero.print) "0" else " "
    if(binary) {
	cx[!m] <- zero.print
	cx[m] <- "|"
    } else { # non-binary ==> has 'x' slot
	## show only "structural" zeros as 'zero.print', not all of them..
	## -> cannot use 'm' alone
        d <- dim(cx)
	ne <- length(iN0 <- 1L + .Call(m_encodeInd, non0ind(x, cld),
				       di = d, FALSE, FALSE))
	if(0 < ne && (logi || ne < prod(d))) {
	    cx <- formatSparseM(x, zero.print, align, m=m, asLogical=logi,
				digits=digits, cx=cx, iN0=iN0, dn=dn)
	} else if (ne == 0)# all zeroes
	    cx[] <- zero.print
    }
    cx
}## formatSpMatrix()


## FIXME(?) -- ``merge this'' (at least ``synchronize'') with
## - - -   prMatrix() from ./Auxiliaries.R
## FIXME: prTriang() in ./Auxiliaries.R  should also get  align = "fancy"
##
printSpMatrix <- function(x, digits = NULL, # getOption("digits"),
			  maxp = getOption("max.print"),
			  cld = getClassDef(class(x)), zero.print = ".",
			  col.names, note.dropping.colnames = TRUE,
			  col.trailer = '', align = c("fancy", "right"))
{
    stopifnot(extends(cld, "sparseMatrix"))
    x.orig <- x # to be returned
    cx <- formatSpMatrix(x, digits=digits, maxp=maxp, cld=cld,
			 zero.print=zero.print, col.names=col.names,
			 note.dropping.colnames=note.dropping.colnames,
			 align=align)
    if(col.trailer != '')
        cx <- cbind(cx, col.trailer, deparse.level = 0)
    ## right = TRUE : cheap attempt to get better "." alignment
    print(cx, quote = FALSE, right = TRUE, max = maxp)
    invisible(x.orig)
} ## printSpMatrix()

##' The "real" show() / print() method, calling the above printSpMatrix():
printSpMatrix2 <- function(x, digits = NULL, # getOption("digits"),
                           maxp = getOption("max.print"), zero.print = ".",
                           col.names, note.dropping.colnames = TRUE,
                           suppRows = NULL, suppCols = NULL,
                           col.trailer = if(suppCols) "......" else "",
                           align = c("fancy", "right"))
{
    d <- dim(x)
    cl <- class(x)
    cld <- getClassDef(cl)
    xtra <- if(extends(cld, "triangularMatrix") && x@diag == "U")
	" (unitriangular)" else ""
    cat(sprintf('%d x %d sparse Matrix of class "%s"%s\n',
                d[1], d[2], cl, xtra))
    if((identical(suppRows,FALSE) && identical(suppCols, FALSE)) ||
       (!isTRUE(suppRows) && !isTRUE(suppCols) && prod(d) <= maxp))
    {
        if(missing(col.trailer) && is.null(suppCols))
            suppCols <- FALSE # for 'col.trailer'
        printSpMatrix(x, cld=cld, digits=digits, maxp=maxp,
                      zero.print=zero.print, col.names=col.names,
                      note.dropping.colnames=note.dropping.colnames,
                      col.trailer=col.trailer, align=align)
    } else { ## d[1] > maxp / d[2] >= nr : -- this needs [,] working:
	validObject(x)
	nR <- d[1] ## nrow
	useW <- getOption("width") - (format.info(nR, digits=digits)[1] + 3+1)
	##			     space for "[<last>,] "

	## --> suppress rows and/or columns in printing ...

	if(is.null(suppCols)) suppCols <- (d[2] * 2 > useW)
	nc <- if(suppCols) (useW - (1 + nchar(col.trailer))) %/% 2 else d[2]
	nr <- maxp %/% nc
	if(is.null(suppRows)) suppRows <- (nr < nR)

	sTxt <- c("in show(); maybe adjust 'options(max.print= *)'",
		  "\n ..............................\n")
	if(suppRows) {
	    if(suppCols)
                x <- x[ , 1:nc, drop = FALSE]
	    n2 <- ceiling(nr / 2)
	    printSpMatrix(x[seq_len(min(nR, max(1, n2))), , drop=FALSE],
			  digits=digits, maxp=maxp,
			  zero.print=zero.print, col.names=col.names,
			  note.dropping.colnames=note.dropping.colnames,
			  col.trailer = col.trailer, align=align)
	    cat("\n ..............................",
		"\n ........suppressing rows ", sTxt, "\n", sep='')
	    ## tail() automagically uses "[..,]" rownames:
	    printSpMatrix(tail(x, max(1, nr-n2)),
			  digits=digits, maxp=maxp,
			  zero.print=zero.print, col.names=col.names,
			  note.dropping.colnames=note.dropping.colnames,
			  col.trailer = col.trailer, align=align)
	}
	else if(suppCols) {
	    printSpMatrix(x[ , 1:nc , drop = FALSE],
			  digits=digits, maxp=maxp,
			  zero.print=zero.print, col.names=col.names,
			  note.dropping.colnames=note.dropping.colnames,
			  col.trailer = col.trailer, align=align)
	    cat("\n .....suppressing columns ", sTxt, sep='')
	}
	else stop("logic programming error in printSpMatrix2(), please report")

	invisible(x)
    }
} ## printSpMatrix2 ()

setMethod("format", signature(x = "sparseMatrix"), formatSpMatrix)

setMethod("print", signature(x = "sparseMatrix"), printSpMatrix2)

setMethod("show", signature(object = "sparseMatrix"),
	  function(object) printSpMatrix2(object))



## For very large and very sparse matrices,  the above show()
## is not really helpful;  Use  summary() as an alternative:

setMethod("summary", signature(object = "sparseMatrix"),
	  function(object, ...) {
	      d <- dim(object)
	      T <- as(object, "TsparseMatrix")
	      ## return a data frame (int, int,	 {double|logical|...})	:
	      r <- if(is(object,"nsparseMatrix"))
		  data.frame(i = T@i + 1L, j = T@j + 1L)
	      else data.frame(i = T@i + 1L, j = T@j + 1L, x = T@x)
	      attr(r, "header") <-
		  sprintf('%d x %d sparse Matrix of class "%s", with %d entries',
			  d[1], d[2], class(object), length(T@i))
	      ## use ole' S3 technology for such a simple case
	      class(r) <- c("sparseSummary", class(r))
	      r
	  })

print.sparseSummary <- function (x, ...) {
    cat(attr(x, "header"),"\n")
    print.data.frame(x, ...)
    invisible(x)
}



### FIXME [from ../TODO ]: Use cholmod_symmetry() --
## Possibly even use 'option' as argument here for fast check to use sparse solve !!

##' This case should be particularly fast
setMethod("isSymmetric", signature(object = "dgCMatrix"),
	  function(object, tol = 100*.Machine$double.eps, ...)
	      isTRUE(all.equal(.dgC.0.factors(object), t(object), tolerance = tol, ...)))

setMethod("isSymmetric", signature(object = "sparseMatrix"),
	  function(object, tol = 100*.Machine$double.eps, ...) {
	      ## pretest: is it square?
	      d <- dim(object)
	      if(d[1] != d[2]) return(FALSE)

	      ## else slower test using t()  --

	      ## FIXME (for tol = 0): use cholmod_symmetry(A, 1, ...)
	      ##        for tol > 0   should modify  cholmod_symmetry(..) to work with tol

	      ## or slightly simpler, rename and export	 is_sym() in ../src/cs_utils.c


	      if (is(object, "dMatrix"))
		  ## use gC; "T" (triplet) is *not* unique!
		  isTRUE(all.equal(.as.dgC.0.factors(  object),
				   .as.dgC.0.factors(t(object)),
				   tolerance = tol, ...))
	      else if (is(object, "lMatrix"))
		  ## test for exact equality; FIXME(?): identical() too strict?
		  identical(as(	 object,  "lgCMatrix"),
			    as(t(object), "lgCMatrix"))
	      else if (is(object, "nMatrix"))
		  ## test for exact equality; FIXME(?): identical() too strict?
		  identical(as(	 object,  "ngCMatrix"),
			    as(t(object), "ngCMatrix"))
	      else stop("not yet implemented")
	  })


setMethod("isTriangular", signature(object = "CsparseMatrix"), isTriC)
setMethod("isTriangular", signature(object = "TsparseMatrix"), isTriT)

setMethod("isDiagonal", signature(object = "sparseMatrix"),
	  function(object) {
              d <- dim(object)
              if(d[1] != d[2]) return(FALSE)
              ## else
	      gT <- as(object, "TsparseMatrix")
	      all(gT@i == gT@j)
	  })


setMethod("determinant", signature(x = "sparseMatrix", logarithm = "missing"),
	  function(x, logarithm, ...)
	  determinant(x, logarithm = TRUE, ...))
setMethod("determinant", signature(x = "sparseMatrix", logarithm = "logical"),
	  function(x, logarithm = TRUE, ...)
	  determinant(as(x,"dsparseMatrix"), logarithm, ...))

setMethod("Cholesky", signature(A = "sparseMatrix"),
	  function(A, perm = TRUE, LDL = !super, super = FALSE, Imult = 0, ...)
	  Cholesky(as(A, "CsparseMatrix"),
		   perm=perm, LDL=LDL, super=super, Imult=Imult, ...))

setMethod("diag", signature(x = "sparseMatrix"),
	  function(x, nrow, ncol) diag(as(x, "CsparseMatrix")))

setMethod("dim<-", signature(x = "sparseMatrix", value = "ANY"),
	  function(x, value) {
	      if(!is.numeric(value) || length(value) != 2)
		  stop("dim(.) value must be numeric of length 2")
	      if(prod(dim(x)) != prod(value <- round(value))) # *not* as.integer !
		  stop("dimensions don't match the number of cells")
	      ## be careful to keep things sparse
	      r <- spV2M(as(x, "sparseVector"), nrow=value[1], ncol=value[2])
	      ## r now is "dgTMatrix"
	      if(is(x, "CsparseMatrix")) as(r, "CsparseMatrix") else r
	  })


setMethod("norm", signature(x = "sparseMatrix", type = "character"),
	  function(x, type, ...) {
	      type <- toupper(substr(type[1], 1, 1))
	      switch(type,  ##  max(<empty>, 0)  |-->  0
		     "O" = ,
                     "1" = max(colSums(abs(x)), 0), ## One-norm (L_1)
		     "I" = max(rowSums(abs(x)), 0), ## L_Infinity
		     "F" = sqrt(sum(x^2)), ## Frobenius
		     "M" = max(abs(x), 0), ## Maximum modulus of all
		     ## otherwise:
		     stop("invalid 'type'"))
	  })

## FIXME: need a version of LAPACK's rcond() algorithm, using sparse-arithmetic
setMethod("rcond", signature(x = "sparseMatrix", norm = "character"),
	  function(x, norm, useInv=FALSE, ...) {
              ## as workaround, allow use of  1/(norm(A) * norm(solve(A)))
              if(!identical(FALSE,useInv)) {
                  Ix <- if(isTRUE(useInv)) solve(x) else
                  if(is(useInv, "Matrix")) useInv
                  return( 1/(norm(x, type=norm) * norm(Ix, type=norm)) )
              }
              ## else
	      d <- dim(x)
              ## FIXME: qr.R(qr(.)) warns about differing R (permutation!)
              ##        really fix qr.R() *or* go via dense even in those cases
	      rcond(if(d[1] == d[2]) {
			warning("rcond(.) via sparse -> dense coercion")
			as(x, "denseMatrix")
		    } else if(d[1] > d[2]) qr.R(qr(x)) else qr.R(qr(t(x))),
		    norm = norm, ...)
	  })

setMethod("cov2cor", signature(V = "sparseMatrix"),
	  function(V) {
	      ## like stats::cov2cor() but making sure all matrices stay sparse
	      p <- (d <- dim(V))[1]
	      if (p != d[2])
		  stop("'V' is not a *square* matrix")
	      if(!is(V, "dMatrix"))
		  V <- as(V, "dMatrix")# actually "dsparseMatrix"
	      Is <- sqrt(1/diag(V))
	      if (any(!is.finite(Is))) ## original had 0 or NA
		  warning("diag(.) had 0 or NA entries; non-finite result is doubtful")
	      Is <- Diagonal(x = Is)
	      r <- Is %*% V %*% Is
	      r[cbind(1:p,1:p)] <- 1 # exact in diagonal
	      as(r, "symmetricMatrix")
	  })

setMethod("is.na", signature(x = "sparseMatrix"),## NB: nsparse* have own method!
	  function(x) {
	      if(any((inax <- is.na(x@x)))) {
		  cld <- getClassDef(class(x))
		  if(extends(cld, "triangularMatrix") && x@diag == "U")
		      inax <- is.na((x <- .diagU2N(x, cld))@x)
		  r <- as(x, "lMatrix") # will be "lsparseMatrix" - *has* x slot
		  r@x <- if(length(inax) == length(r@x)) inax else is.na(r@x)
                  if(!extends(cld, "CsparseMatrix"))
                      r <- as(r, "CsparseMatrix")
		  as(.Call(Csparse_drop, r, 0), "nMatrix") # a 'pattern matrix
	      }
	      else is.na_nsp(x)
	  })

## all.equal(): similar to all.equal_Mat() in ./Matrix.R ;
## -----------	eventually defer to  "sparseVector" methods:
setMethod("all.equal", c(target = "sparseMatrix", current = "sparseMatrix"),
	  function(target, current, check.attributes = TRUE, ...)
      {
	  msg <- attr.all_Mat(target, current, check.attributes=check.attributes, ...)
	  if(is.list(msg)) msg[[1]]
	  else .a.e.comb(msg,
			 all.equal(as(target, "sparseVector"), as(current, "sparseVector"),
				   check.attributes=check.attributes, ...))
      })
setMethod("all.equal", c(target = "sparseMatrix", current = "ANY"),
	  function(target, current, check.attributes = TRUE, ...)
      {
	  msg <- attr.all_Mat(target, current, check.attributes=check.attributes, ...)
	  if(is.list(msg)) msg[[1]]
	  else .a.e.comb(msg,
			 all.equal(as(target, "sparseVector"), current,
				   check.attributes=check.attributes, ...))
      })
setMethod("all.equal", c(target = "ANY", current = "sparseMatrix"),
	  function(target, current, check.attributes = TRUE, ...)
      {
	  msg <- attr.all_Mat(target, current, check.attributes=check.attributes, ...)
	  if(is.list(msg)) msg[[1]]
	  else .a.e.comb(msg,
			 all.equal(target, as(current, "sparseVector"),
				   check.attributes=check.attributes, ...))
      })


setMethod("writeMM", "sparseMatrix",
	  function(obj, file, ...)
	  writeMM(as(obj, "CsparseMatrix"), as.character(file), ...))

### --- sparse model matrix,  fac2sparse, etc ----> ./spModels.R

###  xtabs(*, sparse = TRUE) ---> part of standard package 'stats' since R 2.10.0

##' @title Random Sparse Matrix
##' @param nrow,
##' @param ncol number of rows and columns, i.e., the matrix dimension
##' @param nnz number of non-zero entries
##' @param rand.x random number generator for 'x' slot
##' @param ... optionally further arguments passed to sparseMatrix()
##' @return a sparseMatrix of dimension (nrow, ncol)
##' @author Martin Maechler
##' @examples M1 <- rsparsematrix(1000, 20, nnz = 200)
##'           summary(M1)
if(FALSE) ## better version below
rsparsematrix <- function(nrow, ncol, nnz,
                          rand.x = function(n) signif(rnorm(nnz), 2),
                          warn.nnz = TRUE, ...)
{
    maxi.sample <- 2^31 # maximum n+1 for which sample(n) returns integer
    stopifnot((nnz <- as.integer(nnz)) >= 0,
	      nrow >= 0, ncol >= 0, nnz <= nrow * ncol,
	      nrow < maxi.sample, ncol < maxi.sample)
    ## to ensure that nnz is strictly followed, must act on duplicated (i,j):
    i <- sample.int(nrow, nnz, replace = TRUE)
    j <- sample.int(ncol, nnz, replace = TRUE)
    dim <- c(nrow, ncol)
    it <- 0
    while((it <- it+1) < 100 &&
	  anyDuplicated(n.ij <- encodeInd2(i, j, dim, checkBnds=FALSE))) {
	m <- length(k.dup <- which(duplicated(n.ij)))
	Matrix.msg(sprintf("%3g duplicated (i,j) pairs", m), .M.level = 2)
	if(runif(1) <= 1/2)
	    i[k.dup] <- sample.int(nrow, m, replace = TRUE)
	else
	    j[k.dup] <- sample.int(ncol, m, replace = TRUE)
    }
    if(warn.nnz && it == 100 && anyDuplicated(encodeInd2(i, j, dim, checkBnds=FALSE)))
	warning("number of non zeros is smaller than 'nnz' because of duplicated (i,j)s")
    sparseMatrix(i = i, j = j, x = rand.x(nnz), dims = dim, ...)
}

## No warn.nnz needed, as we sample the encoded (i,j) with*out* replacement:
rsparsematrix <- function(nrow, ncol, density,
                          nnz = round(density * maxE), symmetric = FALSE,
                          rand.x = function(n) signif(rnorm(nnz), 2), ...)
{
    maxE <- if(symmetric) nrow*(nrow+1)/2 else nrow*ncol
    stopifnot((nnz <- as.integer(nnz)) >= 0,
	      nrow >= 0, ncol >= 0, nnz <= maxE)
    ## sampling with*out* replacement (replace=FALSE !):
    ijI <- -1L +
	if(symmetric) sample(indTri(nrow, diag=TRUE), nnz)
	else sample.int(maxE, nnz)
    ## i,j below correspond to  ij <- decodeInd(code, nr) :
    if(is.null(rand.x))
	sparseMatrix(i = ijI  %% nrow,
		     j = ijI %/% nrow,
		     index1 = FALSE, symmetric = symmetric, dims = c(nrow, ncol), ...)
    else
	sparseMatrix(i = ijI  %% nrow,
		     j = ijI %/% nrow,
		     index1 = FALSE, symmetric = symmetric,
		     x = rand.x(nnz), dims = c(nrow, ncol), ...)
}

