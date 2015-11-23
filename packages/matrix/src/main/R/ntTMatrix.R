#### Logical Sparse Triangular Matrices in Triplet format

### contains = "nsparseMatrix"

setAs("matrix", "ntTMatrix",
      function(from) as(as(from, "ntrMatrix"), "TsparseMatrix"))

setAs("ntTMatrix", "ngTMatrix",
      function(from) tT2gT(from, cl = "ntTMatrix", toClass = "ngTMatrix"))
setAs("ntTMatrix", "generalMatrix",
      function(from) tT2gT(from, cl = "ntTMatrix", toClass = "ngTMatrix"))

setAs("ntTMatrix", "ntCMatrix",
      function(from) .Call(Tsparse_to_Csparse, from, TRUE))
setAs("ntTMatrix", "ngCMatrix",
      function(from) as(.Call(Tsparse_to_Csparse, from, TRUE), "ngCMatrix"))


setAs("ntTMatrix", "dtTMatrix",
      function(from) new("dtTMatrix", i = from@i, j = from@j,
                         x = rep.int(1, length(from@i)), uplo = from@uplo,
                         diag = from@diag,
                         Dim = from@Dim, Dimnames = from@Dimnames))

setAs("ntTMatrix", "ntrMatrix",
      function(from) .Call(ntTMatrix_as_ntrMatrix, from))

setAs("ntTMatrix", "matrix",
      function(from) as(as(from, "ntrMatrix"), "matrix"))


setMethod("t", "ntTMatrix",
	  function(x)
	  new("ntTMatrix", Dim = x@Dim[2:1], Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, diag = x@diag,
	      uplo = if (x@uplo == "U") "L" else "U"))
