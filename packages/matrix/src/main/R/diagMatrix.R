#### All methods for "diagonalMatrix" and its subclasses,
####  currently "ddiMatrix", "ldiMatrix"

## Purpose: Constructor of diagonal matrices -- ~= diag() ,
##          but *not* diag() extractor!
Diagonal <- function(n, x = NULL)
{
    ## Allow  Diagonal(4), Diagonal(x=1:5), and  Diagonal(4, TRUE)
    n <- if(missing(n)) length(x) else {
	stopifnot(length(n) == 1, n == as.integer(n), n >= 0)
	as.integer(n)
    }

    if(missing(x)) ## unit diagonal matrix
	new("ddiMatrix", Dim = c(n,n), diag = "U")
    else {
	lx <- length(x)
	lx.1 <- lx == 1L
	stopifnot(lx.1 || lx == n) # but keep 'x' short for now
	if(is.logical(x))
	    cl <- "ldiMatrix"
	else if(is.numeric(x)) {
	    cl <- "ddiMatrix"
	    x <- as.numeric(x)
	}
	else if(is.complex(x)) {
	    cl <- "zdiMatrix"  # will not yet work
	} else stop("'x' has invalid data type")
	if(lx.1 && !is.na(x) && x == 1) # cheap check for uni-diagonal..
	    new(cl, Dim = c(n,n), diag = "U")
	else
	    new(cl, Dim = c(n,n), diag = "N",
		x = if(lx.1) rep.int(x,n) else x)
    }
}

.sparseDiagonal <- function(n, x = 1, uplo = "U",
			    shape = if(missing(cols)) "t" else "g",
			    unitri, kind,
			    cols = if(n) 0:(n - 1L) else integer(0))
{
    stopifnot(n == (n. <- as.integer(n)), (n <- n.) >= 0)
    if(!(mcols <- missing(cols)))
	stopifnot(0 <= (cols <- as.integer(cols)), cols < n)
    m <- length(cols)
    if(missing(kind))
	kind <-
	    if(is.double(x)) "d"
	    else if(is.logical(x)) "l"
	    else { ## for now
		storage.mode(x) <- "double"
		"d"
	    }
    else stopifnot(any(kind == c("d","l","n")))
    stopifnot(is.character(shape), nchar(shape) == 1,
	      any(shape == c("t","s","g"))) # triangular / symmetric / general
    if((missing(unitri) || unitri) && shape == "t" &&
       (mcols || cols == 0:(n-1L)) &&
       ((any(kind == c("l", "n")) && allTrue(x)) ||
	(    kind == "d"	  && allTrue(x == 1)))) { ## uni-triangular
	new(paste0(kind,"tCMatrix"), Dim = c(n,n),
		   uplo = uplo, diag = "U", p = rep.int(0L, n+1L))
    }
    else if(kind == "n") {
	if(shape == "g")
	    new("ngCMatrix", Dim = c(n,m), i = cols, p = 0:m)
	else new(paste0("n", shape, "CMatrix"), Dim = c(n,m), uplo = uplo,
		 i = cols, p = 0:m)
    }
    else { ## kind != "n" -- have x slot :
	if((lx <- length(x)) == 1) x <- rep.int(x, m)
	else if(lx != m) stop("length(x) must be either 1 or #{cols}")
	if(shape == "g")
	    new(paste0(kind, "gCMatrix"), Dim = c(n,m),
		x = x, i = cols, p = 0:m)
	else new(paste0(kind, shape, "CMatrix"), Dim = c(n,m), uplo = uplo,
		 x = x, i = cols, p = 0:m)
    }
}

## Pkg 'spdep' had (relatively slow) versions of this as_dsCMatrix_I()
.symDiagonal <- function(n, x = rep.int(1,n), uplo = "U")
    .sparseDiagonal(n, x, uplo, shape = "s")

# instead of   diagU2N(as(Diagonal(n), "CsparseMatrix")), diag = "N" in any case:
.trDiagonal <- function(n, x = 1, uplo = "U", unitri=TRUE)
    .sparseDiagonal(n, x, uplo, shape = "t", unitri=unitri)


## This is modified from a post of Bert Gunter to R-help on  1 Sep 2005.
## Bert's code built on a post by Andy Liaw who most probably was influenced
## by earlier posts, notably one by Scott Chasalow on S-news, 16 Jan 2002
## who posted his bdiag() function written in December 1995.
if(FALSE)##--- no longer used:
.bdiag <- function(lst) {
    ## block-diagonal matrix [a dgTMatrix] from list of matrices
    stopifnot(is.list(lst), length(lst) >= 1)
    dims <- vapply(lst, dim, 1L, USE.NAMES=FALSE)
    ## make sure we had all matrices:
    if(!(is.matrix(dims) && nrow(dims) == 2))
	stop("some arguments are not matrices")
    csdim <- rbind(rep.int(0L, 2),
                   apply(dims, 1, cumsum))
    r <- new("dgTMatrix")
    r@Dim <- as.integer(csdim[nrow(csdim),])
    add1 <- matrix(1:0, 2,2)
    for(i in seq_along(lst)) {
	indx <- apply(csdim[i:(i+1),] + add1, 2, function(n) n[1]:n[2])
	if(is.null(dim(indx))) ## non-square matrix
	    r[indx[[1]],indx[[2]]] <- lst[[i]]
	else ## square matrix
	    r[indx[,1], indx[,2]] <- lst[[i]]
    }
    r
}
## expand(<mer>) needed something like bdiag() for lower-triangular
## (Tsparse) Matrices; hence Doug Bates provided a much more efficient
##  implementation for those; now extended and generalized:
.bdiag <- function(lst) {
    ## block-diagonal matrix [a dgTMatrix] from list of matrices
    stopifnot(is.list(lst), (nl <- length(lst)) >= 1)

    Tlst <- lapply(lapply(lst, as_Csp2), # includes "diagU2N"
		   as, "TsparseMatrix")
    if(nl == 1) return(Tlst[[1]])
    ## else
    i_off <- c(0L, cumsum(vapply(Tlst, nrow, 1L)))
    j_off <- c(0L, cumsum(vapply(Tlst, ncol, 1L)))

    clss <- vapply(Tlst, class, "")
    typ <- substr(clss, 2, 2)
    knd <- substr(clss, 1, 1)
    sym <- typ == "s" # symmetric ones
    tri <- typ == "t" # triangular ones
    use.n <- any(is.n <- knd == "n")
    if(use.n && !(use.n <- all(is.n))) {
	Tlst[is.n] <- lapply(Tlst[is.n], as, "lMatrix")
	knd [is.n] <- "l"
    }
    use.l <- !use.n && all(knd == "l")
    if(all(sym)) { ## result should be *symmetric*
	uplos <- vapply(Tlst, slot, ".", "uplo") ## either "U" or "L"
	tLU <- table(uplos)# of length 1 or 2 ..
	if(length(tLU) == 1) { ## all "U" or all "L"
	    useU <- uplos[1] == "U"
	} else { ## length(tLU) == 2, counting "L" and "U"
	    useU <- diff(tLU) >= 0
	    if(useU && (hasL <- tLU[1] > 0))
		Tlst[hasL] <- lapply(Tlst[hasL], t)
	    else if(!useU && (hasU <- tLU[2] > 0))
		Tlst[hasU] <- lapply(Tlst[hasU], t)
	}
	if(use.n) { ## return nsparseMatrix :
	    r <- new("nsTMatrix")
	} else {
	    r <- new(paste0(if(use.l) "l" else "d", "sTMatrix"))
	    r@x <- unlist(lapply(Tlst, slot, "x"))
	}
	r@uplo <- if(useU) "U" else "L"
    }
    else if(all(tri) && { ULs <- vapply(Tlst, slot, ".", "uplo")##  "U" or "L"
			  all(ULs[1L] == ULs[-1L]) } ## all upper or all lower
       ){ ## *triangular* result

	if(use.n) { ## return nsparseMatrix :
	    r <- new("ntTMatrix")
	} else {
	    r <- new(paste0(if(use.l) "l" else "d", "tTMatrix"))
	    r@x <- unlist(lapply(Tlst, slot, "x"))
	}
	r@uplo <- ULs[1L]
    }
    else {
	if(any(sym))
	    Tlst[sym] <- lapply(Tlst[sym], as, "generalMatrix")
	if(use.n) { ## return nsparseMatrix :
	    r <- new("ngTMatrix")
	} else {
	    r <- new(paste0(if(use.l) "l" else "d", "gTMatrix"))
	    r@x <- unlist(lapply(Tlst, slot, "x"))
	}
    }
    r@Dim <- c(i_off[nl+1], j_off[nl + 1])
    r@i <- unlist(lapply(1:nl, function(k) Tlst[[k]]@i + i_off[k]))
    r@j <- unlist(lapply(1:nl, function(k) Tlst[[k]]@j + j_off[k]))
    r
}

