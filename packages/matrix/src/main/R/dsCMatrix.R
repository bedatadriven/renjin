#### Symmetric Sparse Matrices in compressed column-oriented format

setAs("dgCMatrix", "dsCMatrix",
      function(from) { ## r2130 ... | 2008-03-14 | added deprecation warning
	  warning("as(.,\"dsCMatrix\") is deprecated (since 2008); do use as(., \"symmetricMatrix\")")
	  as(from, "symmetricMatrix")
      })

## Specific conversions, should they be necessary.  Better to convert as
## as(x, "TsparseMatrix") or as(x, "denseMatrix")

## Moved to ./Csparse.R
## setAs("dsCMatrix", "dsTMatrix",
##       function(from) .Call(Csparse_to_Tsparse, from, FALSE))

setAs("dsCMatrix", "dgTMatrix", # needed for show(), image()
      function(from)
      ## pre-Cholmod -- FIXME: get rid of
      .Call(dsCMatrix_to_dgTMatrix, from))

setAs("dsCMatrix", "dgeMatrix",
      function(from) as(as(from, "dgTMatrix"), "dgeMatrix"))

setAs("dsCMatrix", "matrix",
      function(from) as(as(from, "generalMatrix"), "matrix"))
setAs("matrix", "dsCMatrix",
      function(from)
      as(as(as(from, "CsparseMatrix"), "symmetricMatrix"), "dMatrix"))

setAs("dsCMatrix", "lsCMatrix",
      function(from) new("lsCMatrix", i = from@i, p = from@p, uplo = from@uplo,
                         x = as.logical(from@x),
                         Dim = from@Dim, Dimnames = from@Dimnames))
setAs("dsCMatrix", "nsCMatrix",
      function(from) new("nsCMatrix", i = from@i, p = from@p, uplo = from@uplo,
                         Dim = from@Dim, Dimnames = from@Dimnames))

setAs("dsCMatrix", "dgCMatrix",
      function(from) .Call(Csparse_symmetric_to_general, from))

setAs("dsCMatrix", "dsyMatrix",
      function(from) as(from, "denseMatrix"))

##' Check if \code{name} (== "[sS][pP][dD]Cholesky") fits the values of the
##' logicals (perm, LDL, super).
##' @param name a string such as "sPdCholesky"
##' @param perm also known as \code{pivot}
##' @param LDL
##' @param super
##' @return logical: TRUE if the name matches
.chkName.CHM <- function(name, perm, LDL, super)
    .Call(R_chkName_Cholesky, name, perm, LDL, super)
## ../src/dsCMatrix.c

.CHM.factor.name <- function(perm, LDL, super)
    .Call(R_chm_factor_name, perm, LDL, super)

## have rather tril() and triu() methods than
## setAs("dsCMatrix", "dtCMatrix", ....)
setMethod("tril", "dsCMatrix",
	  function(x, k = 0, ...) {
	      if(x@uplo == "L" && k == 0)
		  ## same internal structure (speedup potential !?)
		  new("dtCMatrix", uplo = x@uplo, i = x@i, p = x@p,
		      x = x@x, Dim = x@Dim, Dimnames = x@Dimnames)
	      else tril(as(x, "dgCMatrix"), k = k, ...)
	  })

setMethod("triu", "dsCMatrix",
	  function(x, k = 0, ...) {
	      if(x@uplo == "U" && k == 0)
		  ## same internal structure (speedup potential !?)
		  new("dtCMatrix", uplo = x@uplo, i = x@i, p = x@p,
		      x = x@x, Dim = x@Dim, Dimnames = x@Dimnames)
	      else triu(as(x, "dgCMatrix"), k = k, ...)
	  })

solve.dsC.mat <- function(a,b, LDL = NA, tol = .Machine$double.eps) {
    r <- tryCatch(.Call(dsCMatrix_matrix_solve, a, b, LDL),
		  error=function(e)NULL, warning=function(w)NULL)
    if(is.null(r)) { ## cholmod factorization was not ok
	Matrix.msg("solve.dsC.mat(): Cholmod factorization unsuccessful --> using LU(<dgC>)")
	.solve.sparse.dgC(as(a,"dgCMatrix"), b=b, tol=tol)
    }
    else r
}

## ``Fully-sparse'' solve()  {different Cholmod routine, otherwise "the same"}:
solve.dsC.dC <- function(a,b, LDL = NA, tol = .Machine$double.eps) {
    r <- tryCatch(.Call(dsCMatrix_Csparse_solve, a, b, LDL),
		  error=function(e)NULL, warning=function(w)NULL)
    if(is.null(r)) { ## cholmod factorization was not ok
	Matrix.msg("solve.dsC.dC(): Cholmod factorization unsuccessful --> using LU(<dgC>)")
	.solve.sparse.dgC(as(a,"dgCMatrix"), b=b, tol=tol)
    }
    else r
}

## <sparse> . <dense> ------------------------

setMethod("solve", signature(a = "dsCMatrix", b = "ddenseMatrix"),
	  function(a, b, LDL = NA, tol = .Machine$double.eps, ...) {
	      solve.dsC.mat(a, b = if(!is(b, "dgeMatrix")) ..2dge(b) else b,
			    LDL=LDL, tol=tol)
	  },
	  valueClass = "dgeMatrix")
setMethod("solve", signature(a = "dsCMatrix", b = "denseMatrix"),
	  ## only triggers for diagonal*, ldense*.. (but *not* ddense: above)
	  function(a, b, LDL = NA, tol = .Machine$double.eps, ...)
	  solve.dsC.mat(a, as(.Call(dup_mMatrix_as_geMatrix, b), "dgeMatrix"),
			LDL=LDL, tol=tol))

