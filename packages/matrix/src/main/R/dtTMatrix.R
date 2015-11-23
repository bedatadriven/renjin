### Coercion and Methods for Triangular Triplet Matrices

setAs("dtTMatrix", "dgTMatrix",
      function(from) tT2gT(from, cl = "dtTMatrix", toClass = "dgTMatrix"))
setAs("dtTMatrix", "generalMatrix",
      function(from) tT2gT(from, cl = "dtTMatrix", toClass = "dgTMatrix"))

if(FALSE) ## needed in ../tests/Class+Meth.R -- replaced by .T.2.l() in ./Tsparse.R
setAs("dtTMatrix", "ltTMatrix",
      function(from) new("ltTMatrix", i = from@i, j = from@j,
                         x = as.logical(from@x),
                         uplo = from@uplo, diag = from@diag,
                         Dim = from@Dim, Dimnames = from@Dimnames))
if(FALSE) ## needed in ../tests/Class+Meth.R -- replaced by .T.2.n() in ./Tsparse.R
setAs("dtTMatrix", "ntTMatrix",
      function(from) new("ntTMatrix", i = from@i, j = from@j,
                         uplo = from@uplo, diag = from@diag,
                         Dim = from@Dim, Dimnames = from@Dimnames))

## Conversion to dense storage is first to a dtrMatrix
setAs("dtTMatrix", "dtrMatrix",
      function(from) .Call(dtTMatrix_as_dtrMatrix, from))

setAs("dtTMatrix", "matrix",
      function(from) as(as(from, "dtrMatrix"), "matrix"))

setAs("dtTMatrix", "dgeMatrix",
      function(from) as(as(from, "dtrMatrix"), "dgeMatrix"))

setAs("matrix", "dtTMatrix",
      function(from) as(as(from, "dtpMatrix"), "dtTMatrix"))


setMethod("t", "dtTMatrix",
	  function(x)
	  new("dtTMatrix", Dim = x@Dim[2:1], Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, x = x@x, diag = x@diag,
	      uplo = if (x@uplo == "U") "L" else "U"))

