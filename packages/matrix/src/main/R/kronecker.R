#### Collect methods for  kronecker() here.
####			  ===========

### ... all but the ``fall back methods'' which are in ./Matrix.R ...
##						       ~~~~~~~~~~

### Request: Should be *fast* particularly when used with Diagonal() !

tmp <- function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
    kronecker(as(X, "TsparseMatrix"), Y,
	      FUN = FUN, make.dimnames = make.dimnames, ...)
}
setMethod("kronecker", signature(X="diagonalMatrix", Y="ANY"	    ), tmp)
setMethod("kronecker", signature(X="diagonalMatrix", Y="Matrix"	    ), tmp)
setMethod("kronecker", signature(X="ANY",	   Y="sparseMatrix" ), tmp)
## the above could recurse infinitely :
setMethod("kronecker", signature(X="sparseMatrix", Y="TsparseMatrix"), tmp)

tmp <- function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
    kronecker(X, as(Y, "TsparseMatrix"),
	      FUN = FUN, make.dimnames = make.dimnames, ...)
}
setMethod("kronecker", signature(X="ANY",	  Y="diagonalMatrix"), tmp)
setMethod("kronecker", signature(X="Matrix",	  Y="diagonalMatrix"), tmp)
setMethod("kronecker", signature(X="sparseMatrix",  Y="ANY"	    ), tmp)
setMethod("kronecker", signature(X="TsparseMatrix", Y="sparseMatrix"), tmp)
rm(tmp)

## from ./dgTMatrix.R :
setMethod("kronecker", signature(X = "dgTMatrix", Y = "dgTMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...)
      {
	  if (FUN != "*") stop("kronecker method must use default 'FUN'")
	  ## otherwise we don't know that many results will be zero
	  ydim <- Y@Dim
	  xi <- X@i
	  xnnz <- length(xi)
	  yi <- Y@i
	  ynnz <- length(yi)
	  new("dgTMatrix", Dim = X@Dim * ydim,
	      i = rep.int(yi, xnnz) + ydim[1] * rep.int(xi, rep.int(ynnz, xnnz)),
	      j = rep.int(Y@j, xnnz) + ydim[2] * rep.int(X@j, rep.int(ynnz, xnnz)),
	      ## faster than x = as.vector(outer(Y@x, X@x, FUN = FUN)
	      x = as.vector(Y@x %*% t(X@x)))
      })

## triangularity -- should be preserved "when obvious":
setMethod("kronecker", signature(X = "dtTMatrix", Y = "dtTMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...)
      {
	  if (FUN != "*") stop("kronecker method must use default 'FUN'")
	  ## otherwise we don't know that many results will be zero
	  if(X@uplo != Y@uplo) { ## result not triangular
	      X <- as(X, "dgTMatrix")
	      Y <- as(Y, "dgTMatrix")
	      return(callGeneric())
	  }
	  ## else: both 'uplo' are the same -- result *is* triangular
	  ## d.U <- (dX <- X@diag == "U") && (dY <- Y@diag == "U")
	  if(Y@diag == "U")
	      Y <- .diagU2N(Y, "dtTMatrix")
	  ydim <- Y@Dim
	  if(X@diag != "U") {
	      xi <- X@i
	      xj <- X@j
	      xx <- X@x
	  } else { ## X@diag == "U"
	      nx <- X@Dim[1] # triangular matrices are square
	      ii <- seq_len(nx) - 1L
	      xi <- c(X@i, ii)
	      xj <- c(X@j, ii)
	      xx <- c(X@x, rep.int(1, nx))
	  }
	  xnnz <- length(xi)
	  yi <- Y@i
	  ynnz <- length(yi)
	  new("dtTMatrix", Dim = X@Dim * ydim,
	      i = rep.int(yi,  xnnz) + ydim[1] * rep.int(xi, rep.int(ynnz, xnnz)),
	      j = rep.int(Y@j, xnnz) + ydim[2] * rep.int(xj, rep.int(ynnz, xnnz)),
	      ## faster than x = as.vector(outer(Y@x, X@x, FUN = FUN)
	      x = as.vector(Y@x %*% t(xx)),
	      uplo = X@uplo,
	      diag = "N" # if(d.U) { "U" , but drop the entries}  else "N"
	      )
      })

setMethod("kronecker", signature(X = "dtTMatrix", Y = "dgTMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      if(trY <- isTriangular(Y)) {
		  Y <- gT2tT(Y, uplo = .if.NULL(attr(trY, "kind"), "U"),
			     diag = "N", ## improve: also test for unit diagonal
			     toClass = "dtTMatrix", do.n= FALSE)
	      }
	      else {
		  X <- as(X, "dgTMatrix")
	      }
	      callGeneric() #-> dtT o dtT   or	 dgT o dgT
	  })

setMethod("kronecker", signature(X = "dgTMatrix", Y = "dtTMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      if(trX <- isTriangular(X)) {
		  X <- gT2tT(X, uplo = .if.NULL(attr(trX, "kind"), "U"),
			     diag = "N", ## improve: also test for unit diagonal
			     toClass = "dtTMatrix", do.n= FALSE)
	      }
	      else {
		  Y <- as(Y, "dgTMatrix")
	      }
	      callGeneric() #-> dtT o dtT   or	 dgT o dgT
	  })

setMethod("kronecker", signature(X = "TsparseMatrix", Y = "TsparseMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      if(!is(X, "dMatrix")) X <- as(X, "dMatrix")
	      if(!is(Y, "dMatrix")) Y <- as(Y, "dMatrix")
	      if(is(X, "symmetricMatrix")) X <- as(X, "generalMatrix")
	      if(is(Y, "symmetricMatrix")) Y <- as(Y, "generalMatrix")
	      callGeneric()
	  })

if(FALSE) # probably not needed
setMethod("kronecker", signature(X = "dgTMatrix", Y = "TsparseMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      ## a case where Y is neither "dgT" nor "dtT" :
	      if(!is(Y, "dMatrix")) Y <- as(Y, "dMatrix")
	      if(is(Y, "symmetricMatrix")) Y <- as(Y, "generalMatrix")
	      callGeneric()
	  })

## from ./dsparseMatrix.R :
setMethod("kronecker", signature(X = "dsparseMatrix", Y = "dsparseMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      if(is(X, "symmetricMatrix")) X <- as(X, "generalMatrix")
	      if(is(Y, "symmetricMatrix")) Y <- as(Y, "generalMatrix")
	      kronecker(as(X, "TsparseMatrix"), as(Y, "TsparseMatrix"),
			FUN = FUN, make.dimnames = make.dimnames, ...)
	  })

