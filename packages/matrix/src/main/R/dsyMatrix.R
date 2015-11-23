### Coercion and Methods for Dense Numeric Symmetric Matrices

##' @export (!) Note: ..?dense2sy() work for "dgeMatrix" *and* "matrix"
.dense2sy <- function(from, ...) {
    if(isSymmetric(from, ...)) # < with tolerance!
	.Call(dense_to_symmetric, from, "U", FALSE)
    else
	stop("not a symmetric matrix; consider forceSymmetric() or symmpart()")
}
## NB: The alternative, 'zero tolerance' { <=> isSymmetric(*, tol=0) }
##     breaks too much previous code -- though it would be much faster --

##' usable directly as function in setAs() <== no "..."
..dense2sy <- function(from) {
    if(isSymmetric(from)) # < with tolerance!
	.Call(dense_to_symmetric, from, "U", FALSE)
    else
	stop("not a symmetric matrix; consider forceSymmetric() or symmpart()")
}

setAs("dgeMatrix", "dsyMatrix", ..dense2sy)
setAs("matrix", "dsyMatrix",
      function(from) .dense2sy(..2dge(from)))

.dsy2mat <- function(from, keep.dimnames=TRUE)# faster
    .Call(dsyMatrix_as_matrix, from, keep.dimnames)
..dsy2mat <- function(from) .Call(dsyMatrix_as_matrix, from, TRUE)
setAs("dsyMatrix", "matrix", ..dsy2mat)

.dsy2dsp <- function(from) .Call(dsyMatrix_as_dspMatrix, from)
setAs("dsyMatrix", "dspMatrix", .dsy2dsp)

dsy2T <- function(from) { # 'dsT': only store upper *or* lower
    uplo <- from@uplo
    if(any0(dim(from))) {
	ij <- matrix(0L, 0,2) ; m <- from@x
    } else {
	## FIXME!	 working via "matrix" is *not* efficient:
	## the "other triangle" is filled, compared with 0, and then trashed:
	m <- .Call(dsyMatrix_as_matrix, from, FALSE) # no dimnames!
	ij <- which(m != 0, arr.ind = TRUE, useNames = FALSE)
	ij <- ij[if(uplo == "U") ij[,1] <= ij[,2] else ij[,1] >= ij[,2], , drop = FALSE]
    }
    new("dsTMatrix", i = ij[,1] - 1L, j = ij[,2] - 1L,
	x = as.vector(m[ij]), uplo = uplo,
	Dim = from@Dim, Dimnames = from@Dimnames)
}
setAs("dsyMatrix", "dsTMatrix", dsy2T)

setAs("dsyMatrix", "dsCMatrix",
      dsy2C <- function(from) .T2Cmat(dsy2T(from), isTri=FALSE))

## Note: Just *because* we have an explicit  dtr -> dge coercion,
##       show( <ddenseMatrix> ) is not okay, and we need our own:
setMethod("show", "dsyMatrix", function(object) prMatrix(object))


setMethod("rcond", signature(x = "dsyMatrix", norm = "character"),
          function(x, norm, ...)
          .Call(dsyMatrix_rcond, x, norm),
          valueClass = "numeric")

setMethod("rcond", signature(x = "dsyMatrix", norm = "missing"),
          function(x, norm, ...)
          .Call(dsyMatrix_rcond, x, "O"),
          valueClass = "numeric")

setMethod("solve", signature(a = "dsyMatrix", b = "missing"),
          function(a, b, ...) .Call(dsyMatrix_solve, a),
          valueClass = "dsyMatrix")

setMethod("solve", signature(a = "dsyMatrix", b = "matrix"),
          function(a, b, ...) .Call(dsyMatrix_matrix_solve, a, b),
          valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dsyMatrix", b = "ddenseMatrix"),
	  function(a, b, ...) .Call(dsyMatrix_matrix_solve, a, b))
setMethod("solve", signature(a = "dsyMatrix", b = "denseMatrix"), ## eg. for ddi* or ldi*
	  function(a, b, ...) .Call(dsyMatrix_matrix_solve, a, as(b,"dMatrix")))

setMethod("norm", signature(x = "dsyMatrix", type = "character"),
          function(x, type, ...) .Call(dsyMatrix_norm, x, type),
          valueClass = "numeric")

setMethod("norm", signature(x = "dsyMatrix", type = "missing"),
          function(x, type, ...) .Call(dsyMatrix_norm, x, "O"),
          valueClass = "numeric")

## *Should* create the opposite storage format:  "U" -> "L"  and vice-versa:
setMethod("t", signature(x = "dsyMatrix"), t_trMatrix,
          valueClass = "dsyMatrix")

setMethod("BunchKaufman", signature(x = "dsyMatrix"),
	  function(x) .Call(dsyMatrix_trf, x))

setAs("dsyMatrix", "dpoMatrix",
      function(from){
	  if(is.null(tryCatch(.Call(dpoMatrix_chol, from),
			      error = function(e) NULL)))
	      stop("not a positive definite matrix")
	  ## else
	  copyClass(from, "dpoMatrix",
		    sNames = c("x", "Dim", "Dimnames", "uplo", "factors"))
      })

setMethod("diag", signature(x = "dsyMatrix"),
	  function(x, nrow, ncol) .Call(dgeMatrix_getDiag, x))
setMethod("diag<-", signature(x = "dsyMatrix"),
	  function(x, value) .Call(dgeMatrix_setDiag, x, value))

## Now that we have "chol", we can define  "determinant" methods,
## exactly like in ./dsCMatrix.R
## DB - Probably figure out how to use the BunchKaufman decomposition instead
## {{FIXME: Shouldn't it be possible to have "determinant" work by
## default automatically for "Matrix"es  when there's a "chol" method available?
## ..> work with ss <- selectMethod("chol", signature("dgCMatrix"))
## -- not have to define showMethod("determinant", ...) for all classes

