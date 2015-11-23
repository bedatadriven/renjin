#### All  %*%, crossprod() and tcrossprod() methods of the Matrix package
#### ^^^  ----------------------------------------------------------
###  with EXCEPTIONS:	./diagMatrix.R 	./indMatrix.R ./pMatrix.R
###	  ~~~~~~~~~~	  ------------    -----------   ---------

### NOTA BENE:   vector %*% Matrix  _and_  Matrix %*% vector
### ---------   The k-vector is treated as  (1,k)-matrix *or* (k,1)-matrix
### on both sides when ever it "helps fit" the matrix dimensions:
##--- ./products.Rout
##    ~~~~~~~~~~~~~~~
## ========> in a M.v or v.M operation ,
##           you *must* look at dim(M) to see how to treat  v  !!!!!!!!!!!!!!!!

## For %*% (M = Matrix; v = vector (double, integer,.. or "sparsevector"):
## Drawback / bug: for (dense)vectors, the *names* are lost [sparsevectors have no names!]
.M.v <- function(x, y) { #
    dim(y) <- if(ncol(x) == (n <- length(y)))
        c(n, 1L) else c(1L, n) ## which works when m == 1, otherwise errors
    x %*% y
}

## For %*% :
.v.M <- function(x, y) {
    dim(x) <- if(nrow(y) == (n <- length(x))) c(1L, n) else c(n, 1L)
    x %*% y
}

## For tcrossprod() :
.v.Mt <- function(x, y=NULL, boolArith=NA, ...) {
    ##_ Not needed: y is never "missing", when used:
    ##_  if(is.null(y)) y <- x
    dim(x) <- if(ncol(y) == (n <- length(x))) c(1L, n) else c(n, 1L)
    tcrossprod(x, y, boolArith=boolArith, ...)
}
## tcrossprod(<Mat>, <sparseVector>)
.M.vt <- function(x, y=NULL, boolArith=NA, ...)
    tcrossprod(x,
               if(nrow(x) == 1L)
                   spV2M(y, nrow=1L, ncol=y@length, check=FALSE)
               else
                   spV2M(y, nrow=y@length, ncol=1L, check=FALSE),
               boolArith=boolArith, ...)

###-- I --- %*% ------------------------------------------------------

## General method for dense matrix multiplication in case specific methods
## have not been defined.
for ( c.x in paste0(c("d", "l", "n"), "denseMatrix")) {
    for(c.y in c("matrix", paste0(c("d", "l", "n"), "denseMatrix")))
	setMethod("%*%", signature(x = c.x, y = c.y),
		  function(x, y) .Call(geMatrix_matrix_mm, x, y, FALSE),
		  valueClass = "dgeMatrix")
    setMethod("%*%", signature(x = "matrix", y = c.x),
	      function(x, y) .Call(geMatrix_matrix_mm, y, x, TRUE),
	      valueClass = "dgeMatrix")
}

setMethod("%*%", signature(x = "dgeMatrix", y = "dgeMatrix"),
	  function(x, y) .Call(dgeMatrix_matrix_mm, x, y, FALSE),
	  valueClass = "dgeMatrix")

setMethod("%*%", signature(x = "dgeMatrix", y = "matrix"),
	  function(x, y) .Call(dgeMatrix_matrix_mm, x, y, FALSE),
	  valueClass = "dgeMatrix")

setMethod("%*%", signature(x = "matrix", y = "dgeMatrix"),
	  function(x, y) .Call(dgeMatrix_matrix_mm, y, x, TRUE),
	  valueClass = "dgeMatrix")

.dsy_m_mm <- function(x, y) .Call(dsyMatrix_matrix_mm, x, y, FALSE)
setMethod("%*%", signature(x = "dsyMatrix", y = "matrix"),  .dsy_m_mm)
setMethod("%*%", signature(x = "dsyMatrix", y = "ddenseMatrix"),  .dsy_m_mm)
## for disambiguity :
setMethod("%*%", signature(x = "dsyMatrix", y = "dsyMatrix"),  .dsy_m_mm)
## or even
## for(yCl in .directSubClasses(getClass("ddenseMatrix")))
##     setMethod("%*%", signature(x = "dsyMatrix", y = yCl), .dsy_m_mm)

setMethod("%*%", signature(x = "ddenseMatrix", y = "dsyMatrix"),
          function(x, y) .Call(dsyMatrix_matrix_mm, y, x, TRUE))
setMethod("%*%", signature(x = "matrix", y = "dsyMatrix"),
          function(x, y) .Call(dsyMatrix_matrix_mm, y, x, TRUE))

setMethod("%*%", signature(x = "dspMatrix", y = "ddenseMatrix"),
          function(x, y) .Call(dspMatrix_matrix_mm, x, y),
          valueClass = "dgeMatrix")
setMethod("%*%", signature(x = "dspMatrix", y = "matrix"),
          function(x, y) .Call(dspMatrix_matrix_mm, x, y),
          valueClass = "dgeMatrix")


## Not needed because of c("numeric", "Matrix") method
##setMethod("%*%", signature(x = "numeric", y = "CsparseMatrix"),
##	    function(x, y) .Call(Csparse_dense_crossprod, y, x, "B"),
##	    valueClass = "dgeMatrix")

