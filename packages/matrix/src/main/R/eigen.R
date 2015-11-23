#### eigen() , Schur() etc
#### =====     =====

## eigen() is not even generic, and we haven't any C code,
##	   but rather  base::eigen()  "magically"  works via as.matrix()
if (FALSE) {
setMethod("eigen", signature(x = "dgeMatrix", only.values = "missing"),
	  function(x, symmetric, only.values, EISPACK) # << must match generic
	  .Call(dgeMatrix_eigen, x, FALSE))

setMethod("eigen", signature(x = "dgeMatrix", only.values = "logical"),
	  function(x, symmetric, only.values, EISPACK)
	  .Call(dgeMatrix_eigen, x, only.values))
} #not yet

.dgeSchur <- function(x, vectors, ...) {
    cl <- .Call(dgeMatrix_Schur, x, TRUE, TRUE)
    realEV <- all(cl$WI == 0)
    ## TODO: do all this in C
    new("Schur", Dim = x@Dim,
	Q = as(cl$Z, "dgeMatrix"),
	T = as(cl$T, if(realEV)"dtrMatrix" else "dgeMatrix"),
	EValues = if(realEV) cl$WR else complex(real = cl$WR, imaginary = cl$WI))
}
setMethod("Schur", signature(x = "dgeMatrix", vectors = "missing"),
	  .dgeSchur)

setMethod("Schur", signature(x = "dgeMatrix", vectors = "logical"),
	  function(x, vectors, ...) {
	      if(vectors) .dgeSchur(x)
	      else {
		  cl <- .Call(dgeMatrix_Schur, x, FALSE, TRUE)
		  realEV <- all(cl$WI == 0)
		  list(T = as(cl$T, if(realEV) "dtrMatrix" else "dgeMatrix"),
		       EValues =
                       if(realEV) cl$WR else complex(real = cl$WR, imaginary = cl$WI))
	      }})

## Ok, for the faint of heart, also provide "matrix" methods :
.mSchur <- function(x, vectors, ...) {
    cl <- .Call(dgeMatrix_Schur, x, TRUE, FALSE)
    list(Q = cl$Z,
	 T = cl$T,
	 EValues = if(all(cl$WI == 0)) cl$WR
	 else complex(real = cl$WR, imaginary = cl$WI))
}
setMethod("Schur", signature(x = "matrix", vectors = "missing"), .mSchur)

setMethod("Schur", signature(x = "matrix", vectors = "logical"),
	  function(x, vectors, ...) {
	      if(vectors) .mSchur(x)
	      else {
		  cl <- .Call(dgeMatrix_Schur, x, FALSE, FALSE)
		  EV <- if(all(cl$WI == 0)) cl$WR
			else complex(real = cl$WR, imaginary = cl$WI)
		  cl$WR <- cl$WI <- NULL
		  cl$EValues <- EV
		  cl
	      }})


Schur.dsy <- function(x, vectors, ...)
{
    if(missing(vectors)) vectors <- TRUE
    ## TODO: do all this in C
    ## Should directly call LAPACK dsyev()
    evl <- eigen(x, only.values = !vectors)
    eVals <- evl$values
    if(vectors)
	new("Schur", Dim = x@Dim,
	    Q = as(evl$vectors, "dgeMatrix"),
	    T = Diagonal(x = eVals),
	    EValues = eVals)
    else
	list(T = Diagonal(x = eVals), EValues = eVals)
}

setMethod("Schur", signature(x = "dsyMatrix", vectors = "ANY"), Schur.dsy)

## FIXME(?) these  coerce from sparse to *dense*
setMethod("Schur", signature(x = "generalMatrix", vectors = "missing"),
	  function(x, vectors, ...) callGeneric(as(x, "dgeMatrix")))
setMethod("Schur", signature(x = "generalMatrix", vectors = "logical"),
	  function(x, vectors, ...) callGeneric(as(x, "dgeMatrix"), vectors))

setMethod("Schur", signature(x = "symmetricMatrix", vectors = "missing"),
	  function(x, vectors, ...) Schur.dsy(as(x, "dsyMatrix")))
setMethod("Schur", signature(x = "symmetricMatrix", vectors = "logical"),
	  function(x, vectors, ...) Schur.dsy(as(x, "dsyMatrix"), vectors))


## Schur(<diagonal>) : {Note that the Schur decomposition is not unique here}
.simpleSchur <- function(x, vectors, ...) {
    x <- as(x, "dMatrix")
    d <- dim(x)
    new("Schur", Dim = d, Q = Diagonal(d[1]), T = x, EValues = diag(x))
}
setMethod("Schur", signature(x = "diagonalMatrix", vectors = "missing"),
	  .simpleSchur)

setMethod("Schur", signature(x = "diagonalMatrix", vectors = "logical"),
	  function(x, vectors, ...) {
	      if(vectors) .simpleSchur(x)
	      else {
		  x <- as(x, "dMatrix")
		  list(T = x, EValues = x@x)
	      }})

.triSchur <- function(x, vectors, ...) {
    x <- as(x, "dMatrix")
    d <- dim(x)
    n <- d[1]
    if(x@uplo == "U" || n == 0)
	new("Schur", Dim = d, Q = Diagonal(n), T = x, EValues = diag(x))
    else {
	i <- n:1
	new("Schur", Dim = d, Q = as(i, "pMatrix"),
	    T = t(t(x)[i,i]), EValues = diag(x)[i])
    }
}

setMethod("Schur", signature(x = "triangularMatrix", vectors = "missing"),
	  .triSchur)

setMethod("Schur", signature(x = "triangularMatrix", vectors = "logical"),
	  function(x, vectors, ...) {
	      if(vectors) .triSchur(x)
	      else {
		  x <- as(x, "dMatrix")
		  list(T = x, EValues = x@x)
	      }})
