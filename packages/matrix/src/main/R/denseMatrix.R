### Simple fallback methods for all dense matrices
### These are "cheap" to program, but potentially far from efficient;
### Methods for specific subclasses will overwrite these:

setAs("ANY",    "denseMatrix", function(from) Matrix(from, sparse=FALSE))
## Conceivably, could write
## setAs("matrix", "denseMatrix", ....) which was slightly more efficient than
##  Matrix(.)  but would have many things in common

setAs("denseMatrix", "generalMatrix", as_geSimpl)

## dense to sparse:
## : if we do this, do it "right", i.e. preserve symmetric/triangular!
## setAs("denseMatrix", "dsparseMatrix",
## ## MM thought that  as() will take the ``closest'' match; but that fails!
## ##      function(from) as(as(from, "dgeMatrix"), "dsparseMatrix"))
##       function(from) as(as(from, "dgeMatrix"), "dgCMatrix"))

.dense2C <- function(from, kind = NA, uplo = "U") {
    useK <- is.character(kind) && length(kind) == 1 &&
        kind %in% c("gen", "sym", "tri")
    if(!useK) {
        cl <- class(from)
        cld <- getClassDef(cl) ## get it once (speedup)
    }
    r <- .Call(dense_to_Csparse, from)# goes via "generalMatrix"
    ## FIXME: for symmetric / triangular matrices, this is a waste, notably if packed
    if (useK && kind == "gen"  ||  !useK && extends(cld, "generalMatrix"))
	r
    else if(useK && kind == "sym" || !useK && extends(cld, "symmetricMatrix"))
	forceCspSymmetric(r, uplo, isTri = FALSE)
    else if(!useK && extends(cld, "diagonalMatrix"))
	stop("diagonalMatrix in .dense2C() -- should never happen, please report!")
    else { ## we have "triangular" :
        if(useK) {
            cl <- class(from)
            cld <- getClassDef(cl) ## get it once (speedup)
        }
	if	(extends(cld,"dMatrix")) as(r, "dtCMatrix")
        else if (extends(cld,"lMatrix")) as(r, "ltCMatrix")
        else if (extends(cld,"nMatrix")) as(r, "ntCMatrix")
        else if (extends(cld,"zMatrix")) as(r, "ztCMatrix")
	else stop(gettextf("undefined method for class %s", dQuote(cl)), domain=NA)
    }
}

setAs("denseMatrix", "CsparseMatrix", function(from) .dense2C(from))

## This sometimes fails (eg. for "lsyMatrix"), and we really want to
## use the generic ``go via Csparse'' (top of ./sparseMatrix.R) instead
## setAs("denseMatrix",  "sparseMatrix",
##       function(from) {
## 	  cl <- class(from)
## 	  cld <- getClassDef(cl)
## 	  if (extends(cld, "generalMatrix"))
## 	      .Call(dense_to_Csparse, from)
## 	  else ## i.e. triangular | symmetric
## 	      as_Csparse(from, cld)
##       })

setAs("denseMatrix", "TsparseMatrix",
      function(from) as(.dense2C(from), "TsparseMatrix"))


setMethod("show", signature(object = "denseMatrix"),
          function(object) prMatrix(object))
##- ## FIXME: The following is only for the "dMatrix" objects that are not
##- ##	      "dense" nor "sparse" -- i.e. "packed" ones :
##- ## But these could be printed better -- "." for structural zeros.
##- setMethod("show", signature(object = "dMatrix"), prMatrix)
##- ## and improve this as well:
##- setMethod("show", signature(object = "pMatrix"), prMatrix)
##- ## this should now be superfluous [keep for safety for the moment]:

setMethod("dim<-", signature(x = "denseMatrix", value = "ANY"),
	  function(x, value) {
	      if(!is.numeric(value) || length(value) != 2)
		  stop("dim(.) value must be numeric of length 2")
	      if(prod(dim(x)) != prod(value <- as.integer(value)))
		  stop("dimensions don't match the number of cells")
	      clx <- as.character(MatrixClass(class(x))) # as.*(): drop attr
	      if(substring(clx,2) == "geMatrix") {
		  x@Dim <- value
		  if(length(x@factors) > 0)
		      x@factors <- list()
		  x
	      } else { ## other "denseMatrix"
		  x <- as_geSimpl2(x, clx)
		  dim(x) <- value
                  x
	      }
          })



## Using "index" for indices should allow
## integer (numeric), logical, or character (names!) indices :

## use geClass() when 'i' or 'j' are missing:
## since  symmetric, triangular, .. will not be preserved anyway:
setMethod("[", signature(x = "denseMatrix", i = "index", j = "missing",
			 drop = "logical"),
	  function (x, i, j, ..., drop) {
	      if((na <- nargs()) == 3)
		  r <- as(x, "matrix")[i, drop=drop]
	      else if(na == 4)
		  r <- as(x, "matrix")[i, , drop=drop]
	      else stop(gettextf("invalid nargs()= %d", na), domain=NA)
	      if(is.null(dim(r))) r else as(r, geClass(x))
	  })

setMethod("[", signature(x = "denseMatrix", i = "missing", j = "index",
			 drop = "logical"),
	  function (x, i, j, ..., drop) {
	      r <- as(x, "matrix")[, j, drop=drop]
	      if(is.null(dim(r))) r else as(r, geClass(x))
	  })

