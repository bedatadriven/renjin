#### Superclass Methods for all sparse nonzero-pattern matrices

.C2nC <- function(from, isTri = is(from, "triangularMatrix"))
    .Call(Csparse_to_nz_pattern, from, isTri)

setAs("CsparseMatrix", "nsparseMatrix", function(from) .C2nC(from))
setAs("CsparseMatrix", "nMatrix",       function(from) .C2nC(from))

setAs("nsparseMatrix", "dsparseMatrix", function(from) as(from, "dMatrix"))


setMethod("is.na", signature(x = "nsparseMatrix"), is.na_nsp)

if(getRversion() >= "3.1.0")
setMethod("anyNA", signature(x = "nsparseMatrix"), function(x) FALSE)


setMethod("image", "nsparseMatrix", function(x, ...) image(as(x,"dMatrix"), ...))
