## --- New "logic" class -- currently using "raw" instead of "logical"
## LOGIC setClass("logic", contains = "raw")

##' To be used in initialize method or other Matrix constructors
##'
##' TODO: via .Call(..)
.fixupDimnames <- function(dnms) {
    N.N <- list(NULL, NULL)
    if(is.null(dnms) || identical(dnms, N.N)) return(N.N)
    ## else
    if(any(i0 <- lengths(dnms) == 0) && !all(vapply(dnms[i0], is.null, NA)))
	## replace character(0) etc, by NULL :
	dnms[i0] <- list(NULL)
    ## coerce, e.g. integer dimnames to character: -- as  R's matrix(..):
    if(any(i0 <- vapply(dnms, function(d) !is.null(d) && !is.character(d), NA)))
	dnms[i0] <- lapply(dnms[i0], as.character)
    dnms
}


## ------------- Virtual Classes ----------------------------------------

## Mother class of all Matrix objects
setClass("Matrix",
	 representation(Dim = "integer", Dimnames = "list", "VIRTUAL"),
	 prototype = prototype(Dim = integer(2), Dimnames = list(NULL,NULL)),
	 validity = function(object) {
	     if(!isTRUE(r <- .Call(Dim_validate, object, "Matrix")))
                 r
             else .Call(dimNames_validate, object)
	 })

if(getRversion() >= "3.2.0") {
setMethod("initialize", "Matrix", function(.Object, ...)
    {
        .Object <- callNextMethod()
	if(length(args <- list(...)) && any(nzchar(snames <- names(args))) && "Dimnames" %in% snames)
	    .Object@Dimnames <- .fixupDimnames(.Object@Dimnames)
	.Object
    })
} else { ## R < 3.2.0
setMethod("initialize", "Matrix", function(.Object, ...)
    {
	.Object <- callNextMethod(.Object, ...)
	if(length(args <- list(...)) && any(nzchar(snames <- names(args))) && "Dimnames" %in% snames)
	    .Object@Dimnames <- .fixupDimnames(.Object@Dimnames)
	.Object
    })
}

## The class of composite matrices - i.e. those for which it makes sense to
## create a factorization
setClass("compMatrix",	representation(factors = "list", "VIRTUAL"),
	 contains = "Matrix")

## Virtual classes of Matrices determined by above/below diagonal relationships

setClass("generalMatrix", representation = "VIRTUAL", contains = "compMatrix")

setClass("symmetricMatrix",
	 representation(uplo = "character", "VIRTUAL"),
	 contains = "compMatrix",
	 prototype = prototype(uplo = "U"),
	 validity = function(object) .Call(symmetricMatrix_validate, object))

setClass("triangularMatrix",
	 representation(uplo = "character", diag = "character", "VIRTUAL"),
	 contains = "Matrix",
	 prototype = prototype(uplo = "U", diag = "N"),
	 validity = function(object) .Call(triangularMatrix_validate, object))


## Virtual class of numeric matrices
setClass("dMatrix",
	 representation(x = "numeric", "VIRTUAL"), contains = "Matrix",
	 validity = function(object) .Call(dMatrix_validate, object))

## Virtual class of integer matrices
setClass("iMatrix",
	 representation(x = "integer", "VIRTUAL"), contains = "Matrix")

## Virtual class of logical matrices
setClass("lMatrix",
## LOGIC representation(x = "logic", "VIRTUAL"), contains = "Matrix")
         representation(x = "logical", "VIRTUAL"), contains = "Matrix")

## Virtual class of nonzero pattern matrices
setClass("nMatrix", representation("VIRTUAL"), contains = "Matrix")
## aka 'pattern' matrices -- have no x slot

## Virtual class of complex matrices
setClass("zMatrix", # letter 'z' is as in the names of Lapack subroutines
	 representation(x = "complex", "VIRTUAL"), contains = "Matrix")

## Virtual class of dense matrices (including "packed")
setClass("denseMatrix", representation("VIRTUAL"),
	 contains = "Matrix")

## Virtual class of dense, numeric matrices
setClass("ddenseMatrix", representation("VIRTUAL"),
	 contains = c("dMatrix", "denseMatrix"))

## Virtual class of dense, logical matrices
setClass("ldenseMatrix", representation("VIRTUAL"),
	 contains = c("lMatrix", "denseMatrix"))

