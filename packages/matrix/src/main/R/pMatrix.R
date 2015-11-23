#### Permutation Matrices -- Coercion and Methods

### NB "pMatrix" extends "indMatrix" and inherits methods -->  indMatrix.R


## The typical   'constructor' : coerce from  'index'
setAs("integer", "pMatrix",
      function(from) {
          nn <- names(from)
          new("pMatrix", Dim = rep.int(length(from), 2L), Dimnames = list(nn,nn),
              perm = from)
      })

setAs("numeric", "pMatrix",
      function(from)
	  if(all(from == (i <- as.integer(from)))) as(i, "pMatrix")
	  else stop("coercion to \"pMatrix\" only works from integer numeric"))



setAs("nMatrix", "pMatrix",
      function(from) {
	  from <- as(as(from, "TsparseMatrix"), "ngTMatrix")
	  n <- (d <- from@Dim)[1]
	  if(n != d[2]) stop("not a square matrix")
	  if(length(i <- from@i) != n)
	      stop("the number of non-zero entries differs from nrow(.)")
	  if((need.sort <- is.unsorted(i))) {
	      ii <- sort.list(i)
	      i <- i[ii]
	  }
	  if(n >= 1 && !identical(i, 0:(n - 1)))
	      stop("must have exactly one non-zero entry per row")
	  new("pMatrix", ## validity checking checks the 'perm' slot:
	      perm = 1L + if(need.sort) from@j[ii] else from@j,
	      Dim = d, Dimnames = from@Dimnames)
      })

setAs("matrix", "pMatrix", function(from) as(as(from, "nMatrix"), "pMatrix"))

setMethod("solve", signature(a = "pMatrix", b = "missing"),
	  function(a, b, ...) {
              a@perm <- invPerm(a@perm)
              a@Dimnames <- a@Dimnames[2:1]
              a
          })

setMethod("solve", signature(a = "pMatrix", b = "Matrix"),
	  function(a, b, ...) crossprod(a, b))
setMethod("solve", signature(a = "pMatrix", b = "matrix"),
	  function(a, b, ...) crossprod(a, b))

setMethod("solve", signature(a = "Matrix", b = "pMatrix"),
	  function(a, b, ...)
	  ## Or alternatively  solve(a, as(b, "CsparseMatrix"))
	  solve(a)[, invPerm(b@perm)])


setMethod("determinant", signature(x = "pMatrix", logarithm = "logical"),
	  function(x, logarithm, ...) {
	      if(any(x@Dim == 0)) mkDet(numeric(0))
	      else mkDet(logarithm=logarithm, ldet = 0, sig = signPerm(x@perm))
	  })


## t(pM) is == the inverse  pM^(-1):
setMethod("t", signature(x = "pMatrix"), function(x) solve(x))


setMethod("%*%", signature(x = "matrix", y = "pMatrix"),
	  function(x, y) { mmultCheck(x,y); x[, invPerm(y@perm)] })
setMethod("%*%", signature(x = "Matrix", y = "pMatrix"),
	  function(x, y) { mmultCheck(x,y); x[, invPerm(y@perm)] })

setMethod("%*%", signature(x = "pMatrix", y = "pMatrix"),
	  function(x, y) {
	      stopifnot(identical(x@Dim, y@Dim))
	      ## FIXME: dimnames dealing: as with S3 matrix's  %*%
	      x@perm <- x@perm[y@perm]
	      x
	  })

setMethod("crossprod", signature(x = "pMatrix", y = "matrix"),
	  function(x, y) { mmultCheck(x,y, 2L); y[invPerm(x@perm) ,]})
setMethod("crossprod", signature(x = "pMatrix", y = "Matrix"),
	  function(x, y) { mmultCheck(x,y, 2L); y[invPerm(x@perm) ,]})
setMethod("crossprod", signature(x = "pMatrix", y = "pMatrix"),
	  function(x, y) {
	      stopifnot(identical(x@Dim, y@Dim))
	      x@perm <- invPerm(x@perm)[y@perm]
	      x
	  })

setMethod("tcrossprod", signature(x = "pMatrix", y = "pMatrix"),
	  function(x, y) {
	      stopifnot(identical(x@Dim, y@Dim))
	      x@perm <- x@perm[invPerm(y@perm)]
	      x
	  })


setMethod("crossprod", signature(x = "pMatrix", y = "missing"),
          function(x, y=NULL) Diagonal(nrow(x)))
setMethod("tcrossprod", signature(x = "pMatrix", y = "missing"),
          function(x, y=NULL) Diagonal(nrow(x)))

