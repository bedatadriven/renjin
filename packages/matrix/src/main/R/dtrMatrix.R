#### Triangular Matrices -- Coercion and Methods


## or rather setIs() {since test can fail }?
## FIXME: get rid of this (coerce to "triangular..") ?!?
setAs("dgeMatrix", "dtrMatrix", function(from) asTri(from, "dtrMatrix"))

setAs("dtrMatrix", "dtpMatrix",
      dtr2dtp <- function(from) .Call(dtrMatrix_as_dtpMatrix, from))

setAs("dtrMatrix", "sparseMatrix", function(from)
    .dense2C(from, kind="tri", uplo=from@uplo))
setAs("dtrMatrix", "CsparseMatrix", function(from)
    .dense2C(from, kind="tri", uplo=from@uplo))


.dtr2mat <- function(from, keep.dimnames=TRUE)
    .Call(dtrMatrix_as_matrix, from, keep.dimnames)
## needed for t() method
setAs("dtrMatrix", "matrix",
      function(from) .Call(dtrMatrix_as_matrix, from, TRUE))

setAs("matrix", "dtrMatrix",
      function(from) as(..2dge(from), "dtrMatrix"))

setAs("Cholesky", "lMatrix",
      function(from) as(as(from, "dtrMatrix"), "lMatrix"))
setAs("BunchKaufman", "lMatrix",
      function(from) as(as(from, "dtrMatrix"), "lMatrix"))


## Group Methods:
## TODO: carefully check for the cases where the result remains triangular
## instead : inherit them from "dgeMatrix" via definition in ./dMatrix.R

## Note: Just *because* we have an explicit  dtr -> dge coercion,
##       show( <ddenseMatrix> ) is not okay, and we need our own:
setMethod("show", "dtrMatrix", function(object) prMatrix(object))

setMethod("determinant", signature(x = "dtrMatrix", logarithm = "missing"),
	  function(x, logarithm, ...) callGeneric(x, TRUE))

setMethod("determinant", signature(x = "dtrMatrix", logarithm = "logical"),
	  function(x, logarithm, ...) mkDet(diag(x), logarithm))

setMethod("diag", signature(x = "dtrMatrix"),
          function(x, nrow, ncol) .Call(dtrMatrix_getDiag, x),
          valueClass = "numeric")
setMethod("diag<-", signature(x = "dtrMatrix"),
	  function(x, value) {
	      .Call(dtrMatrix_setDiag,
		    if(x@diag == "U") .dense.diagU2N(x, "d", isPacked=FALSE) else x,
		    value)
	  })

setMethod("norm", signature(x = "dtrMatrix", type = "character"),
	  function(x, type, ...)
	  .Call(dtrMatrix_norm, x, type),
	  valueClass = "numeric")

setMethod("norm", signature(x = "dtrMatrix", type = "missing"),
	  function(x, type, ...)
	  .Call(dtrMatrix_norm, x, "O"),
	  valueClass = "numeric")

setMethod("rcond", signature(x = "dtrMatrix", norm = "character"),
	  function(x, norm, ...)
	  .Call(dtrMatrix_rcond, x, norm),
	  valueClass = "numeric")

setMethod("rcond", signature(x = "dtrMatrix", norm = "missing"),
	  function(x, norm, ...)
	  .Call(dtrMatrix_rcond, x, "O"),
	  valueClass = "numeric")

setMethod("chol2inv", signature(x = "dtrMatrix"),
	  function (x, ...) {
	      chk.s(..., which.call=-2)
	      if (x@diag != "N") x <- diagU2N(x)
	      .Call(dtrMatrix_chol2inv, x)
	  })

setMethod("solve", signature(a = "dtrMatrix", b="missing"),
	  function(a, b, ...) {
	      ## warn, as e.g. CHMfactor have 'system' as third argument
	      chk.s(..., which.call=-2)
	      .Call(dtrMatrix_solve, a)
	  }, valueClass = "dtrMatrix")

setMethod("solve", signature(a = "dtrMatrix", b="ddenseMatrix"),
	  function(a, b, ...) {
	      chk.s(..., which.call=-2)
	      .Call(dtrMatrix_matrix_solve, a, b)
	  }, valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dtrMatrix", b="dMatrix"),
	  function(a, b, ...) {
	      chk.s(..., which.call=-2)
	      .Call(dtrMatrix_matrix_solve, a, as(b,"denseMatrix"))
	  }, valueClass = "dgeMatrix")
setMethod("solve", signature(a = "dtrMatrix", b="Matrix"),
	  function(a, b, ...) {
	      chk.s(..., which.call=-2)
	      .Call(dtrMatrix_matrix_solve, a, as(as(b, "dMatrix"),
						  "denseMatrix"))
	  }, valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dtrMatrix", b="matrix"),
	  function(a, b, ...) {
	      chk.s(..., which.call=-2)
	      .Call(dtrMatrix_matrix_solve, a, b)
	  }, valueClass = "dgeMatrix")

setMethod("t", signature(x = "dtrMatrix"), t_trMatrix)
