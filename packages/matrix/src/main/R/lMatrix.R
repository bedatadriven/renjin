setAs("matrix", "lMatrix",
      function(from) { storage.mode(from) <- "logical" ; Matrix(from) })

## NOTE: This is *VERY* parallel to  ("dMatrix" -> "nMatrix") in ./dMatrix.R :
setAs("lMatrix", "nMatrix",
      function(from) {
	  if(anyNA(from@x) && ((.w <- isTRUE(getOption("Matrix.warn"))) ||
				   isTRUE(getOption("Matrix.verbose")))) {
	      (if(.w) warning else message)(
		  "\"lMatrix\" object with NAs coerced to \"nMatrix\":  NA |-> TRUE")
	      from@x[is.na(from@x)] <- TRUE
	  }
	  ## ==> from@x are in {TRUE, FALSE}
	  cld <- getClassDef(cl <- MatrixClass(class(from)))
	  if(extends(cld, "diagonalMatrix")) # no "ndi*" class
	      ## should not happen, setAs(diagonalMatrix -> nMatrix) in ./diagMatrix.R:
	      return(di2nMat(from))
	  ## else
	  isSp <- extends(cld, "sparseMatrix")
	  if(isSp && !all(from@x)) {
	      from <- drop0(from) # was drop0(from, cld)
	      if(cl != (c. <- class(from)))
		  cld <- getClassDef(cl <- c.)
	  }
	  sNams <- slotNames(cld)
	  copyClass(from, sub("^l", "n", cl),
		    if(isSp) sNams[sNams != "x"] else sNams)
      })

## and the reverse as well :

setAs("nMatrix", "lMatrix",
      function(from) {
	  cld <- getClassDef(cl <- MatrixClass(class(from)))
	  r <- copyClass(from, sub("^n", "l", cl), slotNames(cld))
	  if(extends(cld, "sparseMatrix"))
	      r@x <- rep.int(TRUE, length(if(!extends(cld, "RsparseMatrix"))
					  from@i else from@j))
	  r
      })

setAs("dMatrix", "lMatrix",
      function(from) {
	  cld <- getClassDef(newCl <- class2(class(from), "l"))
	  sNams <- slotNames(cld)
	  r <- copyClass(from, newCl, sNames = sNams[sNams != "x"])
	  r@x <- as.logical(from@x)
	  r
      })

setAs("lMatrix", "dMatrix",
      function(from) {
	  cld <- getClassDef(cl <- MatrixClass(class(from)))
	  sNams <- slotNames(cld)
	  r <- copyClass(from, newCl = sub("^l", "d", cl),
			 sNames = sNams[sNams != "x"])
	  r@x <- as.double(from@x)
	  r
      })

## needed at least for lsparse* :
setAs("lMatrix", "dgCMatrix",
      function(from) as(as(from, "lgCMatrix"), "dgCMatrix"))

###-------------- which( <logical Matrix> ) -----------------------------------------------------

## "ldi: is both "sparseMatrix" and "lMatrix" but not "lsparseMatrix"
setMethod("which", "ldiMatrix",
	  function(x, arr.ind) {
	      n <- x@Dim[1L]
	      i <- if(x@diag == "U") seq_len(n) else which(x@x)
	      if(arr.ind) cbind(i,i, deparse.level = 0) else i + n*(i - 1L) })

## FIXME?:  not 100% compatible to "base :: which" -- here useNames=FALSE hardcoded!
whichDense <- function(x, arr.ind = FALSE) {
    wh <- which(x@x) ## faster but "forbidden": .Internal(which(x@x))
    if (arr.ind && !is.null(d <- dim(x)))
	arrayInd(wh, d, useNames=FALSE) else wh
}
setMethod("which", "ndenseMatrix",
	  function(x, arr.ind) whichDense(as(x, "ngeMatrix"), arr.ind=arr.ind))
setMethod("which", "ldenseMatrix",
	  function(x, arr.ind) whichDense(as(x, "lgeMatrix"), arr.ind=arr.ind))

setMethod("which", "nsparseMatrix",
	  function(x, arr.ind, useNames = TRUE) {
	      if(arr.ind) which(as(x, "TsparseMatrix"), arr.ind=TRUE, useNames=useNames)
	      else as(x, "sparseVector")@i
	  })
setMethod("which", "lsparseMatrix",
	  function(x, arr.ind, useNames = TRUE) {
	      if(arr.ind) which(as(x, "TsparseMatrix"), arr.ind=TRUE, useNames=useNames)
	      else which(as(x, "sparseVector"))
	  })

## FIXME?:  not 100% compatible to "base :: which" -- here useNames=FALSE hardcoded!
which.ngT <- function(x, arr.ind)
    if(arr.ind) cbind(x@i, x@j) + 1L else as(x, "sparseVector")@i
setMethod("which", "ngTMatrix", which.ngT)
setMethod("which", "ntTMatrix", function(x, arr.ind)
	  which.ngT(.Call(Tsparse_diagU2N, x), arr.ind))
setMethod("which", "nsTMatrix", function(x, arr.ind)
	  which.ngT(as(x, "generalMatrix"), arr.ind))

## FIXME?:  not 100% compatible to "base :: which" -- here useNames=FALSE hardcoded!
which.lgT <- function(x, arr.ind) {
    if(arr.ind) {
	iT <- is1(x@x)
	cbind(x@i[iT], x@j[iT]) + 1L
    } else which(as(x, "sparseVector"))
}
setMethod("which", "lgTMatrix", which.lgT)
setMethod("which", "ltTMatrix", function(x, arr.ind)
	  which.lgT(.Call(Tsparse_diagU2N, x), arr.ind))
setMethod("which", "lsTMatrix", function(x, arr.ind)
	  which.lgT(as(x, "generalMatrix"), arr.ind))



setMethod("is.finite", signature(x = "lMatrix"), function(x) !is.na(x))
setMethod("is.finite", signature(x = "nMatrix"), allTrueMatrix)

setMethod("is.infinite", signature(x = "lMatrix"), is.na_nsp)# all FALSE
setMethod("is.infinite", signature(x = "nMatrix"), is.na_nsp)# all FALSE