## FIXME -- do the "same" for "dtpMatrix" {also, with [t]crossprod()}
## all just like these "%*%" :
setMethod("%*%", signature(x = "dtrMatrix", y = "dtrMatrix"),
	  function(x, y) .Call(dtrMatrix_dtrMatrix_mm, x, y, FALSE, FALSE))

setMethod("%*%", signature(x = "dtrMatrix", y = "ddenseMatrix"),
	  function(x, y) .Call(dtrMatrix_matrix_mm, x, y, FALSE, FALSE),
	  valueClass = "dgeMatrix")

setMethod("%*%", signature(x = "dtrMatrix", y = "matrix"),
	  function(x, y) .Call(dtrMatrix_matrix_mm, x, y, FALSE, FALSE),
	  valueClass = "dgeMatrix")

setMethod("%*%", signature(x = "ddenseMatrix", y = "dtrMatrix"),
	  function(x, y) .Call(dtrMatrix_matrix_mm, y, x, TRUE, FALSE),
	  valueClass = "dgeMatrix")

setMethod("%*%", signature(x = "matrix", y = "dtrMatrix"),
	  function(x, y) .Call(dtrMatrix_matrix_mm, y, x, TRUE, FALSE),
	  valueClass = "dgeMatrix")



setMethod("%*%", signature(x = "dtpMatrix", y = "ddenseMatrix"),
	  function(x, y) .Call(dtpMatrix_matrix_mm, x, y, FALSE, FALSE))
setMethod("%*%", signature(x = "dgeMatrix", y = "dtpMatrix"),
	  function(x, y) .Call(dgeMatrix_dtpMatrix_mm, x, y))

## dtpMatrix <-> matrix : will be used by the "numeric" one
setMethod("%*%", signature(x = "dtpMatrix", y = "matrix"),
          function(x, y) .Call(dtpMatrix_matrix_mm, x, y, FALSE, FALSE))
setMethod("%*%", signature(x = "matrix", y = "dtpMatrix"),
          function(x, y) ..2dge(x) %*% y)

## dtpMatrix <-> numeric : the auxiliary functions are R version specific!
##setMethod("%*%", signature(x = "dtpMatrix", y = "numeric"), .M.v)
##setMethod("%*%", signature(x = "numeric", y = "dtpMatrix"), .v.M)


## For multiplication operations, sparseMatrix overrides other method
## selections.	Coerce a ddensematrix argument to a lsparseMatrix.
setMethod("%*%", signature(x = "lsparseMatrix", y = "ldenseMatrix"),
	  function(x, y) x %*% as(y, "sparseMatrix"))

setMethod("%*%", signature(x = "ldenseMatrix", y = "lsparseMatrix"),
	  function(x, y) as(x, "sparseMatrix") %*% y)

## and coerce lsparse* to lgC*
setMethod("%*%", signature(x = "lsparseMatrix", y = "lsparseMatrix"),
	  function(x, y) as(x, "lgCMatrix") %*% as(y, "lgCMatrix"))


for(c.x in c("lMatrix", "nMatrix")) {
    setMethod("%*%", signature(x = c.x, y = "dMatrix"),
	      function(x, y) as(x, "dMatrix") %*% y)
    setMethod("%*%", signature(x = "dMatrix", y = c.x),
	      function(x, y) x %*% as(y, "dMatrix"))
    for(c.y in c("lMatrix", "nMatrix"))
    setMethod("%*%", signature(x = c.x, y = c.y),
	      function(x, y) as(x, "dMatrix") %*% as(y, "dMatrix"))
}; rm(c.x, c.y)

setMethod("%*%", signature(x = "CsparseMatrix", y = "CsparseMatrix"),
	  function(x, y) .Call(Csparse_Csparse_prod, x, y, boolArith=NA))

setMethod("%*%", signature(x = "CsparseMatrix", y = "ddenseMatrix"),
	  function(x, y) .Call(Csparse_dense_prod, x, y, " "))
setMethod("%*%", signature(x = "CsparseMatrix", y = "matrix"),
	  function(x, y) .Call(Csparse_dense_prod, x, y, " ")) # was  x %*% Matrix(y)
setMethod("%*%", signature(x = "CsparseMatrix", y = "numLike"),
	  function(x, y) .Call(Csparse_dense_prod, x, y, " "))

setMethod("%*%", signature(x = "sparseMatrix", y = "matrix"),
	  function(x, y) .Call(Csparse_dense_prod, as(x,"CsparseMatrix"), y, " "))

## Not yet.  Don't have methods for y = "CsparseMatrix" and general x
#setMethod("%*%", signature(x = "ANY", y = "TsparseMatrix"),
#	   function(x, y) callGeneric(x, as(y, "CsparseMatrix")))

setMethod("%*%", signature(x = "TsparseMatrix", y = "ANY"),
	  function(x, y) .T.2.C(x) %*% y)
setMethod("%*%", signature(x = "ANY", y = "TsparseMatrix"),
	  function(x, y) x %*% .T.2.C(y))
setMethod("%*%", signature(x = "TsparseMatrix", y = "Matrix"),
	  function(x, y) .T.2.C(x) %*% y)
setMethod("%*%", signature(x = "Matrix", y = "TsparseMatrix"),
	  function(x, y) x %*% .T.2.C(y))
setMethod("%*%", signature(x = "TsparseMatrix", y = "TsparseMatrix"),
	  function(x, y) .T.2.C(x) %*% .T.2.C(y))



##-------- Work via  as(*, lgC) : ------------

