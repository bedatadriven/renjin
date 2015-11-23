#### Logical Sparse Matrices in triplet format

### contains = "lsparseMatrix"
###             ============= ---> superclass methods in ./lsparseMatrix.R


setAs("lgTMatrix", "lgeMatrix",
      function(from) .Call(lgTMatrix_to_lgeMatrix, from))

setAs("lgTMatrix", "matrix",
      function(from) .Call(lgTMatrix_to_matrix, from))
## setAs("lgTMatrix", "matrix", # go via fast C code:
##       function(from) as(as(from, "lgCMatrix"), "matrix"))

setAs("matrix", "lgTMatrix",
      function(from) {
	  stopifnot(is.logical(from))
	  dn <- dimnames(from)
	  if(is.null.DN(dn))
	      dn <- list(NULL,NULL)
	  else dimnames(from) <- NULL
	  TorNA <- is.na(from) | from
	  ij <- which(TorNA, arr.ind = TRUE, useNames = FALSE) - 1L
	  if(length(ij) == 0) ij <- matrix(ij, 0, 2)
	  new("lgTMatrix",
	      i = ij[,1],
	      j = ij[,2],
	      x = from[TorNA],
	      Dim = as.integer(dim(from)),
	      Dimnames = dn)
	  })

setAs("lgTMatrix", "dgTMatrix",
      function(from)
      ## more efficient than
      ## as(as(as(sM, "lgCMatrix"), "dgCMatrix"), "dgTMatrix")
      new("dgTMatrix", i = from@i, j = from@j,
          x = as.double(from@x),
          ## cannot copy factors, but can we use them?
          Dim = from@Dim, Dimnames= from@Dimnames))

setAs("lgTMatrix", "triangularMatrix",
      function(from) check.gT2tT(from, toClass = "ltTMatrix", do.n=FALSE))
setAs("lgTMatrix", "ltTMatrix",
      function(from) check.gT2tT(from, toClass = "ltTMatrix", do.n=FALSE))

setAs("lgTMatrix", "symmetricMatrix",
      function(from) check.gT2sT(from, toClass = "lsTMatrix", do.n=FALSE))
## We favor coercion to super-classes, here, "symmetricMatrix"
## setAs("lgTMatrix", "lsTMatrix",
##       function(from) check.gT2sT(from, toClass = "lsTMatrix", do.n=FALSE))


if(FALSE) ## unneeded: use t.<TsparseMatrix>
setMethod("t", signature(x = "lgTMatrix"),
	  function(x) new("lgTMatrix", i = x@j, j = x@i, x = x@x,
			  Dim = x@Dim[2:1],
			  Dimnames= x@Dimnames[2:1]),
	  valueClass = "lgTMatrix")