bdiag <- function(...) {
    if((nA <- nargs()) == 0) return(new("dgCMatrix"))
    if(nA == 1 && !is.list(...))
	return(as(..., "CsparseMatrix"))
    alis <- if(nA == 1 && is.list(..1)) ..1 else list(...)
    if(length(alis) == 1)
	return(as(alis[[1]], "CsparseMatrix"))

    ## else : two or more arguments
    as(.bdiag(alis), "CsparseMatrix")
}

setMethod("tril", "diagonalMatrix", function(x, k = 0, ...)
    if(k >= 0) x else .setZero(x, paste0(.M.kind(x), "tCMatrix")))

setMethod("triu", "diagonalMatrix", function(x, k = 0, ...)
    if(k <= 0) x else  .setZero(x, paste0(.M.kind(x), "tCMatrix")))



.diag2tT <- function(from, uplo = "U", kind = .M.kind(from)) {
    ## to triangular Tsparse
    i <- if(from@diag == "U") integer(0) else seq_len(from@Dim[1]) - 1L
    new(paste0(kind, "tTMatrix"),
	diag = from@diag, Dim = from@Dim, Dimnames = from@Dimnames,
	uplo = uplo,
	x = from@x, # <- ok for diag = "U" and "N" (!)
	i = i, j = i)
}

.diag2sT <- function(from, uplo = "U", kind = .M.kind(from)) {
    ## to symmetric Tsparse
    n <- from@Dim[1]
    i <- seq_len(n) - 1L
    new(paste0(kind, "sTMatrix"),
	Dim = from@Dim, Dimnames = from@Dimnames,
	i = i, j = i, uplo = uplo,
	x = if(from@diag == "N") from@x else ## "U"-diag
	rep.int(switch(kind,
		       "d" = 1.,
		       "l" =,
		       "n" = TRUE,
		       ## otherwise
		       stop(gettextf("%s kind not yet implemented",
				     sQuote(kind)), domain=NA)),
		n))
}

## diagonal -> triangular,  upper / lower depending on "partner" 'x':
diag2tT.u <- function(d, x, kind = .M.kind(d))
    .diag2tT(d, uplo = if(is(x,"triangularMatrix")) x@uplo else "U", kind)

## diagonal -> sparse {triangular OR symmetric} (upper / lower) depending on "partner":
diag2Tsmart <- function(d, x, kind = .M.kind(d)) {
    clx <- getClassDef(class(x))
    if(extends(clx, "symmetricMatrix"))
	.diag2sT(d, uplo = x@uplo, kind)
    else
	.diag2tT(d, uplo = if(extends(clx,"triangularMatrix")) x@uplo else "U", kind)
}

## FIXME: should not be needed {when ddi* is dsparse* etc}:
setMethod("is.finite", signature(x = "diagonalMatrix"),
	  function(x) is.finite(.diag2tT(x)))
setMethod("is.infinite", signature(x = "diagonalMatrix"),
	  function(x) is.infinite(.diag2tT(x)))

## In order to evade method dispatch ambiguity warnings,
## and because we can save a .M.kind() call, we use this explicit
## "hack"  instead of signature  x = "diagonalMatrix" :
##
## ddi*:
di2tT <- function(from) .diag2tT(from, "U", "d")
setAs("ddiMatrix", "triangularMatrix", di2tT)
##_no_longer_ setAs("ddiMatrix", "sparseMatrix", di2tT)
## needed too (otherwise <dense> -> Tsparse is taken):
setAs("ddiMatrix", "TsparseMatrix", di2tT)
setAs("ddiMatrix", "dsparseMatrix", di2tT)
setAs("ddiMatrix", "CsparseMatrix",
      function(from) as(.diag2tT(from, "U", "d"), "CsparseMatrix"))
setAs("ddiMatrix", "symmetricMatrix", function(from) .diag2sT(from, "U", "d"))
##
## ldi*:
di2tT <- function(from) .diag2tT(from, "U", "l")
setAs("ldiMatrix", "triangularMatrix", di2tT)
##_no_longer_ setAs("ldiMatrix", "sparseMatrix", di2tT)
## needed too (otherwise <dense> -> Tsparse is taken):
setAs("ldiMatrix", "TsparseMatrix", di2tT)
setAs("ldiMatrix", "lsparseMatrix", di2tT)
setAs("ldiMatrix", "CsparseMatrix",
      function(from) as(.diag2tT(from, "U", "l"), "CsparseMatrix"))
setAs("ldiMatrix", "symmetricMatrix", function(from) .diag2sT(from, "U", "l"))
rm(di2tT)

setAs("diagonalMatrix", "nMatrix",
      di2nMat <- function(from) {
	  i <- if(from@diag == "U") integer(0) else which(isN0(from@x)) - 1L
	  new("ntTMatrix", i = i, j = i, diag = from@diag,
	      Dim = from@Dim, Dimnames = from@Dimnames)
      })

setAs("diagonalMatrix", "nsparseMatrix", function(from) as(from, "nMatrix"))

##' A version of diag(x,n) which *does* preserve the mode of x, where diag() "fails"
mkDiag <- function(x, n) {
    y <- matrix(as0(mod=mode(x)), n,n)
    if (n > 0) y[1L + 0:(n - 1L) * (n + 1)] <- x
    y
}
## NB: diag(x,n) is really faster for n >= 20, and even more for large n
## --> using diag() where possible, ==> .ddi2mat()

.diag2mat <- function(from)
    ## want "ldiMatrix" -> <logical> "matrix"  (but integer -> <double> for now)
    mkDiag(if(from@diag == "U") as1(from@x) else from@x, n = from@Dim[1])

.ddi2mat <- function(from)
    base::diag(if(from@diag == "U") as1(from@x) else from@x, nrow = from@Dim[1])

setAs("ddiMatrix", "matrix", .ddi2mat)
## the non-ddi diagonalMatrix -- only "ldiMatrix" currently:
setAs("diagonalMatrix", "matrix", .diag2mat)

setMethod("as.vector", signature(x = "diagonalMatrix", mode="missing"),
	  function(x, mode) {
	      n <- x@Dim[1]
              mod.x <- mode(x@x)
	      r <- vector(mod.x, length = n^2)
	      if(n)
		  r[1 + 0:(n - 1L) * (n + 1)] <-
		      if(x@diag == "U") as1(mod=mod.x) else x@x
	      r
	  })