## For multiplication operations, sparseMatrix overrides other method
## selections.	Coerce a ddensematrix argument to a nsparseMatrix.
setMethod("%*%", signature(x = "nsparseMatrix", y = "ndenseMatrix"),
	  function(x, y) x %*% as(y, "nsparseMatrix"))

setMethod("%*%", signature(x = "ndenseMatrix", y = "nsparseMatrix"),
	  function(x, y) as(x, "nsparseMatrix") %*% y)
## and coerce nsparse* to lgC*
setMethod("%*%", signature(x = "nsparseMatrix", y = "nsparseMatrix"),
	  function(x, y) as(x, "ngCMatrix") %*% as(y, "ngCMatrix"))


## x %*% y =  t(crossprod(y, t(x)))  unless when x is vector
setMethod("%*%", signature(x = "ddenseMatrix", y = "CsparseMatrix"),
	  function(x, y) .Call(Csparse_dense_crossprod, y, x, "B"),
	  valueClass = "dgeMatrix")
setMethod("%*%", signature(x = "matrix", y = "CsparseMatrix"),
	  function(x, y) .Call(Csparse_dense_crossprod, y, x, "B"),
	  valueClass = "dgeMatrix")
setMethod("%*%", signature(x = "matrix", y = "sparseMatrix"),
	  function(x, y) .Call(Csparse_dense_crossprod, as(y, "CsparseMatrix"), x, "B"),
	  valueClass = "dgeMatrix")
setMethod("%*%", signature(x = "numLike", y = "CsparseMatrix"),
	  function(x, y) .Call(Csparse_dense_crossprod, y, x, "c"),
	  valueClass = "dgeMatrix")


## "Matrix"
## Methods for operations where one argument is numeric
setMethod("%*%", signature(x = "Matrix", y = "numLike"), .M.v)
setMethod("%*%", signature(x = "numLike", y = "Matrix"), .v.M)

setMethod("%*%", signature(x = "Matrix", y = "matrix"),
	  function(x, y) x %*% Matrix(y))
setMethod("%*%", signature(x = "matrix", y = "Matrix"),
	  function(x, y) Matrix(x) %*% y)

## bail-out methods in order to get better error messages
.local.bail.out <- function (x, y)
    stop(gettextf('not-yet-implemented method for <%s> %%*%% <%s>',
		  class(x), class(y)), domain=NA)
setMethod("%*%", signature(x = "ANY", y = "Matrix"), .local.bail.out)
setMethod("%*%", signature(x = "Matrix", y = "ANY"), .local.bail.out)


### sparseVector
sp.x.sp <- function(x, y) Matrix(sum(x * y), 1L, 1L, sparse=FALSE)
    ## inner product -- no sense to return sparse!
sp.X.sp <- function(x, y) {
    if((n <- length(x)) == length(y)) sp.x.sp(x,y)
    else if(n == 1L) spV2M(x, nrow = 1L, ncol = 1L, check = FALSE) %*% y
    else stop("non-conformable arguments")
}
v.X.sp <- function(x, y) {
    if((n <- length(x)) == length(y)) sp.x.sp(x,y)
    else if(n == 1L) matrix(x, nrow = 1L, ncol = 1L) %*% y
    else stop("non-conformable arguments")
}

setMethod("%*%", signature(x = "mMatrix", y = "sparseVector"), .M.v)
setMethod("%*%", signature(x = "sparseVector", y = "mMatrix"), .v.M)
setMethod("%*%", signature(x = "sparseVector", y = "sparseVector"), sp.X.sp)
setMethod("%*%", signature(x = "sparseVector", y = "numLike"),      sp.X.sp)
setMethod("%*%", signature(x = "numLike",      y = "sparseVector"), v.X.sp)
## setMethod("%*%", signature(x = "sparseMatrix", y = "sparseVector"),
##           function(x, y) x %*% .sparseV2Mat(y))

###--- II --- crossprod -----------------------------------------------------

setMethod("crossprod", signature(x = "dgeMatrix", y = "missing"),
	  function(x, y = NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_crossprod, x, FALSE)
	  })

## crossprod (x,y)
setMethod("crossprod", signature(x = "dgeMatrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_dgeMatrix_crossprod, x, y, FALSE)
	  })

setMethod("crossprod", signature(x = "dgeMatrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
                  .Call(dgeMatrix_matrix_crossprod, x, y, FALSE)
	  })

setMethod("crossprod", signature(x = "dgeMatrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith))
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseVector"), boolArith=TRUE)
	      else
                  .Call(dgeMatrix_matrix_crossprod, x, y, FALSE)
	  })