setMethod("solve", signature(a = "dsCMatrix", b = "matrix"),
	  function(a, b, LDL = NA, tol = .Machine$double.eps, ...)
	  solve.dsC.mat(a, ..2dge(b), LDL=LDL, tol=tol),
	  valueClass = "dgeMatrix")

setMethod("solve", signature(a = "dsCMatrix", b = "numeric"),
	  function(a, b, LDL = NA, tol = .Machine$double.eps, ...)
	  solve.dsC.mat(a, ..2dge(b), LDL=LDL, tol=tol),
	  valueClass = "dgeMatrix")

## <sparse> . <sparse> ------------------------

setMethod("solve", signature(a = "dsCMatrix", b = "dsparseMatrix"),
	  function(a, b, LDL = NA, tol = .Machine$double.eps, ...) {
	      cb <- getClassDef(class(b))
	      if (!extends(cb, "CsparseMatrix"))
		  cb <- getClassDef(class(b <- as(b, "CsparseMatrix")))
	      if (extends(cb, "symmetricMatrix")) ## not supported (yet) by cholmod_spsolve
		  b <- as(b, "dgCMatrix")
	      solve.dsC.dC(a,b, LDL=LDL, tol=tol)
	  })

setMethod("solve", signature(a = "dsCMatrix", b = "missing"),
	  function(a, b, ...) solve(a, .trDiagonal(nrow(a), unitri=FALSE), ...))



setMethod("chol", signature(x = "dsCMatrix"),
	  function(x, pivot = FALSE, ...) .Call(dsCMatrix_chol, x, pivot),
	  valueClass = "dtCMatrix")

setMethod("Cholesky", signature(A = "dsCMatrix"),
          ## signature(): leaving away (perm, LDL,..), but specify below:
          ##              <==> all "ANY"
          function(A, perm = TRUE, LDL = !super, super = FALSE, Imult = 0, ...)
          .Call(dsCMatrix_Cholesky, A, perm, LDL, super, Imult))


setMethod("t", signature(x = "dsCMatrix"),
          function(x) .Call(Csparse_transpose, x, FALSE),
          valueClass = "dsCMatrix")

### These two are very similar, the first one has the advantage to be applicable to 'Chx' directly:

## "used" currently only in ../tests/factorizing.R
.diag.dsC <- function(x, Chx = Cholesky(x, LDL=TRUE), res.kind = "diag") {
    force(Chx)
    if(!missing(Chx)) stopifnot(.isLDL(Chx), is.integer(Chx@p), is.double(Chx@x))
    .Call(diag_tC, Chx, res.kind)
    ##    ^^^^^^^ from ../src/Csparse.c
    ## => res.kind in ("trace", "sumLog", "prod", "min", "max", "range", "diag", "diagBack")
}

## nowhere used/tested (FIXME?)
## here, we  *could* allow a 'mult = 0' factor :
.CHM.LDL.D <- function(x, perm = TRUE, res.kind = "diag") {
    .Call(dsCMatrix_LDL_D, x, perm, res.kind)
    ##    ^^^^^^^^^^^^^^^^ from ../src/dsCMatrix.c
}


## FIXME:  kind = "diagBack" is not yet implemented
##	would be much more efficient, but there's no CHOLMOD UI (?)
##
## Note: for det(), permutation is unimportant;
##       for diag(), apply *inverse* permutation
##    	q <- p ; q[q] <- seq_along(q); q



ldet1.dsC <- function(x, ...) .Call(CHMfactor_ldetL2, Cholesky(x, ...))
## these are slightly faster (ca. 3 to 4 %):
ldet2.dsC <- function(x, ...) {
    Ch <- Cholesky(x, super = FALSE, ...)
    .Call(diag_tC, Ch, "sumLog")
}
## only very slightly ( ~ < 1% ) faster (than "ldet2"):
ldet3.dsC <- function(x, perm = TRUE)
    .Call(dsCMatrix_LDL_D, x, perm=perm, "sumLog")

setMethod("determinant", signature(x = "dsCMatrix", logarithm = "missing"),
          function(x, logarithm, ...) determinant(x, TRUE))

setMethod("determinant", signature(x = "dsCMatrix", logarithm = "logical"),
	  function(x, logarithm, ...)
      {
	  if(x@Dim[1] <= 1L)
	      return(mkDet(diag(x), logarithm))
	  Chx <- tryCatch(suppressWarnings(Cholesky(x, LDL=TRUE)),
                          error = function(e) NULL)
	  ## or
	  ## ldet <- .Call("CHMfactor_ldetL2", Chx) # which would also work
	  ##				     when Chx <- Cholesky(x, super=TRUE)
          ## ldet <- tryCatch(.Call(dsCMatrix_LDL_D, x, perm=TRUE, "sumLog"),
	  ## if(is.null(ldet))

          if(is.null(Chx))  ## we do *not* have a positive definite matrix
	      detSparseLU(x, logarithm)
	  else {
              d <- .Call(diag_tC, Chx, res.kind = "diag")
	      mkDet(d, logarithm=logarithm)
          }
      })

## setMethod("writeHB", signature(obj = "dsCMatrix"),
##           function(obj, file, ...) {
##               .Deprecated("writeMM")
##               .Call(Matrix_writeHarwellBoeing,
##                     if (obj@uplo == "U") t(obj) else obj,
##                     as.character(file), "DSC")
##           })
