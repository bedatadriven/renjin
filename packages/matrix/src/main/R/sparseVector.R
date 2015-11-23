#### All Methods in relation with the sparseVector (sub)classes


## atomicVector : classUnion (logical,integer,double,....)
setAs("atomicVector", "sparseVector",
      function(from) {
	  n <- length(from)# *is* integer for atomic vectors
	  r <- new(paste0(.V.kind(from), "sparseVector"), length = n)
	  ii <- isN0(from)
	  r@x <- from[ii]
	  r@i <- seq_len(n)[ii]
	  r
      })
## dsparseVector: currently important, as later potentially made into d..Matrix :
setAs("atomicVector", "dsparseVector",
      function(from) {
	  n <- length(from)# *is* integer for atomic vectors
	  r <- new("dsparseVector", length = n)
	  ii <- isN0(from)
	  r@x <- as.numeric(from)[ii]
	  r@i <- seq_len(n)[ii]
	  r
      })

setAs("nsparseVector", "lsparseVector",
      function(from) new("lsparseVector", i = from@i, length = from@length,
			 x = rep.int(TRUE, length(from@i))))
setAs("nsparseVector", "dsparseVector", function(from)
      as(as(from, "lsparseVector"), "dsparseVector"))
setAs("nsparseVector", "isparseVector", function(from)
      as(as(from, "lsparseVector"), "isparseVector"))
setAs("nsparseVector", "zsparseVector", function(from)
      as(as(from, "lsparseVector"), "zsparseVector"))


## "xsparseVector" : those with an 'x' slot (i.e., currently := not nsparse*)
setAs("xsparseVector", "dsparseVector",
      function(from)
      new("dsparseVector", x= as.double(from@x) , i= from@i, length= from@length))
setAs("xsparseVector", "isparseVector",
      function(from)
      new("isparseVector", x= as.integer(from@x), i= from@i, length= from@length))
setAs("xsparseVector", "lsparseVector",
      function(from)
      new("lsparseVector", x= as.logical(from@x), i= from@i, length= from@length))
setAs("xsparseVector", "zsparseVector",
      function(from)
      new("zsparseVector", x= as.complex(from@x), i= from@i, length= from@length))

setAs("xsparseVector", "nsparseVector",
      function(from) {
	  if(anyNA(from@x))
              stop("cannot coerce 'NA's to \"nsparseVector\"")
          new("nsparseVector", i = from@i, length = from@length)
      })

setMethod("is.na", signature(x = "nsparseVector"),
	  function(x) new("nsparseVector", length = x@length))## all FALSE
setMethod("is.na", signature(x = "sparseVector"),
	  ## x is *not* "nsparse*" as that has own method
	  function(x) new("nsparseVector", i = x@i[is.na(x@x)], length= x@length))


if(getRversion() >= "3.1.0") {
setMethod("anyNA", signature(x = "nsparseVector"), function(x) FALSE)
setMethod("anyNA", signature(x = "sparseVector"), function(x) anyNA(x@x))
}

setMethod("is.infinite", signature(x = "nsparseVector"),
	  function(x) new("nsparseVector", length = x@length))## all FALSE
setMethod("is.infinite", signature(x = "sparseVector"),
	  ## x is *not* "nsparse*" as that has own method
	  function(x) new("nsparseVector", i = x@i[is.infinite(x@x)], length= x@length))

setMethod("is.finite", signature(x = "nsparseVector"),
	  function(x) rep.int(TRUE, x@length))## all TRUE
setMethod("is.finite", signature(x = "sparseVector"),
	  function(x)  {
	      ## x is *not* "nsparse*" as that has own method
	      r <- rep.int(TRUE, x@length) ## mostly TRUE
	      r[x@i[!is.finite(x@x)]] <- FALSE
	      r
	  })


sp2vec <- function(x, mode = .type.kind[.M.kindC(cl)]) {
    ## sparseVector  ->  vector
    cl <- class(x)
    has.x <- any(.slotNames(cl) == "x") # cheap test for 'has x slot'
    m.any <- (mode == "any")
    if(m.any)
	mode <- if(has.x) mode(x@x) else "logical"
    r <- vector(mode, x@length)
    r[x@i] <-
	if(has.x) {
	    if(m.any || is(x@x, mode)) x@x else as(x@x, mode)
	} else TRUE
    r
}