if(FALSE) { ##--not yet--
setClass("idenseMatrix", representation("VIRTUAL"),
	 contains = c("iMatrix", "denseMatrix"))
}

## Virtual class of dense, nonzero pattern matrices - rarely used, for completeness
setClass("ndenseMatrix", representation(x = "logical", "VIRTUAL"),
	 contains = c("nMatrix", "denseMatrix"))

## virtual SPARSE ------------

setClass("sparseMatrix", representation("VIRTUAL"), contains = "Matrix")

## diagonal: has 'diag' slot;  diag = "U"  <--> have identity matrix
setClass("diagonalMatrix", representation(diag = "character", "VIRTUAL"),
	 contains = "sparseMatrix",
         ## NOTE:    ^^^^^^ was dense Matrix, until 0.999375-11 (2008-07)
	 validity = function(object) {
	     d <- object@Dim
	     if(d[1] != (n <- d[2])) return("matrix is not square")
	     lx <- length(object@x)
	     if(object@diag == "U") {
		 if(lx != 0)
		     return("diag = \"U\" (identity matrix) requires empty 'x' slot")
	     } else if(object@diag == "N") {
		 if(lx != n)
		     return("diagonal matrix has 'x' slot of length != 'n'")
	     } else return("diagonal matrix 'diag' slot must be \"U\" or \"N\"")
	     TRUE
	 },
	 prototype = prototype(diag = "N")
	 )

## sparse matrices in Triplet representation (dgT, lgT, ..):
setClass("TsparseMatrix", representation(i = "integer", j = "integer", "VIRTUAL"),
	 contains = "sparseMatrix",
	 validity = function(object) .Call(Tsparse_validate, object)
         )

setClass("CsparseMatrix", representation(i = "integer", p = "integer", "VIRTUAL"),
	 contains = "sparseMatrix",
	 prototype = prototype(p = 0L),# to be valid
         validity = function(object) .Call(Csparse_validate, object)
         )

setClass("RsparseMatrix", representation(p = "integer", j = "integer", "VIRTUAL"),
	 contains = "sparseMatrix",
	 prototype = prototype(p = 0L),# to be valid
	 validity = function(object) .Call(Rsparse_validate, object)
         )

setClass("dsparseMatrix", representation("VIRTUAL"),
	 contains = c("dMatrix", "sparseMatrix"))

setClass("lsparseMatrix", representation("VIRTUAL"),
	 contains = c("lMatrix", "sparseMatrix"))

if(FALSE) { ##--not yet--
setClass("isparseMatrix", representation("VIRTUAL"),
	 contains = c("iMatrix", "sparseMatrix"))
}

## these are the "pattern" matrices for "symbolic analysis" of sparse OPs:
setClass("nsparseMatrix", representation("VIRTUAL"),
	 contains = c("nMatrix", "sparseMatrix"))

## More Class Intersections {for method dispatch}:
if(FALSE) { ## this is "natural" but gives WARNINGs when other packages use "it"
setClass("dCsparseMatrix", representation("VIRTUAL"),
	 contains = c("CsparseMatrix", "dsparseMatrix"))
setClass("lCsparseMatrix", representation("VIRTUAL"),
	 contains = c("CsparseMatrix", "lsparseMatrix"))
setClass("nCsparseMatrix", representation("VIRTUAL"),
	 contains = c("CsparseMatrix", "nsparseMatrix"))

## dense general
setClass("geMatrix", representation("VIRTUAL"),
	 contains = c("denseMatrix", "generalMatrix"))

} else { ## ----------- a version that maybe works better for other pkgs ---------

 ##--> setClassUnion() ... below
}


## ------------------ Proper (non-virtual) Classes ----------------------------

##----------------------  DENSE	 -----------------------------------------

## numeric, dense, general matrices
setClass("dgeMatrix", contains = c("ddenseMatrix", "generalMatrix"),
	 ## checks that length( @ x) == prod( @ Dim):
	 validity = function(object) .Call(dgeMatrix_validate, object))
## i.e. "dgeMatrix" cannot be packed, but "ddenseMatrix" can ..