setAs("diagonalMatrix", "generalMatrix", # prefer sparse:
      function(from) as(as(from, "CsparseMatrix"), "generalMatrix"))

setAs("diagonalMatrix", "denseMatrix",
      function(from) as(as(from, "CsparseMatrix"), "denseMatrix"))

..diag.x <- function(m)                   rep.int(as1(m@x), m@Dim[1])
.diag.x  <- function(m) if(m@diag == "U") rep.int(as1(m@x), m@Dim[1]) else m@x

.diag.2N <- function(m) {
    if(m@diag == "U") m@diag <- "N"
    m
}

setAs("ddiMatrix", "dgeMatrix", ..2dge)

setAs("ddiMatrix", "ddenseMatrix", #-> "dtr"
      function(from) as(as(from, "triangularMatrix"),"denseMatrix"))
setAs("ldiMatrix", "ldenseMatrix", #-> "ltr"
      function(from) as(as(from, "triangularMatrix"),"denseMatrix"))


setAs("matrix", "diagonalMatrix",
      function(from) {
	  d <- dim(from)
	  if(d[1] != (n <- d[2])) stop("non-square matrix")
	  if(any(from[row(from) != col(from)] != 0))
	      stop("matrix with non-zero off-diagonals cannot be coerced to \"diagonalMatrix\"")
	  x <- diag(from)
	  if(is.logical(x)) {
	      cl <- "ldiMatrix"
	      uni <- allTrue(x) ## uni := {is it unit-diagonal ?}
	  } else {
	      cl <- "ddiMatrix"
	      uni <- allTrue(x == 1)
	      storage.mode(x) <- "double"
	  } ## TODO: complex
	  new(cl, Dim = c(n,n), diag = if(uni) "U" else "N",
	      x = if(uni) x[FALSE] else x, Dimnames = .M.DN(from))
      })

## ``generic'' coercion to  diagonalMatrix : build on  isDiagonal() and diag()
setAs("Matrix", "diagonalMatrix",
      function(from) {
	  d <- dim(from)
	  if(d[1] != (n <- d[2])) stop("non-square matrix")
	  if(!isDiagonal(from)) stop("matrix is not diagonal")
	  ## else:
	  x <- diag(from)
	  if(is.logical(x)) {
	      cl <- "ldiMatrix"
	      uni <- allTrue(x)
	  } else {
	      cl <- "ddiMatrix"
	      uni <- allTrue(x == 1)
	      storage.mode(x) <- "double"
	  } ## TODO: complex
	  new(cl, Dim = c(n,n), diag = if(uni) "U" else "N",
	      x = if(uni) x[FALSE] else x, Dimnames = from@Dimnames)
      })


setMethod("diag", signature(x = "diagonalMatrix"),
          function(x = 1, nrow, ncol) .diag.x(x))

subDiag <- function(x, i, j, ..., drop) {
    x <- as(x, "CsparseMatrix") ## << was "TsparseMatrix" (Csparse is faster now)
    x <- if(missing(i))
	x[, j, drop=drop]
    else if(missing(j))
	if(nargs() == 4) x[i, , drop=drop] else x[i, drop=drop]
    else
	x[i,j, drop=drop]
    if(isS4(x) && isDiagonal(x)) as(x, "diagonalMatrix") else x
}

setMethod("[", signature(x = "diagonalMatrix", i = "index",
			 j = "index", drop = "logical"), subDiag)
setMethod("[", signature(x = "diagonalMatrix", i = "index",
			 j = "missing", drop = "logical"),
	  function(x, i, j, ..., drop) {
	      na <- nargs()
	      Matrix.msg("diag[i,m,l] : nargs()=", na, .M.level = 2)
	      if(na == 4)
		   subDiag(x, i=i, , drop=drop)
	      else subDiag(x, i=i,   drop=drop)
	  })
setMethod("[", signature(x = "diagonalMatrix", i = "missing",
			 j = "index", drop = "logical"),
	  function(x, i, j, ..., drop) subDiag(x, j=j, drop=drop))

## When you assign to a diagonalMatrix, the result should be
## diagonal or sparse ---
## FIXME: this now fails because the "denseMatrix" methods come first in dispatch
## Only(?) current bug:  x[i] <- value  is wrong when  i is *vector*
replDiag <- function(x, i, j, ..., value) {
    x <- as(x, "CsparseMatrix")# was "Tsparse.." till 2012-07
    if(missing(i))
	x[, j] <- value
    else if(missing(j)) { ##  x[i , ] <- v  *OR*   x[i] <- v
        na <- nargs()
##         message("diagnosing replDiag() -- nargs()= ", na)
	if(na == 4)
            x[i, ] <- value
	else if(na == 3)
            x[i] <- value
	else stop(gettextf("Internal bug: nargs()=%d; please report",
			   na), domain=NA)
    } else
	x[i,j] <- value
    if(isDiagonal(x)) as(x, "diagonalMatrix") else x
}

setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index",
				j = "index", value = "replValue"), replDiag)

setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index",
				j = "missing", value = "replValue"),
		 function(x,i,j, ..., value) {
                     ## message("before replDiag() -- nargs()= ", nargs())
                     if(nargs() == 3)
                         replDiag(x, i=i, value=value)
                     else ## nargs() == 4 :
                         replDiag(x, i=i, , value=value)
                 })

setReplaceMethod("[", signature(x = "diagonalMatrix", i = "missing",
				j = "index", value = "replValue"),
		 function(x,i,j, ..., value) replDiag(x, j=j, value=value))

## x[] <- value :
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "missing",
				j = "missing", value = "ANY"),
		 function(x,i,j, ..., value)
	     {
	      if(all0(value)) { # be faster
		  r <- new(paste0(.M.kindC(getClassDef(class(x))),"tTMatrix"))# of all "0"
		  r@Dim <- x@Dim
		  r@Dimnames <- x@Dimnames
		  r
	      } else { ## typically non-sense: assigning to full sparseMatrix
		  x[TRUE] <- value
		  x
	      }
	  })


setReplaceMethod("[", signature(x = "diagonalMatrix",
                                i = "matrix", # 2-col.matrix
				j = "missing", value = "replValue"),
		 function(x,i,j, ..., value) {
		     if(ncol(i) == 2) {
			 if(all((ii <- i[,1]) == i[,2])) { # replace in diagonal only
			     if(x@diag == "U") {
				 one <- as1(x@x)
				 if(any(value != one | is.na(value))) {
				     x@diag <- "N"
				     x@x <- rep.int(one, x@Dim[1])
				 } else return(x)
			     }
			     x@x[ii] <- value
			     x
			 } else { ## no longer diagonal, but remain sparse:
			     x <- as(x, "TsparseMatrix")
			     x[i] <- value
			     x
			 }
		     }
		     else { # behave as "base R": use as if vector
			 x <- as(x, "matrix")
			 x[i] <- value
			 Matrix(x)
		     }
		 })


## value = "sparseMatrix":
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "missing", j = "index",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		 callGeneric(x=x, , j=j, value = as(value, "sparseVector")))
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index", j = "missing",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		 callGeneric(x=x, i=i, , value = as(value, "sparseVector")))
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index", j = "index",
				value = "sparseMatrix"),
		 function (x, i, j, ..., value)
		 callGeneric(x=x, i=i, j=j, value = as(value, "sparseVector")))

## value = "sparseVector":
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "missing", j = "index",
				value = "sparseVector"),
		 replDiag)
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index", j = "missing",
				value = "sparseVector"),
		 replDiag)
setReplaceMethod("[", signature(x = "diagonalMatrix", i = "index", j = "index",
				value = "sparseVector"),
		 replDiag)


