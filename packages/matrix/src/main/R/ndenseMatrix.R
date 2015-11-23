#### "ndenseMatrix" - virtual class of nonzero pattern dense matrices
####  ------------
#### Contains  nge*;  ntr*, ntp*;  nsy*, nsp*;   ndi*

### NOTA BENE: Much of this is *very* parallel to ./ldenseMatrix.R
###						  ~~~~~~~~~~~~~~~~

## packed <->  non-packed :

setAs("nspMatrix", "nsyMatrix",	##  1L for "n*", 0L for "l*" :      vv
      nsp2nsy <- function(from) .Call(lspMatrix_as_lsyMatrix, from, 1L))

setAs("nsyMatrix", "nspMatrix",
      nsy2nsp <- function(from) .Call(lsyMatrix_as_lspMatrix, from, 1L))

setAs("ntpMatrix", "ntrMatrix",
      ntp2ntr <- function(from) .Call(ltpMatrix_as_ltrMatrix, from, 1L))

setAs("ntrMatrix", "ntpMatrix",
      ntr2ntp <- function(from) .Call(ltrMatrix_as_ltpMatrix, from, 1L))


## Nonzero Pattern -> Double {of same structure}:

setAs("ngeMatrix", "dgeMatrix", function(from) n2d_Matrix(from, "ngeMatrix"))
setAs("nsyMatrix", "dsyMatrix", function(from) n2d_Matrix(from, "nsyMatrix"))
setAs("nspMatrix", "dspMatrix", function(from) n2d_Matrix(from, "nspMatrix"))
setAs("ntrMatrix", "dtrMatrix", function(from) n2d_Matrix(from, "ntrMatrix"))
setAs("ntpMatrix", "dtpMatrix", function(from) n2d_Matrix(from, "ntpMatrix"))

setAs("ndenseMatrix", "ldenseMatrix", function(from) n2l_Matrix(from))

setAs("ngeMatrix", "lgeMatrix", function(from) n2l_Matrix(from, "ngeMatrix"))
setAs("nsyMatrix", "lsyMatrix", function(from) n2l_Matrix(from, "nsyMatrix"))
setAs("nspMatrix", "lspMatrix", function(from) n2l_Matrix(from, "nspMatrix"))
setAs("ntrMatrix", "ltrMatrix", function(from) n2l_Matrix(from, "ntrMatrix"))
setAs("ntpMatrix", "ltpMatrix", function(from) n2l_Matrix(from, "ntpMatrix"))

## all need be coercable to "ngeMatrix":

setAs("nsyMatrix", "ngeMatrix",
      nsy2nge <- function(from) .Call(lsyMatrix_as_lgeMatrix, from, 1L))
setAs("ntrMatrix", "ngeMatrix",
      ntr2nge <- function(from) .Call(ltrMatrix_as_lgeMatrix, from, 1L))
setAs("ntpMatrix", "ngeMatrix", function(from) ntr2nge(ntp2ntr(from)))
setAs("nspMatrix", "ngeMatrix", function(from) nsy2nge(nsp2nsy(from)))
## and the reverse
setAs("ngeMatrix", "ntpMatrix", function(from) ntr2ntp(as(from, "ntrMatrix")))
setAs("ngeMatrix", "nspMatrix", function(from) nsy2nsp(as(from, "nsyMatrix")))



### -> symmetric :

if(FALSE) ## not sure if this is a good idea ... -- FIXME?
setIs("ngeMatrix", "nsyMatrix",
      test = function(obj) isSymmetric(obj),
      replace = function(obj, value) { ## copy all slots
          for(n in slotNames(obj)) slot(obj, n) <- slot(value, n)
      })

### Alternative (at least works):
setAs("ngeMatrix", "nsyMatrix",
      function(from) {
	  if(isSymmetric(from))
	      new("nsyMatrix", x = from@x, Dim = from@Dim,
		  Dimnames = from@Dimnames, factors = from@factors)
	  else
	      stop("not a symmetric matrix; consider forceSymmetric() or symmpart()")
      })

setAs("ngeMatrix", "ntrMatrix",
      function(from) {
	  if(isT <- isTriangular(from))
	      new("ntrMatrix", x = from@x, Dim = from@Dim,
		  Dimnames = from@Dimnames, uplo = .if.NULL(attr(isT, "kind"), "U"))
          ## TODO: also check 'diag'
	  else stop("not a triangular matrix")
      })


