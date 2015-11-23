#### Logical Sparse Triangular Matrices in Compressed column-oriented format

setAs("ltCMatrix", "matrix",
      function(from) as(as(from, "lgCMatrix"), "matrix"))
setAs("matrix", "ltCMatrix",
      function(from) as(as(from, "dtCMatrix"), "ltCMatrix"))

setAs("ltCMatrix", "lgCMatrix",
      function(from) copyClass(diagU2N(from), "lgCMatrix",
			       c("i", "p", "x", "Dim", "Dimnames")))

setAs("ltCMatrix", "ltTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, TRUE))

setAs("ltCMatrix", "dMatrix", # < instead of "dtCMatrix"
      function(from) new("dtCMatrix", i = from@i, p = from@p,
                         x = as.double(from@x), uplo = from@uplo,
                         diag = from@diag,
                         Dim = from@Dim, Dimnames = from@Dimnames))

setAs("lgCMatrix", "ltCMatrix", # to triangular {needed in triu() }
      function(from) as(as(as(from, "lgTMatrix"), "ltTMatrix"), "ltCMatrix"))


## setAs("ltCMatrix", "generalMatrix",
##       function(from) ......)

## setMethod("t", signature(x = "ltCMatrix"),
##           function(x) .Call(ltCMatrix_trans, x),
##           valueClass = "ltCMatrix")
