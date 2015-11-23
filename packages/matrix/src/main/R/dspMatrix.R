### Coercion and Methods for Symmetric Packed Matrices

dsp2dsy <- function(from) .Call(dspMatrix_as_dsyMatrix, from)
dsp2C <- function(from) dsy2C(.Call(dspMatrix_as_dsyMatrix, from))
setAs("dspMatrix", "dsyMatrix", dsp2dsy)
## setAs("dspMatrix", "dsCMatrix", dsp2C)
setAs("dspMatrix", "CsparseMatrix", dsp2C)
setAs("dspMatrix", "sparseMatrix", dsp2C)

## dge <--> dsp   via  dsy
.dense2sp <- function(from) .dsy2dsp(.dense2sy(from))
setAs("dgeMatrix", "dspMatrix", .dense2sp)
setAs("matrix", "dspMatrix",
      function(from) .dense2sp(..2dge(from)))
## S3-matrix <--> dsp   via  dsy
setAs("dspMatrix", "matrix", function(from) .dsy2mat(dsp2dsy(from)))



setMethod("rcond", signature(x = "dspMatrix", norm = "character"),
          function(x, norm, ...)
          .Call(dspMatrix_rcond, x, norm),
          valueClass = "numeric")

setMethod("rcond", signature(x = "dspMatrix", norm = "missing"),
          function(x, norm, ...)
          .Call(dspMatrix_rcond, x, "O"),
          valueClass = "numeric")

setMethod("BunchKaufman", signature(x = "dspMatrix"),
	  function(x) .Call(dspMatrix_trf, x))

## Should define multiplication from the right

setMethod("solve", signature(a = "dspMatrix", b = "missing"),
	  function(a, b, ...) .Call(dspMatrix_solve, a),
	  valueClass = "dspMatrix")

setMethod("solve", signature(a = "dspMatrix", b = "matrix"),
	  function(a, b, ...) .Call(dspMatrix_matrix_solve, a, b),
	  valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dspMatrix", b = "ddenseMatrix"),
	  function(a, b, ...) .Call(dspMatrix_matrix_solve, a, b),
	  valueClass = "dgeMatrix")

##setMethod("solve", signature(a = "dspMatrix", b = "numeric"),
##	  function(a, b, ...)
##	  .Call(dspMatrix_matrix_solve, a, as.matrix(b)),
##	  valueClass = "dgeMatrix")

## No longer needed
## setMethod("solve", signature(a = "dspMatrix", b = "integer"),
## 	  function(a, b, ...) {
## 	      storage.mode(b) <- "double"
## 	      .Call(dspMatrix_matrix_solve, a, as.matrix(b))
## 	  }, valueClass = "dgeMatrix")

setMethod("norm", signature(x = "dspMatrix", type = "character"),
          function(x, type, ...) .Call(dspMatrix_norm, x, type),
          valueClass = "numeric")

setMethod("norm", signature(x = "dspMatrix", type = "missing"),
          function(x, type, ...) .Call(dspMatrix_norm, x, "O"),
          valueClass = "numeric")

## FIXME: speed up!
setMethod("t", signature(x = "dspMatrix"),
          function(x) as(t(as(x, "dsyMatrix")), "dspMatrix"),
          valueClass = "dspMatrix")

setMethod("diag", signature(x = "dspMatrix"),
	  function(x, nrow, ncol) .Call(dspMatrix_getDiag, x))
setMethod("diag<-", signature(x = "dspMatrix"),
	  function(x, value) .Call(dspMatrix_setDiag, x, value))

## The following allows  as(*, "dppMatrix").
## However it *requires* that dppMatrix_chol() gives an error
## for non-positive-semi-definite matrices -- which it does since 2005-10-03
if(FALSE)## FIXME: This gives an error for singular pos.SEMI-def. matrices:
setIs("dspMatrix", "dppMatrix",
      test = function(obj)
          "try-error" != class(try(.Call(dppMatrix_chol, obj), TRUE)),
      replace = function(obj, value) {
          ## copy all slots
          for(n in slotNames(obj)) slot(obj, n) <- slot(value, n)
          obj
      })