setMethod("crossprod", signature(x = "matrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(..2dge(x), y, boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "numLike", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(as.matrix(as.double(x)), y, boolArith=boolArith, ...))

for(c.x in paste0(c("d", "l", "n"), "denseMatrix")) {
    setMethod("crossprod", signature(x = c.x, y = "missing"),
	      function(x, y = NULL, boolArith=NA, ...)
		  if(isTRUE(boolArith)) ## FIXME: very inefficient
		      crossprod(as(x,"sparseMatrix"), boolArith=TRUE)
		  else
		      .Call(geMatrix_crossprod, x, FALSE))

    for(c.y in c("matrix", paste0(c("d", "l", "n"), "denseMatrix"))) {
	setMethod("crossprod", signature(x = c.x, y = c.y),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(geMatrix_geMatrix_crossprod, x, y, FALSE))
    }
}
## setMethod("crossprod", signature(x = "dtrMatrix", y = "missing"),
## 	  function(x, y = NULL, boolArith=NA, ...)
## 	      crossprod(..2dge(x), boolArith=boolArith, ...))

## "dtrMatrix" - remaining (uni)triangular if possible
setMethod("crossprod", signature(x = "dtrMatrix", y = "dtrMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_dtrMatrix_mm, x, y, FALSE, TRUE))

setMethod("crossprod", signature(x = "dtrMatrix", y = "ddenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_matrix_mm, x, y, FALSE, TRUE))


setMethod("crossprod", signature(x = "dtrMatrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_matrix_mm, x, y, FALSE, TRUE))

## Not quite optimal, have unnecessary  t(x)  below: _FIXME_
setMethod("crossprod", signature(x = "matrix", y = "dtrMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_matrix_mm, y, t(x), TRUE, FALSE))


## "dtpMatrix"
if(FALSE) ## not yet in C
setMethod("crossprod", signature(x = "dtpMatrix", y = "dtpMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtpMatrix_dtpMatrix_mm, x, y, FALSE, TRUE))

setMethod("crossprod", signature(x = "dtpMatrix", y = "ddenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtpMatrix_matrix_mm, x, y, FALSE, TRUE))

setMethod("crossprod", signature(x = "dtpMatrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtpMatrix_matrix_mm, x, y, FALSE, TRUE))



## "crossprod" methods too ...
## setMethod("crossprod", signature(x = "dgTMatrix", y = "missing"),
##	     function(x, y=NULL, boolArith=NA, ...)
##	     .Call(csc_crossprod, as(x, "dgCMatrix")))

## setMethod("crossprod", signature(x = "dgTMatrix", y = "matrix"),
##	     function(x, y)
##	     .Call(csc_matrix_crossprod, as(x, "dgCMatrix"), y))

##setMethod("crossprod", signature(x = "dgTMatrix", y = "numeric"),
##	    function(x, y)
##	    .Call(csc_matrix_crossprod, as(x, "dgCMatrix"), as.matrix(y)))

## setMethod("tcrossprod", signature(x = "dgTMatrix", y = "missing"),
##	     function(x, y=NULL, boolArith=NA, ...)
##	     .Call(csc_tcrossprod, as(x, "dgCMatrix")))

setMethod("crossprod", signature(x = "CsparseMatrix", y = "missing"),
	  function(x, y = NULL, boolArith=NA, ...)
	      .Call(Csparse_crossprod, x, trans = FALSE, triplet = FALSE, boolArith=boolArith))

setMethod("crossprod", signature(x = "CsparseMatrix", y = "CsparseMatrix"),
	  function(x, y = NULL, boolArith = NA, ...)
	  .Call(Csparse_Csparse_crossprod, x, y, trans = FALSE, boolArith=boolArith))

## FIXME: Generalize the class of y. (?? still ??)
setMethod("crossprod", signature(x = "CsparseMatrix", y = "ddenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(x, as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, x, y, " "))
setMethod("crossprod", signature(x = "CsparseMatrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(x, as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, x, y, " "))
setMethod("crossprod", signature(x = "CsparseMatrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(x, as(y,"sparseVector"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, x, y, " "))


setMethod("crossprod", signature(x = "TsparseMatrix", y = "missing"),
	  function(x, y = NULL, boolArith = NA, ...)
	      .Call(Csparse_crossprod, x, trans = FALSE, triplet = TRUE, boolArith=boolArith))

setMethod("crossprod", signature(x = "TsparseMatrix", y = "ANY"),
	  function(x, y = NULL, boolArith = NA, ...)
              crossprod(.T.2.C(x), y, boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "ANY", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
              crossprod(x, .T.2.C(y), boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "TsparseMatrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
              crossprod(.T.2.C(x), y, boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "Matrix", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
              crossprod(x, .T.2.C(y), boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "TsparseMatrix", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
              crossprod(.T.2.C(x), .T.2.C(y), boolArith=boolArith, ...))


setMethod("crossprod", signature(x = "dsparseMatrix", y = "ddenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "CsparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, as(x, "CsparseMatrix"), y, " "))

setMethod("crossprod", signature(x = "ddenseMatrix", y = "dgCMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "sparseMatrix"), y, boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, y, x, "c"))
setMethod("crossprod", signature(x = "ddenseMatrix", y = "dsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "sparseMatrix"), as(y, "CsparseMatrix"), boolArith=TRUE)
	      else
                  .Call(Csparse_dense_crossprod, as(y, "CsparseMatrix"), x, "c"))
setMethod("crossprod", signature(x = "dgCMatrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(x, as(y, "CsparseMatrix"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, x, y, " "))
setMethod("crossprod", signature(x = "dsparseMatrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "CsparseMatrix"), as(y, "CsparseMatrix"), boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, as(x, "CsparseMatrix"), y, " "))

## NB: there's already
##     ("CsparseMatrix", "missing") and ("TsparseMatrix", "missing") methods

## infinite recursion:
## setMethod("crossprod", signature(x = "dgeMatrix", y = "dsparseMatrix"),
##	  function(x, y) crossprod(x, as(y, "dgCMatrix")))


setMethod("crossprod", signature(x = "lsparseMatrix", y = "ldenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(x, as(y, "sparseMatrix"), boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "ldenseMatrix", y = "lsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(as(x, "sparseMatrix"), y, boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "lsparseMatrix", y = "lsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(as(x, "lgCMatrix"), as(y, "lgCMatrix"), boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "nsparseMatrix", y = "ndenseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(x, as(y, "sparseMatrix"), boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "ndenseMatrix", y = "nsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(as(x, "sparseMatrix"), y, boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "nsparseMatrix", y = "nsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(as(x, "ngCMatrix"), as(y, "ngCMatrix"), boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "ddenseMatrix", y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "CsparseMatrix"), y, boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, y, x, "c"))
setMethod("crossprod", signature(x = "matrix",	     y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "CsparseMatrix"), y, boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, y, x, "c"))
setMethod("crossprod", signature(x = "numLike",	     y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith))
		  crossprod(as(x, "sparseVector"), y, boolArith=TRUE)
	      else
		  .Call(Csparse_dense_crossprod, y, x, "c"))


## "Matrix" : cbind(), rbind() do  names -> dimnames
setMethod("crossprod", signature(x = "Matrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...) crossprod(x, cbind(y, deparse.level=0), boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "numLike", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...) crossprod(cbind(x, deparse.level=0), y, boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "Matrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...) crossprod(x, Matrix(y), boolArith=boolArith, ...))
setMethod("crossprod", signature(x = "matrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...) crossprod(Matrix(x), y, boolArith=boolArith, ...))

## sparseVector
setMethod("crossprod", signature(x = "mMatrix", y = "sparseVector"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(x,
			if(nrow(x) == 1L)
			    spV2M(y, nrow=1L, ncol=y@length, check=FALSE)
			else
			    spV2M(y, nrow=y@length, ncol=1L, check=FALSE),
			boolArith=boolArith, ...))

setMethod("crossprod", signature(x = "sparseVector", y = "mMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      crossprod(spV2M(x, nrow = length(x), ncol = 1L, check = FALSE), y,
			boolArith=boolArith, ...))

sp.t.sp <- function(x, y=NULL, boolArith=NA, ...)
    Matrix(if(isTRUE(boolArith)) any(x & y) else sum(x * y),
	    1L, 1L, sparse=FALSE)
## inner product -- no sense to return sparse!
sp.T.sp <- function(x, y=NULL, boolArith=NA, ...) {
    if((n <- length(x)) == length(y)) sp.t.sp(x,y, boolArith=boolArith, ...)
    else if(n == 1L)
	(if(isTRUE(boolArith)) `%&%` else `%*%`)(
	    spV2M(x, nrow = 1L, ncol = 1L, check = FALSE), y)
    else stop("non-conformable arguments")
}
v.T.sp <- function(x, y=NULL, boolArith=NA, ...) {
    if((n <- length(x)) == length(y)) sp.t.sp(x,y, boolArith=boolArith, ...)
    else if(n == 1L)
	(if(isTRUE(boolArith)) `%&%` else `%*%`)(matrix(x, nrow = 1L, ncol = 1L), y)
    else stop("non-conformable arguments")
}

setMethod("crossprod", signature(x = "sparseVector", y = "sparseVector"), sp.T.sp)
setMethod("crossprod", signature(x = "sparseVector", y = "numLike"),      sp.T.sp)
setMethod("crossprod", signature(x = "numLike",      y = "sparseVector"),  v.T.sp)
setMethod("crossprod", signature(x = "sparseVector", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...) sp.t.sp(x,x, boolArith=boolArith, ...))

## Fallbacks -- symmetric LHS --> saving a t(.):
##  {FIXME: want the method to be `%*%` -- but primitives are not allowed as methods}
setMethod("crossprod", signature(x = "symmetricMatrix", y = "missing"),
	  function(x,y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) x %&% x else x %*% x)
setMethod("crossprod", signature(x = "symmetricMatrix", y = "Matrix"),
	  function(x,y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) x %&% y else x %*% y)
setMethod("crossprod", signature(x = "symmetricMatrix", y = "ANY"),
	  function(x,y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) x %&% y else x %*% y)
##
## cheap fallbacks
setMethod("crossprod", signature(x = "Matrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(sprintf(
	  "potentially suboptimal crossprod(\"%s\",\"%s\") as t(.) %s y",
		  class(x), class(y), "%*%"))
	      if(isTRUE(boolArith)) t(x) %&% y else t(x) %*% y })
setMethod("crossprod", signature(x = "Matrix", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(paste0(
	  "potentially suboptimal crossprod(<",class(x),">) as t(.) %*% . "))
	      if(isTRUE(boolArith)) t(x) %&% x else t(x) %*% x })
setMethod("crossprod", signature(x = "Matrix", y = "ANY"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(sprintf(
	  "potentially suboptimal crossprod(\"%s\", <%s>[=<ANY>]) as t(.) %s y",
		  class(x), class(y), "%*%"))
	      if(isTRUE(boolArith)) t(x) %&% y else t(x) %*% y })
setMethod("crossprod", signature(x = "ANY", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) t(x) %&% y else t(x) %*% y)

###--- III --- tcrossprod ---------------------------------------------------

setMethod("tcrossprod", signature(x = "dgeMatrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_dgeMatrix_crossprod, x, y, TRUE))
setMethod("tcrossprod", signature(x = "dgeMatrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_matrix_crossprod, x, y, TRUE))

setMethod("tcrossprod", signature(x = "dgeMatrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseVector"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_matrix_crossprod, x, y, TRUE))

setMethod("tcrossprod", signature(x = "matrix", y = "dgeMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(..2dge(x), y, boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "numLike", y = "dgeMatrix"), .v.Mt)

setMethod("tcrossprod", signature(x = "dgeMatrix", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_crossprod, x, TRUE))

for(c.x in paste0(c("d", "l", "n"), "denseMatrix")) {
    setMethod("tcrossprod", signature(x = c.x, y = "missing"),
	      function(x, y=NULL, boolArith=NA, ...)
		  if(isTRUE(boolArith)) ## FIXME: very inefficient
		      tcrossprod(as(x, "sparseMatrix"), boolArith=TRUE)
		  else
		      .Call(geMatrix_crossprod, x, TRUE))

    for(c.y in c("matrix", paste0(c("d", "l", "n"), "denseMatrix"))) {
	setMethod("tcrossprod", signature(x = c.x, y = c.y),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x,"sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(geMatrix_geMatrix_crossprod, x, y, TRUE))
    }
}

if(FALSE) { ## this would mask 'base::tcrossprod'
setMethod("tcrossprod", signature(x = "matrix", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dgeMatrix_crossprod, ..2dge(x), TRUE))
setMethod("tcrossprod", signature(x = "numLike", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(as.matrix(as.double(x)), boolArith=boolArith, ...))
}# FALSE

setMethod("tcrossprod", signature(x = "ddenseMatrix", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(as(x, "dgeMatrix"), boolArith=boolArith, ...))


setMethod("tcrossprod", signature(x = "dtrMatrix", y = "dtrMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_dtrMatrix_mm, y, x, TRUE, TRUE))


## Must	 have 1st arg. = "dtrMatrix" in	 dtrMatrix_matrix_mm ():
## would need another way, to define  tcrossprod()  --- TODO? ---
##
## setMethod("tcrossprod", signature(x = "dtrMatrix", y = "ddenseMatrix"),
##	  function(x, y=NULL, boolArith=NA, ...) .Call(dtrMatrix_matrix_mm, y, x, TRUE, TRUE))

###__ FIXME __ currently goes via geMatrix and loses triangularity !!
## setMethod("tcrossprod", signature(x = "dtrMatrix", y = "matrix"),
##	  function(x, y=NULL, boolArith=NA, ...) .Call(dtrMatrix_matrix_mm, y, x, TRUE, TRUE))

setMethod("tcrossprod", signature(x = "ddenseMatrix", y = "dtrMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_matrix_mm, y, x, TRUE, TRUE))

setMethod("tcrossprod", signature(x = "matrix", y = "dtrMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtrMatrix_matrix_mm, y, x, TRUE, TRUE))

if(FALSE) { ## TODO in C
setMethod("tcrossprod", signature(x = "ddenseMatrix", y = "dtpMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtpMatrix_matrix_mm, y, x, TRUE, TRUE))

setMethod("tcrossprod", signature(x = "matrix", y = "dtpMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x, "sparseMatrix"), as(y,"sparseMatrix"), boolArith=TRUE)
	      else
		  .Call(dtpMatrix_matrix_mm, y, x, TRUE, TRUE))
}# FALSE



setMethod("tcrossprod", signature(x = "CsparseMatrix", y = "CsparseMatrix"),
	  function(x, y = NULL, boolArith = NA, ...)
	  .Call(Csparse_Csparse_crossprod, x, y, trans = TRUE, boolArith=boolArith))

setMethod("tcrossprod", signature(x = "CsparseMatrix", y = "missing"),
	  function(x, y = NULL, boolArith = NA, ...)
	      .Call(Csparse_crossprod, x, trans = TRUE, triplet = FALSE, boolArith=boolArith))

for(dmat in c("ddenseMatrix", "matrix")) {
setMethod("tcrossprod", signature(x = "CsparseMatrix", y = dmat),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(x, as(y,"CsparseMatrix"), boolArith=TRUE)
	      else
                  .Call(Csparse_dense_prod, x, y, "2"))
setMethod("tcrossprod", signature(x = dmat, y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
              if(isTRUE(boolArith)) ## FIXME: very inefficient
                  tcrossprod(as(x,"CsparseMatrix"), y, boolArith=TRUE)
              else
		  .Call(Csparse_dense_prod, y, x, "B"))

}
setMethod("tcrossprod", signature(x = "CsparseMatrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...)
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(x, as(y,"sparseVector"), boolArith=TRUE)
	      else
                  .Call(Csparse_dense_prod, x, y, "2"))
setMethod("tcrossprod", signature(x = "numLike",      y = "CsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...) ## ~== .v.Mt :
	      if(isTRUE(boolArith)) ## FIXME: very inefficient
		  tcrossprod(as(x,"sparseVector"), y, boolArith=TRUE)
	      else
                  ## x or t(x) depending on dimension of y [checked inside C]:
                  .Call(Csparse_dense_prod, y, x, "B"))

### -- xy' = (yx')' --------------------
tcr.dd.sC <- function(x, y=NULL, boolArith=NA, ...) {
    if(isTRUE(boolArith)) ## FIXME: very inefficient
	tcrossprod(as(x,"CsparseMatrix"), y, boolArith=TRUE)
    else
	.Call(Csparse_dense_prod, y, x, "c")
}
for(.sCMatrix in paste0(c("d", "l", "n"), "sCMatrix")) { ## speedup for *symmetric* RHS
    setMethod("tcrossprod", signature(x = "ddenseMatrix", y = .sCMatrix), tcr.dd.sC)
    setMethod("tcrossprod", signature(x = "matrix", y = .sCMatrix), 	  tcr.dd.sC)
}
rm(dmat, .sCMatrix)

setMethod("tcrossprod", signature(x = "TsparseMatrix", y = "missing"),
	  function(x, y = NULL, boolArith = NA, ...)
	      .Call(Csparse_crossprod, x, trans = TRUE, triplet = TRUE,
		    boolArith=boolArith))

setMethod("tcrossprod", signature(x = "ANY", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(x, .T.2.C(y), boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "TsparseMatrix", y = "ANY"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(.T.2.C(x), y, boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "Matrix", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(x, .T.2.C(y), boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "TsparseMatrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(.T.2.C(x), y, boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "TsparseMatrix", y = "TsparseMatrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(.T.2.C(x), .T.2.C(y), boolArith=boolArith, ...))


## "Matrix"
setMethod("tcrossprod", signature(x = "Matrix", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...)
	      (if(isTRUE(boolArith)) `%&%` else `%*%`)(x,
                                                       rbind(y, deparse.level=0)))
setMethod("tcrossprod", signature(x = "numLike", y = "Matrix"), .v.Mt)
setMethod("tcrossprod", signature(x = "Matrix", y = "matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(x, Matrix(y), boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "matrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...)
	      tcrossprod(Matrix(x), y, boolArith=boolArith, ...))

## sparseVector
## NB: the two "sparseMatrix" are "unneeded", only used to avoid ambiguity warning
setMethod("tcrossprod", signature(x = "sparseMatrix", y = "sparseVector"), .M.vt)
setMethod("tcrossprod", signature(x = "mMatrix",      y = "sparseVector"), .M.vt)
setMethod("tcrossprod", signature(x = "sparseVector", y = "sparseMatrix"), .v.Mt)
setMethod("tcrossprod", signature(x = "sparseVector", y = "mMatrix"),	   .v.Mt)
setMethod("tcrossprod", signature(x = "sparseVector", y = "sparseVector"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith))
		  .sparseV2Mat(x) %&%
		      spV2M(y, nrow=1L, ncol=length(y), check=FALSE)
	      else {
		  if(!is.na(boolArith))
		      warning(gettextf("'boolArith = %d' not yet implemented",
				       boolArith), domain=NA)
		  .sparseV2Mat(x) %*%
		      spV2M(y, nrow=1L, ncol=length(y), check=FALSE)
	      }
	  })
setMethod("tcrossprod", signature(x = "sparseVector", y = "missing"),
	  ## could be speeded: spV2M(x, *) called twice with different ncol/nrow
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith))
		  .sparseV2Mat(x) %&%
		      spV2M(x, nrow=1L, ncol=length(x), check=FALSE)
	      else {
		  if(!is.na(boolArith))
		      warning(gettextf("'boolArith = %d' not yet implemented",
				       boolArith), domain=NA)
		  .sparseV2Mat(x) %*%
		      spV2M(x, nrow=1L, ncol=length(x), check=FALSE)
	      }
          })

setMethod("tcrossprod", signature(x = "numLike",      y = "sparseVector"),
	  function(x, y=NULL, boolArith=NA, ...)
              tcrossprod(x, .sparseV2Mat(y), boolArith=boolArith, ...))
setMethod("tcrossprod", signature(x = "sparseVector", y = "numLike"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      if(isTRUE(boolArith))
		  .sparseV2Mat(x) %&% t(x)
	      else {
		  if(!is.na(boolArith))
		      warning(gettextf("'boolArith = %d' not yet implemented",
				       boolArith), domain=NA)
		  .sparseV2Mat(x) %*% t(x)
	      }
	  })


## Fallbacks -- symmetric RHS --> saving a t(.):
##  {FIXME: want the method to be `%*%` -- but primitives are not allowed as methods}
setMethod("tcrossprod", signature(x = "Matrix", y = "symmetricMatrix"),
          function(x, y=NULL, boolArith=NA, ...)
              if(isTRUE(boolArith)) x %&% y else x %*% y)
setMethod("tcrossprod", signature(x = "ANY",    y = "symmetricMatrix"),
          function(x, y=NULL, boolArith=NA, ...)
              if(isTRUE(boolArith)) x %&% y else x %*% y)
##
## cheap fallbacks
setMethod("tcrossprod", signature(x = "Matrix", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(sprintf(
	  "potentially suboptimal tcrossprod(\"%s\",\"%s\") as  x %s t(y)",
		  class(x), class(y), "%*%"))
              if(isTRUE(boolArith)) x %&% t(y) else
	      x %*% t(y) })
setMethod("tcrossprod", signature(x = "Matrix", y = "missing"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(paste0(
	  "potentially suboptimal tcrossprod(<",class(x), ">) as  . %*% t(.)"))
              if(isTRUE(boolArith)) x %&% t(x) else
	      x %*% t(x) })
setMethod("tcrossprod", signature(x = "Matrix", y = "ANY"),
	  function(x, y=NULL, boolArith=NA, ...)
              if(isTRUE(boolArith)) x %&% t(y) else x %*% t(y))
setMethod("tcrossprod", signature(x = "ANY", y = "Matrix"),
	  function(x, y=NULL, boolArith=NA, ...) {
	      Matrix.msg(sprintf(
	  "potentially suboptimal tcrossprod(<%s>[=<ANY>], \"%s\") as  x %s t(y)",
		  class(x), class(y), "%*%"))
              if(isTRUE(boolArith)) x %&% t(y) else x %*% t(y) })

###--- IV --- %&%  Boolean Matrix Products ----------------------------------

## Goal: crossprod / tcrossprod  with a 'boolArith' option:
## ---- boolArith = NA [default now]   <==> boolean arithmetic if *both* matrices
##                                           are pattern matrices
##     boolArith = TRUE                <==> boolean arithmetic: return n.CMatrix
##     boolArith = FALSE [default later?] <==> numeric arithmetic even for pattern
##
##   A %&% B   <==>       prod(..... boolArith = TRUE)
##   A %*% B   <==>  now: prod(..... boolArith = NA)
##             but later: prod(..... boolArith = FALSE)  # <==> always numeric
## RFC: Should we introduce  matprod(x, y, boolArith)  as generalized  "%*%"
##      which also has all three boolArith options ?
##      since %*% does not allow 'boolArith = FALSE' now, or  'boolArith = NA' later

setMethod("%&%", signature(x = "ANY", y = "ANY"),
	  function(x, y) as.matrix(x) %&% as.matrix(y))
setMethod("%&%", signature(x = "matrix", y = "ANY"), function(x, y) x %&% as.matrix(y))
setMethod("%&%", signature(x = "ANY", y = "matrix"), function(x, y) as.matrix(x) %&% y)
setMethod("%&%", signature(x = "Matrix", y = "ANY"), function(x, y) x %&% as(y, "Matrix"))
setMethod("%&%", signature(x = "ANY", y = "Matrix"), function(x, y) as(x, "Matrix") %&% y)
## catch all
setMethod("%&%", signature(x = "mMatrix", y = "mMatrix"),
	  function(x, y) as(x, "nMatrix") %&% as(y, "nMatrix"))
setMethod("%&%", signature(x = "Matrix", y = "Matrix"),
	  function(x, y) as(x, "nMatrix") %&% as(y, "nMatrix"))
setMethod("%&%", signature(x = "mMatrix", y = "nMatrix"), function(x, y) as(x, "nMatrix") %&% y)
setMethod("%&%", signature(x = "nMatrix", y = "mMatrix"), function(x, y) x %&% as(y, "nMatrix"))

## sparseVectors :
sp.bx.sp <- function(x, y) Matrix(any(x & y), 1L, 1L, sparse=FALSE)
sp.bX.sp <- function(x, y) {
    if((n <- length(x)) == length(y)) sp.bx.sp(x,y)
    else if(n == 1L) spV2M(x, nrow = 1L, ncol = 1L, check = FALSE) %&% y
    else stop("non-conformable arguments")
}
v.bX.sp <- function(x, y) {
    if((n <- length(x)) == length(y)) sp.bx.sp(x,y)
    else if(n == 1L) matrix(x, nrow = 1L, ncol = 1L) %&% y
    else stop("non-conformable arguments")
}
setMethod("%&%", signature(x = "mMatrix", y = "sparseVector"), function(x, y)
    x %&% `dim<-`(y, if(ncol(x) == (n <- length(y))) c(n, 1L) else c(1L, n)))

setMethod("%&%", signature(x = "sparseVector", y = "mMatrix"), function(x, y)
    `dim<-`(x, if(nrow(y) == (n <- length(x))) c(1L, n) else c(n, 1L)) %&% y)

setMethod("%&%", signature(x = "sparseVector", y = "sparseVector"), sp.bX.sp)
setMethod("%&%", signature(x = "sparseVector", y = "numLike"),      sp.bX.sp)
setMethod("%&%", signature(x = "numLike",      y = "sparseVector"), v.bX.sp)

## For now --- suboptimally!!! --- we coerce to nsparseMatrix always:
setMethod("%&%", signature(x = "nMatrix", y = "nsparseMatrix"),
	  function(x, y) as(x, "nsparseMatrix") %&% y)
setMethod("%&%", signature(x = "nsparseMatrix", y = "nMatrix"),
	  function(x, y) x %&% as(y, "nsparseMatrix"))
setMethod("%&%", signature(x = "nMatrix", y = "nMatrix"),
	  function(x, y) as(x, "nsparseMatrix") %&% as(y, "nsparseMatrix"))
setMethod("%&%", signature(x = "nsparseMatrix", y = "nsparseMatrix"),
	  function(x, y) .Call(Csparse_Csparse_prod, as(x,"CsparseMatrix"), as(y,"CsparseMatrix"),
			       boolArith=TRUE))
setMethod("%&%", signature(x = "nsparseMatrix", y = "nCsparseMatrix"),
	  function(x, y) .Call(Csparse_Csparse_prod, as(x,"CsparseMatrix"), y, boolArith=TRUE))
setMethod("%&%", signature(x = "nCsparseMatrix", y = "nsparseMatrix"),
	  function(x, y) .Call(Csparse_Csparse_prod, x, as(y,"CsparseMatrix"), boolArith=TRUE))
setMethod("%&%", signature(x = "nCsparseMatrix", y = "nCsparseMatrix"),
	  function(x, y) .Call(Csparse_Csparse_prod, x, y, boolArith=TRUE))



## Local variables:
## mode: R
## page-delimiter: "^###---"
## End:
