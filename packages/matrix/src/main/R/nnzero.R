#### Number of "structural" non-zeros --- this is  nnzmax() in Matlab
####        of effectively  non-zero values =      nnz()     "   "

## Our nnzero() is like Matlab's nnz() -- but more sophisticated because of NAs
## New: generic function instead of  if(..) ... else if(..) ......
##
## na.counted: TRUE : NA's are counted, they are not 0
##	       NA   : NA's are not known (0 or not) ==>	 result := NA
##	       FALSE: NA's are omitted before counting
## "Default" : for non-"Matrix" (e.g. classical matrices):
setMethod("nnzero", "ANY",
	  function(x, na.counted = NA)	sum(nz.NA(x, na.counted)))
setMethod("nnzero", "diagonalMatrix",
	  function(x, na.counted = NA) sum(nz.NA(diag(x), na.counted)))
setMethod("nnzero", "indMatrix", function(x, na.counted = NA) x@Dim[1])
## other (not "indMatrix", not "diagonalMatrix") "sparseMatrix":
setMethod("nnzero", "sparseMatrix",
	  function(x, na.counted = NA)
      {
	d <- x@Dim
	if(any(d == 0)) return(0L)
	cl <- class(x)
	## speedup:
	cld <- getClassDef(cl)
	n <- d[1]
	iSym <- extends(cld, "symmetricMatrix")
        iTri <- if(iSym) FALSE else extends(cld, "triangularMatrix")
	nn <- switch(.sp.class(cl),
		     "CsparseMatrix" = x@p[d[2]+1L],# == length(x@i) only if not over-alloc.
		     "TsparseMatrix" = {
			 if(anyDuplicatedT(x, di = d))
			     x <- .Call(Tsparse_to_Csparse, x, iTri)
			 length(x@i)
		     },
		     "RsparseMatrix" = x@p[n+1L])
	if(!extends(cld, "nMatrix")) # <==> has 'x' slot : consider NAs in it:
	    nn <- sum(nz.NA(if(nn < length(x@x)) x@x[seq_len(nn)] else x@x,
			    na.counted))

	if(iSym)
	    nn+nn - sum(nz.NA(diag(x), na.counted))
	else if(iTri && x@diag == "U")
	    nn + n else nn
    })

setMethod("nnzero", "denseMatrix",
	  function(x, na.counted = NA)
      {
	  d <- x@Dim
	  if(any(d == 0)) return(0L)
	  cl <- class(x)
	  ## speedup:
	  cld <- getClassDef(cl)
	  n <- d[1]
	  iSym <- extends(cld, "symmetricMatrix")
	  ## dense, not diagonal: Can use 'x' slot;
	  if(iSym || extends(cld, "triangularMatrix")) {
	      ## now !iSym  <==> "triangularMatrix"
	      upper <- (x@uplo == "U")
	      if(length(x@x) < n*n) { ## packed symmetric | triangular
		  if(iSym) {
		      ## indices of *diagonal* entries for packed :
		      iDiag <- cumsum(if(upper) 1:n else c(1L, if(n > 1)n:2))
		      ## symmetric packed: count off-diagonals *twice*
		      2L* sum(nz.NA(x@x[-iDiag], na.counted)) +
			  sum(nz.NA(x@x[ iDiag], na.counted))
		  }
		  else ## triangular packed
		      sum(nz.NA(x@x, na.counted))
	      }
	      else {
		  ## not packed, but may have "arbitrary"
		  ## entries in the non-relevant upper/lower triangle
		  s <- sum(nz.NA(x@x[indTri(n, upper=upper)], na.counted))
		  (if(iSym) 2L * s else s) +
		      (if(!iSym && x@diag == "U")
		       n else sum(nz.NA(x@x[indDiag(n)], na.counted)))
	      }
	  }
	  else { ## dense general <--> .geMatrix
	      sum(nz.NA(x@x, na.counted))
	  }
      })
## Working via sparse*:
setMethod("nnzero", "CHMfactor",
	  function(x, na.counted = NA)
	  nnzero(as(x,"sparseMatrix"), na.counted=na.counted))
