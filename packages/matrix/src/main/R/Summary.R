####--- All "Summary" group methods for all Matrix classes (incl sparseVector) ------
####         =======  but diagonalMatrix  -> ./diagMatrix.R and abIndex.R
####                                           ~~~~~~~~~~~~     ~~~~~~~~~

## M-x grep -E -e 'Method\("(Summary|max|min|range|all|any|prod|sum)"' *.R
##     ----

sG <- getGroupMembers("Summary")
if(FALSE)
    sG ## "max"   "min"   "range" "prod"  "sum"   "any"   "all"
## w/o "prod" & "sum":
summGener1 <- sG[match(sG, c("prod","sum"), 0) == 0]
rm(sG)

###---------- dMatrix

setMethod("Summary", "ddenseMatrix",
	  function(x, ..., na.rm) {
	      d <- x@Dim
	      if(any(d == 0)) return(callGeneric(numeric(0), ..., na.rm=na.rm))
	      clx <- getClassDef(class(x))
	      if(extends(clx, "generalMatrix"))
		  callGeneric(x@x, ..., na.rm = na.rm)
	      else if(extends(clx, "symmetricMatrix")) { # incl packed, pos.def.
		  if(.Generic %in% summGener1) {
		      callGeneric(if (length(x@x) < prod(d)) x@x
				  else x@x[indTri(d[1], upper= x@uplo == "U",
						  diag= TRUE)],
				  ..., na.rm = na.rm)
		  } else callGeneric(..2dge(x)@x, ..., na.rm = na.rm)
	      }
	      else { ## triangular , possibly packed
		  if(.Generic %in% summGener1) {
		      if(.Generic %in% c("any","all")) {
			  Zero <- FALSE; One <- TRUE; xx <- as.logical(x@x)
		      } else {
			  Zero <- 0; One <- 1; xx <- x@x
		      }
		      callGeneric(if (length(xx) < prod(d)) xx ## <- 'packed'
				  else xx[indTri(d[1], upper= x@uplo == "U",
						  diag= TRUE)],
				  if(d[1] >= 2) Zero, if(x@diag == "U") One,
				  ..., na.rm = na.rm)
		  } else callGeneric(..2dge(x)@x, ..., na.rm = na.rm)
	      }
	  })

setMethod("Summary", "dsparseMatrix",
	  function(x, ..., na.rm)
      {
	  ne <- prod(d <- dim(x))
	  if(ne == 0) return(callGeneric(numeric(0), ..., na.rm=na.rm))
	  n <- d[1]
	  clx <- getClassDef(class(x))
	  isTri <- extends(clx, "triangularMatrix")
	  if(extends(clx, "TsparseMatrix") && anyDuplicatedT(x, di = d))
	      x <- .Call(Tsparse_to_Csparse, x, isTri)# = as(x, "Csparsematrix")
	  l.x <- length(x@x)
	  if(l.x == ne) ## fully non-zero (and "general") - very rare but quick
	      return( callGeneric(x@x, ..., na.rm = na.rm) )
	  ## else  l.x < ne

	  isSym <- !isTri && extends(clx, "symmetricMatrix")
	  isU.tri <- isTri && x@diag == "U"
	  ## "full": has *no* structural zero : very rare, but need to catch :
	  full.x <- ((isSym && l.x == choose(n+1, 2)) ||
		     (n == 1 && (isU.tri || l.x == 1)))
	  isGener1 <- .Generic %in% summGener1
	  if(isGener1) { ## not prod() or sum() -> no need check for symmetric
	      ## we rely on  <generic>(x, NULL, y, ..)	:==  <generic>(x, y, ..):
	      if(any(.Generic == c("any","all"))) ## logic:
		  callGeneric(as.logical(x@x), if(!full.x) FALSE, if(isU.tri) TRUE,
			      ..., na.rm = na.rm)
	      else
		  callGeneric(x@x, if(!full.x) 0, if(isU.tri) 1,
			      ..., na.rm = na.rm)
	  }
	  else { ## prod() or sum() : care for "symmetric" and U2N
	      if(!full.x && .Generic == "prod") {
		  if(anyNA(x@x)) NaN else 0
	      }
	      else
		  callGeneric((if(isSym) as(x, "generalMatrix") else x)@x,
			      if(!full.x) 0, # one 0 <==> many 0's
			      if(isU.tri) rep.int(1, n),
			      ..., na.rm = na.rm)
	  }
      })

###---------- ldenseMatrix

if(FALSE) # not correct (@x may contain "wrong" in "other" triangel
setMethod("all", "lsyMatrix",
          function(x, ..., na.rm = FALSE)
          all(x@x, ..., na.rm = na.rm))
if(FALSE) # replaced by "Summary" below
## Note: the above "lsy*" method is needed [case below can be wrong]
setMethod("all", "ldenseMatrix",
	  function(x, ..., na.rm = FALSE) {
	      if(prod(dim(x)) >= 1)
		  (!is(x, "triangularMatrix") && !is(x, "diagonalMatrix") &&
		   all(x@x, ..., na.rm = na.rm))
	      else all(x@x, ..., na.rm = na.rm)
	  })

