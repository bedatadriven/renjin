#### Logical Sparse Triangular Matrices in Triplet format

### contains = "lsparseMatrix"

setAs("matrix", "ltTMatrix",
      function(from) as(as(from, "ltrMatrix"), "TsparseMatrix"))

setAs("ltTMatrix", "lgTMatrix",
      function(from) tT2gT(from, cl = "ltTMatrix", toClass = "lgTMatrix"))
setAs("ltTMatrix", "generalMatrix",
      function(from) tT2gT(from, cl = "ltTMatrix", toClass = "lgTMatrix"))

setAs("ltTMatrix", "ltCMatrix",
      function(from) .Call(Tsparse_to_Csparse, from, TRUE))
setAs("ltTMatrix", "lgCMatrix",
      function(from) as(.Call(Tsparse_to_Csparse, from, TRUE), "lgCMatrix"))

setAs("ltTMatrix", "dtTMatrix",
      function(from) new("dtTMatrix", i = from@i, j = from@j,
                         x = rep.int(1, length(from@i)), uplo = from@uplo,
                         diag = from@diag,
                         Dim = from@Dim, Dimnames = from@Dimnames))

setAs("ltTMatrix", "ltrMatrix",
      function(from) .Call(ltTMatrix_as_ltrMatrix, from))

setAs("ltTMatrix", "matrix",
      function(from) as(as(from, "ltrMatrix"), "matrix"))



setMethod("t", "ltTMatrix",
	  function(x)
	  new("ltTMatrix", Dim = x@Dim[2:1], Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, x = x@x, diag = x@diag,
	      uplo = if (x@uplo == "U") "L" else "U"))
