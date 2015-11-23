### Define Methods that can be inherited for all subclasses

## This replaces many "d..Matrix" -> "dgeMatrix" ones
## >> but << needs all sub(sub(sub)) classes of "ddenseMatrix" listed
##   -----  in  ../src/Mutils.c

setAs("ddenseMatrix", "dgeMatrix", ..2dge)

setAs("ddenseMatrix", "matrix",
      function(from) as(..2dge(from), "matrix"))

## d(ouble) to l(ogical):
setAs("dgeMatrix", "lgeMatrix", function(from) d2l_Matrix(from, "dgeMatrix"))
setAs("dsyMatrix", "lsyMatrix", function(from) d2l_Matrix(from, "dsyMatrix"))
setAs("dspMatrix", "lspMatrix", function(from) d2l_Matrix(from, "dspMatrix"))
setAs("dtrMatrix", "ltrMatrix", function(from) d2l_Matrix(from, "dtrMatrix"))
setAs("dtpMatrix", "ltpMatrix", function(from) d2l_Matrix(from, "dtpMatrix"))

if(FALSE) ## FIXME, this fails for ("dtpMatrix" -> "CsparseMatrix") where .dense2C() works
setAs("ddenseMatrix", "CsparseMatrix",
      function(from) {
	  if (class(from) != "dgeMatrix") # don't lose symmetry/triangularity/...
	      as_Csparse(from)
	  else .Call(dense_to_Csparse, from)
      })

## special case
setAs("dgeMatrix", "dgCMatrix",
      function(from) .Call(dense_to_Csparse, from))

setAs("matrix", "CsparseMatrix",
      function(from) .Call(dense_to_Csparse, from))

## for historical i.e. backcompatibility reasons ..
setAs("matrix", "dgCMatrix",
      function(from) {
          storage.mode(from) <- "double"
          .Call(dense_to_Csparse, from)
      })

setAs("numeric", "CsparseMatrix",
      function(from)
      .Call(dense_to_Csparse, .Call(dup_mMatrix_as_dgeMatrix, from)))

setMethod("as.numeric", "ddenseMatrix", function(x, ...) ..2dge(x)@x)

## -- see also ./Matrix.R  e.g., for a show() method

## These methods are the 'fallback' methods for all dense numeric
## matrices in that they simply coerce the ddenseMatrix to a
## dgeMatrix. Methods for special forms override these.

setMethod("norm", signature(x = "ddenseMatrix", type = "missing"),
	  function(x, type, ...) norm(..2dge(x)))

setMethod("norm", signature(x = "ddenseMatrix", type = "character"),
	  function(x, type, ...) norm(..2dge(x), type))

setMethod("rcond", signature(x = "ddenseMatrix", norm = "missing"),
	  function(x, norm, ...) rcond(..2dge(x), ...))

setMethod("rcond", signature(x = "ddenseMatrix", norm = "character"),
	  function(x, norm, ...) rcond(..2dge(x), norm, ...))

## Not really useful; now require *identical* class for result:
## setMethod("t", signature(x = "ddenseMatrix"),
## 	  function(x) callGeneric(..2dge(x)))

## "diag" --> specific methods for dge, dtr,dtp, dsy,dsp

setMethod("solve", signature(a = "ddenseMatrix", b = "missing"),
          function(a, b, ...) solve(..2dge(a)))

for(.b in c("Matrix","ANY")) ## << against ambiguity notes
setMethod("solve", signature(a = "ddenseMatrix", b = .b),
	  function(a, b, ...) solve(..2dge(a), b))
for(.b in c("matrix","numeric")) ## << against ambiguity notes
setMethod("solve", signature(a = "ddenseMatrix", b = .b),
	  function(a, b, ...) solve(..2dge(a), Matrix(b)))
rm(.b)

setMethod("lu", signature(x = "ddenseMatrix"),
	  function(x, ...)
	  .set.factors(x, "LU", lu(..2dge(x), ...)))

setMethod("chol", signature(x = "ddenseMatrix"), cholMat)