###  ldense* <-> "matrix" :

## 1) "nge* :
setAs("ngeMatrix", "matrix", ge2mat)

setAs("matrix", "ngeMatrix",
      function(from) {
	  new("ngeMatrix",
	      x = as.logical(from),
	      Dim = as.integer(dim(from)),
	      Dimnames = .M.DN(from))
      })

## 2) base others on "nge*":

setAs("matrix", "nsyMatrix",
      function(from) as(as(from, "ngeMatrix"), "nsyMatrix"))
setAs("matrix", "nspMatrix", function(from) nsy2nsp(as(from, "nsyMatrix")))
setAs("matrix", "ntrMatrix",
      function(from) as(as(from, "ngeMatrix"), "ntrMatrix"))
setAs("matrix", "ntpMatrix", function(from) ntr2ntp(as(from, "ntrMatrix")))

## Useful if this was called e.g. for as(*, "nsyMatrix"), but it isn't
setAs("matrix", "ndenseMatrix", function(from) as(from, "ngeMatrix"))

setAs("ndenseMatrix", "matrix", ## uses the above l*M. -> lgeM.
      function(from) as(as(from, "ngeMatrix"), "matrix"))

## dense |-> compressed :

## go via "l" because dense_to_Csparse can't be used for "n" [missing CHOLMOD function]
setAs("ndenseMatrix", "CsparseMatrix",
      function(from) as(as(as(from, "lMatrix"), "CsparseMatrix"), "nMatrix"))
setAs("ndenseMatrix", "nsparseMatrix",
      function(from) as(as(as(from, "lMatrix"), "sparseMatrix"), "nMatrix"))
setAs("ndenseMatrix", "sparseMatrix", function(from) as(from, "nsparseMatrix"))

setAs("ndenseMatrix", "TsparseMatrix",
      function(from) {
	  if(is(from, "generalMatrix")) {
	      ##  cheap but not so efficient:
	      ij <- which(as(from,"matrix"), arr.ind = TRUE, useNames = FALSE) - 1L
	      new("ngTMatrix", i = ij[,1], j = ij[,2],
		  Dim = from@Dim, Dimnames = from@Dimnames,
		  factors = from@factors)
	  }
	  else
	      ## triangular or	symmetric (have *no* diagonal nMatrix)
	      ##     is delicate {packed or not, upper /lower indices ..} -> easy way
	      as(as(as(from, "lMatrix"), "TsparseMatrix"), "nMatrix")
      })

## Not sure, if these are needed or more efficient than the above:
## First one probably is
setAs("ngeMatrix", "ngTMatrix",
      function(from) {
          ##  cheap but not so efficient:
          ij <- which(as(from,"matrix"), arr.ind = TRUE, useNames = FALSE) - 1L
          new("ngTMatrix", i = ij[,1], j = ij[,2],
              Dim = from@Dim, Dimnames = from@Dimnames,
              factors = from@factors)
      })

setAs("ngeMatrix", "ngCMatrix",
      function(from) as(as(from, "ngTMatrix"), "ngCMatrix"))

setMethod("as.logical", signature(x = "ndenseMatrix"),
	  function(x, ...) as(x, "ngeMatrix")@x)

###----------------------------------------------------------------------


setMethod("t", signature(x = "ngeMatrix"), t_geMatrix)
setMethod("t", signature(x = "ntrMatrix"), t_trMatrix)
setMethod("t", signature(x = "nsyMatrix"), t_trMatrix)
setMethod("t", signature(x = "ntpMatrix"),
	  function(x) as(t(as(x, "ntrMatrix")), "ntpMatrix"))
setMethod("t", signature(x = "nspMatrix"),
	  function(x) as(t(as(x, "nsyMatrix")), "nspMatrix"))

## NOTE:  "&" and "|"  are now in group "Logic" c "Ops" --> ./Ops.R
##        "!" is in ./not.R

setMethod("as.vector", signature(x = "ndenseMatrix", mode = "missing"),
	  function(x, mode) as(x, "ngeMatrix")@x)

setMethod("norm", signature(x = "ndenseMatrix", type = "character"),
	  function(x, type, ...)
          .Call(dgeMatrix_norm, as(as(x,"dMatrix"),"dgeMatrix"), type),
	  valueClass = "numeric")

setMethod("rcond", signature(x = "ndenseMatrix", norm = "character"),
	  .rcond_via_d, valueClass = "numeric")
