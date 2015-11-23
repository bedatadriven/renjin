#### Collect methods for  colSums(), rowSums(), colMeans(), rowMeans() here.
####			  =======    -------    --------    --------

## Utilities:

## .as.dgC.Fun <- function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE) {
##     x <- as(x, "dgCMatrix")
##     callGeneric()
## }

## .as.dgT.Fun <- function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE) {
##     x <- as(x, "dgTMatrix")
##     callGeneric()
## }

.as.d.Fun <- function(x, na.rm = FALSE, dims = 1) {
    x <- as(x, "dMatrix")
    callGeneric()
}

.as.dge.Fun <- function(x, na.rm = FALSE, dims = 1) {
    x <- as(x, "dgeMatrix")
    callGeneric()
}

.as.gC.Fun <- function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE) {
    x <- as_gCsimpl(x)
    callGeneric()
}

.as.C.Fun <- function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE) {
    x <- as(x, "CsparseMatrix") ## or if necessary  as_Csparse(.)
    callGeneric()
}

### Dense Matrices: -------------------------------------------------

setMethod("colSums",  signature(x = "denseMatrix"), .as.d.Fun)
setMethod("colMeans", signature(x = "denseMatrix"), .as.d.Fun)
setMethod("rowSums",  signature(x = "denseMatrix"), .as.d.Fun)
setMethod("rowMeans", signature(x = "denseMatrix"), .as.d.Fun)

## FIXME: "works" but not optimally for triangular/symmetric(packed)/..
setMethod("colSums",  signature(x = "ddenseMatrix"), .as.dge.Fun)
setMethod("colMeans", signature(x = "ddenseMatrix"), .as.dge.Fun)
setMethod("rowSums",  signature(x = "ddenseMatrix"), .as.dge.Fun)
setMethod("rowMeans", signature(x = "ddenseMatrix"), .as.dge.Fun)

