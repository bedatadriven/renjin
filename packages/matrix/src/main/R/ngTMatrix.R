#### Nonzero Pattern Sparse Matrices in triplet format

### contains = "nsparseMatrix"
###             ============= ---> superclass methods in ./nsparseMatrix.R


setAs("ngTMatrix", "lgeMatrix",
      function(from) .Call(lgTMatrix_to_lgeMatrix, as(from,"lgTMatrix")))
setAs("ngTMatrix", "ngeMatrix",
      function(from) as(as(from, "lgeMatrix"), "nMatrix"))

setAs("ngTMatrix", "generalMatrix", function(from) as(from, "ngeMatrix"))

setAs("ngTMatrix", "matrix",
      function(from) .Call(lgTMatrix_to_matrix, as(from, "lgTMatrix")))
## setAs("ngTMatrix", "matrix", # go via fast C code:
##       function(from) as(as(from, "ngCMatrix"), "matrix"))

setAs("matrix", "ngTMatrix",
      function(from) {
	  if(!is.logical(from))
	      storage.mode(from) <- "logical"
	  if(anyNA(from))
	      stop("cannot coerce 'NA's to \"nsparseMatrix\"")
          dn <- dimnames(from)
          if(is.null.DN(dn))
              dn <- list(NULL,NULL)
          else dimnames(from) <- NULL # such that which(.) does not see any:
	  ij <- which(from, arr.ind = TRUE, useNames = FALSE) - 1L
	  if(length(ij) == 0) ij <- matrix(ij, 0, 2)
	  new("ngTMatrix",
	      i = ij[,1],
	      j = ij[,2],
	      Dim = as.integer(dim(from)),
	      Dimnames = dn)
	  })

setAs("matrix", "nMatrix", function(from) as(from, "ngTMatrix"))

setAs("ngTMatrix", "dgTMatrix",
      function(from)
      ## more efficient than
      ## as(as(as(sM, "ngCMatrix"), "dgCMatrix"), "dgTMatrix")
      new("dgTMatrix", i = from@i, j = from@j,
	  x = rep.int(1, length(from@i)),
	  ## cannot copy factors, but can we use them?
	  Dim = from@Dim, Dimnames= from@Dimnames))
setAs("ngTMatrix", "dMatrix", function(from) as(from, "dgTMatrix"))
setAs("ngTMatrix", "dsparseMatrix", function(from) as(from, "dgTMatrix"))


setAs("ngTMatrix", "lgTMatrix",
      function(from)
      new("lgTMatrix", i = from@i, j = from@j,
	  x = rep.int(TRUE, length(from@i)),
	  ## cannot copy factors, but can we use them?
	  Dim = from@Dim, Dimnames= from@Dimnames))
setAs("ngTMatrix", "lMatrix", function(from) as(from, "lgTMatrix"))

setAs("ngTMatrix", "triangularMatrix",
      function(from) check.gT2tT(from, toClass = "ntTMatrix", do.n=TRUE))
setAs("ngTMatrix", "ntTMatrix",
      function(from) check.gT2tT(from, toClass = "ntTMatrix", do.n=TRUE))
setAs("ngTMatrix", "symmetricMatrix",
      function(from) check.gT2sT(from, toClass = "nsTMatrix", do.n=TRUE))
## We favor coercion to super-classes, here, "symmetricMatrix"
## setAs("ngTMatrix", "nsTMatrix",
##       function(from) check.gT2sT(from, toClass = "nsTMatrix", do.n=TRUE))


if(FALSE) ## unneeded: use t.<TsparseMatrix>
setMethod("t", signature(x = "ngTMatrix"),
	  function(x) new("ngTMatrix", i = x@j, j = x@i,
			  Dim = x@Dim[2:1],
			  Dimnames= x@Dimnames[2:1]),
	  valueClass = "ngTMatrix")
