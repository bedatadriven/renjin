#### Logical Sparse Symmetric Matrices in Triplet format

### contains = "lsparseMatrix"

setAs("lsTMatrix", "matrix",
      function(from) as(as(from, "lgTMatrix"), "matrix"))

setAs("lsTMatrix", "lgCMatrix", # for diag
      function(from) as(as(from, "lsCMatrix"), "lgCMatrix"))

setAs("lsTMatrix", "lgTMatrix",
      function(from) .Call(lsTMatrix_as_lgTMatrix, from))


if(FALSE) # should use  as(., "dMatrix")
setAs("lsTMatrix", "dsTMatrix",
      function(from)
      new("dsTMatrix", i = from@i, j = from@j, uplo = from@uplo,
	  x = as.double(from@x), # *not* just 1; from@x *can* have FALSE
	  Dim = from@Dim, Dimnames = from@Dimnames))

setAs("lsTMatrix", "lsyMatrix",
      function(from) .Call(lsTMatrix_as_lsyMatrix, from))


setMethod("t", "lsTMatrix",
	  function(x)
	  new("lsTMatrix", Dim = x@Dim, Dimnames = x@Dimnames[2:1],
	      i = x@j, j = x@i, x = x@x,
	      uplo = if (x@uplo == "U") "L" else "U"))