setMethod("t", signature(x = "diagonalMatrix"),
          function(x) { x@Dimnames <- x@Dimnames[2:1] ; x })

setMethod("isDiagonal",   "diagonalMatrix", function(object) TRUE)
setMethod("isTriangular", "diagonalMatrix", function(object, upper=NA, ...) TRUE)
setMethod("isSymmetric",  "diagonalMatrix", function(object, ...) TRUE)

setMethod("symmpart", signature(x = "diagonalMatrix"), function(x) x)
setMethod("skewpart", signature(x = "diagonalMatrix"), function(x) .setZero(x))

setMethod("chol", signature(x = "ddiMatrix"),
	  function(x, pivot, ...) {
	      if(x@diag == "U") return(x)
	      ## else
	      if(any(x@x < 0))
		  stop("chol() is undefined for diagonal matrix with negative entries")
	      x@x <- sqrt(x@x)
	      x
	  })
## chol(L) is L for logical diagonal:
setMethod("chol", signature(x = "ldiMatrix"), function(x, pivot, ...) x)

setMethod("determinant", signature(x = "diagonalMatrix", logarithm = "logical"),
	  function(x, logarithm, ...) mkDet(.diag.x(x), logarithm))

setMethod("norm", signature(x = "diagonalMatrix", type = "character"),
	  function(x, type, ...) {
	      if((n <- x@Dim[1]) == 0) return(0) # as for "sparseMatrix"
	      type <- toupper(substr(type[1], 1, 1))
	      isU <- (x@diag == "U") # unit-diagonal
	      if(type == "F") sqrt(if(isU) n else sum(x@x^2))
	      else { ## norm == "I","1","O","M" :
		  if(isU) 1 else max(abs(x@x))
	      }
	  })



## Basic Matrix Multiplication {many more to add}
##       ---------------------
## Note that "ldi" logical are treated as numeric
diagdiagprod <- function(x, y) {
    dimCheck(x,y)
    if(x@diag != "U") {
	if(y@diag != "U") {
	    nx <- x@x * y@x
	    if(is.numeric(nx) && !is.numeric(x@x))
		x <- as(x, "dMatrix")
	    x@x <- as.numeric(nx)
	}
	x
    } else ## x is unit diagonal
	y
}

##' Boolean Algebra/Arithmetic Product of Diagonal Matrices
##'  %&%
diagdiagprodBool <- function(x, y) {
    dimCheck(x,y)
    if(x@diag != "U") {
	if(!is.logical(x@x)) x <- as(x, "lMatrix")
	if(y@diag != "U") {
	    nx <- x@x & y@x
	    x@x <- as.logical(nx)
	}
	x
    } else { ## x is unit diagonal: return y
	if(!is.logical(y@x)) y <- as(y, "lMatrix")
	y
    }
}

setMethod("%*%", signature(x = "diagonalMatrix", y = "diagonalMatrix"),
	  diagdiagprod, valueClass = "ddiMatrix")

setMethod("%&%", signature(x = "diagonalMatrix", y = "diagonalMatrix"),
	  diagdiagprodBool, valueClass = "ldiMatrix")# do *not* have "ndiMatrix" !

##' Both Numeric or Boolean Algebra/Arithmetic Product of Diagonal Matrices
diagdiagprodFlexi <- function(x, y=NULL, boolArith = NA, ...)
{
    dimCheck(x,y)
    bool <- isTRUE(boolArith)
    if(x@diag != "U") {
	if(bool && !is.logical(x@x)) x <- as(x, "lMatrix")
	if(y@diag != "U") {
	    if(bool) {
		nx <- x@x & y@x
		x@x <- as.logical(nx)
	    } else { ## boolArith is NA or FALSE: ==> numeric, as have *no* "diagMatrix" patter[n]:
		nx <- x@x * y@x
		if(is.numeric(nx) && !is.numeric(x@x))
		    x <- as(x, "dMatrix")
		x@x <- as.numeric(nx)
	    }
	}
	x
    } else { ## x is unit diagonal: return y
	if(bool && !is.logical(y@x)) y <- as(y, "lMatrix")
	y
    }
}
setMethod("crossprod", signature(x = "diagonalMatrix", y = "diagonalMatrix"),
	  diagdiagprodFlexi)
setMethod("tcrossprod", signature(x = "diagonalMatrix", y = "diagonalMatrix"),
	  diagdiagprodFlexi)

##' crossprod(x) := x'x
diagprod <- function(x, y = NULL, boolArith = NA, ...) {
    bool <- isTRUE(boolArith)
    if(bool && !is.logical(x@x)) x <- as(x, "lMatrix")
    if(x@diag != "U") {
        if(bool) {
            nx <- x@x & y@x
            x@x <- as.logical(nx)
        } else { ## boolArith is NA or FALSE: ==> numeric, as have *no* "diagMatrix" patter[n]:
            nx <- x@x * x@x
            if(is.numeric(nx) && !is.numeric(x@x))
                x <- as(x, "dMatrix")
            x@x <- as.numeric(nx)
        }
    }
    x
}
setMethod( "crossprod", signature(x = "diagonalMatrix", y = "missing"), diagprod)
setMethod("tcrossprod", signature(x = "diagonalMatrix", y = "missing"), diagprod)


## analogous to matdiagprod() below:
diagmatprod <- function(x, y) {
    ## x is diagonalMatrix
    if(x@Dim[2] != nrow(y)) stop("non-matching dimensions")
    Matrix(if(x@diag == "U") y else x@x * y)
}
setMethod("%*%", signature(x = "diagonalMatrix", y = "matrix"), diagmatprod)

##formals(diagmatprod) <- alist(x=, y=NULL, boolArith = NA, ...=) ## FIXME boolArith
diagmatprod2 <- function(x, y=NULL, boolArith = NA, ...) {
    ## x is diagonalMatrix
    if(x@Dim[2] != nrow(y)) stop("non-matching dimensions")
    Matrix(if(x@diag == "U") y else x@x * y)
}
setMethod("crossprod",  signature(x = "diagonalMatrix", y = "matrix"), diagmatprod2)

diagGeprod <- function(x, y) {
    if(x@Dim[2] != y@Dim[1]) stop("non-matching dimensions")
    if(x@diag != "U")
        y@x <- x@x * y@x
    y
}
setMethod("%*%", signature(x= "diagonalMatrix", y= "dgeMatrix"), diagGeprod)
setMethod("%*%", signature(x= "diagonalMatrix", y= "lgeMatrix"), diagGeprod)

diagGeprodBool <- function(x, y) {
    if(x@Dim[2] != y@Dim[1]) stop("non-matching dimensions")
    if(!is.logical(y@x)) y <- as(y, "lMatrix")
    if(x@diag != "U")
        y@x <- x@x & y@x
    y
}
setMethod("%&%", signature(x= "diagonalMatrix", y= "geMatrix"), diagGeprodBool)

diagGeprod2 <- function(x, y=NULL, boolArith = NA, ...) {
    if(x@Dim[2] != y@Dim[1]) stop("non-matching dimensions")
    bool <- isTRUE(boolArith)
    if(bool && !is.logical(y@x)) y <- as(y, "lMatrix")
    if(x@diag != "U")
        y@x <- if(bool) x@x & y@x else x@x * y@x
    y
}
setMethod("crossprod", signature(x = "diagonalMatrix", y = "dgeMatrix"), diagGeprod2)
setMethod("crossprod", signature(x = "diagonalMatrix", y = "lgeMatrix"), diagGeprod2)


