#### Methods for the virtual class 'CsparseMatrix' of sparse matrices stored in
####  "column compressed" format.
#### -- many more specific things are e.g. in ./dgCMatrix.R

setAs("CsparseMatrix", "TsparseMatrix",
      function(from)
          ## |-> cholmod_C -> cholmod_T -> chm_triplet_to_SEXP
          ## modified to support triangular (../src/Csparse.c)
          .Call(Csparse_to_Tsparse, from, is(from, "triangularMatrix")))


## special cases (when a specific "to" class is specified)
setAs("dgCMatrix", "dgTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, FALSE))

setAs("dsCMatrix", "dsTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, FALSE))

setAs("dsCMatrix", "dgCMatrix",
      function(from) .Call(Csparse_symmetric_to_general, from))

for(prefix in c("d", "l", "n"))
    setAs(paste0(prefix,"sCMatrix"), "generalMatrix",
	  function(from) .Call(Csparse_symmetric_to_general, from))
rm(prefix)

setAs("dtCMatrix", "dtTMatrix",
      function(from) .Call(Csparse_to_Tsparse, from, TRUE))

if(FALSE) ## old version
C2dense <- function(from) {
    ## |-> cholmod_C -> cholmod_dense -> chm_dense_to_dense
    cld <- getClassDef(class(from))
    if (extends(cld, "generalMatrix"))
	.Call(Csparse_to_dense, from, FALSE)
    else { ## "triangular" or "symmetric" :
        tri <- extends(cld, "triangularMatrix")
	## Csparse_to_dense  loses symmetry and triangularity properties.
	## With suitable changes to chm_dense_to_SEXP (../src/chm_common.c)
	## we could do this in C code -- or do differently in C {FIXME!}
	if (tri && from@diag == "U")
	    from <- .Call(Csparse_diagU2N, from)
	as(.Call(Csparse_to_dense, from, symm = !tri), # -> "[dln]geMatrix"
	   paste0(.M.kindC(cld),
		  .dense.prefixes[if(tri) "t" else "s"], "Matrix"))
    }
}
C2dense <- function(from) .Call(Csparse_to_dense, from, NA_integer_)

setAs("CsparseMatrix", "denseMatrix", C2dense)

## special cases (when a specific "to" class is specified)
setAs("dgCMatrix", "dgeMatrix",   function(from) .Call(Csparse_to_dense, from,  0L))
setAs("dsCMatrix", "denseMatrix", function(from) .Call(Csparse_to_dense, from,  1L))
setAs("dtCMatrix", "denseMatrix", function(from) .Call(Csparse_to_dense, from, -1L))

setAs("dgCMatrix", "vector", function(from) .Call(Csparse_to_vector, from))
setAs("dsCMatrix", "vector", function(from) .Call(Csparse_to_vector, from))
setMethod("as.vector", signature(x = "dgCMatrix", mode = "missing"),
	  function(x, mode) .Call(Csparse_to_vector, x))
setMethod("as.vector", signature(x = "dsCMatrix", mode = "missing"),
	  function(x, mode) .Call(Csparse_to_vector, x))
## could do these and more for as(., "numeric") ... but we *do* recommend  as(*,"vector"):
## setAs("dgCMatrix", "numeric", Csp2vec)
## setAs("dsCMatrix", "numeric", Csp2vec)

## |-> cholmod_C -> cholmod_dense -> chm_dense_to_matrix
## cholmod_sparse_to_dense converts symmetric storage to general
## storage so symmetric classes are ok for conversion to matrix.
## unit triangular needs special handling
##' exported
.dxC2mat <- function(from, chkUdiag=TRUE) .Call(Csparse_to_matrix, from, chkUdiag, NA)
setAs("dgCMatrix", "matrix", function(from) .Call(Csparse_to_matrix, from, FALSE, FALSE))
setAs("dsCMatrix", "matrix", function(from) .Call(Csparse_to_matrix, from, FALSE, TRUE))
setAs("dtCMatrix", "matrix", function(from) .Call(Csparse_to_matrix, from, TRUE,  FALSE))
## NB: Would *not* be ok for l*Matrix or n*Matrix,
## --------- as cholmod coerces to "REAL" aka "double"

setAs("CsparseMatrix", "symmetricMatrix",
      function(from) {
	  if(isSymmetric(from)) forceCspSymmetric(from)
	  else stop("not a symmetric matrix; consider forceSymmetric() or symmpart()")
      })


.validateCsparse <- function(x, sort.if.needed = FALSE)
    .Call(Csparse_validate2, x, sort.if.needed)
##-> to be used in sparseMatrix(.), e.g. --- but is unused currently
## NB: 'sort.if.needed' is called 'maybe_modify' in C -- so be careful
## more useful:
.sortCsparse <- function(x) .Call(Csparse_sort, x) ## modifies 'x' !!

### Some group methods:


### Subsetting -- basic things (drop = "missing") are done in ./Matrix.R
### ---------- "["  and (currently) also ./sparseMatrix.R

subCsp_cols <- function(x, j, drop)
{
    ## x[ , j, drop=drop]   where we know that	x is Csparse*
    dn <- x@Dimnames
    jj <- intI(j, n = x@Dim[2], dn[[2]], give.dn = FALSE)
    r <- .Call(Csparse_submatrix, x, NULL, jj)
    if(!is.null(n <- dn[[1]])) r@Dimnames[[1]] <- n
    if(!is.null(n <- dn[[2]])) r@Dimnames[[2]] <- n[jj+1L]
    if(drop && any(r@Dim == 1L)) drop(as(r, "matrix")) else {
	if(!is.null(n <- names(dn))) names(r@Dimnames) <- n
	r
    }
}

subCsp_rows <- function(x, i, drop)# , cl = getClassDef(class(x))
{
    ## x[ i,  drop=drop]   where we know that  x is Csparse*
    dn <- x@Dimnames
    ii <- intI(i, n = x@Dim[1], dn[[1]], give.dn = FALSE)
    r <- .Call(Csparse_submatrix, x, ii, NULL)
    if(!is.null(n <- dn[[1]])) r@Dimnames[[1]] <- n[ii+1L]
    if(!is.null(n <- dn[[2]])) r@Dimnames[[2]] <- n
    if(drop && any(r@Dim == 1L)) drop(as(r, "matrix")) else {
	if(!is.null(n <- names(dn))) names(r@Dimnames) <- n
	r
    }
}

subCsp_ij <- function(x, i, j, drop)
{
    ## x[i, j, drop=drop]   where we know that	x is Csparse*
    d <- x@Dim
    dn <- x@Dimnames
    ## Take care that	x[i,i]	for symmetricM* stays symmetric
    i.eq.j <- identical(i,j) # < want fast check
    ii <- intI(i, n = d[1], dn[[1]], give.dn = FALSE)
    jj <- if(i.eq.j && d[1] == d[2]) ii
    else intI(j, n = d[2], dn[[2]], give.dn = FALSE)
    r <- .Call(Csparse_submatrix, x, ii, jj)
    if(!is.null(n <- dn[[1]])) r@Dimnames[[1]] <- n[ii + 1L]
    if(!is.null(n <- dn[[2]])) r@Dimnames[[2]] <- n[jj + 1L]
    if(!i.eq.j) {
	if(drop && any(r@Dim == 1L)) drop(as(r, "matrix")) else {
	    if(!is.null(n <- names(dn))) names(r@Dimnames) <- n
	    r
	}
    } else { ## i == j
	if(drop) drop <- any(r@Dim == 1L)
	if(drop)
	    drop(as(r, "matrix"))
	else {
	    if(!is.null(n <- names(dn))) names(r@Dimnames) <- n
	    if(extends((cx <- getClassDef(class(x))),
		       "symmetricMatrix")) ## TODO? make more efficient:
		.gC2sym(r, uplo = x@uplo)## preserve uplo !
	    else if(extends(cx, "triangularMatrix") && !is.unsorted(ii))
		as(r, "triangularMatrix")
	    else r
	}
    }
}

setMethod("[", signature(x = "CsparseMatrix", i = "index", j = "missing",
			 drop = "logical"),
	  function (x, i,j, ..., drop) {
	      na <- nargs()
	      Matrix.msg("Csp[i,m,l] : nargs()=",na, .M.level = 2)
	      if(na == 4)
		  subCsp_rows(x, i, drop=drop)
	      else if(na == 3)
		  .M.vectorSub(x, i) # as(x, "TsparseMatrix")[i, drop=drop]
	      else ## should not happen
		  stop("Matrix-internal error in <CsparseM>[i,,d]; please report")
	  })

setMethod("[", signature(x = "CsparseMatrix", i = "missing", j = "index",
			 drop = "logical"),
	  function (x,i,j, ..., drop) {
	      Matrix.msg("Csp[m,i,l] : nargs()=",nargs(), .M.level = 2)
	      subCsp_cols(x, j, drop=drop)
	  })

setMethod("[", signature(x = "CsparseMatrix",
			 i = "index", j = "index", drop = "logical"),
	  function (x, i, j, ..., drop) {
	      Matrix.msg("Csp[i,i,l] : nargs()=",nargs(), .M.level = 2)
	      subCsp_ij(x, i, j, drop=drop)
	  })




## workhorse for "[<-" -- for d*, l*, and n..C-sparse matrices :
## ---------     -----
replCmat <- function (x, i, j, ..., value)
{
    di <- dim(x)
    dn <- dimnames(x)
    iMi <- missing(i)
    jMi <- missing(j)
    na <- nargs()
    Matrix.msg("replCmat[x,i,j,.., val] : nargs()=", na,"; ",
	       if(iMi | jMi) sprintf("missing (i,j) = (%d,%d)", iMi,jMi),
	       .M.level = 2)
    if(na == 3) { ## vector (or 2-col) indexing M[i] <- v : includes M[TRUE] <- v or M[] <- v !
	x <- as(x, "TsparseMatrix")
	x[i] <- value # may change class e.g. from dtT* to dgT*
	clx <- sub(".Matrix$", "CMatrix", (c.x <- class(x)))
	if("x" %in% .slotNames(c.x) && any0(x@x))
	    ## drop all values that "happen to be 0"
	    drop0(x, is.Csparse=FALSE) else as_CspClass(x, clx)
    }
    else ## nargs() == 4 :
	replCmat4(x,
		  i1 = if(iMi) 0:(di[1] - 1L) else .ind.prep2(i, 1, di, dn),
		  i2 = if(jMi) 0:(di[2] - 1L) else .ind.prep2(j, 2, di, dn),
                  iMi=iMi, jMi=jMi, value=value)
} ## replCmat

replCmat4 <- function(x, i1, i2, iMi, jMi, value, spV = is(value,"sparseVector"))
{
    dind <- c(length(i1), length(i2)) # dimension of replacement region
    lenRepl <- prod(dind)
    lenV <- length(value)
    if(lenV == 0) {
	if(lenRepl != 0)
	    stop("nothing to replace with")
	else return(x)
    }
    ## else: lenV := length(value)	 is > 0
    if(lenRepl %% lenV != 0)
	stop("number of items to replace is not a multiple of replacement length")
    if(lenV > lenRepl)
	stop("too many replacement values")

    clx <- class(x)
    clDx <- getClassDef(clx) # extends() , is() etc all use the class definition

    ## keep "symmetry" if changed here:
    x.sym <- extends(clDx, "symmetricMatrix")
    if(x.sym) { ## only half the indices are there..
	## using array() for large dind is a disaster...
	mkArray <- if(spV) # TODO: room for improvement
	    function(v, dim) spV2M(v, dim[1],dim[2]) else array
	x.sym <-
	    (dind[1] == dind[2] && all(i1 == i2) &&
	     (lenRepl == 1 || lenV == 1 ||
	      isSymmetric(mkArray(value, dim=dind))))
	## x.sym : result is *still* symmetric
	x <- .Call(Csparse_symmetric_to_general, x) ## but do *not* redefine clx!
    }
    else if(extends(clDx, "triangularMatrix")) {
	xU <- x@uplo == "U"
	r.tri <- ((any(dind == 1) || dind[1] == dind[2]) &&
		  if(xU) max(i1) <= min(i2) else max(i2) <= min(i1))
	if(r.tri) { ## result is *still* triangular
	    if(any(i1 == i2)) # diagonal will be changed
		x <- diagU2N(x) # keeps class (!)
	}
	else { # go to "generalMatrix" and continue
	    x <- as(x, paste0(.M.kind(x), "gCMatrix")) ## & do not redefine clx!
	}
    }
    ## Temporary hack for debugging --- remove eventually -- FIXME :
    ## see also	 MATRIX_SUBASSIGN_VERBOSE in ../src/t_Csparse_subassign.c
    if(!is.null(v <- getOption("Matrix.subassign.verbose")) && v) {
	op <- options(Matrix.verbose = 2); on.exit(options(op))
	## the "hack" to signal "verbose" to the C code:
	i1[1] <- -i1[1]
	if(i1[1] == 0)
	    warning("i1[1] == 0 ==> C-level verbosity will not happen!")
    }

    if(extends(clDx, "dMatrix")) {
	has.x <- TRUE
	x <- .Call(dCsparse_subassign,
		   if(clx %in% c("dgCMatrix", "dtCMatrix"))x
		   else as(x, "dgCMatrix"),
		   i1, i2,
		   as(value, "sparseVector"))
    }
    else if(extends(clDx, "lMatrix")) {
	has.x <- TRUE
	x <- .Call(lCsparse_subassign,
		   if(clx %in% c("lgCMatrix", "ltCMatrix"))x
		   else as(x, "lgCMatrix"),
		   i1, i2,
		   as(value, "sparseVector"))
    }
    else if(extends(clDx, "nMatrix")) {
	has.x <- FALSE
	x <- .Call(nCsparse_subassign,
		   if(clx %in% c("ngCMatrix", "ntCMatrix"))x
		   else as(x, "ngCMatrix"),
		   i1, i2,
		   as(value, "sparseVector"))
    }
    else if(extends(clDx, "iMatrix")) {
	has.x <- TRUE
	x <- .Call(iCsparse_subassign,
		   if(clx %in% c("igCMatrix", "itCMatrix"))x
		   else as(x, "igCMatrix"),
		   i1, i2,
		   as(value, "sparseVector"))
    }
    else if(extends(clDx, "zMatrix")) {
	has.x <- TRUE
	x <- .Call(zCsparse_subassign,
		   if(clx %in% c("zgCMatrix", "ztCMatrix"))x
		   else as(x, "zgCMatrix"),
		   i1, i2,
		   ## here we only want zsparseVector {to not have to do this in C}:
		   as(value, "zsparseVector"))
    }
    else { ## use "old" code ...
        ## does this happen ? ==>
	if(identical(Sys.getenv("USER"),"maechler"))## does it still happen? __ FIXME __
	    stop("using	 \"old code\" part in  Csparse subassignment")
        ## else
	warning("using\"old code\" part in Csparse subassignment\n >>> please report to Matrix-authors@r-project.org",
		immediate. = TRUE)

	xj <- .Call(Matrix_expand_pointers, x@p)
	sel <- (!is.na(match(x@i, i1)) &
		!is.na(match( xj, i2)))
	has.x <- "x" %in% slotNames(clDx)# === slotNames(x),
	## has.x  <==> *not* nonzero-pattern == "nMatrix"

	if(has.x && sum(sel) == lenRepl) { ## all entries to be replaced are non-zero:
	    ## need indices instead of just 'sel', for, e.g.,  A[2:1, 2:1] <- v
	    non0 <- cbind(match(x@i[sel], i1),
			  match(xj [sel], i2), deparse.level=0L)
	    iN0 <- 1L + .Call(m_encodeInd, non0, di = dind, orig1=TRUE, checkBounds=FALSE)

	    has0 <-
		if(spV) length(value@i) < lenV else any(value[!is.na(value)] == 0)
	    if(lenV < lenRepl)
		value <- rep_len(value, lenRepl)
	    ## Ideally we only replace them where value != 0 and drop the value==0
	    ## ones; FIXME: see Davis(2006) "2.7 Removing entries", p.16, e.g. use cs_dropzeros()
	    ##	     but really could be faster and write something like cs_drop_k(A, k)
	    ## v0 <- 0 == value
	    ## if (lenRepl == 1) and v0 is TRUE, the following is not doing anything
	    ##-	 --> ./dgTMatrix.R	and its	 replTmat()
	    ## x@x[sel[!v0]] <- value[!v0]
	    x@x[sel] <- as.vector(value[iN0])
	    if(extends(clDx, "compMatrix") && length(x@factors)) # drop cashed ones
		x@factors <- list()
	    if(has0) x <- .Call(Csparse_drop, x, 0)

	    return(if(x.sym) as_CspClass(x, clx) else x)
	}
	## else go via Tsparse.. {FIXME: a waste! - we already have 'xj' ..}
	## and inside  Tsparse... the above i1, i2,..., sel  are *all* redone!
	## Happens too often {not anymore, I hope!}
	##
	Matrix.msg("wasteful C -> T -> C in replCmat(x,i,j,v) for <sparse>[i,j] <- v")
	x <- as(x, "TsparseMatrix")
	if(iMi)
	    x[ ,i2+1L] <- value
	else if(jMi)
	    x[i1+1L, ] <- value
	else
	    x[i1+1L,i2+1L] <- value
	if(extends(clDx, "compMatrix") && length(x@factors)) # drop cashed ones
	    x@factors <- list()
    }# else{ not using new memory-sparse  code
    if(has.x && any0(x@x)) ## drop all values that "happen to be 0"
	as_CspClass(drop0(x), clx)
    else as_CspClass(x, clx)
} ## replCmat4

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "index", j = "missing",
                                value = "replValue"),
                 replCmat)

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "missing", j = "index",
                                value = "replValue"),
                 replCmat)

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "index", j = "index",
				value = "replValue"),
                 replCmat)