## so base functions calling as.vector() work too:
## S3 dispatch works for base::as.vector(), but S4 dispatch does not:
as.vector.sparseVector <- sp2vec
as.array.sparseVector <- as.matrix.sparseVector <- function(x, ...) .sparseV2Mat(x)

##' Construct new sparse vector , *dropping* zeros

##' @param class  character, the sparseVector class
##' @param x      numeric/logical/...:  the 'x' slot
##' @param i      integer: index of non-zero entries
##' @param length integer: the 'length' slot

##' @return a sparseVector, with 0-dropped 'x' (and 'i')
newSpV <- function(class, x, i, length, drop0 = TRUE, checkSort = TRUE) {
    if(length(x) == 1 && (li <- length(i)) != 1) ## recycle x :
	x <- rep.int(x, li)
    if(drop0 && isTRUE(any(x0 <- x == 0))) {
	keep <- is.na(x) | !x0
	x <- x[keep]
	i <- i[keep]
    }
    if(checkSort && is.unsorted(i)) {
	ii <- sort.list(i)
	x <- x[ii]
	i <- i[ii]
    }
    new(class, x = x, i = i, length = length)
}
## a "version" of 'prev' with changed contents:
newSpVec <- function(class, x, prev)
    newSpV(class, x=x, i=prev@i, length=prev@length)

## Exported:
sparseVector <- function(x, i, length) {
    newSpV(class = paste0(.V.kind(x), "sparseVector"),
           x=x, i=i, length=length)
}


setAs("sparseVector", "vector", function(from) sp2vec(from))

setMethod("as.vector", signature(x = "sparseVector", mode = "missing"),
	  sp2vec)
setMethod("as.vector", signature(x = "sparseVector", mode = "character"),
	  sp2vec)

setMethod("as.numeric", "sparseVector", function(x) sp2vec(x, mode = "double"))
setMethod("as.logical", "sparseVector", function(x) sp2vec(x, mode = "logical"))

setAs("sparseVector", "numeric", function(from) sp2vec(from, mode = "double"))
setAs("sparseVector", "integer", function(from) sp2vec(from, mode = "integer"))
setAs("sparseVector", "logical", function(from) sp2vec(from, mode = "logical"))

## the "catch all remaining" method:
setAs("ANY", "sparseVector",
      function(from) as(as.vector(from), "sparseVector"))
## "nsparse*" is special -- by default "lsparseVector" are produced
setAs("ANY", "nsparseVector",
      function(from) as(as(from, "sparseVector"),"nsparseVector"))

setAs("diagonalMatrix", "sparseVector",
      function(from) {
	  kind <- .M.kind(from) ## currently only "l" and "d" --> have 'x'
	  n <- nrow(from)
          n2 <- as.double(n) * n
	  if(n2 > .Machine$integer.max) { ## double (i, length)
	      ii <- seq(1, by = n+1, length.out = n) ## 1-based indexing
	  } else { # integer ok
	      n2 <- as.integer(n2)
	      ii <- as.integer(seq(1L, by = n+1L, length.out = n))
	  }
	  new(paste0(kind, "sparseVector"),
	      length = n2, i = ii,
	      x = if(from@diag != "U") from@x else
		  rep.int(switch(kind, "d" = 1, "l" = TRUE, "i" = 1L, "z" = 1+0i), n))
	 })

setAs("sparseMatrix", "sparseVector",
      function(from) as(as(from, "TsparseMatrix"), "sparseVector"))

setAs("CsparseMatrix", "sparseVector", ## could go via TsparseMatrix, but this is faster:
      function(from) {
	  d <- dim(from)
	  n <- prod(d) # -> numeric, no integer overflow
	  if((int.n <- n <= .Machine$integer.max)) n <- as.integer(n)
          cld <- getClassDef(class(from))
	  kind <- .M.kind(from, cld)
	  if(extends(cld, "symmetricMatrix"))
	      from <- as(from, "generalMatrix")
	  else if(extends(cld, "triangularMatrix") && from@diag == "U")
	      from <- .Call(Csparse_diagU2N, from)
          xj <- .Call(Matrix_expand_pointers, from@p)
	  ii <- if(int.n)
	      1L + from@i + d[1] * xj
	  else
	      1 + from@i + as.double(d[1]) * xj
	  cl <- paste0(kind, "sparseVector")
	  if(kind != "n") ## have 'x' slot
	      new(cl, i = ii, length = n, x = from@x)
	  else
	      new(cl, i = ii, length = n)
      })

