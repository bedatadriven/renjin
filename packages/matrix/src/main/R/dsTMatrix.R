### Coercion and Methods for Symmetric Triplet Matrices

## Now in ./Tsparse.R
## setAs("dsTMatrix", "dsCMatrix",
##       function(from) .Call(Tsparse_to_Csparse, from, FALSE))

setAs("dsTMatrix", "dgTMatrix",
      function(from) .Call(dsTMatrix_as_dgTMatrix, from))

setAs("dsTMatrix", "lsTMatrix",
      function(from) new("lsTMatrix", i = from@i, j = from@j, uplo = from@uplo,
                         Dim = from@Dim, Dimnames = from@Dimnames))


## Conversion <--> dense storage is via dsyMatrix :
setAs("dsTMatrix", "dsyMatrix",
      function(from) .Call(dsTMatrix_as_dsyMatrix, from))

setAs("dsTMatrix", "dgeMatrix",
      function(from) as(as(from, "dsyMatrix"), "dgeMatrix"))
setAs("dsTMatrix", "matrix",
      function(from) as(as(from, "dsyMatrix"), "matrix"))

to_dsT <- function(from) as(as(from, "dsyMatrix"), "dsTMatrix")
setAs("dgeMatrix", "dsTMatrix", to_dsT)
setAs("matrix",    "dsTMatrix", to_dsT)

setMethod("t", "dsTMatrix",
	  function(x)
	  new("dsTMatrix", Dim = x@Dim, Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, x = x@x,
	      uplo = if (x@uplo == "U") "L" else "U"))


## setMethod("writeHB", signature(obj = "dsTMatrix"),
##           function(obj, file, ...) callGeneric(as(obj, "CsparseMatrix"), file, ...))
