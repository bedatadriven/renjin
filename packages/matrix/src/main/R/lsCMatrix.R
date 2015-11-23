#### Logical Symmetric Sparse Matrices in Compressed column-oriented format

### contains = "lsparseMatrix"

setAs("lsCMatrix", "matrix",
      function(from) as(as(from, "generalMatrix"), "matrix"))

setAs("lsCMatrix", "lgCMatrix",
      function(from) .Call(Csparse_symmetric_to_general, from))

## needed for indexing (still ?)
setAs("lsCMatrix", "lgTMatrix",
      function(from) as(as(from, "generalMatrix"), "lgTMatrix"))

aslsC.by.lgC <- function(from) as(as(from, "lgCMatrix"), "symmetricMatrix")
setAs("lgTMatrix", "lsCMatrix", aslsC.by.lgC) # <-> needed for Matrix()
setAs("matrix",    "lsCMatrix", aslsC.by.lgC)

## Specific conversions, should they be necessary.  Better to convert as
## as(x, "TsparseMatrix") or as(x, "denseMatrix")
setAs("lsCMatrix", "lsTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, FALSE))

setAs("lsCMatrix", "dsCMatrix",
      function(from) new("dsCMatrix", i = from@i, p = from@p,
                         x = as.double(from@x), uplo = from@uplo,
                         Dim = from@Dim, Dimnames = from@Dimnames))

if(FALSE) # needed ?
setAs("lsCMatrix", "dgTMatrix",
      function(from) as(as(from, "dsCMatrix"), "dgTMatrix"))


## have rather tril() and triu() methods than
## setAs("lsCMatrix", "ltCMatrix", ....)
setMethod("tril", "lsCMatrix",
	  function(x, k = 0, ...) {
	      if(x@uplo == "L" && k == 0)
		  ## same internal structure + diag
		  new("ltCMatrix", uplo = x@uplo, i = x@i, p = x@p,
		      x = x@x, Dim = x@Dim, Dimnames = x@Dimnames)
	      else tril(as(x, "lgCMatrix"), k = k, ...)
	  })
setMethod("triu", "lsCMatrix",
	  function(x, k = 0, ...) {
	      if(x@uplo == "U" && k == 0)
		  new("ltCMatrix", uplo = x@uplo, i = x@i, p = x@p,
		      x = x@x, Dim = x@Dim, Dimnames = x@Dimnames)
	      else triu(as(x, "lgCMatrix"), k = k, ...)
	  })

setMethod("chol", signature(x = "lsCMatrix"),
	  function(x, pivot=FALSE, ...)
	  chol(as(x, "dgCMatrix"), pivot=pivot, ...))

## Use more general method from CsparseMatrix class
## setMethod("t", signature(x = "lsCMatrix"),
##           function(x)
##           .Call(lsCMatrix_trans, x),
##           valueClass = "lsCMatrix")