setAs("TsparseMatrix", "sparseVector",
      function(from) {
	  d <- dim(from)
	  n <- prod(d) # -> numeric, no integer overflow
	  if((int.n <- n <= .Machine$integer.max)) n <- as.integer(n)
          cld <- getClassDef(class(from))
	  kind <- .M.kind(from, cld)
	  if(extends(cld, "symmetricMatrix"))
	      from <- as(from, "generalMatrix")
	  else if(extends(cld, "triangularMatrix") && from@diag == "U")
	      from <- .Call(Tsparse_diagU2N, from)
	  if(anyDuplicatedT(from, di = d))
	      from <- uniqTsparse(from)
	  ii <- if(int.n)
	      1L + from@i + d[1] * from@j
	  else
	      1 + from@i + as.double(d[1]) * from@j
	  cl <- paste0(kind, "sparseVector")
	  if(kind != "n") ## have 'x' slot
	      new(cl, i = ii, length = n, x = from@x)
	  else
	      new(cl, i = ii, length = n)
      })


##' <description>
##'
##' <details>
## Utility -- used in `dim<-` below, but also in  Matrix(.) :
##' @title sparseVector --> sparseMatrix constructor
##' @param x "sparseVector" object
##' @param nrow integer or missing, as in matrix(), see ?matrix
##' @param ncol (ditto)
##' @param byrow logical (see ?matrix)
##' @param check logical indicating if it needs to be checked that 'x' is a sparseVector
##' @param symmetric logical indicating if result must be "symmetricMatrix"
##' @return an object inheriting from "sparseMatrix"
##' @author Martin Maechler, May 2007 ff.
spV2M <- function (x, nrow, ncol, byrow = FALSE, check = TRUE, symmetric = FALSE)
{
    cx <- class(x)
    if(check && !extends(cx, "sparseVector"))
	stop("'x' must inherit from \"sparseVector\"")
    if(!missing(ncol)) { ncol <- as.integer(ncol)
			 if(ncol < 0) stop("'ncol' must be >= 0") }
    if(!missing(nrow)) { nrow <- as.integer(nrow)
			 if(nrow < 0) stop("'nrow' must be >= 0") }
    n <- length(x)
    if(symmetric) {
	if(missing(nrow)) stop("Must specify 'nrow' when 'symmetric' is true")
	if(!missing(ncol) && nrow != ncol)
	    stop("'nrow' and 'ncol' must be the same when 'symmetric' is true")
	## otherwise  ncol will not used at all when (symmetric)
	if(check && as.double(nrow)^2 != n)
	    stop("'x' must have length nrow^2 when 'symmetric' is true")
	## x <- x[indTri(nrow, upper=TRUE, diag=TRUE)]
    } else if(missing(nrow)) {
	nrow <- as.integer(
	    if(missing(ncol)) { ## both missing: --> (n x 1)
		ncol <- 1L
		n
	    } else {
		if(n %% ncol != 0) warning("'ncol' is not a factor of length(x)")
		as.integer(ceiling(n / ncol))
	    })
    } else if(missing(ncol)) {
        ncol <- if(symmetric) nrow else {
            if(n %% nrow != 0) warning("'nrow' is not a factor of length(x)")
            as.integer(ceiling(n / nrow)) }
    } else {                          ## both nrow and ncol specified
        n.n <- as.double(ncol) * nrow # no integer overflow
        if(n.n <  n) stop("nrow * ncol < length(x)", domain = NA)
        if(n.n != n) warning("nrow * ncol != length(x)", domain = NA)
    }
    ## now nrow * ncol >= n  (or 'symmetric')
    ##	   ~~~~~~~~~~~~~~~~
    cld <- getClassDef(cx)
    kind <- .M.kindC(cld)		# "d", "n", "l", "i", "z", ...
    has.x <- kind != "n"
    clStem <- if(symmetric) "sTMatrix" else "gTMatrix"
    ## "careful_new()" :
    cNam <- paste0(kind, clStem)
    chngCl <- is.null(slotNames(newCl <- getClass(cNam, .Force=TRUE)))
    if(chngCl) { ## e.g. "igTMatrix" is not yet implemented
	if(substr(cNam,1,1) == "z")
	    stop(gettextf("Class %s is not yet implemented", dQuote(cNam)),
		 domain=NA)
	## coerce to "double":
	newCl <- getClass(paste0("d", clStem))
    }
    r <- new(newCl, Dim = c(nrow, ncol))
    ## now "compute"  the (i,j,x) slots given x@(i,x)
    i0 <- x@i - 1L
    if(byrow) { ## need as.integer(.) since <sparseVector> @ i can be double
	j <- as.integer(i0 %% ncol)
	i <- as.integer(i0 %/% ncol)
    } else { ## default{byrow = FALSE}
	i <- as.integer(i0 %% nrow)
	j <- as.integer(i0 %/% nrow)
    }
    if(has.x)
	x <- if(chngCl) as.numeric(x@x) else x@x
    if(symmetric) {  ## using  uplo = "U"
	i0 <- i <= j ## i.e., indTri(nrow, upper=TRUE, diag=TRUE)
	i <- i[i0]
	j <- j[i0]
	if(has.x) x <- x[i0]
    }
    r@j <- j
    r@i <- i
    if(has.x) r@x <- x
    r
}## {spV2M}

