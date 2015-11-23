#### Superclass Methods for all sparse logical matrices


C2l <- function(from) {
    if(extends(cld <- getClassDef(class(from)), "lsparseMatrix"))
	return(from)
    ## else
    if(!(is.n <- extends(cld, "nsparseMatrix"))) {
        ## len.x <- length(from@x)
        from <- .Call(Csparse_drop, from, 0)
        ## did.drop <- length(from@x) != len.x
    }
    r <- as(.C2nC(from, extends(cld, "triangularMatrix")), "lsparseMatrix")
    if(!is.n && any(ina <- is.na(from@x))) { ## NAs must remain NA
        ## since we dropped, we "know"  that the 'x' slots match:
        stopifnot(length(from@x) == length(r@x))
        is.na(r@x) <- ina
    }
    r
}

setAs("CsparseMatrix", "lMatrix", C2l)
setAs("CsparseMatrix", "lsparseMatrix", C2l)

setAs("lsparseMatrix", "matrix",
      function(from) as(as(from, "ldenseMatrix"), "matrix"))

setAs("lsparseMatrix", "dsparseMatrix", function(from) as(from, "dMatrix"))



setMethod("image", "lsparseMatrix", function(x, ...) image(as(x,"dMatrix"), ...))
