#### Logical Sparse Matrices in Compressed column-oriented format

### contains = "nsparseMatrix"

.nC2d <- function(from) .Call(nz_pattern_to_Csparse, from, 0L)## 0 --> "double"
.nC2l <- function(from) .Call(nz_pattern_to_Csparse, from, 1L)## 1 --> "logical"

if(FALSE) { ## nice idea, but needs more method re-definitions ---
setAs("nCsparseMatrix", "dMatrix", .nC2d)
setAs("nCsparseMatrix", "dsparseMatrix", .nC2d)
setAs("nCsparseMatrix", "dgCMatrix", .nC2d)

setAs("nCsparseMatrix", "lMatrix", .nC2l)
setAs("nCsparseMatrix", "lsparseMatrix", .nC2l)
setAs("nCsparseMatrix", "lgCMatrix", .nC2l)
} else {
setAs("ngCMatrix", "dMatrix", .nC2d)
setAs("ngCMatrix", "dsparseMatrix", .nC2d)
setAs("ngCMatrix", "dgCMatrix", .nC2d)

setAs("ngCMatrix", "lMatrix", .nC2l)
setAs("ngCMatrix", "lsparseMatrix", .nC2l)
setAs("ngCMatrix", "lgCMatrix", .nC2l)
}

setAs("ngCMatrix", "matrix", function(from) .Call(ngC_to_matrix, from))
## not this: .Call(Csparse_to_matrix, from)), since it goes via dense -> double precision

## TODO (maybe): write  matrix_to_lcsc()  in ../src/ngCMatrix.c
setAs("matrix", "ngCMatrix",
      function(from) as(as(from, "ngTMatrix"), "ngCMatrix"))