setMethod("[", signature(x = "denseMatrix", i = "index", j = "index",
			 drop = "logical"),
	  function (x, i, j, ..., drop) {
	      r <- callGeneric(x = as(x, "matrix"), i=i, j=j, drop=drop)
	      if(is.null(dim(r)))
		  r
	      else {
		  cld <- getClassDef(cl <- class(x))
		  if(extends(cld, "symmetricMatrix") &&
		     length(i) == length(j) && isTRUE(all(i == j)))
                      ## keep original symmetric class (but not "dpo")
                      as(r, class2(cl, .M.kindC(cld)))

		  else as_smartClass(r, cl)
	      }
	  })

.dense.sub.i.2col <- function(x, i, j, ..., drop) {
    r <- as(x, "matrix")[ i ]
    if(is.null(dim(r))) r else as(r, geClass(x))
}
setMethod("[", signature(x = "denseMatrix", i = "matrix", j = "missing"),#drop="ANY"
	  .dense.sub.i.2col)
setMethod("[", signature(x = "denseMatrix", i = "matrix", j = "missing", drop="missing"),
	  .dense.sub.i.2col)


## Now the "[<-" ones --- see also those in ./Matrix.R
## It's recommended to use setReplaceMethod() rather than setMethod("[<-",.)
## even though the former is currently just a wrapper for the latter

## x[] <- value :
setReplaceMethod("[", signature(x = "denseMatrix", i = "missing", j = "missing",
				value = "ANY"),## double/logical/...
	  function (x, value) {
	      x <- as(x, "generalMatrix")
	      x@x[] <- value
	      validObject(x)# check if type and lengths above match
	      x
	  })

## FIXME: 1) These are far from efficient
## -----
setReplaceMethod("[", signature(x = "denseMatrix", i = "index", j = "missing",
				value = "replValue"),
		 function (x, i, j, ..., value) {
		     r <- as(x, "matrix")
## 		     message("`[<-` with nargs()= ",nargs())
		     if((na <- nargs()) == 3)
			 r[i] <- value
		     else if(na == 4)
			 r[i, ] <- value
		     else stop(gettextf("invalid nargs()= %d", na), domain=NA)
		     as(r, geClass(x))
		 })

setReplaceMethod("[", signature(x = "denseMatrix", i = "missing", j = "index",
				value = "replValue"),
		 function (x, i, j, ..., value) {
		     r <- as(x, "matrix")
		     r[, j] <- value
		     as(r, geClass(x))
		 })

setReplaceMethod("[", signature(x = "denseMatrix", i = "index", j = "index",
				value = "replValue"),
		 function (x, i, j, ..., value) {
		     r <- as(x, "matrix")
		     r[i, j] <- value
		     as_smartClass(r, class(x)) ## was as(r, class(x))
		 })

setReplaceMethod("[", signature(x = "denseMatrix", i = "matrix",  # 2-col.matrix
				j = "missing", value = "replValue"),
		 function(x, i, j, ..., value) {
		     r <- as(x, "matrix")
		     r[ i ] <- value
		     as(r, geClass(x))
		 })


setMethod("isSymmetric", signature(object = "denseMatrix"),
	  function(object, tol = 100*.Machine$double.eps, ...) {
	      ## pretest: is it square?
	      d <- dim(object)
	      if(d[1] != d[2]) return(FALSE)
	      ## else slower test
	      if (is(object,"dMatrix"))
		  isTRUE(all.equal(as(object, "dgeMatrix"),
				   as(t(object), "dgeMatrix"),
				   tolerance = tol, ...))
	      else if (is(object, "nMatrix"))
		  identical(as(object, "ngeMatrix"),
			    as(t(object), "ngeMatrix"))
	      else if (is(object, "lMatrix"))# not possible currently
		  ## test for exact equality; FIXME(?): identical() too strict?
		  identical(as(object, "lgeMatrix"),
			    as(t(object), "lgeMatrix"))
	      else if (is(object, "zMatrix")) ## will error out here
		  identical(as(object, "zgeMatrix"),
			    as(t(object), "zgeMatrix"))
	      else if (is(object, "iMatrix")) ## will error out here
		  identical(as(object, "igeMatrix"),
			    as(t(object), "igeMatrix"))
	  })

## rather methods in ./triangularMatrix.R
## setMethod("isTriangular", signature(object = "triangularMatrix"),
## 	  function(object, ...) TRUE)

setMethod("isTriangular", signature(object = "denseMatrix"), isTriMat)

setMethod("isDiagonal", signature(object = "denseMatrix"), .is.diagonal)

setMethod("rcond", signature(x = "denseMatrix", norm = "character"),
	  function(x, norm, ...)
	  rcond(as(as(x, "dMatrix"), "dgeMatrix"), norm=norm, ...))

setMethod("symmpart", signature(x = "denseMatrix"),
	  function(x) symmpart(as(x, "dMatrix")))
setMethod("skewpart", signature(x = "denseMatrix"),
	  function(x) skewpart(as(x, "dMatrix")))

setMethod("is.na", signature(x = "denseMatrix"),
	  function(x) {
	      if(any((inax <- is.na(x@x)))) {
		  r <- as(x, "lMatrix")#-> logical x-slot
		  r@x <- inax
		  as(r, "nMatrix")
	      } else {
		  d <- x@Dim
		  new("ngCMatrix", Dim = d, Dimnames = dimnames(x),
		      i = integer(0), p = rep.int(0L, d[2]+1L))
	      }
	  })