.sparseV2Mat <- function(from) spV2M(from, nrow=from@length, ncol=1L, check=FALSE)
setAs("sparseVector","Matrix", .sparseV2Mat)
setAs("sparseVector","sparseMatrix", .sparseV2Mat)
setAs("sparseVector","TsparseMatrix", .sparseV2Mat)
setAs("sparseVector","CsparseMatrix",
      function(from) .Call(Tsparse_to_Csparse, .sparseV2Mat(from), FALSE))

## This is very similar to the 'x = "sparseMatrix"' method in ./sparseMatrix.R:
setMethod("dim<-", signature(x = "sparseVector", value = "ANY"),
	  function(x, value) {
	      if(!is.numeric(value) || length(value) != 2)
		  stop("dim(.) value must be numeric of length 2")
	      if(length(x) != prod(value <- round(value)))
		  stop("dimensions don't match the number of cells")
	      spV2M(x, nrow=value[1], ncol=value[2])
	  })


setMethod("length", "sparseVector", function(x) x@length)

setMethod("t", "sparseVector", function(x) spV2M(x, nrow=1L, ncol=x@length, check=FALSE))

setMethod("show", signature(object = "sparseVector"),
   function(object) {
       n <- object@length
       cl <- class(object)
       cat(sprintf('sparse vector (nnz/length = %d/%.0f) of class "%s"\n',
		   length(object@i), as.double(n), cl))
       maxp <- max(1, getOption("max.print"))
       if(n <= maxp) {
	   prSpVector(object, maxp = maxp)
       } else { # n > maxp : will cut length of what we'll display :
	   ## cannot easily show head(.) & tail(.) because of "[1] .." printing of tail
	   prSpVector(head(object, maxp), maxp = maxp)
	   cat(" ............................",
	       "\n ........suppressing ", n - maxp,
	       " entries in show(); maybe adjust 'options(max.print= *)'",
	       "\n ............................\n\n", sep='')
       }
       invisible(object)
   })

prSpVector <- function(x, digits = getOption("digits"),
		    maxp = getOption("max.print"), zero.print = ".")
{
    cld <- getClassDef(class(x))
    stopifnot(extends(cld, "sparseVector"), maxp >= 1)
    if(is.logical(zero.print))
	zero.print <- if(zero.print) "0" else " "
##     kind <- .M.kindC(cld)
##     has.x <- kind != "n"
    n <- x@length
    if(n > 0) {
	if(n > maxp) { # n > maxp =: nn : will cut length of what we'll display :
	    x <- head(x, maxp)
	    n <- maxp
	}
        xi <- x@i
        is.n <- extends(cld, "nsparseVector")
        logi <- is.n || extends(cld, "lsparseVector")
        cx <- if(logi) rep.int("N", n) else character(n)
        cx[if(length(xi)) -xi else TRUE] <- zero.print
        cx[ xi] <- {
	    if(is.n) "|" else if(logi) c(":","|")[x@x + 1L] else
	    ## numeric (or --not yet-- complex): 'has.x' in any cases
	    format(x@x, digits = digits)
        }
        ## right = TRUE : cheap attempt to get better "." alignment
        print(cx, quote = FALSE, right = TRUE, max = maxp)
    }
    invisible(x) # TODO? in case of n > maxp, "should" return original x
}

