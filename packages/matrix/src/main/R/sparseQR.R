#### Methods for the sparse QR decomposition

## TODO: qr.R() generic that allows optional args ['backPermute']
## --- so we can add it to our qr.R() method,  *instead* of this :
qrR <- function(qr, complete = FALSE, backPermute = TRUE, row.names = TRUE) {
    ir <- seq_len(qr@Dim[if(complete) 1L else 2L])
    r <- if(backPermute <- backPermute && (n <- length(qr@q)) && !isSeq(qr@q, n-1L))
	qr@R[ir, order(qr@q), drop = FALSE] else
	qr@R[ir,	    , drop = FALSE]
    if(row.names && !is.null(rn <- qr@V@Dimnames[[1]])) # qr.R() in 'base' gives rownames
	r@Dimnames[[1]] <- rn[seq_len(r@Dim[1L])]
    if(complete || backPermute) r else as(r, "triangularMatrix")
}
setMethod("qr.R", signature(qr = "sparseQR"),
	  function(qr, complete = FALSE) {
	      if((is.null(v <- getOption("Matrix.quiet.qr.R")) || !v) &&
		 (is.null(v <- getOption("Matrix.quiet")) || !v))
		  warning("qr.R(<sparse>) may differ from qr.R(<dense>) because of permutations.  Possibly use our qrR() instead")
	      qrR(qr, complete=complete, backPermute=FALSE)
	      })

## if(identical("", as.character(formals(qr.Q)$Dvec))) { # "new"
setMethod("qr.Q", "sparseQR",
	  function(qr, complete=FALSE, Dvec)
      {
	  d <- qr@Dim
	  ## ir <- seq_len(d[k <- if(complete) 1L else 2L])
	  k <- if(complete) 1L else 2L
	  if(missing(Dvec)) Dvec <- rep.int(1, if(complete) d[1] else min(d))
	  D <- .sparseDiagonal(d[1], x=Dvec, cols=0L:(d[k] -1L))
	  qr.qy(qr, D)
      })
## } else {
## setMethod("qr.Q", "sparseQR",
## 	  function(qr, complete=FALSE, Dvec = rep.int(1, if(complete) d[1] else min(d)))
##       {
## 	  d <- qr@Dim
## 	  ir <- seq_len(d[k <- if(complete) 1L else 2L])
## 	  D <- .sparseDiagonal(d[1], x=Dvec, cols=0L:(d[k] -1L))
## 	  qr.qy(qr, D)
##       })
## }