### When the RHS 'value' is  a sparseVector, now can use  replCmat  as well
setReplaceMethod("[", signature(x = "CsparseMatrix", i = "missing", j = "index",
				value = "sparseVector"),
		 replCmat)

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "index", j = "missing",
				value = "sparseVector"),
		 replCmat)

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "index", j = "index",
				value = "sparseVector"),
		 replCmat)

## A[ ij ] <- value,  where ij is (i,j) 2-column matrix
setReplaceMethod("[", signature(x = "CsparseMatrix", i = "matrix", j = "missing",
				value = "replValue"),
		 function(x, i, j, ..., value)
		 ## goto Tsparse modify and convert back:
		 as(.TM.repl.i.mat(as(x, "TsparseMatrix"), i=i, value=value),
		    "CsparseMatrix"))
## more in ./sparseMatrix.R (and ./Matrix.R )

setReplaceMethod("[", signature(x = "CsparseMatrix", i = "Matrix", j = "missing",
				value = "replValue"),
		 function(x, i, j, ..., value)
		 ## goto Tsparse modify and convert back:
		 as(.TM.repl.i.mat(as(x, "TsparseMatrix"), i=i, value=value),
		    "CsparseMatrix"))


setMethod("t", signature(x = "CsparseMatrix"),
	  function(x) .Call(Csparse_transpose, x, is(x, "triangularMatrix")))


