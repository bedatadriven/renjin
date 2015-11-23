#### Sparse Matrices in Compressed row-oriented format
####                               --- "R"

### ``mainly for completeness'' --- we *do* favour Csparse
##    - - - - - - - - - - - -   hence only "minimal" methods here !
##  see also ./SparseM-conv.R

### contains = "dMatrix"

## compressed_to_TMatrix -- fails on 32bit--enable-R-shlib with segfault {Kurt}
## ------------ --> ../src/dgCMatrix.c
.R.2.T <- function(from) .Call(compressed_to_TMatrix, from, FALSE)
## slow R-level workaround
## this is cheap; alternative: going there directly, using
##	i <- .Call(Matrix_expand_pointers, from@p),
if(FALSE)
.R.2.T <- function(from) as(.R.2.C(from), "TsparseMatrix")

## R_to_CMatrix
## ------------ --> ../src/dgCMatrix.c
.R.2.C <- function(from) .Call(R_to_CMatrix, from)

if(FALSE)## "slow" unneeded R-level version
.R.2.C <- function(from)
{
    cl <- class(from)
    valid <- c("dgRMatrix", "dsRMatrix", "dtRMatrix",
               "lgRMatrix", "lsRMatrix", "ltRMatrix",
               "ngRMatrix", "nsRMatrix", "ntRMatrix",
               "zgRMatrix", "zsRMatrix", "ztRMatrix")
    icl <- match(cl, valid) - 1L
    if(is.na(icl)) stop(gettextf("invalid class: %s", dQuote(cl)), domain=NA)
    Ccl <- sub("^(..)R","\\1C", cl)  # corresponding Csparse class name
    r <- new(Ccl)
    r@Dim <- from@Dim[2:1]
    if(icl %/% 3 != 2) ## not "n..Matrix" --> has 'x' slot
        r@x <- from@x
    if(icl %% 3 != 0) {                 # symmetric or triangular
        r@uplo <- from@uplo
        if(icl %% 3 == 2)               # triangular
            r@diag <- from@diag
    }
    r@i <- from@j
    r@p <- from@p
    r <- t(r)
    r@Dimnames <- from@Dimnames
    r
}

## However, a quick way to "treat a t(<R..>) as corresponding <C..> " :
.tR.2.C <- function(from)
{
    cl <- class(from)
    valid <- c("dgRMatrix", "dsRMatrix", "dtRMatrix",
               "lgRMatrix", "lsRMatrix", "ltRMatrix",
               "ngRMatrix", "nsRMatrix", "ntRMatrix",
               "zgRMatrix", "zsRMatrix", "ztRMatrix")
    icl <- match(cl, valid) - 1L
    if(is.na(icl)) stop(gettextf("invalid class: %s", dQuote(cl)), domain=NA)
    Ccl <- sub("^(..)R","\\1C", cl)  # corresponding Csparse class name
    r <- new(Ccl)
    r@i <- from@j
    ##-         -
    r@p <- from@p
    r@Dim      <- from@Dim[2:1]
    r@Dimnames <- from@Dimnames[2:1]

    if(icl %/% 3 != 2) ## not "n..Matrix" --> has 'x' slot
        r@x <- from@x
    if(icl %% 3 != 0) {                 # symmetric or triangular
        r@uplo <- from@uplo
        if(icl %% 3 == 2)               # triangular
            r@diag <- from@diag
    }
    r
}



## coercion to other virtual classes --- the functionality we want to encourage

setAs("RsparseMatrix", "TsparseMatrix", .R.2.T)
setAs("RsparseMatrix", "CsparseMatrix", .R.2.C)

setAs("RsparseMatrix", "denseMatrix",
      function(from) as(.R.2.C(from), "denseMatrix"))

setAs("RsparseMatrix", "dsparseMatrix",
      function(from) as(.R.2.C(from), "dsparseMatrix"))
setAs("RsparseMatrix", "lsparseMatrix",
      function(from) as(.R.2.C(from), "lsparseMatrix"))
setAs("RsparseMatrix", "nsparseMatrix",
      function(from) as(.R.2.C(from), "nsparseMatrix"))

setAs("RsparseMatrix", "dMatrix",
      function(from) as(.R.2.C(from), "dMatrix"))
setAs("RsparseMatrix", "lMatrix",
      function(from) as(.R.2.C(from), "lMatrix"))
setAs("RsparseMatrix", "nMatrix",
      function(from) as(.R.2.C(from), "nMatrix"))

setAs("RsparseMatrix", "generalMatrix",
      function(from) as(.R.2.C(from), "generalMatrix"))


## for printing etc:
setAs("RsparseMatrix", "dgeMatrix",
      function(from) as(.R.2.C(from), "dgeMatrix"))