## This is a simplified intI() {-> ./Tsparse.R } -- for sparseVector indexing:
intIv <- function(i, n, cl.i = getClass(class(i)))
{
### Note: undesirable to use this for negative indices;
### ----  using seq_len(n) below means we are  NON-sparse ...
### Fixed, for "x[i] with negative i" at least.

    ## Purpose: translate numeric | logical index     into  1-based integer
    ## --------------------------------------------------------------------
    ## Arguments: i: index vector (numeric | logical) *OR* sparseVector
    ##		  n: array extent { ==	length(.) }
    if(missing(i))
	return(seq_len(n))
    ## else :
    if(extends(cl.i, "numeric")) {
        storage.mode(i) <- "integer"
        int2i(i,n) ##-> ./Tsparse.R
    }
    else if (extends(cl.i, "logical")) {
	seq_len(n)[i]
    } else if(extends(cl.i, "nsparseVector")) {
	i@i # the indices are already there !
    } else if(extends(cl.i, "lsparseVector")) {
	i@i[i@x] # "drop0", i.e. FALSE; NAs ok
    } else if (extends(cl.i, "sparseVector")) { ## 'i'sparse, 'd'sparse	 (etc)
	as.integer(i@x[i@i])
    }
    else
        stop("index must be numeric, logical or sparseVector for indexing sparseVectors")
} ## intIv()


setMethod("head", signature(x = "sparseVector"),
	  function(x, n = 6, ...) {
	      stopifnot(length(n) == 1)
	      if(n >= (nx <- x@length)) return(x)
	      if(is.integer(x@i)) n <- as.integer(n) else stopifnot(n == round(n))
              if(n < 0) n <- max(0L, n + nx)
	      ## now be careful to *NOT* use seq_len(n), as this be efficient for huge n
	      ## n < x@length  now.
	      ## As we *know* that '@i' is sorted increasingly: [x@i <= n] <==> [1:kk]
	      x@length <- n
	      x@i <- x@i[ii <- seq_len(which.max(x@i > n) - 1L)]
	      if(substr(class(x), 1,1) != "n") ## be fast, ...
		  x@x <- x@x[ii]
	      x
	  })
setMethod("tail", signature(x = "sparseVector"),
	  function(x, n = 6, ...) {
	      stopifnot(length(n) == 1)
	      if(n >= (nx <- x@length)) return(x)
	      if(is.integer(x@i)) n <- as.integer(n) else stopifnot(n == round(n))
	      if(n < 0) n <- max(0L, n + nx)
	      ## now be careful to *NOT* use seq_len(n), as this be efficient for huge n
	      ## n < x@length  now.
	      ## As we *know*  '@i' is sorted increasingly: [x@i > nx-n] <==> [kk:nx]
	      x@length <- n
	      n <- nx-n # and keep indices > n
	      N <- length(x@i)
	      ii <- if(any(G <- x@i > n)) which.max(G):N else FALSE
	      x@i <- x@i[ii] - n
	      if(substr(class(x), 1,1) != "n") ## be fast, ...
		  x@x <- x@x[ii]
	      x
	  })


setMethod("[", signature(x = "sparseVector", i = "index"),
	  function (x, i, j, ..., drop) {
	      cld <- getClassDef(class(x))
	      has.x <- !extends(cld, "nsparseVector")
	      n <- x@length
	      if(extends(cl.i <- getClass(class(i)), "numeric") && any(i < 0)) {
		  ## negative indices - remain sparse --> *not* using intIv()
		  if(any(i > 0))
		      stop("you cannot mix negative and positive indices")
		  if(any(z <- i == 0)) i <- i[!z]

		  ## all (i < 0) :

		  ## FIXME: an efficient solution would use C here
		  i <- unique(sort(-i)) # so we need to drop the 'i's
		  if(any(nom <- is.na(m <- match(x@i, i)))) {
		      ## eliminate those with non-0 match
		      x@i <- x@i[nom]
		      if(has.x) x@x <- x@x[nom]
		  }
		  ii <- findInterval(x@i, i)	## subtract that :
		  x@i <- x@i - ii
		  x@length <- x@length - length(i)

              } else {
                  ii <- intIv(i, n, cl.i=cl.i)
                  m <- match(x@i, ii, nomatch = 0)
                  sel <- m > 0L
                  x@length <- length(ii)
                  x@i <- m[sel]
		  if(any(iDup <- duplicated(ii))) {
                      i.i <- match(ii[iDup], ii)
                      jm <- lapply(i.i, function(.) which(. == m))
                      sel <- c(which(sel), unlist(jm))
                      x@i <- c(x@i, rep.int(which(iDup), lengths(jm)))
                  }
                  if (has.x)
                      x@x <- x@x[sel]
              }
	      x
	  })