## analogous to diagmatprod() above:
matdiagprod <- function(x, y) {
    dx <- dim(x)
    if(dx[2] != y@Dim[1]) stop("non-matching dimensions")
    Matrix(if(y@diag == "U") x else x * rep(y@x, each = dx[1]))
}
setMethod("%*%", signature(x = "matrix", y = "diagonalMatrix"), matdiagprod)

gediagprod <- function(x, y) {
    dx <- dim(x)
    if(dx[2] != y@Dim[1]) stop("non-matching dimensions")
    if(y@diag == "N")
	x@x <- x@x * rep(y@x, each = dx[1])
    x
}
setMethod("%*%", signature(x= "dgeMatrix", y= "diagonalMatrix"), gediagprod)
setMethod("%*%", signature(x= "lgeMatrix", y= "diagonalMatrix"), gediagprod)

gediagprodBool <- function(x, y) {
    dx <- dim(x)
    if(dx[2] != y@Dim[1]) stop("non-matching dimensions")
    if(!is.logical(x@x)) x <- as(x, "lMatrix")
    if(y@diag == "N")
	x@x <- x@x & rep(y@x, each = dx[1])
    x
}
setMethod("%&%", signature(x= "geMatrix", y= "diagonalMatrix"), gediagprodBool)

setMethod("tcrossprod",signature(x = "matrix", y = "diagonalMatrix"),
          function(x, y=NULL, boolArith = NA, ...) {
              dx <- dim(x)
              if(dx[2] != y@Dim[1]) stop("non-matching dimensions")
              bool <- isTRUE(boolArith)
              if(bool && !is.logical(y@x)) y <- as(y, "lMatrix")
              Matrix(if(y@diag == "U") x else
                     if(bool) x & rep(y@x, each = dx[1])
                     else     x * rep(y@x, each = dx[1]))
          })

setMethod("crossprod", signature(x = "matrix", y = "diagonalMatrix"),
	  function(x, y=NULL, boolArith = NA, ...) {
	      dx <- dim(x)
	      if(dx[1] != y@Dim[1]) stop("non-matching dimensions")
              bool <- isTRUE(boolArith)
              if(bool && !is.logical(y@x)) y <- as(y, "lMatrix")
	      Matrix(if(y@diag == "U") t(x) else
		     if(bool) t(rep.int(y@x, dx[2]) & x)
		     else     t(rep.int(y@x, dx[2]) * x))
	  })


gediagprod2 <- function(x, y=NULL, boolArith = NA, ...) {
    dx <- dim(x)
    if(dx[2] != y@Dim[1]) stop("non-matching dimensions")
    bool <- isTRUE(boolArith)
    if(bool && !is.logical(x@x)) x <- as(x, "lMatrix")
    if(y@diag == "N")
	x@x <- if(bool) x@x & rep(y@x, each = dx[1])
	       else     x@x * rep(y@x, each = dx[1])
    x
}
setMethod("tcrossprod", signature(x = "dgeMatrix", y = "diagonalMatrix"), gediagprod2)
setMethod("tcrossprod", signature(x = "lgeMatrix", y = "diagonalMatrix"), gediagprod2)


## crossprod {more of these}

## tcrossprod --- all are not yet there: do the dense ones here:

setMethod("%*%", signature(x = "diagonalMatrix", y = "denseMatrix"),
	  function(x, y) if(x@diag == "U") y else x %*% as(y, "generalMatrix"))
setMethod("%*%", signature(x = "denseMatrix", y = "diagonalMatrix"),
	  function(x, y) if(y@diag == "U") x else as(x, "generalMatrix") %*% y)


## FIXME:
## setMethod("tcrossprod", signature(x = "diagonalMatrix", y = "denseMatrix"),
## 	  function(x, y = NULL) {
##           })

##' @param x CsparseMatrix
##' @param y diagonalMatrix
##' @return x %*% y
Cspdiagprod <- function(x, y, boolArith = NA, ...) {
    if((m <- ncol(x)) != y@Dim[1]) stop("non-matching dimensions")
    if(y@diag == "N") { ## otherwise: y == Diagonal(n) : multiplication is identity
	x <- .Call(Csparse_diagU2N, x)
	cx <- getClass(class(x))
	if(!all(y@x[1L] == y@x[-1L]) && extends(cx, "symmetricMatrix"))
	    x <- as(x, "generalMatrix")
	ind <- rep.int(seq_len(m), x@p[-1] - x@p[-m-1L])
	if(isTRUE(boolArith)) {
	    if(extends(cx, "nMatrix")) x <- as(x, "lMatrix") # so, has y@x
	    x@x <- r <- x@x & y@x[x@i + 1L]
	    if(!anyNA(r) && !extends(cx, "diagonalMatrix")) x <- as(drop0(x), "nMatrix")
	} else {
	    if(!extends(cx, "dMatrix")) x <- as(x, "dMatrix") # <- FIXME if we have zMatrix
	    x@x <- x@x * y@x[ind]
	}
        if(.hasSlot(x, "factors") && length(x@factors)) {# drop cashed ones
	    ## instead of dropping all factors, be smart about some
	    ## TODO ......
	    x@factors <- list()
	}
        x
    } else { #	y is unit-diagonal ==> "return x"
	cx <- getClass(class(x))
	if(isTRUE(boolArith)) {
	    is.l <- if(extends(cx, "dMatrix")) { ## <- FIXME: extend once we have iMatrix, zMatrix
		x <- as(x, "lMatrix"); TRUE } else extends(cx, "lMatrix")
	    if(is.l && !anyNA(x@x)) as(drop0(x), "nMatrix")
	    else if(is.l) x else # defensive:
	    as(x, "lMatrix")
	} else {
	    ## else boolArith is  NA or FALSE {which are equivalent here, das diagonal = "numLike"}
	    if(extends(cx, "nMatrix") || extends(cx, "lMatrix"))
		as(x, "dMatrix") else x
	}
    }
}

##' @param x diagonalMatrix
##' @param y CsparseMatrix
##' @return x %*% y
diagCspprod <- function(x, y, boolArith = NA, ...) {
    if(x@Dim[2] != y@Dim[1]) stop("non-matching dimensions")
    if(x@diag == "N") {
	y <- .Call(Csparse_diagU2N, y)
	cy <- getClass(class(y))
	if(!all(x@x[1L] == x@x[-1L]) && extends(cy, "symmetricMatrix"))
	    y <- as(y, "generalMatrix")
	if(isTRUE(boolArith)) {
	    if(extends(cy, "nMatrix")) y <- as(y, "lMatrix") # so, has y@x
	    y@x <- r <- y@x & x@x[y@i + 1L]
	    if(!anyNA(r) && !extends(cy, "diagonalMatrix")) y <- as(drop0(y), "nMatrix")
	} else {
	    if(!extends(cy, "dMatrix")) y <- as(y, "dMatrix") # <- FIXME if we have zMatrix
	    y@x <- y@x * x@x[y@i + 1L]
	}
	if(.hasSlot(y, "factors") && length(y@factors)) {
     ## if(.hasSlot(y, "factors") && length(yf <- y@factors)) { ## -- TODO? --
	    ## instead of dropping all factors, be smart about some
	    ## keep <- character()
	    ## if(any(names(yf) == "LU")) { ## <- not easy: y = P'LUQ,  x y = xP'LUQ => LU ???
	    ##     keep <- "LU"
	    ## }
	    ## y@factors <- yf[keep]
	    y@factors <- list()
        }
        y
    } else { ## x @ diag  == "U"
	cy <- getClass(class(y))
	if(isTRUE(boolArith)) {
	    is.l <- if(extends(cy, "dMatrix")) { ## <- FIXME: extend once we have iMatrix, zMatrix
		y <- as(y, "lMatrix"); TRUE } else extends(cy, "lMatrix")
	    if(is.l && !anyNA(y@x)) as(drop0(y), "nMatrix")
	    else if(is.l) y else # defensive:
	    as(y, "lMatrix")
	} else {
	    ## else boolArith is  NA or FALSE {which are equivalent here, das diagonal = "numLike"}
	    if(extends(cy, "nMatrix") || extends(cy, "lMatrix"))
		as(y, "dMatrix") else y
	}
    }
}