## almost copy_paste from "ddenseMatrix" above
Summ.ln.dense <- function(x, ..., na.rm) {
    d <- x@Dim
    if(any(d == 0)) return(callGeneric(logical(0), ..., na.rm=na.rm))
    ext <- extends(getClassDef(class(x)))
    if(any("generalMatrix" == ext))
	callGeneric(x@x, ..., na.rm = na.rm)
    else if(any("symmetricMatrix" == ext)) { # incl packed, pos.def.
	if(.Generic != "sum") { ## i.e., %in% summGener1
	    callGeneric(if (length(x@x) < prod(d)) x@x
			else x@x[indTri(d[1], upper= x@uplo == "U",
					diag= TRUE)],
			..., na.rm = na.rm)
	} else ## sum() -- FIXME-faster: use x@x[indTri(...)] similar to above
	    callGeneric(as(x, paste0(if(any("ldenseMatrix" == ext)) "l" else "n", "geMatrix"))@x,
			..., na.rm = na.rm)
    }
    else { ## triangular , possibly packed
	if(.Generic != "sum") ## incl. prod() !
	    callGeneric(x@x, if(d[1] >= 2) FALSE, if(x@diag == "U") TRUE, ..., na.rm = na.rm)
	else ## sum() -- FIXME-faster: using indTri()..; in unit-diag. case: plus  n x TRUE = d[1]
	    ## if packed: sum(x@x, if(x@diag == "U") d[1], ..., na.rm = na.rm)
	    callGeneric(as(x, paste0(if(any("ldenseMatrix" == ext)) "l" else "n", "geMatrix"))@x,
			..., na.rm = na.rm)
    }
}

setMethod("Summary", "ldenseMatrix", Summ.ln.dense)
setMethod("Summary", "ndenseMatrix", Summ.ln.dense)


###---------- lMatrix

setMethod("any", "lMatrix",
	  function(x, ..., na.rm = FALSE)
	  ## logical unit-triangular has TRUE diagonal:
	  (prod(dim(x)) >= 1 && is(x, "triangularMatrix") && x@diag == "U") ||
	  any(x@x, ..., na.rm = na.rm))

###---------- lsparseMatrix

##------- Work via  as(*, lgC) : ------------

setMethod("all", "lsparseMatrix",
	  function(x, ..., na.rm = FALSE) {
	      d <- x@Dim
	      l.x <- length(x@x)
	      if(l.x == prod(d)) ## fully non-zero
		  all(x@x, ..., na.rm = na.rm)
	      else if(is(x, "symmetricMatrix") && l.x == choose(d[1]+1, 2)) {
		  if(.Generic %in% summGener1)
		      all(x@x, ..., na.rm = na.rm)
		  else all(as(x, "generalMatrix")@x, ..., na.rm = na.rm)
	      }
	      else FALSE ## has at least one structural 0
	  })


###---------- Matrix

## For all other Matrix objects {and note that "all" and "any" have their own}:

setMethod("all", "Matrix",
	  function(x, ..., na.rm)
	  callGeneric(as(x, "lMatrix"), ..., na.rm=na.rm))

setMethod("any", "Matrix",
	  function(x, ..., na.rm)
	  callGeneric(as(x, "lMatrix"), ..., na.rm=na.rm))

setMethod("Summary", "Matrix", ## FIXME (too cheap): all(<lMatrix>) should not go via dMatrix!!
	  function(x, ..., na.rm)
	  callGeneric(as(x,"dMatrix"), ..., na.rm = na.rm))

## Try to make   min(1, <Matrix>)  work, i.e., not dispatch on first arg to .Primitive
## This for(..) gives {during installation}
## Error in setGeneric(F, signature = "...") :
##   ‘max’ is a primitive function;  methods can be defined, but the generic function is implicit, and cannot be changed.
if(FALSE)
for(F in c("max", "min", "range", "prod", "sum", "any", "all")) {
    setGeneric(F, signature = "...")
}
## try on "min" for now --- ~/R/Pkgs/Rmpfr/R/mpfr.R is the example (for "pmin")
if(FALSE)## This gives error message that the "ANY" is method is sealed ...
setMethod("min", "ANY",
	  function(..., na.rm = FALSE) {
	      args <- list(...)
	      if(all(isAtm <- vapply(args, is.atomic, NA)))
		  return( base::min(..., na.rm = na.rm) )
              ## else try to dispatch on an argument which is a Matrix.. or in a
              if(any(isM <- vapply(args, is, NA, class2="Matrix"))) {
                  ## swap the Matrix with the first argument
                  i <- which.max(isM)# the first "Matrix"
                  if(i == 1)
                      stop("programming error: min() should have dispatched w/ 1st arg much earlier")
              } else { ## if no "Matrix", take the first non-atomic argument
                  ## (FIXME: should take the first for which there is a method !)
                  i <- which.max(!isAtm)
              }
              ii <- seq_along(args)
              ii[c(1,i)] <- c(i,1)
              do.call(min, c(args[ii], list(na.rm=na.rm)))
          })

