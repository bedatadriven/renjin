
## ..2dge() -> ./Auxiliaries.R

setAs("matrix",  "dgeMatrix", ..2dge)
setAs("numLike", "dgeMatrix", ..2dge)

ge2mat <- function(from) array(from@x, dim = from@Dim, dimnames = from@Dimnames)
setAs("dgeMatrix", "matrix", ge2mat)


##  "[" settings are "up in"  Matrix.R & denseMatrix.R


setMethod("as.vector", signature(x = "dgeMatrix", mode = "missing"),
          function(x, mode) x@x)

setMethod("norm", signature(x = "dgeMatrix", type = "missing"),
	  function(x, type, ...) norm(x, type = "O", ...))
setMethod("norm", signature(x = "dgeMatrix", type = "character"),
	  function(x, type, ...) .Call(dgeMatrix_norm, x, type),
	  valueClass = "numeric")

setMethod("rcond", signature(x = "dgeMatrix", norm = "missing"),
	  function(x, norm, ...) rcond(x, norm = "O", ...))
setMethod("rcond", signature(x = "dgeMatrix", norm = "character"),
	  function(x, norm, ...)  {
	      if({d <- dim(x); d[1] == d[2]})
		  .Call(dgeMatrix_rcond, x, norm)
	      else rcond(qr.R(qr(if(d[1] < d[2]) t(x) else x)), norm=norm, ...)
	  },
	  valueClass = "numeric")

##> FIXME: R-devel (2.11.0) norm() is *wrong* for NAs, whereas this dgeMatrix
##> -----  one works,  even though both should call the identical LAPACK 'dlange' ?????
##> Hence, keep the Matrix version active for now:
##> if(getRversion() < "2.11.0" || R.version$`svn rev` < 51018)
##--- the same for "traditional"  'matrix':
setMethod("norm", signature(x = "matrix", type = "character"),
	  function(x, type, ...) .Call(dgeMatrix_norm, ..2dge(x), type),
	  valueClass = "numeric")

setMethod("t", signature(x = "dgeMatrix"), t_geMatrix)


.dge.diag <- function(x, nrow, ncol) .Call(dgeMatrix_getDiag, x)
setMethod("diag", signature(x = "dgeMatrix"), .dge.diag)
setMethod("diag<-", signature(x = "dgeMatrix"),
	  function(x, value) .Call(dgeMatrix_setDiag, x, value))

setMethod("chol", signature(x = "dgeMatrix"), cholMat)

setMethod("solve", signature(a = "dgeMatrix", b = "missing"),
	  function(a, b, ...) .Call(dgeMatrix_solve, a),
	  valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dgeMatrix", b = "ddenseMatrix"),
	  function(a, b, ...) .Call(dgeMatrix_matrix_solve, a, b),
	  valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dgeMatrix", b = "matrix"),
	  function(a, b, ...) .Call(dgeMatrix_matrix_solve, a, b),
          valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dgeMatrix", b = "sparseMatrix"),
	  function(a, b, ...) .Call(dgeMatrix_matrix_solve, a,
				    as(b, "denseMatrix")),
	  valueClass = "dgeMatrix")
## not needed - method for numeric defined for Matrix class
## setMethod("solve", signature(a = "dgeMatrix", b = "numeric"),
## 	  function(a, b, ...)
## 	  .Call(dgeMatrix_matrix_solve, a, as.matrix(as.double(b))))

setMethod("lu", signature(x = "dgeMatrix"),
	  function(x, warnSing = TRUE, ...) .Call(dgeMatrix_LU, x, warnSing),
	  valueClass = "denseLU")

setMethod("determinant", signature(x = "dgeMatrix", logarithm = "missing"),
	  function(x, logarithm, ...)
	  .Call(dgeMatrix_determinant, x, TRUE))

setMethod("determinant", signature(x = "dgeMatrix", logarithm = "logical"),
	  function(x, logarithm, ...)
	  .Call(dgeMatrix_determinant, x, logarithm))

##-> ./expm.R  for expm()

##-> ./colSums.R  for colSums,... rowMeans