## + 'boolArith' argument  { ==> .local() is used in any case; keep formals simple :}
setMethod("crossprod", signature(x = "diagonalMatrix", y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) diagCspprod(x, y, boolArith=boolArith))

setMethod("crossprod", signature(x = "diagonalMatrix", y = "sparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      diagCspprod(x, as(y, "CsparseMatrix"), boolArith=boolArith))

## Prefer calling diagCspprod to Cspdiagprod if going to transpose anyway
##  x'y == (y'x)'
setMethod("crossprod", signature(x = "CsparseMatrix", y = "diagonalMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) t(diagCspprod(y, x, boolArith=boolArith)))

setMethod("crossprod", signature(x = "sparseMatrix", y = "diagonalMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) t(diagCspprod(y, as(x, "Csparsematrix"), boolArith=boolArith)))

setMethod("tcrossprod", signature(x = "diagonalMatrix", y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) diagCspprod(x, t(y), boolArith=boolArith))

setMethod("tcrossprod", signature(x = "diagonalMatrix", y = "sparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) diagCspprod(x, t(as(y, "CsparseMatrix")), boolArith=boolArith))

setMethod("tcrossprod", signature(x = "CsparseMatrix", y = "diagonalMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) Cspdiagprod(x, y, boolArith=boolArith))

setMethod("tcrossprod", signature(x = "sparseMatrix", y = "diagonalMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) Cspdiagprod(as(x, "CsparseMatrix"), y, boolArith=boolArith))

setMethod("%*%", signature(x = "diagonalMatrix", y = "CsparseMatrix"),
	  function(x, y) diagCspprod(x, y, boolArith=NA))
setMethod("%&%", signature(x = "diagonalMatrix", y = "CsparseMatrix"),
	  function(x, y) diagCspprod(x, y, boolArith=TRUE))

## instead of "sparseMatrix", use: [RT]sparse.. ("closer" in method dispatch)
for(cl in c("TsparseMatrix", "RsparseMatrix")) {

setMethod("%*%", signature(x = "diagonalMatrix", y = "sparseMatrix"),
	  function(x, y) diagCspprod(as(x, "CsparseMatrix"), y, boolArith=NA))

setMethod("%*%", signature(x = "sparseMatrix", y = "diagonalMatrix"),
	  function(x, y) Cspdiagprod(as(x, "CsparseMatrix"), y, boolArith=NA))

setMethod("%&%", signature(x = "diagonalMatrix", y = "sparseMatrix"),
	  function(x, y) diagCspprod(as(x, "CsparseMatrix"), y, boolArith=TRUE))

setMethod("%&%", signature(x = "sparseMatrix", y = "diagonalMatrix"),
	  function(x, y) Cspdiagprod(as(x, "CsparseMatrix"), y, boolArith=TRUE))

}

setMethod("%*%", signature(x = "CsparseMatrix", y = "diagonalMatrix"),
	  function(x, y) Cspdiagprod(x, y, boolArith=NA))
setMethod("%&%", signature(x = "CsparseMatrix", y = "diagonalMatrix"),
	  function(x, y) Cspdiagprod(x, y, boolArith=TRUE))

## TODO: Write tests in ./tests/ which ensure that many "ops" with diagonal*
##       do indeed work by going through sparse (and *not* ddense)!

setMethod("solve", signature(a = "diagonalMatrix", b = "missing"),
	  function(a, b, ...) {
	      a@x <- 1/ a@x
	      a@Dimnames <- a@Dimnames[2:1]
	      a
	  })

solveDiag <- function(a, b, ...) {
    if(a@Dim[1] != nrow(b))
        stop("incompatible matrix dimensions")
    ## trivially invert a 'in place' and multiply:
    a@x <- 1/ a@x
    a@Dimnames <- a@Dimnames[2:1]
    a %*% b
}
setMethod("solve", signature(a = "diagonalMatrix", b = "matrix"),
          solveDiag)
setMethod("solve", signature(a = "diagonalMatrix", b = "Matrix"),
          solveDiag)

## Schur()  ---> ./eigen.R



###---------------- <Ops> (<Arith>, <Logic>, <Compare> ) ----------------------

## Use function for several signatures, in order to evade
diagOdiag <- function(e1,e2) {
    ## result should also be diagonal _ if possible _
    r <- callGeneric(.diag.x(e1), .diag.x(e2)) # error if not "compatible"
    ## Check what happens with non-diagonals, i.e. (0 o 0), (FALSE o 0), ...:
    r00 <- callGeneric(if(is.numeric(e1@x)) 0 else FALSE,
		       if(is.numeric(e2@x)) 0 else FALSE)
    if(is0(r00)) { ##  r00 == 0 or FALSE --- result *is* diagonal
	if(is.numeric(r)) { # "double" *or* "integer"
	    if(is.numeric(e2@x)) {
		e2@x <- r; return(.diag.2N(e2)) }
	    if(!is.numeric(e1@x))
		## e.g. e1, e2 are logical;
		e1 <- as(e1, "dMatrix")
	    if(!is.double(r)) r <- as.double(r)
	}
	else if(is.logical(r))
	    e1 <- as(e1, "lMatrix")
	else stop(gettextf("intermediate 'r' is of type %s",
			   typeof(r)), domain=NA)
	e1@x <- r
	.diag.2N(e1)
    }
    else { ## result not diagonal, but at least symmetric:
        ## e.g., m == m
	isNum <- (is.numeric(r) || is.numeric(r00))
	isLog <- (is.logical(r) || is.logical(r00))
        Matrix.msg("exploding <diag> o <diag> into dense matrix", .M.level = 2)
	d <- e1@Dim
	n <- d[1]
	stopifnot(length(r) == n)
	if(isNum && !is.double(r)) r <- as.double(r)
	xx <- as.vector(matrix(rbind(r, matrix(r00,n,n)), n,n))
	newcl <-
	    paste0(if(isNum) "d" else if(isLog) {
		if(!anyNA(r) && !anyNA(r00)) "n" else "l"
	    } else stop("not yet implemented .. please report"), "syMatrix")

	new(newcl, Dim = e1@Dim, Dimnames = e1@Dimnames, x = xx)
    }
}

### This would be *the* way, but we get tons of "ambiguous method dispatch"
## we use this hack instead of signature  x = "diagonalMatrix" :
diCls <- names(getClass("diagonalMatrix")@subclasses)
if(FALSE) {
setMethod("Ops", signature(e1 = "diagonalMatrix", e2 = "diagonalMatrix"),
          diagOdiag)
} else { ## These are just for method disambiguation:
    for(c1 in diCls)
	for(c2 in diCls)
	    setMethod("Ops", signature(e1 = c1, e2 = c2), diagOdiag)
}

## diagonal  o  triangular  |-->  triangular
## diagonal  o  symmetric   |-->  symmetric
##    {also when other is sparse: do these "here" --
##     before conversion to sparse, since that loses "diagonality"}
diagOtri <- function(e1,e2) {
    ## result must be triangular
    r <- callGeneric(d1 <- .diag.x(e1), diag(e2)) # error if not "compatible"
    ## Check what happens with non-diagonals, i.e. (0 o 0), (FALSE o 0), ...:
    e1.0 <- if(is.numeric(d1)) 0 else FALSE
    r00 <- callGeneric(e1.0, if(.n2 <- is.numeric(e2[0])) 0 else FALSE)
    if(is0(r00)) { ##  r00 == 0 or FALSE --- result *is* triangular
        diag(e2) <- r
        ## check what happens "in the triangle"
        e2.2 <- if(.n2) 2 else TRUE
        if(!callGeneric(e1.0, e2.2) == e2.2) { # values "in triangle" can change:
            n <- dim(e2)[1L]
            it <- indTri(n, upper = (e2@uplo == "U"))
            e2[it] <- callGeneric(e1.0, e2[it])
        }
        e2
    }
    else { ## result not triangular ---> general
        rr <- as(e2, "generalMatrix")
        diag(rr) <- r
        rr
    }
}


setMethod("Ops", signature(e1 = "diagonalMatrix", e2 = "triangularMatrix"),
          diagOtri)
## For the reverse,  Ops == "Arith" | "Compare" | "Logic"
##   'Arith'  :=  '"+"', '"-"', '"*"', '"^"', '"%%"', '"%/%"', '"/"'
setMethod("Arith", signature(e1 = "triangularMatrix", e2 = "diagonalMatrix"),
          function(e1,e2)
      { ## this must only trigger for *dense* e1
	  switch(.Generic,
		 "+" = .Call(dtrMatrix_addDiag, as(e1,"dtrMatrix"),   .diag.x(e2)),
		 "-" = .Call(dtrMatrix_addDiag, as(e1,"dtrMatrix"), - .diag.x(e2)),
		 "*" = {
		     n <- e2@Dim[1L]
		     d2 <- if(e2@diag == "U") { # unit-diagonal
			 d <- rep.int(as1(e2@x), n)
			 e2@x <- d
			 e2@diag <- "N"
			 d
		     } else e2@x
		     e2@x <- diag(e1) * d2
		     e2
		 },
		 "^" = { ## will be dense ( as  <ANY> ^ 0 == 1 ):
		     e1 ^ as(e2, "denseMatrix")
		 },
		 ## otherwise:
		 callGeneric(e1, diag2Tsmart(e2,e1)))
})

## Compare --> 'swap' (e.g.   e1 < e2   <==>  e2 > e1 ):
setMethod("Compare", signature(e1 = "triangularMatrix", e2 = "diagonalMatrix"),
	  .Cmp.swap)
## '&' and "|'  are commutative:
setMethod("Logic", signature(e1 = "triangularMatrix", e2 = "diagonalMatrix"),
          function(e1,e2) callGeneric(e2, e1))

## For almost everything else, diag* shall be treated "as sparse" :
## These are cheap implementations via coercion

## For disambiguation --- define this for "sparseMatrix" , then for "ANY";
## and because we can save an .M.kind() call, we use this explicit
## "hack" for all diagonalMatrix *subclasses* instead of just "diagonalMatrix" :
##
## ddi*:
setMethod("Ops", signature(e1 = "ddiMatrix", e2 = "sparseMatrix"),
	  function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "d"), e2))
setMethod("Ops", signature(e1 = "sparseMatrix", e2 = "ddiMatrix"),
	  function(e1,e2) callGeneric(e1, diag2Tsmart(e2,e1, "d")))
## ldi*
setMethod("Ops", signature(e1 = "ldiMatrix", e2 = "sparseMatrix"),
	  function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "l"), e2))
setMethod("Ops", signature(e1 = "sparseMatrix", e2 = "ldiMatrix"),
	  function(e1,e2) callGeneric(e1, diag2Tsmart(e2,e1, "l")))

## Ops:	 Arith	--> numeric : "dMatrix"
##	 Compare --> logical
##	 Logic	 --> logical: "lMatrix"

## Other = "numeric" : stay diagonal if possible
## ddi*: Arith: result numeric, potentially ddiMatrix
for(arg2 in c("numeric","logical"))
setMethod("Arith", signature(e1 = "ddiMatrix", e2 = arg2),
	  function(e1,e2) {
	      n <- e1@Dim[1]
	      f0 <- callGeneric(0, e2)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e2)) == 1L
		  if(e1@diag == "U") {
		      if(any((r <- callGeneric(1, e2)) != 1)) {
			  e1@diag <- "N"
			  e1@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = e1  (is "U" diag)
		  } else {
		      r <- callGeneric(e1@x, e2)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      e1@x[] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  e1
	      } else
		  callGeneric(diag2tT.u(e1,e2, "d"), e2)
	  })

for(arg1 in c("numeric","logical"))
setMethod("Arith", signature(e1 = arg1, e2 = "ddiMatrix"),
	  function(e1,e2) {
	      n <- e2@Dim[1]
	      f0 <- callGeneric(e1, 0)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e1)) == 1L
		  if(e2@diag == "U") {
		      if(any((r <- callGeneric(e1, 1)) != 1)) {
			  e2@diag <- "N"
			  e2@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = e2  (is "U" diag)
		  } else {
		      r <- callGeneric(e1, e2@x)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      e2@x[] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  e2
	      } else
		  callGeneric(e1, diag2tT.u(e2,e1, "d"))
	  })

## ldi* Arith --> result numeric, potentially ddiMatrix
for(arg2 in c("numeric","logical"))
setMethod("Arith", signature(e1 = "ldiMatrix", e2 = arg2),
	  function(e1,e2) {
	      n <- e1@Dim[1]
	      f0 <- callGeneric(0, e2)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e2)) == 1L
		  E <- copyClass(e1, "ddiMatrix", c("diag", "Dim", "Dimnames"), check=FALSE)
		  ## storage.mode(E@x) <- "double"
		  if(e1@diag == "U") {
		      if(any((r <- callGeneric(1, e2)) != 1)) {
			  E@diag <- "N"
			  E@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = E  (is "U" diag)
		  } else {
		      r <- callGeneric(e1@x, e2)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      E@x[seq_len(n)] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  E
	      } else
		  callGeneric(diag2tT.u(e1,e2, "l"), e2)
	  })

for(arg1 in c("numeric","logical"))
setMethod("Arith", signature(e1 = arg1, e2 = "ldiMatrix"),
	  function(e1,e2) {
	      n <- e2@Dim[1]
	      f0 <- callGeneric(e1, 0)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e1)) == 1L
		  E <- copyClass(e2, "ddiMatrix", c("diag", "Dim", "Dimnames"), check=FALSE)
		  ## storage.mode(E@x) <- "double"
		  if(e2@diag == "U") {
		      if(any((r <- callGeneric(e1, 1)) != 1)) {
			  E@diag <- "N"
			  E@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = E  (is "U" diag)
		  } else {
		      r <- callGeneric(e1, e2@x)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      E@x[seq_len(n)] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  E
	      } else
		  callGeneric(e1, diag2tT.u(e2,e1, "l"))
	  })

## ddi*: for "Ops" without "Arith": <Compare> or <Logic> --> result logical, potentially ldi
##
## Note that  ("numeric", "ddiMatrix")  is simply swapped, e.g.,
if(FALSE) {
    selectMethod("<", c("numeric","lMatrix"))# Compare
    selectMethod("&", c("numeric","lMatrix"))# Logic
} ## so we don't need to define a method here :

for(arg2 in c("numeric","logical"))
setMethod("Ops", signature(e1 = "ddiMatrix", e2 = arg2),
	  function(e1,e2) {
	      n <- e1@Dim[1]
	      f0 <- callGeneric(0, e2)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e2)) == 1L
		  E <- copyClass(e1, "ldiMatrix", c("diag", "Dim", "Dimnames"), check=FALSE)
		  ## storage.mode(E@x) <- "logical"
		  if(e1@diag == "U") {
		      if(any((r <- callGeneric(1, e2)) != 1)) {
			  E@diag <- "N"
			  E@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = E  (is "U" diag)
		  } else {
		      r <- callGeneric(e1@x, e2)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      E@x[seq_len(n)] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  E
	      } else
		  callGeneric(diag2tT.u(e1,e2, "d"), e2)
	  })

## ldi*: for "Ops" without "Arith": <Compare> or <Logic> --> result logical, potentially ldi
for(arg2 in c("numeric","logical"))
setMethod("Ops", signature(e1 = "ldiMatrix", e2 = arg2),
	  function(e1,e2) {
	      n <- e1@Dim[1]
	      f0 <- callGeneric(FALSE, e2)
	      if(all0(f0)) { # remain diagonal
		  L1 <- (le <- length(e2)) == 1L

		  if(e1@diag == "U") {
		      if(any((r <- callGeneric(TRUE, e2)) != 1)) {
			  e1@diag <- "N"
			  e1@x[seq_len(n)] <- r # possibly recycling r
		      } ## else: result = e1  (is "U" diag)
		  } else {
		      r <- callGeneric(e1@x, e2)
		      ## "future fixme": if we have idiMatrix, and r is 'integer', use idiMatrix
		      e1@x[] <- if(L1) r else r[1L + ((n+1)*(0:(n-1L))) %% le]
		  }
		  e1
	      } else
		  callGeneric(diag2tT.u(e1,e2, "l"), e2)
	  })


## Not {"sparseMatrix", "numeric} :  {"denseMatrix", "matrix", ... }
for(other in c("ANY", "Matrix", "dMatrix")) {
    ## ddi*:
    setMethod("Ops", signature(e1 = "ddiMatrix", e2 = other),
	      function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "d"), e2))
    setMethod("Ops", signature(e1 = other, e2 = "ddiMatrix"),
	      function(e1,e2) callGeneric(e1, diag2Tsmart(e2,e1, "d")))
    ## ldi*:
    setMethod("Ops", signature(e1 = "ldiMatrix", e2 = other),
	      function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "l"), e2))
    setMethod("Ops", signature(e1 = other, e2 = "ldiMatrix"),
	      function(e1,e2) callGeneric(e1, diag2Tsmart(e2,e1, "l")))
}