setMethod("colSums", signature(x = "dgeMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
          .Call(dgeMatrix_colsums, x, na.rm, TRUE, FALSE),
	  valueClass = "numeric")

setMethod("colMeans", signature(x = "dgeMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
          .Call(dgeMatrix_colsums, x, na.rm, TRUE, TRUE),
	  valueClass = "numeric")

setMethod("rowSums", signature(x = "dgeMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
          .Call(dgeMatrix_colsums, x, na.rm, FALSE, FALSE),
	  valueClass = "numeric")

setMethod("rowMeans", signature(x = "dgeMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
          .Call(dgeMatrix_colsums, x, na.rm, FALSE, TRUE),
	  valueClass = "numeric")

### Sparse Matrices: -------------------------------------------------

## Diagonal ones:
.diag.Sum <- function(x, na.rm = FALSE, dims = 1)
    if(x@diag == "U") rep(1, x@Dim[1]) else as.numeric(x@x)
.diag.Mean <- function(x, na.rm = FALSE, dims = 1) {
    n <- x@Dim[1L]
    if(x@diag == "U") rep(1/n, n) else as.numeric(x@x)/n
}

setMethod("colSums",  signature(x = "diagonalMatrix"), .diag.Sum)
setMethod("rowSums",  signature(x = "diagonalMatrix"), .diag.Sum)
setMethod("colMeans", signature(x = "diagonalMatrix"), .diag.Mean)
setMethod("rowMeans", signature(x = "diagonalMatrix"), .diag.Mean)

rm(.diag.Sum, .diag.Mean)

### Csparse --- the fast workhorse ones

### 1) those with .Call(.), {d, i, l, n} gCMatrix  x  {col|row}{Sums|Means} :

## the last two arguments to .gCMatrix_(col|col)(Sums|Means)  are 'trans' and 'means'
setMethod("colSums", signature(x = "dgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(dgCMatrix_colSums, x, na.rm, sparseResult, FALSE, FALSE))

setMethod("rowSums", signature(x = "dgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(dgCMatrix_colSums, x, na.rm, sparseResult, TRUE, FALSE))

setMethod("colMeans", signature(x = "dgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(dgCMatrix_colSums, x, na.rm, sparseResult, FALSE, TRUE))

setMethod("rowMeans", signature(x = "dgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(dgCMatrix_colSums, x, na.rm, sparseResult, TRUE, TRUE))

setMethod("colSums", signature(x = "igCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(igCMatrix_colSums, x, na.rm, sparseResult, FALSE, FALSE))

setMethod("rowSums", signature(x = "igCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(igCMatrix_colSums, x, na.rm, sparseResult, TRUE, FALSE))

setMethod("colMeans", signature(x = "igCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(igCMatrix_colSums, x, na.rm, sparseResult, FALSE, TRUE))

setMethod("rowMeans", signature(x = "igCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(igCMatrix_colSums, x, na.rm, sparseResult, TRUE, TRUE))

setMethod("colSums", signature(x = "lgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(lgCMatrix_colSums, x, na.rm, sparseResult, FALSE, FALSE))

setMethod("rowSums", signature(x = "lgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(lgCMatrix_colSums, x, na.rm, sparseResult, TRUE, FALSE))

setMethod("colMeans", signature(x = "lgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(lgCMatrix_colSums, x, na.rm, sparseResult, FALSE, TRUE))

setMethod("rowMeans", signature(x = "lgCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(lgCMatrix_colSums, x, na.rm, sparseResult, TRUE, TRUE))

setMethod("colSums", signature(x = "ngCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(ngCMatrix_colSums, x, na.rm, sparseResult, FALSE, FALSE))

setMethod("rowSums", signature(x = "ngCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(ngCMatrix_colSums, x, na.rm, sparseResult, TRUE, FALSE))

setMethod("colMeans", signature(x = "ngCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(ngCMatrix_colSums, x, na.rm, sparseResult, FALSE, TRUE))

setMethod("rowMeans", signature(x = "ngCMatrix"),
	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          .Call(ngCMatrix_colSums, x, na.rm, sparseResult, TRUE, TRUE))

### 2) the other Csparse ones are "just" coerced to a *gCMatrix :
setMethod("colSums",  signature(x = "CsparseMatrix"), .as.gC.Fun)
setMethod("colMeans", signature(x = "CsparseMatrix"), .as.gC.Fun)
setMethod("rowSums",  signature(x = "CsparseMatrix"), .as.gC.Fun)
setMethod("rowMeans", signature(x = "CsparseMatrix"), .as.gC.Fun)

##setMethod("rowSums", signature(x = "dgCMatrix"),
##	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
##	  sparsapply(x, 1, sum, sparseResult = sparseResult, na.rm = na.rm))

##setMethod("rowMeans", signature(x = "dgCMatrix"), sp.rowMeans)


## --- Tsparse ----

## .as.C.Fun -- since there's now  C code for dgCMatrix_colSums
setMethod("colSums",  signature(x = "TsparseMatrix"), .as.C.Fun)
setMethod("colMeans", signature(x = "TsparseMatrix"), .as.C.Fun)
setMethod("rowSums",  signature(x = "TsparseMatrix"), .as.C.Fun)
setMethod("rowMeans", signature(x = "TsparseMatrix"), .as.C.Fun)

## setMethod("colSums", signature(x = "TsparseMatrix"), .as.dgT.Fun,
## 	  valueClass = "numeric")
## setMethod("colMeans", signature(x = "TsparseMatrix"), .as.dgT.Fun,
## 	  valueClass = "numeric")
##
## setMethod("rowSums", signature(x = "TsparseMatrix"), .as.dgT.Fun,
## 	  valueClass = "numeric")
## setMethod("rowMeans", signature(x = "TsparseMatrix"), .as.dgT.Fun,
## 	  valueClass = "numeric")


## setMethod("colSums", signature(x = "dgTMatrix"),
## 	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
## 	  sparsapply(x, 2, sum, sparseResult = sparseResult, na.rm = na.rm))

## setMethod("rowSums", signature(x = "dgTMatrix"),
## 	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
## 	  sparsapply(x, 1, sum, sparseResult = sparseResult, na.rm = na.rm))

## setMethod("colMeans", signature(x = "dgTMatrix"), sp.colMeans)

## setMethod("rowMeans", signature(x = "dgTMatrix"), sp.rowMeans)



## --- Rsparse ----

## row <-> col of the "transposed, seen as C" :
setMethod("rowSums", signature(x = "RsparseMatrix"),
          function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          colSums(.tR.2.C(x),
                  na.rm=na.rm, dims=dims, sparseResult=sparseResult))

setMethod("rowMeans", signature(x = "RsparseMatrix"),
          function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          colMeans(.tR.2.C(x),
                   na.rm=na.rm, dims=dims, sparseResult=sparseResult))
setMethod("colSums", signature(x = "RsparseMatrix"),
          function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          rowSums(.tR.2.C(x),
                  na.rm=na.rm, dims=dims, sparseResult=sparseResult))
setMethod("colMeans", signature(x = "RsparseMatrix"),
          function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
          rowMeans(.tR.2.C(x),
                   na.rm=na.rm, dims=dims, sparseResult=sparseResult))

## ## These two are obviously more efficient than going through Tsparse:
## setMethod("colSums", signature(x = "dgRMatrix"),
## 	  function(x, na.rm = FALSE, dims = 1, sparseResult = FALSE)
## 	  sparsapply(x, 2, sum, sparseResult = sparseResult, na.rm = na.rm))

## setMethod("colMeans", signature(x = "dgRMatrix"), sp.colMeans)

## --- indMatrix [incl pMatrix ] ---

setMethod("colSums",  signature(x = "indMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
	  tabulate(x@perm, nbins=x@Dim[2]))
setMethod("colMeans",  signature(x = "indMatrix"),
	  function(x, na.rm = FALSE, dims = 1)
	  tabulate(x@perm, nbins=x@Dim[2])/x@Dim[1])
## for completeness:
setMethod("rowSums",  signature(x = "indMatrix"),
	  function(x, na.rm = FALSE, dims = 1) rep.int(1, x@Dim[1]))
setMethod("rowMeans",  signature(x = "indMatrix"),
	  function(x, na.rm = FALSE, dims = 1) rep.int(1/x@Dim[2], x@Dim[1]))