## NB:  Here, the .Call()s to  sparseQR_qty all set  keep_names = TRUE
## ---  instead of allowing it to become an argument,
##      mainly because the base functions qr.qy() / qr.qty() have no '...' formal argument
## To change, would make these *implicit* generics in 'methods' - as qr.R
## Also,  qr() itself has  keep.names = TRUE/FALSE -- should be enough
setMethod("qr.qy", signature(qr = "sparseQR", y = "ddenseMatrix"),
          function(qr, y) .Call(sparseQR_qty, qr, y, FALSE, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.qy", signature(qr = "sparseQR", y = "matrix"),
          function(qr, y) .Call(sparseQR_qty, qr, y, FALSE, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.qy", signature(qr = "sparseQR", y = "numeric"),
	  ## drop to vector {to be 100% standard-R-matrix compatible} :
	  function(qr, y) .Call(sparseQR_qty, qr, y, FALSE, TRUE)@x)

setMethod("qr.qy", signature(qr = "sparseQR", y = "Matrix"),
	  function(qr, y) .Call(sparseQR_qty, qr,
				as(as(y, "denseMatrix"),"dgeMatrix"), FALSE, TRUE),
	  valueClass = "dgeMatrix")

setMethod("qr.qty", signature(qr = "sparseQR", y = "ddenseMatrix"),
          function(qr, y) .Call(sparseQR_qty, qr, y, TRUE, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.qty", signature(qr = "sparseQR", y = "matrix"),
          function(qr, y) .Call(sparseQR_qty, qr, y, TRUE, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.qty", signature(qr = "sparseQR", y = "numeric"),
	  function(qr, y) .Call(sparseQR_qty, qr, y, TRUE, TRUE)@x)

setMethod("qr.qty", signature(qr = "sparseQR", y = "Matrix"),
	  function(qr, y) .Call(sparseQR_qty, qr,
				as(as(y, "denseMatrix"),"dgeMatrix"), TRUE, TRUE),
	  valueClass = "dgeMatrix")

## FIXME: really should happen in C, i.e sparseQR_coef() in ../src/sparseQR.c :
.coef.trunc <- function(qr, res, drop=FALSE) {
    if((d <- lengths(res@Dimnames)) != c(0L,0L) && !identical(d, D <- res@Dim)) {
	## Fix dimnames from dim:
	length(res@Dimnames[[1]]) <- D[1]
	length(res@Dimnames[[2]]) <- D[2]
    }
    res[seq_len(ncol(qr@R)),,drop=drop]
}

setMethod("qr.coef", signature(qr = "sparseQR", y = "ddenseMatrix"),
          function(qr, y)
          .coef.trunc(qr, .Call(sparseQR_coef, qr, y)),
          valueClass = "dgeMatrix")

setMethod("qr.coef", signature(qr = "sparseQR", y = "matrix"),
          function(qr, y)
          .coef.trunc(qr, .Call(sparseQR_coef, qr, y)),
          valueClass = "dgeMatrix")

setMethod("qr.coef", signature(qr = "sparseQR", y = "numeric"),
          function(qr, y)
	  .coef.trunc(qr, .Call(sparseQR_coef, qr, y), drop=TRUE))

setMethod("qr.coef", signature(qr = "sparseQR", y = "Matrix"),
	  function(qr, y)
	  .coef.trunc(qr, .Call(sparseQR_coef, qr,
				as(as(y, "denseMatrix"),"dgeMatrix"))),
	  valueClass = "dgeMatrix")

##  qr.resid()  &  qr.fitted() : ---------------------------

setMethod("qr.resid", signature(qr = "sparseQR", y = "ddenseMatrix"),
          function(qr, y)
          .Call(sparseQR_resid_fitted, qr, y, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.resid", signature(qr = "sparseQR", y = "matrix"),
          function(qr, y)
          .Call(sparseQR_resid_fitted, qr, y, TRUE),
          valueClass = "dgeMatrix")

setMethod("qr.resid", signature(qr = "sparseQR", y = "numeric"),
	  function(qr, y) drop(.Call(sparseQR_resid_fitted, qr, y, TRUE)))

setMethod("qr.resid", signature(qr = "sparseQR", y = "Matrix"),
	  function(qr, y)
	  .Call(sparseQR_resid_fitted, qr,
		as(as(y, "denseMatrix"),"dgeMatrix"), TRUE),
	  valueClass = "dgeMatrix")

setMethod("qr.fitted", signature(qr = "sparseQR", y = "ddenseMatrix"),
          function(qr, y, k)
          .Call(sparseQR_resid_fitted, qr, y, FALSE),
          valueClass = "dgeMatrix")

setMethod("qr.fitted", signature(qr = "sparseQR", y = "matrix"),
          function(qr, y, k)
          .Call(sparseQR_resid_fitted, qr, y, FALSE),
          valueClass = "dgeMatrix")

setMethod("qr.fitted", signature(qr = "sparseQR", y = "numeric"),
	  function(qr, y, k) drop(.Call(sparseQR_resid_fitted, qr, y, FALSE)))

setMethod("qr.fitted", signature(qr = "sparseQR", y = "Matrix"),
	  function(qr, y, k)
	  .Call(sparseQR_resid_fitted, qr,
		as(as(y, "denseMatrix"),"dgeMatrix"), FALSE),
	  valueClass = "dgeMatrix")


##
setMethod("solve", signature(a = "sparseQR", b = "ANY"),
	  function(a, b, ...) qr.coef(a, b))
