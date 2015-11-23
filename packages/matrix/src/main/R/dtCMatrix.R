#### Triangular Sparse Matrices in compressed column-oriented format

setAs("dtCMatrix", "ltCMatrix",
      function(from) new("ltCMatrix", i = from@i, p = from@p,
			 uplo = from@uplo, diag = from@diag,
                         x = as.logical(from@x),
			 ## FIXME?: use from@factors smartly
			 Dim = from@Dim, Dimnames = from@Dimnames))
setAs("dtCMatrix", "ntCMatrix", # just drop 'x' slot:
      function(from) new("ntCMatrix", i = from@i, p = from@p,
			 uplo = from@uplo, diag = from@diag,
			 ## FIXME?: use from@factors smartly
			 Dim = from@Dim, Dimnames = from@Dimnames))


setAs("matrix", "dtCMatrix",
      function(from) as(as(from, "dtTMatrix"), "dtCMatrix"))

setAs("dtCMatrix", "dgCMatrix",
      function(from) {
          if (from@diag == "U")
              from <- .Call(Csparse_diagU2N, from)
          new("dgCMatrix",
              i = from@i, p = from@p, x = from@x,
              Dim = from@Dim, Dimnames = from@Dimnames)
      })

setAs("dtCMatrix", "dsCMatrix", function(from) as(from, "symmetricMatrix"))

setAs("dtCMatrix", "dgTMatrix",
      function(from) {
          if (from@diag == "U") from <- .Call(Csparse_diagU2N, from)
          ## ignore triangularity in conversion to TsparseMatrix
          .Call(Csparse_to_Tsparse, from, FALSE)
      })

## FIXME: make more efficient
## -----  and  as(., "triangularMatrix") is even worse via as_Sp()
setAs("dgCMatrix", "dtCMatrix", # to triangular, needed for triu,..
      function(from) as(.Call(Csparse_to_Tsparse, from, FALSE), "dtCMatrix"))

setAs("dtCMatrix", "dgeMatrix",
      function(from) as(as(from, "dgTMatrix"), "dgeMatrix"))

## These are all needed because cholmod doesn't support triangular:
## (see end of ./Csparse.R ), e.g. for triu()
setAs("dtCMatrix", "dtTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, TRUE))
##   {# and this is not elegant:
##           x <- as(from, "dgTMatrix")
##  	  if (from@diag == "U") { ## drop diagonal entries '1':
##  	      i <- x@i; j <- x@j
##  	      nonD <- i != j
##  	      xx <- x@x[nonD] ; i <- i[nonD] ; j <- j[nonD]
##  	  } else {
##  	      xx <- x@x; i <- x@i; j <- x@j
##  	  }
##  	  new("dtTMatrix", x = xx, i = i, j = j, Dim = x@Dim,
##  	      Dimnames = x@Dimnames, uplo = from@uplo, diag = from@diag)
##       })

## Now that we support triangular matrices use the inherited method.
## setAs("dtCMatrix", "TsparseMatrix", function(from) as(from, "dtTMatrix"))

setAs("dtCMatrix", "dtrMatrix",
      function(from) as(as(from, "dtTMatrix"), "dtrMatrix"))

setMethod("determinant", signature(x = "dtCMatrix", logarithm = "logical"),
	  function(x, logarithm = TRUE, ...) {
	      if(x@diag == "N")
		  mkDet(diag(x), logarithm)
	      else
		  structure(list(modulus = structure(if (logarithm) 0 else 1,
				 "logarithm" = logarithm),
				 sign = 1L),
			    class = "det")
	  })


setMethod("solve", signature(a = "dtCMatrix", b = "missing"),
	  function(a, b, ...) {
	      stopifnot((n <- nrow(a)) == ncol(a))
	      as(.Call(dtCMatrix_sparse_solve, a, .trDiagonal(n, unitri=FALSE)),
                 "dtCMatrix")
          }, valueClass = "dtCMatrix")

setMethod("solve", signature(a = "dtCMatrix", b = "dgeMatrix"),
	  function(a, b, ...) .Call(dtCMatrix_matrix_solve, a, b, TRUE),
	  valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dtCMatrix", b = "CsparseMatrix"),
	  function(a, b, ...) .sortCsparse(.Call(dtCMatrix_sparse_solve, a, b)),
	  ##                  ------------ TODO: both in C code
	  valueClass = "dgCMatrix")

setMethod("solve", signature(a = "dtCMatrix", b = "matrix"),
	  function(a, b, ...) {
            storage.mode(b) <- "double"
            .Call(dtCMatrix_matrix_solve, a, b, FALSE)
	  }, valueClass = "dgeMatrix")

## Isn't this case handled by the method for (a = "Matrix', b =
## "numeric") in ./Matrix.R? Or is this method defined here for
## the as.double coercion?
setMethod("solve", signature(a = "dtCMatrix", b = "numeric"),
	  function(a, b, ...) .Call(dtCMatrix_matrix_solve, a,
                                    as.matrix(as.double(b)), FALSE),
          valueClass = "dgeMatrix")

if(FALSE)## still not working
setMethod("diag", "dtCMatrix",
	  function(x, nrow, ncol) .Call(diag_tC, x, "diag"))


## no pivoting here, use  L or U
setMethod("lu", "dtCMatrix",
	  function(x, ...) {
	      n <- (d <- x@Dim)[1L]
	      p <- 0:(n-1L)
	      if(x@uplo == "U")
		  new("sparseLU",
		      L = .trDiagonal(n, uplo="L"),
		      U = x,
		      p = p, q = p, Dim = d)
	      else { ## "L" :  x = L = L I
		  d <- diag(x)
		  new("sparseLU",
		      L = x %*% Diagonal(n, 1/d),
		      U = .trDiagonal(n, x = d),
		      p = p, q = p, Dim = d)
	      }
	  })