if(FALSE) { ## FIXME: it does *not* solve the problem anyway ..
##
##  (m <- Matrix(c(0,0,2:0), 3,5))
##   min(1,m)
##-> error, as it calls the .Primitive min() and that does *not* dispatch on 2nd arg
##
setMethod("Summary", "ANY",
	  function(x, ..., na.rm) {
          if(!length(a <- list(...))) (get(.Generic, envir=baseenv()))(x, na.rm=na.rm)
          else {
              if(!is.null(v <- getOption("Matrix.verbose")) && v >= 1)
                  if(length(a) > 1)
                      message(gettextf("in Summary(<ANY>, .): %s(<%s>, <%s>,...)\n",
                                       .Generic, class(x), class(a[[1]])), domain = NA)
                  else
                      message(gettextf("in Summary(<ANY>, .): %s(<%s>, <%s>)\n",
                                       .Generic, class(x), class(a[[1]])), domain = NA)

              do.call(.Generic, c(x, a, list(na.rm=na.rm)))
	  }})
}## {does not help --> not used}

Summary.l <- function(x, ..., na.rm) { ## must be method directly
    if(.Generic %in% c("all", "any"))
	callGeneric(x@x, ..., na.rm = na.rm)
    else {
	r <- callGeneric(as(x,"dMatrix"), ..., na.rm = na.rm)
	if(.Generic != "prod" && !any(is.infinite(r))) as.integer(r) else r
    }
}
## almost identical:
Summary.np <- function(x, ..., na.rm) {
    if(.Generic %in% c("all", "any"))
	callGeneric(as(x, "lMatrix"), ..., na.rm = na.rm)
    else {
	r <- callGeneric(as(x,"dMatrix"), ..., na.rm = na.rm)
	if(.Generic != "prod" && !any(is.infinite(r))) as.integer(r) else r
    }
}
##
setMethod("Summary", "lMatrix", Summary.l)
setMethod("Summary", "nMatrix", Summary.np)
setMethod("Summary", "indMatrix", Summary.np)

###---------- nsparseMatrix

setMethod("all", "nsparseMatrix",
	  function(x, ..., na.rm = FALSE) {
	      pd <- prod(d <- dim(x))
	      if(pd == 0) return(TRUE)
	      cld <- getClassDef(class(x))
	      if(extends(cld, "triangularMatrix"))
		  return(FALSE)
	      ## else
	      if(extends(cld, "TsparseMatrix"))
		  cld <- getClassDef(class(x <- as(x, "CsparseMatrix")))
	      ## now have Csparse or Rsparse: length of index slot = no.{TRUE}
	      l.x <- length(if(extends(cld, "CsparseMatrix")) x@i else x@j)

	      (l.x == pd) || ## fully non-zero
	      (extends(cld, "symmetricMatrix") && l.x == choose(d[1]+1, 2))
	      ## else FALSE
	  })

setMethod("any", "nsparseMatrix",
	  function(x, ..., na.rm = FALSE) {
	      if(any(dim(x) == 0)) return(FALSE)
	      cld <- getClassDef(class(x))
	      if(extends(cld, "triangularMatrix") && x@diag == "U")
		  TRUE # unit-diagonal
	      else if(extends(cld, "CsparseMatrix") ||
		      extends(cld, "TsparseMatrix"))
		  length(x@i) > 0
	      else # RsparseMatrix
		  length(x@j) > 0
	  })


###---------- sparseVector

setMethod("Summary", "nsparseVector",
	  function(x, ..., na.rm) { ## no 'x' slot, no NA's ..
	      n <- x@length
	      l.x <- length(x@i)
	      if(l.x == n)
		  callGeneric(rep.int(TRUE, n), ..., na.rm = na.rm)
	      else ## l.x < n :	 has some FALSE entries
		  switch(.Generic,
			 "prod" = 0,
			 "min"	= 0L,
			 "all" = FALSE,
			 "any" = l.x > 0,
			 "sum" = l.x,
			 "max" = as.integer(l.x > 0),
			 "range" = c(0L, as.integer(l.x > 0)))
	  })

## The "other" "sparseVector"s ("d", "l", "i" ..): all have an	'x' slot :
setMethod("Summary", "sparseVector",
	  function(x, ..., na.rm) {
	      n <- x@length
	      l.x <- length(x@x)
	      if(l.x == n) ## fully non-zero (and "general") - very rare but quick
		  callGeneric(x@x, ..., na.rm = na.rm)
	      else if(.Generic != "prod") {
		  ## we rely on	 <generic>(x, NULL, y, ..) :==	<generic>(x, y, ..):
		  if(any(.Generic == c("any","all"))) ## logic:
		      callGeneric(as.logical(x@x), FALSE, ..., na.rm = na.rm)
		  else # "numeric"
		      callGeneric(x@x, 0, ..., na.rm = na.rm)
	      }
	      else { ## prod()
		  if(anyNA(x@x)) NaN else 0
	      }
	  })