setMethod("[", signature(x = "sparseVector", i = "lsparseVector"),
	  function (x, i, j, ..., drop) x[sort.int(i@i[i@x])])
setMethod("[", signature(x = "sparseVector", i = "nsparseVector"),
	  function (x, i, j, ..., drop) x[sort.int(i@i)])

##--- Something else:  Allow    v[ <sparseVector> ] -- exactly similarly:
if(FALSE) { ## R_FIXME: Not working, as internal "[" only dispatches on 1st argument
setMethod("[", signature(x = "atomicVector", i = "lsparseVector"),
	  function (x, i, j, ..., drop) x[sort.int(i@i[i@x])])
setMethod("[", signature(x = "atomicVector", i = "nsparseVector"),
	  function (x, i, j, ..., drop) x[sort.int(i@i)])
}

##' Implement   x[i] <- value

##' @param x  a "sparseVector"
##' @param i  an "index" (integer, logical, ..)
##' @param value

##' @return  a "sparseVector" of the same length as 'x'
## This is much analogous to replTmat in ./Tsparse.R:
replSPvec <- function (x, i, value)
{
    n <- x@length
    ii <- intIv(i, n)
    lenRepl <- length(ii)
    lenV <- length(value)
    if(lenV == 0) {
	if(lenRepl != 0)
	    stop("nothing to replace with")
	else return(x)
    }
    ## else: lenV := length(value) > 0
    if(lenRepl %% lenV != 0)
	stop("number of items to replace is not a multiple of replacement length")
    if(anyDuplicated(ii)) { ## multiple *replacement* indices: last one wins
	## TODO: in R 2.6.0 use	 duplicate(*, fromLast=TRUE)
	ir <- lenRepl:1
	keep <- match(ii, ii[ir]) == ir
	ii <- ii[keep]
	lenV <- length(value <- rep(value, length = lenRepl)[keep])
	lenRepl <- length(ii)
    }

    cld <- getClassDef(class(x))
    has.x <- !extends(cld, "nsparseVector")
    m <- match(x@i, ii, nomatch = 0)
    sel <- m > 0L

    ## the simplest case
    if(all0(value)) { ## just drop the non-zero entries
	if(any(sel)) { ## non-zero there
	    x@i <- x@i[!sel]
	    if(has.x)
		x@x <- x@x[!sel]
	}
	return(x)

    }
    ## else --	some( value != 0 ) --
    if(lenV > lenRepl)
	stop("too many replacement values")
    else if(lenV < lenRepl)
	value <- rep(value, length = lenRepl)
    ## now:  length(value) == lenRepl

    v0 <- is0(value)
    ## value[1:lenRepl]:  which are structural 0 now, which not?

    if(any(sel)) {
	## indices of non-zero entries -- WRT to subvector
	iN0 <- m[sel] ## == match(x@i[sel], ii)

	## 1a) replace those that are already non-zero with new val.
	vN0 <- !v0[iN0]
	if(any(vN0) && has.x)
	    x@x[sel][vN0] <- value[iN0[vN0]]

	## 1b) replace non-zeros with 0 --> drop entries
	if(any(!vN0)) {
	    i <- which(sel)[!vN0]
	    if(has.x)
		x@x <- x@x[-i]
	    x@i <- x@i[-i]
	}
	iI0 <- if(length(iN0) < lenRepl)
	    seq_len(lenRepl)[-iN0]
    } else iI0 <- seq_len(lenRepl)

    if(length(iI0) && any(vN0 <- !v0[iI0])) {
	## 2) add those that were structural 0 (where value != 0)
	ij0 <- iI0[vN0]
	x@i <- c(x@i, ii[ij0])
	if(has.x)
	    x@x <- c(x@x, value[ij0])
    }
    x
}

setReplaceMethod("[", signature(x = "sparseVector", i = "index", j = "missing",
				value = "replValue"),
		 replSPvec)

setReplaceMethod("[", signature(x = "sparseVector",
                                i = "sparseVector", j = "missing",
				value = "replValue"),
                 ## BTW, the important case: 'i' a *logical* sparseVector
		 replSPvec)