## numeric, dense, non-packed, triangular matrices
setClass("dtrMatrix",
	 contains = c("ddenseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(dense_nonpacked_validate, object))

## numeric, dense, packed, triangular matrices
setClass("dtpMatrix",
	 contains = c("ddenseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(dtpMatrix_validate, object))


## numeric, dense, non-packed symmetric matrices
setClass("dsyMatrix",
         contains = c("ddenseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(dense_nonpacked_validate, object))

## numeric, dense, packed symmetric matrices
setClass("dspMatrix",
	 contains = c("ddenseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(dspMatrix_validate, object))

## numeric, dense, non-packed, positive-definite, symmetric matrices
setClass("dpoMatrix", contains = "dsyMatrix",
	 validity = function(object) .Call(dpoMatrix_validate, object)
	 )

## numeric, dense, packed, positive-definite, symmetric matrices
setClass("dppMatrix", contains = "dspMatrix",
	 validity = function(object) .Call(dppMatrix_validate, object)

)
##----- logical dense Matrices -- e.g. as result of <ddenseMatrix>  COMPARISON

## logical, dense, general matrices
setClass("lgeMatrix", contains = c("ldenseMatrix", "generalMatrix"),
         ## since "lge" inherits from "ldenseMatrix", only need this:
	 ## checks that length( @ x) == prod( @ Dim):
	 validity = function(object) .Call(dense_nonpacked_validate, object))
## i.e. "lgeMatrix" cannot be packed, but "ldenseMatrix" can ..

## logical, dense, non-packed, triangular matrices
setClass("ltrMatrix",
	 validity = function(object) .Call(dense_nonpacked_validate, object),
	 contains = c("ldenseMatrix", "triangularMatrix"))

## logical, dense, packed, triangular matrices
setClass("ltpMatrix",
	 contains = c("ldenseMatrix", "triangularMatrix"))

## logical, dense, non-packed symmetric matrices
setClass("lsyMatrix",
	 validity = function(object) .Call(dense_nonpacked_validate, object),
	 contains = c("ldenseMatrix", "symmetricMatrix"))

## logical, dense, packed symmetric matrices
setClass("lspMatrix",
	 contains = c("ldenseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(dspMatrix_validate, object)
	 ## "dsp", "lsp" and "nsp" have the same validate
	 )

##----- nonzero pattern dense Matrices -- "for completeness"

## logical, dense, general matrices
setClass("ngeMatrix", contains = c("ndenseMatrix", "generalMatrix"),
	 validity = function(object) .Call(dense_nonpacked_validate, object))
## i.e. "ngeMatrix" cannot be packed, but "ndenseMatrix" can ..

## logical, dense, non-packed, triangular matrices
setClass("ntrMatrix",
	 validity = function(object) .Call(dense_nonpacked_validate, object),
	 contains = c("ndenseMatrix", "triangularMatrix"))

## logical, dense, packed, triangular matrices
setClass("ntpMatrix",
	 contains = c("ndenseMatrix", "triangularMatrix"))

## logical, dense, non-packed symmetric matrices
setClass("nsyMatrix",
	 validity = function(object) .Call(dense_nonpacked_validate, object),
	 contains = c("ndenseMatrix", "symmetricMatrix"))

## logical, dense, packed symmetric matrices
setClass("nspMatrix",
	 contains = c("ndenseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(dspMatrix_validate, object)
	 ## "dsp", "lsp" and "nsp" have the same validate
	 )


## 'diagonalMatrix' already has validity checking
## diagonal, numeric matrices; "dMatrix" has 'x' slot :
setClass("ddiMatrix", contains = c("diagonalMatrix", "dMatrix"))# or "dMatrix"
## diagonal, logical matrices; "lMatrix" has 'x' slot :
setClass("ldiMatrix", contains = c("diagonalMatrix", "lMatrix"))

setClass("corMatrix", representation(sd = "numeric"), contains = "dpoMatrix",
	 validity = function(object) {
	     ## assuming that 'dpoMatrix' validity check has already happened:
	     n <- object@Dim[2]
	     if(length(sd <- object@sd) != n)
		 return("'sd' slot must be of length 'dim(.)[1]'")
	     if(any(!is.finite(sd)))# including NA
		 return("'sd' slot has non-finite entries")
	     if(any(sd < 0))
		 return("'sd' slot has negative entries")
	     TRUE
	 })


##-------------------- S P A R S E (non-virtual) --------------------------

##---------- numeric sparse matrix classes --------------------------------

## numeric, sparse, triplet general matrices
setClass("dgTMatrix",
	 contains = c("TsparseMatrix", "dsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xTMatrix_validate, object)
	 )

## Should not have dtTMatrix inherit from dgTMatrix because a dtTMatrix could
## be less than fully stored if diag = "U".  Methods for the dgTMatrix
## class would not produce correct results even though all the slots
## are present.

## numeric, sparse, triplet triangular matrices
setClass("dtTMatrix",
	 contains = c("TsparseMatrix", "dsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## numeric, sparse, triplet symmetric matrices(also only store one triangle)
setClass("dsTMatrix",
	 contains = c("TsparseMatrix", "dsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## numeric, sparse, sorted compressed sparse column-oriented general matrices
setClass("dgCMatrix",
	 contains = c("CsparseMatrix", "dsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## special case: indicator rows for a factor - needs more careful definition
#setClass("indicators", representation(levels = "character"),
#	 contains = "dgCMatrix")

## see comments for dtTMatrix above
## numeric, sparse, sorted compressed sparse column-oriented triangular matrices
setClass("dtCMatrix",
	 contains = c("CsparseMatrix", "dsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tCMatrix_validate, object)
	 )

## see comments for dsTMatrix above
## numeric, sparse, sorted compressed sparse column-oriented symmetric matrices
setClass("dsCMatrix",
	 contains = c("CsparseMatrix", "dsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tCMatrix_validate, object)
	 )

if(FALSE) ## TODO ??? Class of positive definite (Csparse symmetric) Matrices:
setClass("dpCMatrix", contains = "dsCMatrix",
	 validity = function(object) TODO("test for pos.definite ??"))

## numeric, sparse, sorted compressed sparse row-oriented general matrices
setClass("dgRMatrix",
	 contains = c("RsparseMatrix", "dsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xRMatrix_validate, object)
	 )

## numeric, sparse, sorted compressed sparse row-oriented triangular matrices
setClass("dtRMatrix",
	 contains = c("RsparseMatrix", "dsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )

## numeric, sparse, sorted compressed sparse row-oriented symmetric matrices
setClass("dsRMatrix",
	 contains = c("RsparseMatrix", "dsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )

##---------- logical sparse matrix classes --------------------------------

## these classes are typically result of Matrix comparisons, e.g.,
##   <..Matrix>  >= v     (and hence can have NA's)

## logical, sparse, triplet general matrices
setClass("lgTMatrix",
	 contains = c("TsparseMatrix", "lsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xTMatrix_validate, object)
	 )

## logical, sparse, triplet triangular matrices
setClass("ltTMatrix",
	 contains = c("TsparseMatrix", "lsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## logical, sparse, triplet symmetric matrices
setClass("lsTMatrix",
	 contains = c("TsparseMatrix", "lsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse column-oriented general matrices
setClass("lgCMatrix",
	 contains = c("CsparseMatrix", "lsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse column-oriented triangular matrices
setClass("ltCMatrix",
	 contains = c("CsparseMatrix", "lsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse column-oriented symmetric matrices
setClass("lsCMatrix",
	 contains = c("CsparseMatrix", "lsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse row-oriented general matrices
setClass("lgRMatrix",
	 contains = c("RsparseMatrix", "lsparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xRMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse row-oriented triangular matrices
setClass("ltRMatrix",
	 contains = c("RsparseMatrix", "lsparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )

## logical, sparse, sorted compressed sparse row-oriented symmetric matrices
setClass("lsRMatrix",
	 contains = c("RsparseMatrix", "lsparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )

##---------- nonzero pattern sparse matrix classes ---------------------------

## these classes are used in symbolic analysis to determine the
## locations of non-zero entries

## nonzero pattern, sparse, triplet general matrices
setClass("ngTMatrix",
	 contains = c("TsparseMatrix", "nsparseMatrix", "generalMatrix")
         ## validity: Tsparse_validate should be enough
	 )

## nonzero pattern, sparse, triplet triangular matrices
setClass("ntTMatrix",
	 contains = c("TsparseMatrix", "nsparseMatrix", "triangularMatrix"),
         ## validity: Tsparse_ and triangular*_validate should be enough
	 )

## nonzero pattern, sparse, triplet symmetric matrices
setClass("nsTMatrix",
	 contains = c("TsparseMatrix", "nsparseMatrix", "symmetricMatrix"),
         ## validity: Tsparse_ and symmetric*_validate should be enough
	 )

## nonzero pattern, sparse, sorted compressed column-oriented matrices
setClass("ngCMatrix",
	 contains = c("CsparseMatrix", "nsparseMatrix", "generalMatrix"),
         ## validity: Csparse_validate should be enough
	 )

setClass("ngCMatrix",
	 contains = c("CsparseMatrix", "nsparseMatrix", "generalMatrix"),
         ## validity: Csparse_validate should be enough
	 )

## nonzero pattern, sparse, sorted compressed column-oriented triangular matrices
setClass("ntCMatrix",
	 contains = c("CsparseMatrix", "nsparseMatrix", "triangularMatrix"),
         ## validity: Csparse_ and triangular*_validate should be enough
	 )

## nonzero pattern, sparse, sorted compressed column-oriented symmetric matrices
setClass("nsCMatrix",
	 contains = c("CsparseMatrix", "nsparseMatrix", "symmetricMatrix"),
         ## validity: Csparse_ and symmetric*_validate should be enough
	 )

## nonzero pattern, sparse, sorted compressed row-oriented general matrices
setClass("ngRMatrix",
	 contains = c("RsparseMatrix", "nsparseMatrix", "generalMatrix"),
	 )

## nonzero pattern, sparse, sorted compressed row-oriented triangular matrices
setClass("ntRMatrix",
	 contains = c("RsparseMatrix", "nsparseMatrix", "triangularMatrix"),
	 )

## nonzero pattern, sparse, sorted compressed row-oriented symmetric matrices
setClass("nsRMatrix",
	 contains = c("RsparseMatrix", "nsparseMatrix", "symmetricMatrix"),
	 )

if(FALSE) { ##--not yet--

##---------- integer sparse matrix classes --------------------------------

## integer, sparse, triplet general matrices
setClass("igTMatrix",
	 contains = c("TsparseMatrix", "isparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xTMatrix_validate, object)
	 )

## integer, sparse, triplet triangular matrices
setClass("itTMatrix",
	 contains = c("TsparseMatrix", "isparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## integer, sparse, triplet symmetric matrices
setClass("isTMatrix",
	 contains = c("TsparseMatrix", "isparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tTMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse column-oriented general matrices
setClass("igCMatrix",
	 contains = c("CsparseMatrix", "isparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse column-oriented triangular matrices
setClass("itCMatrix",
	 contains = c("CsparseMatrix", "isparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse column-oriented symmetric matrices
setClass("isCMatrix",
	 contains = c("CsparseMatrix", "isparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(xCMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse row-oriented general matrices
setClass("igRMatrix",
	 contains = c("RsparseMatrix", "isparseMatrix", "generalMatrix"),
	 validity = function(object) .Call(xRMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse row-oriented triangular matrices
setClass("itRMatrix",
	 contains = c("RsparseMatrix", "isparseMatrix", "triangularMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )

## integer, sparse, sorted compressed sparse row-oriented symmetric matrices
setClass("isRMatrix",
	 contains = c("RsparseMatrix", "isparseMatrix", "symmetricMatrix"),
	 validity = function(object) .Call(tRMatrix_validate, object)
	 )
}##--not yet--

##-------------------- index and permutation matrices--------------------------

setClass("indMatrix", representation(perm = "integer"),
	 contains = c("sparseMatrix", "generalMatrix"),
	 validity = function(object) {
	     n <- object@Dim[1]
	     d <- object@Dim[2]
	     perm <- object@perm
	     if (length(perm) != n)
		 return(paste("length of 'perm' slot must be", n))
	     if(n > 0 && (any(perm > d) || any(perm < 1)))
		 return("'perm' slot is not a valid index")
	     TRUE
	 })

setClass("pMatrix", representation(perm = "integer"),
	 contains = c("indMatrix"),
	 validity = function(object) {
	     d <- object@Dim
	     if (d[2] != (n <- d[1])) return("pMatrix must be square")
	     perm <- object@perm
	     if (length(perm) != n)
		 return(paste("length of 'perm' slot must be", n))
	     if(n > 0 &&
		!(all(range(perm) == c(1, n)) && length(unique(perm)) == n))
		 return("'perm' slot is not a valid permutation")
	     TRUE
	 })


### Factorization classes ---------------------------------------------

## Mother class:
setClass("MatrixFactorization", representation(Dim = "integer", "VIRTUAL"),
	 validity = function(object) .Call(MatrixFactorization_validate, object))

setClass("CholeskyFactorization", representation = "VIRTUAL",
         contains = "MatrixFactorization")

## -- Those (exceptions) inheriting from "Matrix" : ---

setClass("Cholesky",  contains = c("dtrMatrix", "CholeskyFactorization"))

#unUsed: setClass("LDL", contains = c("dtrMatrix", "CholeskyFactorization"))

setClass("pCholesky", contains = c("dtpMatrix", "CholeskyFactorization"))

## These are currently only produced implicitly from *solve()
setClass("BunchKaufman",
	 contains = c("dtrMatrix", "MatrixFactorization"),
	 representation(perm = "integer"),
	 validity = function(object) .Call(BunchKaufman_validate, object))

setClass("pBunchKaufman",
	 contains = c("dtpMatrix", "MatrixFactorization"),
	 representation(perm = "integer"),
	 validity = function(object) .Call(pBunchKaufman_validate, object))

## -- the usual ``non-Matrix'' factorizations : ---------

setClass("CHMfactor",		 # cholmod_factor struct as S4 object
	 contains = "CholeskyFactorization",
	 representation(colcount = "integer", perm = "integer",
			type = "integer", "VIRTUAL"),
	 validity = function(object) .Call(CHMfactor_validate, object))

setClass("CHMsuper",		       # supernodal cholmod_factor
	 contains = "CHMfactor",
	 representation(super = "integer", pi = "integer", px = "integer",
			s = "integer", "VIRTUAL"),
	 validity = function(object) .Call(CHMsuper_validate, object))

setClass("CHMsimpl",		       # simplicial cholmod_factor
	 contains = "CHMfactor",
	 representation(p = "integer", i = "integer", nz = "integer",
			nxt = "integer", prv = "integer", "VIRTUAL"),
	 validity = function(object) .Call(CHMsimpl_validate, object))

setClass("dCHMsuper", contains = "CHMsuper", representation(x = "numeric"))

setClass("nCHMsuper", contains = "CHMsuper")

setClass("dCHMsimpl", contains = "CHMsimpl", representation(x = "numeric"))

setClass("nCHMsimpl", contains = "CHMsimpl")

##--- LU ---

setClass("LU", contains = "MatrixFactorization", representation("VIRTUAL"))

setClass("denseLU", contains = "LU",
	 representation(x = "numeric", perm = "integer", Dimnames = "list"),
	 validity = function(object) .Call(LU_validate, object))

setClass("sparseLU", contains = "LU",
	 representation(L = "dtCMatrix", U = "dtCMatrix",
			p = "integer", q = "integer"))

##--- QR ---

setClass("sparseQR", contains = "MatrixFactorization",
	 representation(V = "dgCMatrix", beta = "numeric",
			p = "integer", R = "dgCMatrix", q = "integer"),
	 validity = function(object) .Call(sparseQR_validate, object))

##-- "SPQR" ---> ./spqr.R  for now

## "denseQR" -- ?  (``a version of''  S3 class "qr")

if (FALSE) { ## unused classes
setClass("csn_QR", representation(U = "dgCMatrix", L = "dgCMatrix",
                                  beta = "numeric"))

setClass("csn_LU", representation(U = "dgCMatrix", L = "dgCMatrix",
                                  Pinv = "integer"))

setClass("css_QR", representation(Pinv = "integer", Q = "integer",
                                  parent = "integer", cp = "integer",
                                  nz = "integer"))

setClass("css_LU", representation(Q = "integer", nz = "integer"))
}

##-- Schur ---

## non-"Matrix" Class 1  --- For Eigen values:
setClassUnion("number", members = c("numeric", "complex"))

setClass("Schur", contains = "MatrixFactorization",
	 representation(T = "Matrix", # <- "block-triangular"; maybe triangular
			Q = "Matrix", EValues = "number"),
	 validity = function(object) {
	     dim <- object@Dim
	     if((n <- dim[1]) != dim[2])
		 "'Dim' slot is not (n,n)"
	     else if(any(dim(object@T) != n))
		 "'dim(T)' is incorrect"
	     else if(any(dim(object@Q) != n))
		 "'dim(Q)' is incorrect"
	     else if(length(object@EValues) != n)
		 "'EValues' is not of correct length"
	     else TRUE
	 })


### Class Union :  no inheritance, but is(*, <class>) :

setClassUnion("mMatrix", members = c("matrix", "Matrix"))

## CARE: Sometimes we'd want all those for which 'x' contains all the data.
##       e.g. Diagonal() is "ddiMatrix" with 'x' slot of length 0, does *not* contain 1
setClassUnion("xMatrix", ## those Matrix classes with an 'x' slot
              c("dMatrix",
                "iMatrix",
                "lMatrix",
                "ndenseMatrix",
                "zMatrix"))

if(TRUE) { ##--- variant of setClass("dCsparse..." ..) etc working better for other pkgs -----

setClassUnion("dCsparseMatrix", members = c("dgCMatrix", "dtCMatrix", "dsCMatrix"))
setClassUnion("lCsparseMatrix", members = c("lgCMatrix", "ltCMatrix", "lsCMatrix"))
setClassUnion("nCsparseMatrix", members = c("ngCMatrix", "ntCMatrix", "nsCMatrix"))

## dense general
setClassUnion("geMatrix", members = c("dgeMatrix", "lgeMatrix", "ngeMatrix"))
}



## Definition  Packed := dense with length( . @x) < prod( . @Dim)
##	       ~~~~~~
## REPLACED the following with	isPacked() in ./Auxiliaries.R :
## setClassUnion("packedMatrix",
##		 members = c("dspMatrix", "dppMatrix", "dtpMatrix",
##		  "lspMatrix", "ltpMatrix", "diagonalMatrix"))


## --------------------- non-"Matrix" Classes --------------------------------

## --- "General" (not Matrix at all) ----

## e.g. for "Arith" methods:
setClassUnion("numLike", members = c("numeric", "logical"))

##setClassUnion("numIndex", members = "numeric")

## Note "rle" is a sealed oldClass (and "virtual" as w/o prototype)
setClass("rleDiff", representation(first = "numLike", rle = "rle"),
	 prototype = prototype(first = integer(),
		     ## workaround buglet in R <= 2.10.x: rle = rle(integer())
	 rle = structure(list(lengths = integer(0L), values = 0[0L]), class = "rle")
	 ),
	 validity = function(object) {
	     if(length(object@first) != 1)
		 return("'first' must be of length one")
	     rl <- object@rle
	     if(!is.list(rl) || length(rl) != 2 ||
		!identical(sort(names(rl)), c("lengths", "values")))
		 return("'rle' must be a list (lengths = *, values = *)")
	     if(length(lens <- rl$lengths) != length(vals <- rl$values))
		 return("'lengths' and 'values' differ in length")
	     if(any(lens <= 0))
		 return("'lengths' must be positive")
	     TRUE
	 })

### 2010-03-04 -- thinking about *implementing* some 'abIndex' methodology,
### I conclude that the following structure would probably be even more
### efficient than the "rleDiff" one :
### IDEA: Store subsequences in a numeric matrix of three rows, where
### ----- one column = [from, to, by]  defining a sub seq()ence

## for now, at least use it, and [TODO!] define  "seqMat" <--> "abIndex" coercions:
setClass("seqMat", contains = "matrix",
	 prototype = prototype(matrix(0, nrow = 3, ncol=0)),
	 validity = function(object) {
	     if(!is.numeric(object)) return("is not numeric")
	     d <- dim(object)
	     if(length(d) != 3 || d[1] != 3)
		 return("not a	 3 x n	matrix")
	     if(any(object != floor(object)))
		 return("some entries are not integer valued")
	     TRUE
	 })

setClass("abIndex", # 'ABSTRact Index'
         representation(kind = "character",
                        ## one of ("int32", "double", "rleDiff")
                                        # i.e., numeric or "rleDiff"
                        x = "numLike", # for  numeric [length 0 otherwise]
                        rleD = "rleDiff"),  # "rleDiff" result
         prototype = prototype(kind = "int32", x = integer(0)),# rleD = ... etc
         validity = function(object) {
            switch(object@kind,
                   "int32" = if(!is.integer(object@x))
                   return("'x' slot must be integer when kind is 'int32'")
                   ,
                   "double" = if(!is.double(object@x))
                   return("'x' slot must be double when kind is 'double'")
                   ,
                   "rleDiff" = {
                       if(length(object@x))
                   return("'x' slot must be empty when kind is 'rleDiff'")
                   },
                   ## otherwise
                   return("'kind' must be one of (\"int32\", \"double\", \"rleDiff\")")
                   )
            TRUE
         })

## for 'i' in x[i] or A[i,] : (numeric = {double, integer})
## TODO: allow "abIndex" as well !
setClassUnion("index", members =  c("numeric", "logical", "character"))

## "atomic vectors" (-> ?is.atomic ) -- but note that is.atomic(<matrix>) !
## ---------------  those that we want to convert from old-style "matrix"
setClassUnion("atomicVector", ## "double" is not needed, and not liked by some
	      members = c("logical", "integer", "numeric",
			  "complex", "raw", "character"))

## --- Matrix - related (but not "Matrix" nor "Decomposition/Factorization):

## for 'value' in  x[..] <- value hence for all "contents" of our Matrices:
setClassUnion("replValue", members = c("numeric", "logical", "complex", "raw"))

### Sparse Vectors ---- here use 1-based indexing ! -----------

## 'longindex' should allow sparseVectors of "length" > 2^32,
## which is necessary e.g. when converted from large sparse matrices
## setClass("longindex", contains = "numeric")
## but we use "numeric" instead, for simplicity (efficiency?)
setClass("sparseVector",
         representation(length = "numeric", i = "numeric", "VIRTUAL"),
         ##                     "longindex"    "longindex"
         ## note that "numeric" contains "integer" (if I like it or not..)
	 prototype = prototype(length = 0),
         validity = function(object) {
	     n <- object@length
	     if(anyNA(i <- object@i))	 "'i' slot has NAs"
	     else if(any(!is.finite(i))) "'i' slot is not all finite"
	     else if(any(i < 1))	 "'i' must be >= 1"
	     else if(n == 0 && length(i))"'i' must be empty when the object length is zero"
	     else if(any(i > n)) sprintf("'i' must be in 1:%d", n)
	     else if(is.unsorted(i, strictly=TRUE))
		 "'i' must be sorted strictly increasingly"
             else TRUE
         })

##' initialization -- ensuring that  'i' is sorted (and 'x' alongside)
if(getRversion() >= "3.2.0") {
setMethod("initialize", "sparseVector", function(.Object, i, x, ...)
      {
	  has.x <- !missing(x)
	  if(!missing(i)) {
	      i <- ## (be careful to assign in all cases)
		  if(is.unsorted(i, strictly=TRUE)) {
		      if(is(.Object, "xsparseVector") && has.x) {
			  si <- sort.int(i, index.return=TRUE)
			  x <- x[si$ix]
			  si$x
		      }
		      else
			  sort.int(i, method = "quick")
		  }
		  else i
	  }
	  if(has.x) x <- x
	  callNextMethod()
      })
} else { ## R < 3.2.0
setMethod("initialize", "sparseVector", function(.Object, i, x, ...)
      {
	  has.x <- !missing(x)
	  if(!missing(i)) {
	      .Object@i <- ## (be careful to assign in all cases)
		  if(is.unsorted(i, strictly=TRUE)) {
		      if(is(.Object, "xsparseVector") && has.x) {
			  si <- sort.int(i, index.return=TRUE)
			  x <- x[si$ix]
			  si$x
		      }
		      else
			  sort.int(i, method = "quick")
		  }
		  else i
	  }
	  if(has.x) .Object@x <- x
	  callNextMethod(.Object, ...)
      })
}

.validXspVec <- function(object) {
    ## n <- object@length
    if(length(object@i) != length(object@x))
        "'i' and 'x' differ in length"
    else TRUE
}
setClass("dsparseVector",
	 representation(x = "numeric"), contains = "sparseVector",
	 validity = .validXspVec)
setClass("isparseVector",
	 representation(x = "integer"), contains = "sparseVector",
	 validity = .validXspVec)
setClass("lsparseVector",
	 representation(x = "logical"), contains = "sparseVector",
	 validity = .validXspVec)
setClass("zsparseVector",
	 representation(x = "complex"), contains = "sparseVector",
	 validity = .validXspVec)
## nsparse has no new slot: 'i' just contains the locations!
setClass("nsparseVector", contains = "sparseVector")

setClassUnion("xsparseVector", ## those sparseVector's with an 'x' slot
              c("dsparseVector",
                "isparseVector",
                "lsparseVector",
                "zsparseVector"))


setClass("determinant",
	 representation(modulus = "numeric",
			logarithm = "logical",
			sign = "integer",
			call = "call"))