setMethod("determinant", signature(x = "ddenseMatrix", logarithm = "missing"),
	  function(x, logarithm, ...) determinant(..2dge(x)))

setMethod("determinant", signature(x = "ddenseMatrix", logarithm = "logical"),
	  function(x, logarithm, ...)
	  determinant(..2dge(x), logarithm))

## now done for "dMatrix":
## setMethod("expm", signature(x = "ddenseMatrix"),
##           function(x) callGeneric(..2dge(x)))


.trilDense <- function(x, k = 0, ...) {
    k <- as.integer(k[1])
    d <- dim(x)
    stopifnot(-d[1] <= k, k <= d[1]) # had k <= 0
    ## returns "lower triangular" if k <= 0 && sqr
    .Call(dense_band, x, -d[1], k)
}
## NB: have extra tril(), triu() methods for symmetric ["dsy" and "dsp"] and
##     for triangular ["dtr" and "dtp"]
setMethod("tril", "denseMatrix", .trilDense)
setMethod("tril",      "matrix", .trilDense)

.triuDense <- function(x, k = 0, ...) {
    k <- as.integer(k[1])
    d <- dim(x)
    stopifnot(-d[1] <= k, k <= d[1]) # had k >= 0
    ## returns "upper triangular" if k >= 0
    .Call(dense_band, x, k, d[2])
}
setMethod("triu", "denseMatrix", .triuDense)
setMethod("triu",      "matrix", .triuDense)

.bandDense <- function(x, k1, k2, ...) {
    k1 <- as.integer(k1[1])
    k2 <- as.integer(k2[1])
    dd <- dim(x)
    sqr <- dd[1] == dd[2]
    stopifnot(-dd[1] <= k1, k1 <= k2, k2 <= dd[2])
    r <- .Call(dense_band, x, k1, k2)
    if (sqr &&  k1 < 0 &&  k1 == -k2  && isSymmetric(x)) ## symmetric
	forceSymmetric(r)
    else
	r
}
setMethod("band", "denseMatrix", .bandDense)
setMethod("band",      "matrix", .bandDense)


setMethod("symmpart", signature(x = "ddenseMatrix"),
	  function(x) .Call(ddense_symmpart, x))
setMethod("skewpart", signature(x = "ddenseMatrix"),
	  function(x) .Call(ddense_skewpart, x))


setMethod("is.finite", signature(x = "dgeMatrix"),
	  function(x) {
	      if(all(ifin <- is.finite(x@x)))
		  allTrueMat(x)
	      else if(any(ifin)) {
		  r <- as(x, "lMatrix") #-> logical x-slot
		  r@x <- ifin
		  as(r, "nMatrix")
	      }
	      else is.na_nsp(x)
	  })

## TODO? -- rather methods for specific subclasses of ddenseMatrix
setMethod("is.finite", signature(x = "ddenseMatrix"),
	  function(x) {
	      if(all(ifin <- is.finite(x@x))) return(allTrueMat(x))
	      ## *NOT* dge, i.e., either triangular or symmetric
	      ## (possibly packed): has finite 0-triangle
	      cdx <- getClassDef(class(x))

	      r <- new(if(extends(cdx,"symmetricMatrix"))"nsyMatrix" else "ngeMatrix")
	      r@Dim <- (d <- x@Dim)
	      r@Dimnames <- x@Dimnames
	      isPacked <- (le <- prod(d)) > length(ifin)
	      r@x <- rep.int(TRUE, le)
	      iTr <- indTri(d[1], upper= x@uplo == "U", diag= TRUE)
	      if(isPacked) { ## x@x is "usable"
		  r@x[iTr] <- ifin
	      } else {
		  r@x[iTr] <- ifin[iTr]
	      }
	      r
	  })

setMethod("is.infinite", signature(x = "ddenseMatrix"),
	  function(x) {
	      if(any((isInf <- is.infinite(x@x)))) {
		  r <- as(x, "lMatrix")#-> logical x-slot; 0 |--> FALSE
		  r@x <- isInf
		  as(r, "nMatrix")# often sparse .. better way?
	      }
	      else is.na_nsp(x)
	  })