## NB: have extra tril(), triu() methods for symmetric ["dsC" and "lsC"] and
##     for all triangular ones, where the latter may 'callNextMethod()' these:
setMethod("tril", "CsparseMatrix",
	  function(x, k = 0, ...) {
	      k <- as.integer(k[1])
	      dd <- dim(x); sqr <- dd[1] == dd[2]
	      stopifnot(-dd[1] <= k, k <= dd[1]) # had k <= 0
	      r <- .Call(Csparse_band, x, -dd[1], k)
	      ## return "lower triangular" if k <= 0
	      if(sqr && k <= 0) .gC2tC(r, uplo = "L") else r
	  })

setMethod("triu", "CsparseMatrix",
	  function(x, k = 0, ...) {
	      k <- as.integer(k[1])
	      dd <- dim(x); sqr <- dd[1] == dd[2]
	      stopifnot(-dd[1] <= k, k <= dd[1]) # had k >= 0
	      r <- .Call(Csparse_band, x, k, dd[2])
	      ## return "upper triangular" if k >= 0
	      if(sqr && k >= 0) .gC2tC(r, uplo = "U") else r
	  })

setMethod("band", "CsparseMatrix",
	  function(x, k1, k2, ...) {
	      k1 <- as.integer(k1[1])
	      k2 <- as.integer(k2[1])
	      dd <- dim(x); sqr <- dd[1] == dd[2]
	      stopifnot(-dd[1] <= k1, k1 <= k2, k2 <= dd[2])
	      r <- .Call(Csparse_band, diagU2N(x), k1, k2)
	      if(sqr && k1 * k2 >= 0) ## triangular
		  as(r, paste0(.M.kind(x), "tCMatrix"))
	      else if (k1 < 0  &&  k1 == -k2  && isSymmetric(x)) ## symmetric
		  as(r, paste0(.M.kind(x), "sCMatrix"))
	      else
		  r
	  })