setAs("RsparseMatrix", "matrix",
      function(from) as(.R.2.C(from), "matrix"))

## **VERY** cheap substitute:  work via dgC and t(.)
.viaC.to.dgR <- function(from) {
    m <- as(t(from), "dgCMatrix")
    new("dgRMatrix", Dim = dim(from), Dimnames = .M.DN(from),
	p = m@p, j = m@i, x = m@x)
}

## one of the few coercions "to <specific>" {tested in ../tests/Class+Meth.R}
setAs("matrix", "dgRMatrix", .viaC.to.dgR)

## *very* cheap substitute:  work via t(.) and Csparse
.viaC.to.R <- function(from) {
    m <- as(t(from), "CsparseMatrix")# preserve symmetry/triangular
    clx <- getClassDef(class(m))
    has.x <- !extends(clx, "nsparseMatrix")## <==> has 'x' slot
    ## instead of "d": .M.kind (m,cl)
    ## instead of "g": ..M.shape(m,cl)
    sh <- .M.shapeC(m,clx)
    r <- new(paste0(.M.kindC(clx), sh, "RMatrix"))
    r@Dim <- dim(from)
    r@Dimnames <-  .M.DN(from)
    r@p <- m@p
    r@j <- m@i
    if(has.x)
	r@x <- m@x
    if(sh != "g") {
	r@uplo <- if(m@uplo != "U") "U" else "L"
	if(sh == "t")
	    r@diag <- m@diag
    }
    r
}

setAs("matrix",      "RsparseMatrix", .viaC.to.R)
setAs("denseMatrix", "RsparseMatrix", .viaC.to.R)
setAs("sparseMatrix","RsparseMatrix", .viaC.to.R)

## symmetric: can use same 'p' slot
setAs("dsCMatrix", "dsRMatrix",
      function(from) new("dsRMatrix", Dim = dim(from), Dimnames = .M.DN(from),
	      p = from@p, j = from@i, x = from@x,
	      uplo = if (from@uplo == "U") "L" else "U"))
## FIXME: if this makes sense, do it for "l" and "n" as well as "d"

## setAs("dtCMatrix", "dtRMatrix", .viaC.to.dgR) # should work; can NOT use 'p'


##setAs("dgRMatrix", "dgeMatrix",
##      function(from) .Call(csc_to_dgeMatrix, from))

##setAs("matrix", "dgRMatrix",
##      function(from) {
##          storage.mode(from) <- "double"
##          .Call(matrix_to_csc, from)
##      })


##setMethod("diag", signature(x = "dgRMatrix"),
##          function(x = 1, nrow, ncol = n) .Call(csc_getDiag, x))

## try to define for "Matrix" -- once and for all -- but that fails -- why? __ FIXME __
## setMethod("dim", signature(x = "dgRMatrix"),
##           function(x) x@Dim, valueClass = "integer")

##setMethod("t", signature(x = "dgRMatrix"),
##          function(x) .Call(csc_transpose, x),
##          valueClass = "dgRMatrix")

setMethod("image", "dgRMatrix", function(x, ...) image(as(x, "TsparseMatrix"), ...))

setMethod("t", "RsparseMatrix", function(x) as(t(.R.2.T(x)), "RsparseMatrix"))


## Want tril(), triu(), band() --- just as "indexing" ---
## return a "close" class:
setMethod("tril", "RsparseMatrix",
	  function(x, k = 0, ...)
	  as(tril(.R.2.C(x), k = k, ...), "RsparseMatrix"))
setMethod("triu", "RsparseMatrix",
	  function(x, k = 0, ...)
	  as(triu(.R.2.C(x), k = k, ...), "RsparseMatrix"))
setMethod("band", "RsparseMatrix",
	  function(x, k1, k2, ...)
	  as(band(.R.2.C(x), k1 = k1, k2 = k2, ...), "RsparseMatrix"))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "index", j = "missing",
				value = "replValue"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), i=i, value=value))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "missing", j = "index",
				value = "replValue"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), j=j, value=value))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "index", j = "index",
				value = "replValue"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), i=i, j=j, value=value))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "index", j = "missing",
				value = "sparseVector"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), i=i, value=value))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "missing", j = "index",
				value = "sparseVector"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), j=j, value=value))

setReplaceMethod("[", signature(x = "RsparseMatrix", i = "index", j = "index",
				value = "sparseVector"),
		 function (x, i, j, ..., value)
		 replTmat(as(x,"TsparseMatrix"), i=i, j=j, value=value))


setReplaceMethod("[", signature(x = "RsparseMatrix", i = "matrix", j = "missing",
				value = "replValue"),
		 function (x, i, j, ..., value)
		 .TM.repl.i.mat(as(x,"TsparseMatrix"), i=i, value=value))