## Direct subclasses of "denseMatrix": currently ddenseMatrix, ldense... :
if(FALSE) # now also contains "geMatrix"
dense.subCl <- local({ dM.scl <- getClass("denseMatrix")@subclasses
		       names(dM.scl)[vapply(dM.scl, slot, 0, "distance") == 1] })
dense.subCl <- paste0(c("d","l","n"), "denseMatrix")
for(DI in diCls) {
    dMeth <- if(extends(DI, "dMatrix"))
	function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "d"), e2)
    else # "lMatrix", the only other kind for now
	function(e1,e2) callGeneric(diag2Tsmart(e1,e2, "l"), e2)
    for(c2 in c(dense.subCl, "Matrix")) {
	for(Fun in c("*", "&")) {
	    setMethod(Fun, signature(e1 = DI, e2 = c2),
		      function(e1,e2) callGeneric(e1, Diagonal(x = diag(e2))))
	    setMethod(Fun, signature(e1 = c2, e2 = DI),
		      function(e1,e2) callGeneric(Diagonal(x = diag(e1)), e2))
	}
	setMethod("^", signature(e1 = c2, e2 = DI),
		  function(e1,e2) callGeneric(Diagonal(x = diag(e1)), e2))
	for(Fun in c("%%", "%/%", "/")) ## 0 <op> 0 |--> NaN  for these.
	    setMethod(Fun, signature(e1 = DI, e2 = c2), dMeth)
    }
}