setMethod("diag", "CsparseMatrix",
	  function(x, nrow, ncol) {
              ## "FIXME": could be more efficient; creates new ..CMatrix:
	      dm <- .Call(Csparse_band, diagU2N(x), 0, 0)
	      dlen <- min(dm@Dim)
	      ind1 <- dm@i + 1L	# 1-based index vector
	      if (is(dm, "nMatrix")) {
		  val <- rep.int(FALSE, dlen)
		  val[ind1] <- TRUE
	      }
	      else if (is(dm, "lMatrix")) {
		  val <- rep.int(FALSE, dlen)
		  val[ind1] <- as.logical(dm@x)
	      }
	      else {
		  val <- rep.int(0, dlen)
		  ## cMatrix not yet active but for future expansion
		  if (is(dm, "cMatrix")) val <- as.complex(val)
		  val[ind1] <- dm@x
	      }
	      val
	  })

setMethod("writeMM", "CsparseMatrix",
	  function(obj, file, ...)
	  .Call(Csparse_MatrixMarket, obj, path.expand(as.character(file))))

setMethod("Cholesky", signature(A = "CsparseMatrix"),
	  function(A, perm = TRUE, LDL = !super, super = FALSE, Imult = 0, ...)
	  Cholesky(as(A, "symmetricMatrix"),
		   perm=perm, LDL=LDL, super=super, Imult=Imult, ...))

## TODO (in ../TODO for quite a while .....):
setMethod("Cholesky", signature(A = "nsparseMatrix"),
	  function(A, perm = TRUE, LDL = !super, super = FALSE, Imult = 0, ...)
	  stop("Cholesky(<nsparse...>) -> *symbolic* factorization -- not yet implemented"))
