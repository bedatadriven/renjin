#### Index Matrices -- Coercion and Methods (--> ../man/indMatrix-class.Rd )

## The typical	 'constructor' : coerce from  'perm'
setAs("integer", "indMatrix",
      function(from)
	  new("indMatrix", Dim = c(length(from), max(from)),
              Dimnames = list(names(from), NULL),
	      perm = from))

setAs("numeric", "indMatrix",
      function(from)
	  if(all(from == (i <- as.integer(from)))) as(i, "indMatrix")
      else stop("coercion to \"indMatrix\" only works from integer numeric"))

## A constructor from a list giving the index ('perm') and the number of columns
## (need this for cases in which the value(s) represented by the last
## column(s) has no observations):
.list2indMatrix <- function(from) {
    if(length(from) == 2 &&
       all(from[[1]] == (i <- as.integer(from[[1]]))) &&
       from[[2]] == (d <- as.integer(from[[2]])) &&
       length(d) == 1 && d >= max(i)) {
	new("indMatrix", perm = i, Dim = c(length(i), d))
    } else
	stop("coercion from list(i1,...,ik, d) to \"indMatrix\" failed.
 All entries must be integer valued and the number of columns, d, not smaller
 than the maximal index i*.")
}
setAs("list", "indMatrix", .list2indMatrix)

setAs("indMatrix", "matrix",
      function(from) {
	  fp <- from@perm
	  r <- ldiag(n = from@Dim[2])[fp,]
	  if(.has.DN(from)) dimnames(r) <- from@Dimnames
	  r
      })


## coerce to 0/1 sparse matrix, i.e. sparse pattern
.ind2ngT <- function(from) {
    d <- from@Dim
    new("ngTMatrix", i = seq_len(d[1]) - 1L, j = from@perm - 1L,
        Dim = d, Dimnames = from@Dimnames)
}
setAs("indMatrix", "ngTMatrix", .ind2ngT)

setAs("indMatrix", "TsparseMatrix", .ind2ngT)
setAs("indMatrix", "nMatrix", .ind2ngT)
setAs("indMatrix", "lMatrix", function(from) as(.ind2ngT(from), "lMatrix"))
setAs("indMatrix", "dMatrix", function(from) as(.ind2ngT(from), "dMatrix"))
setAs("indMatrix", "dsparseMatrix", function(from) as(from, "dMatrix"))
setAs("indMatrix", "lsparseMatrix", function(from) as(from, "lMatrix"))
setAs("indMatrix", "nsparseMatrix", .ind2ngT)
setAs("indMatrix", "CsparseMatrix",
      function(from) as(.ind2ngT(from), "CsparseMatrix"))
setAs("indMatrix", "ngeMatrix", function(from) as(.ind2ngT(from),"ngeMatrix"))

setAs("nMatrix", "indMatrix",
      function(from) {
	  from <- as(as(from, "TsparseMatrix"), "ngTMatrix")
	  n <- (d <- from@Dim)[1]
	  if(length(i <- from@i) != n)
	      stop("the number of non-zero entries differs from nrow(.)")
	  if((need.sort <- is.unsorted(i))) {
	      ii <- sort.list(i)
	      i <- i[ii]
	  }
	  if(n >= 1 && !identical(i, 0:(n - 1)))
	      stop("must have exactly one non-zero entry per row")

	  new("indMatrix", ## validity checking checks the 'perm' slot:
	      perm = 1L + if(need.sort) from@j[ii] else from@j,
	      Dim = d, Dimnames = from@Dimnames)
      })

setAs("matrix", "indMatrix", function(from) as(as(from, "nMatrix"), "indMatrix"))

setAs("indMatrix", "matrix", function(from) as(.ind2ngT(from), "matrix"))

setAs("sparseMatrix", "indMatrix", function(from)
    as(as(from, "nsparseMatrix"), "indMatrix"))

setMethod("is.na", signature(x = "indMatrix"), is.na_nsp)
setMethod("is.infinite", signature(x = "indMatrix"), is.na_nsp)
setMethod("is.finite", signature(x = "indMatrix"), allTrueMatrix)

setMethod("t", signature(x = "indMatrix"), function(x) t(.ind2ngT(x)))



setMethod("%*%", signature(x = "matrix", y = "indMatrix"),
	  function(x, y) x %*% as(y, "lMatrix"))
setMethod("%*%", signature(x = "Matrix", y = "indMatrix"),
	  function(x, y) x %*% as(y, "lMatrix"))

setMethod("%*%", signature(x = "indMatrix", y = "matrix"),
	  function(x, y) { mmultCheck(x,y); y[x@perm ,] })
setMethod("%*%", signature(x = "indMatrix", y = "Matrix"),
	  function(x, y) { mmultCheck(x,y); y[x@perm ,] })


setMethod("crossprod", signature(x = "indMatrix", y = "matrix"),
	  function(x, y) as(t(x), "lMatrix") %*% y)
setMethod("crossprod", signature(x = "indMatrix", y = "Matrix"),
	  function(x, y) as(t(x), "lMatrix") %*% y)
setMethod("crossprod", signature(x = "indMatrix", y = "indMatrix"),
	  function(x, y) {
	      mmultCheck(x,y, 2L)
              ## xy <- interaction(x@perm, y@perm)
              ## this is wrong if any of the columns in X or Y are empty because interaction()
              ## drops non-occuring levels from a non-factor. Explicitly defining a factor with
              ## levels 1:ncol(<indMatrix>) avoids that.
              nx <- x@Dim[2L]
              ny <- y@Dim[2L]
	      xy <- interaction(factor(x@perm, levels=seq_len(nx)),
				factor(y@perm, levels=seq_len(ny)))
	      Matrix(data= tabulate(xy, nbins=nlevels(xy)),
		     nrow = nx, ncol = ny)
	  })

setMethod("tcrossprod", signature(x = "matrix", y = "indMatrix"),
	  function(x, y) { mmultCheck(x,y, 3L); x[, y@perm] })
setMethod("tcrossprod", signature(x = "Matrix", y = "indMatrix"),
	  function(x, y) { mmultCheck(x,y, 3L); x[, y@perm] })
setMethod("tcrossprod", signature(x = "indMatrix", y = "indMatrix"),
	  function(x, y) { mmultCheck(x,y, 3L); x[, y@perm] })

setMethod("crossprod", signature(x = "indMatrix", y = "missing"),
	  function(x, y=NULL) Diagonal(x = tabulate(x@perm, nbins=x@Dim[2L])))

setMethod("tcrossprod", signature(x = "indMatrix", y = "missing"),
	  function(x, y=NULL) x[,x@perm])


setMethod("kronecker", signature(X = "indMatrix", Y = "indMatrix"),
	  function (X, Y, FUN = "*", make.dimnames = FALSE, ...) {
	      if (FUN != "*") stop("kronecker method must use default 'FUN'")
              ## Explicitly defining a factor with levels 1:ncol(.) avoids that
              ## interaction() drops non-occuring levels when any of the
              ## columns in X or Y are empty:
              perm <-  as.integer(interaction(factor(rep(X@perm, each =Y@Dim[1]),
                                                     levels=seq_len(X@Dim[2])),
                                              factor(rep.int(Y@perm, times=X@Dim[1]),
                                                     levels=seq_len(Y@Dim[2])),
                                              lex.order=TRUE))

	      new("indMatrix", perm=perm, Dim=X@Dim*Y@Dim)
	  })


setMethod("[", signature(x = "indMatrix", i = "index", j = "missing",
			 drop = "logical"),
	  function (x, i, j, ..., drop)
      {
	  n <- length(newperm <- x@perm[i])
	  if(drop && n == 1) { ## -> logical unit vector
	      newperm == seq_len(x@Dim[2])
	  } else { ## stay matrix
	      if(!is.null((DN <- x@Dimnames)[[1]])) DN[[1]] <- DN[[1]][i]
	      new("indMatrix", perm = newperm,
		  Dim = c(n, x@Dim[2]), Dimnames = DN)
	  }
      })



.indMat.nosense <- function (x, i, j, ..., value)
    stop('replacing "indMatrix" entries is not allowed, as rarely sensible')
setReplaceMethod("[", signature(x = "indMatrix", i = "index"), .indMat.nosense)
setReplaceMethod("[", signature(x = "indMatrix", i = "missing", j = "index"),
		 .indMat.nosense) ##   explicit	 ^^^^^^^^^^^^ for disambiguation
setReplaceMethod("[", signature(x = "indMatrix", i = "missing", j = "missing"),
		 .indMat.nosense)


### rbind2() method: --> bind2.R
