#### Logical Sparse Triangular Matrices in Compressed column-oriented format

setAs("ntCMatrix", "matrix",
      function(from) as(copyClass(diagU2N(from), "ngCMatrix"), "matrix"))
setAs("matrix", "ntCMatrix",
      function(from) as(as(from, "dtCMatrix"), "ntCMatrix"))

setAs("ntCMatrix", "TsparseMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, TRUE))

setAs("ntCMatrix", "ngCMatrix",
      function(from) copyClass(diagU2N(from), "ngCMatrix"))



## "FIXME": Not needed, once we use "nCsparseMatrix" (-> ./ngCMatrix.R ):
setAs("ntCMatrix", "dMatrix", .nC2d)
setAs("ntCMatrix", "dsparseMatrix", .nC2d)
setAs("ntCMatrix", "dtCMatrix", .nC2d)
##
setAs("ntCMatrix", "lMatrix", .nC2l)
setAs("ntCMatrix", "lsparseMatrix", .nC2l)
setAs("ntCMatrix", "ltCMatrix", .nC2l)


setAs("ngCMatrix", "ntCMatrix", # to triangular, needed for triu,..
      function(from) as(as(as(from, "TsparseMatrix"),
                           "ntTMatrix"), "ntCMatrix"))

## setMethod("t", signature(x = "ntCMatrix"),
##           function(x) .Call(ntCMatrix_trans, x),
##           valueClass = "ntCMatrix")