## Something else:  Also allow	  x[ <sparseVector> ] <- v  e.g. for atomic x :

if(FALSE) { ## R_FIXME: Not working, as internal "[<-" only dispatches on 1st argument
## Now "the work is done" inside  intIv() :
setReplaceMethod("[", signature(x = "atomicVector",
				i = "sparseVector", j = "missing",
				value = "replValue"),
		 function (x, i, value)
		 callGeneric(x, i = intIv(i, x@length), value=value))
}

## a "method" for c(<(sparse)Vector>, <(sparse)Vector>):
## FIXME: This is not exported, nor used (nor documented)
c2v <- function(x, y) {
    ## these as(., "sp..V..") check input implicitly:
    cx <- class(x <- as(x, "sparseVector"))
    cy <- class(y <- as(y, "sparseVector"))
    if(cx != cy) { ## find "common" class; result does have 'x' slot
        cxy <- c(cx,cy)
        commType <- {
            if(all(cxy %in% c("nsparseVector", "lsparseVector")))
                "lsparseVector"
            else { # ==> "numeric" ("integer") or "complex"
                xslot1 <- function(u, cl.u)
                    if(cl.u != "nsparseVector") u@x[1] else TRUE
                switch(typeof(xslot1(x, cx) + xslot1(y, cy)),
                       ## "integer", "double", or "complex"
                       "integer" = "isparseVector",
                       "double" = "dsparseVector",
                       "complex" = "zsparseVector")
            }
        }
        if(cx != commType) x <- as(x, commType)
        if(cy != commType) y <- as(y, commType)
        cx <- commType
    }
    ## now *have* common type -- transform 'x' into result:
    nx <- x@length
    x@length <- nx + y@length
    x@i <- c(x@i, nx + y@i)
    if(cx != "nsparseVector")
        x@x <- c(x@x, y@x)
    x
}

## sort.default() does
##		x[order(x, na.last = na.last, decreasing = decreasing)]
## but that uses a *dense* integer order vector
## ==> need direct sort() method for "sparseVector" for mean(*,trim), median(),..
sortSparseV <- function(x, decreasing = FALSE, na.last = NA) {
    if(length(ina <- which(is.na(x)))) {
        if(is.na(na.last)) x <- x[-ina]
    }
    ## TODO
    .NotYetImplemented()
}

all.equal.sparseV <- function(target, current, ...)
{
    if(!is(target, "sparseVector") || !is(current, "sparseVector")) {
	return(paste0("target is ", data.class(target), ", current is ",
		      data.class(current)))
    }
    lt <- length(target)
    lc <- length(current)
    if(lt != lc) {
	return(paste0("sparseVector", ": lengths (", lt, ", ", lc, ") differ"))
    }

    t.has.x <- class(target)  != "nsparseVector"
    c.has.x <- class(current) != "nsparseVector"
    nz.t <- length(i.t <- target @i)
    nz.c <- length(i.c <- current@i)
    t.x <- if(t.has.x)	target@x else rep.int(TRUE, nz.t)
    c.x <- if(c.has.x) current@x else rep.int(TRUE, nz.c)
    if(nz.t != nz.c || any(i.t != i.c)) { ## "work" if indices are not the same
	i1.c <- setdiff(i.t, i.c)# those in i.t, not yet in i.c
	i1.t <- setdiff(i.c, i.t)
	if((n1t <- length(i1.t))) {
	    target@i <- i.t <- c(i.t, i1.t)
	    t.x <- c(t.x, rep.int(if(t.has.x) 0 else 0L, n1t))
	}
	if((n1c <- length(i1.c))) {
	    current@i <- i.c <- c(i.c, i1.c)
	    c.x <- c(c.x, rep.int(if(c.has.x) 0 else 0L, n1c))
	}
    }
    if(is.unsorted(i.t)) {  ## method="quick" {"radix" not ok for large range}
	ii <- sort.list(i.t, method = "quick", na.last=NA)
	target@i <- i.t <- i.t[ii]
	t.x <- t.x[ii]
    }
    if(is.unsorted(i.c)) {
	ii <- sort.list(i.c, method = "quick", na.last=NA)
	current@i <- i.c <- i.c[ii]
	c.x <- c.x[ii]
    }

    ## Now, we have extended both target and current
    ## *and* have sorted the respective i-slot, the i-slots should match!
    stopifnot(all(i.c == i.t))

    all.equal.numeric(c.x, t.x, ...)
} ## all.equal.sparseV


