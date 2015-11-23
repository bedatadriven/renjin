#### Positive-definite Symmetric Packed Matrices -- Coercion and Methods

setAs("dppMatrix", "dpoMatrix",
      function(from)
      copyClass(.Call(dspMatrix_as_dsyMatrix, from),
		"dpoMatrix",
		sNames = c("x", "Dim", "Dimnames", "uplo", "factors")))#FIXME , check=FALSE
dpp2sC <- function(from) as(.Call(dspMatrix_as_dsyMatrix, from), "dsCMatrix")
## setAs("dppMatrix", "dsCMatrix", dpp2sC)
setAs("dppMatrix", "CsparseMatrix", dpp2sC)
setAs("dppMatrix", "sparseMatrix", dpp2sC)

setAs("dppMatrix", "lMatrix",
      function(from) as(as(from, "dsyMatrix"), "lMatrix"))
setAs("dppMatrix", "nMatrix",
      function(from) as(as(from, "dsyMatrix"), "nMatrix"))

to_dpp <- function(from) as(as(as(as(from, "symmetricMatrix"), "dMatrix"),
			       "dpoMatrix"), "dppMatrix")
setAs("Matrix", "dppMatrix", to_dpp)# some may fail, but this tries
setAs("matrix", "dppMatrix", to_dpp)

setAs("dspMatrix", "dppMatrix",
      function(from){
	  if(is.null(tryCatch(.Call(dppMatrix_chol, from),
			      error = function(e) NULL)))
	      stop("not a positive definite matrix")
	  ## else
	  copyClass(from, "dppMatrix",
		    sNames = c("x", "Dim", "Dimnames", "uplo", "factors"))#FIXME , check=FALSE
      })


setMethod("chol", signature(x = "dppMatrix"),
	  function(x, pivot, LINPACK) .Call(dppMatrix_chol, x))

setMethod("determinant", signature(x = "dppMatrix", logarithm = "logical"), mkDet.via.chol)
setMethod("determinant", signature(x = "dppMatrix", logarithm = "missing"),
	  function(x, logarithm, ...) mkDet.via.chol(x, logarithm=TRUE))

setMethod("rcond", signature(x = "dppMatrix", norm = "character"),
	  function(x, norm, ...)
	  .Call(dppMatrix_rcond, x, norm),
	  valueClass = "numeric")

setMethod("rcond", signature(x = "dppMatrix", norm = "missing"),
          function(x, norm, ...)
          .Call(dppMatrix_rcond, x, "O"),
          valueClass = "numeric")

setMethod("solve", signature(a = "dppMatrix", b = "missing"),
          function(a, b, ...)
          .Call(dppMatrix_solve, a),
          valueClass = "dppMatrix")

setMethod("solve", signature(a = "dppMatrix", b = "dgeMatrix"),
          function(a, b, ...)
          .Call(dppMatrix_matrix_solve, a, b),
          valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dppMatrix", b = "matrix"),
          function(a, b, ...)
          .Call(dppMatrix_matrix_solve, a, b),
          valueClass = "dgeMatrix")

##setMethod("solve", signature(a = "dppMatrix", b = "numeric"),
##          function(a, b, ...)
##          .Call(dppMatrix_matrix_solve, a, as.matrix(b)),
##          valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dppMatrix", b = "integer"),
          function(a, b, ...) {
              storage.mode(b) <- "double"
              .Call(dppMatrix_matrix_solve, a, as.matrix(b))
          }, valueClass = "dgeMatrix")

setMethod("t", signature(x = "dppMatrix"),
          function(x) as(t(as(x, "dspMatrix")), "dppMatrix"),
          valueClass = "dppMatrix")

