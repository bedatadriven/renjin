#### Sparse Symmetric non-zero pattern Matrices in Triplet format

### contains = "nsparseMatrix"

setAs("nsTMatrix", "matrix",
      function(from) as(as(from, "ngTMatrix"), "matrix"))

setAs("nsTMatrix", "ngCMatrix", # for diag
      function(from) as(as(from, "nsCMatrix"), "ngCMatrix"))

setAs("nsTMatrix", "ngTMatrix",
      function(from) .Call(nsTMatrix_as_ngTMatrix, from))

setAs("nsTMatrix", "dsTMatrix",
      function(from)
      new("dsTMatrix", i = from@i, j = from@j, uplo = from@uplo,
	  x = rep.int(1., length(from@i)),
	  Dim = from@Dim, Dimnames = from@Dimnames))

setAs("nsTMatrix", "nsyMatrix",
      function(from) .Call(nsTMatrix_as_nsyMatrix, from))


setMethod("t", "nsTMatrix",
	  function(x)
	  new("nsTMatrix", Dim = x@Dim, Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, uplo = if (x@uplo == "U") "L" else "U"))