## For these, we remain sparse:
setMethod("all.equal", c(target = "sparseVector", current = "sparseVector"),
	  all.equal.sparseV)
setMethod("all.equal", c(target = "sparseVector", current = "sparseMatrix"),
	  function(target, current, ...)
	  all.equal.sparseV(target, as(current, "sparseVector"), ...))
setMethod("all.equal", c(target = "sparseMatrix", current = "sparseVector"),
	  function(target, current, ...)
	  all.equal.sparseV(as(target, "sparseVector"), current, ...))
## For the others, where one is "dense", "go to" dense rather now than later:
setMethod("all.equal", c(target = "ANY", current = "sparseVector"),
	  function(target, current, ...)
	  all.equal(target, as.vector(current), ...))
setMethod("all.equal", c(target = "sparseVector", current = "ANY"),
	  function(target, current, ...)
	  all.equal(as.vector(target), current, ...))


### rep(x, ...) -- rep() is primitive with internal default method with these args:
### -----------
### till R 2.3.1, it had  rep.default()  which we use as 'model' here.

repSpV <- function(x, times) {
    ## == rep.int(<sparseVector>, times)"
    times <- as.integer(times)# truncating as rep.default()
    n <- x@length
    has.x <- substr(class(x), 1,1) != "n" ## fast, but hackish
    ## just assign new correct slots:
    if(times <= 1) { ## be quick for {0, 1} times
        if(times < 0) stop("'times >= 0' is required")
        if(times == 0) {
            x@length <- 0L
            x@i <- integer(0)
            if(has.x) x@x <- rep.int(x@x, 0)
        }
        return(x)
    }
    n. <- as.double(n)
    if(n. * times >= .Machine$integer.max)
        n <- n. # so won't have overflow in subsequent multiplys
    x@length <- n * times
    x@i <- rep.int(x@i, times) + n * rep(0:(times-1L), each=length(x@i))
    ## := outer(x@i, 0:(times-1) * n, "+")   but a bit faster
    if(has.x) x@x <- rep.int(x@x, times)
    x
}

setMethod("rep", "sparseVector",
	  function(x, times, length.out, each, ...) {
	      if (length(x) == 0)
		  return(if(missing(length.out)) x else head(x, length.out))
	      if (!missing(each)) {
		  tm <- rep.int(each, length(x))
		  x <- rep(x, tm) # "recursively"
		  if(missing(length.out) && missing(times))
		      return(x)
	      } ## else :
	      if (!missing(length.out)) # takes precedence over times
		  times <- ceiling(length.out/length(x))
	      r <- repSpV(x, times)
	      if (!missing(length.out) && length(r) != length.out) {
		  if(length.out > 0) head(r, length.out) else r[integer(0)]
	      }
	      else r
	  })


### Group Methods (!)
## "Ops" : ["Arith", "Compare", "Logic"]:  ---> in ./Ops.R
##						     -----
## "Summary"  ---> ./Summary.R
##		     ---------
## "Math", "Math2": ./Math.R
##		     -------


setMethod("solve", signature(a = "Matrix", b = "sparseVector"),
	  function(a, b, ...) callGeneric(a, as(b, "sparseMatrix")))

## the 'i' slot is 1-based *and* has no NA's:

setMethod("which", "nsparseVector", function(x, arr.ind) x@i)
setMethod("which", "lsparseVector", function(x, arr.ind) x@i[is1(x@x)])
## and *error* for "dsparseVector", "i*", ...

##' indices of vector x[] to construct  Toeplitz matrix
##' FIXME: write in C, port to  R('stats' package), and use in stats::toeplitz()
ind4toeplitz <- function(n) {
    A <- matrix(raw(), n, n)
    abs(as.vector(col(A) - row(A))) + 1L
}

.toeplitz.spV <-  function(x, symmetric=TRUE, giveCsparse=TRUE) {
    ## semantically "identical" to stats::toeplitz
    n <- length(x)
    r <- spV2M(x[ind4toeplitz(n)], n,n, symmetric=symmetric, check=FALSE)
    if (giveCsparse) as(r, "CsparseMatrix") else r
}
setMethod("toeplitz", "sparseVector", .toeplitz.spV)