## Group methods "Math", "Math2" in			--> ./Math.R

### "Summary" : "max"   "min"   "range" "prod"  "sum"   "any"   "all"
### ----------   the last 4: separately here
for(cl in diCls) {
setMethod("any", cl,
	  function (x, ..., na.rm) {
	      if(any(x@Dim == 0)) FALSE
	      else if(x@diag == "U") TRUE else any(x@x, ..., na.rm = na.rm)
	  })
setMethod("all",  cl, function (x, ..., na.rm) {
    n <- x@Dim[1]
    if(n >= 2) FALSE
    else if(n == 0 || x@diag == "U") TRUE
    else all(x@x, ..., na.rm = na.rm)
})
setMethod("prod", cl, function (x, ..., na.rm) {
    n <- x@Dim[1]
    if(n >= 2) 0
    else if(n == 0 || x@diag == "U") 1
    else ## n == 1, diag = "N" :
	prod(x@x, ..., na.rm = na.rm)
})

setMethod("sum", cl,
	  function(x, ..., na.rm) {
	      r <- sum(x@x, ..., na.rm = na.rm)# double or integer, correctly
	      if(x@diag == "U" && !is.na(r)) r + x@Dim[1] else r
	  })
}

## The remaining ones are  max, min, range :

setMethod("Summary", "ddiMatrix",
	  function(x, ..., na.rm) {
	      if(any(x@Dim == 0)) callGeneric(numeric(0), ..., na.rm=na.rm)
	      else if(x@diag == "U")
		  callGeneric(x@x, 0, 1, ..., na.rm=na.rm)
	      else callGeneric(x@x, 0, ..., na.rm=na.rm)
	  })
setMethod("Summary", "ldiMatrix",
	  function(x, ..., na.rm) {
	      if(any(x@Dim == 0)) callGeneric(logical(0), ..., na.rm=na.rm)
	      else if(x@diag == "U")
		  callGeneric(x@x, FALSE, TRUE, ..., na.rm=na.rm)
	      else callGeneric(x@x, FALSE, ..., na.rm=na.rm)
	  })



## similar to prTriang() in ./Auxiliaries.R :
prDiag <-
    function(x, digits = getOption("digits"), justify = "none", right = TRUE)
{
    cf <- array(".", dim = x@Dim, dimnames = x@Dimnames)
    cf[row(cf) == col(cf)] <-
	vapply(diag(x), format, "", digits = digits, justify = justify)
    print(cf, quote = FALSE, right = right)
    invisible(x)
}

## somewhat consistent with "print" for sparseMatrix :
setMethod("print", signature(x = "diagonalMatrix"), prDiag)

setMethod("show", signature(object = "diagonalMatrix"),
	  function(object) {
	      d <- dim(object)
	      cl <- class(object)
	      cat(sprintf('%d x %d diagonal matrix of class "%s"',
			  d[1], d[2], cl))
	      if(d[1] < 50) {
		  cat("\n")
		  prDiag(object)
	      } else {
		  cat(", with diagonal entries\n")
		  show(diag(object))
		  invisible(object)
	      }
	  })

rm(arg1, arg2, other, DI, Fun, cl, c1, c2,
   dense.subCl, diCls)# not used elsewhere

setMethod("summary", signature(object = "diagonalMatrix"),
	  function(object, ...) {
	      d <- dim(object)
	      r <- summary(object@x, ...)
	      attr(r, "header") <-
		  sprintf('%d x %d diagonal Matrix of class "%s"',
			  d[1], d[2], class(object))
	      ## use ole' S3 technology for such a simple case
	      class(r) <- c("diagSummary", class(r))
	      r
	  })

print.diagSummary <- function (x, ...) {
    cat(attr(x, "header"),"\n")
    class(x) <- class(x)[-1]
    print(x, ...)
    invisible(x)
}
