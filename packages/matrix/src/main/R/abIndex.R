#### Methods for the "abIndex" := ``abstract Index'' class

### Note: this partly builds on ideas and code from  Jens Oehlschlaegel,
### ----  as implemented (in the GPL'ed part of) package 'ff'.

## Basic idea:  a vector  x  of integer indices often has long stretches
##              i, i+1, i+2, ...  such that diff(x) has stretches of '1'.
## 		Now keep x[1] =: first and diff(x) =: d,
## 		and use rle() to encode d.  Here, use a C version for rle()
rleMaybe <- function(i, force = FALSE) {
    ## TODO: move all this to a new C fnc., still keeping the *_i() and *_d()
    if(is.na(force <- as.logical(force)))
        stop("'force' must be (coercable to) TRUE or FALSE")
    int <- is.integer(i) || is.logical(i) || {
        i. <- suppressWarnings(as.integer(i))
        if(r <- isTRUE(all(is.na(i) | i. == i))) i <- i.
        r }
    ## if(int),  'i' will be coerced to integer on C level
    ##N R-devel codetools get FP again:
    ##N Matrix.rle <- if(int) Matrix_rle_i else Matrix_rle_d
    ##N .Call(Matrix.rle, i, force)
    if(int) Matrix_rle_d <- Matrix_rle_i
    .Call(Matrix_rle_d, i, force)
}

.rle <- function(lengths, values)
    structure(list(lengths = lengths, values = values), class = "rle")

##' @param x
##'
##' @return diff(x), giving '0' for 'Inf - Inf' or similar
.diff <- function(x) {
    ## TODO:  considerably faster in C
    if((n <- length(x)) <= 1) return(x[0])
    r <- (x1 <- x[-1]) - (x2 <- x[-n])
    if(any(ina <- is.na(r)))
	r[ina & (x1 == x2 | (is.na(x1) & is.na(x2)))] <- 0
    r
}

##' @param from: logical or numeric vector
##'
##' @return an "abIndex" vector, "semantically equivalent" to 'from'
vec2abI <- function(from, force = FALSE) {
    ans <- new("abIndex")
    r <- rleMaybe(.diff(from), force=force)## .diff(): also work for rep(Inf, *)
    if(is.null(r)) { ## no "compression"
        ans@kind <- if(is.integer(from)) "int32" else "double"
        ans@x <- from
    } else {
        ans@kind <- "rleDiff"
        ## ans@x <- integer(0) # <- prototype does that
        ans@rleD <- new("rleDiff", first = from[1], rle = r)
    }
    ans
}

## "abIndex" version of  indDiag(n) === which(diag(n) == 1) -> ./Auxiliaries.R
abIindDiag <- function(n) {
    ## cumsum(c(1L, rep.int(n+1L, n-1)))
    stopifnot((n <- as.integer(n)) >= 1)
    rl <- if(n == 1) .rle(n[0],n[0]) else .rle(n-1L, n+1L)
    new("abIndex", kind = "rleDiff",
        rleD = new("rleDiff", first = 1, rle = rl))
}

## "abIndex" version of  indTri(n) ... --> ./Auxiliaries.R
abIindTri <- function(n, upper = TRUE, diag = FALSE) {
    ## Indices of strict upper/lower triangular part
    ## == which(upper.tri(diag(n), diag=diag) or
    ##	  which(lower.tri(diag(n), diag=diag) -- but as abIndex
    stopifnot(length(n) == 1, n == (n. <- as.integer(n)), (n <- n.) >= 0)
    if(n <= 2) {
	vec2abI(
		if(n == 0) integer(0)
		else if(n == 1) { if(diag) 1L else integer(0) }
		else { ## n == 2
		    v <- if(upper) 3L else 2L
		    if(diag) c(1L, v, 4L) else v
		})
    }
    else { ## n >= 3 [also for n == 2 && diag (==TRUE)] :
	## First, compute the 'diff(.)' of the result [fast, using integers]
	n. <- if(diag) n else n - 1L
	n1 <- n. - 1L
	tt <- if(diag) 2L else 3L
	mk1s <- function(n,m) as.vector(rbind(1L, n:m))
	mks1 <- function(n,m) as.vector(rbind(n:m, 1L))
	rl <- .rle(lengths= if(upper) mk1s(1L,n1) else mks1(n1,1L),
		   values = if(upper) mks1(n, tt) else mk1s(tt, n))
	frst <- if(diag) 1L else if(upper) n+1L else 2L
	new("abIndex", kind = "rleDiff",
	    rleD = new("rleDiff", first = frst, rle = rl))
    }
}


setAs("numeric", "abIndex", function(from) vec2abI(from))
setAs("logical", "abIndex", function(from) vec2abI(from))

setMethod("show", "rleDiff",
	  function(object) {
	      cat(sprintf(## first can be 'NULL' --> cannot use %g
	" RLE difference (class 'rleDiff'): first = %s, \"rle\":%s",
			  format(object@first),
			  if(length(rl <- object@rle)) "\n" else " "))
	      print(rl, prefix = "   ")
	      invisible(object)
	  })

setMethod("show", "abIndex",
	  function(object) {
	      knd <- object@kind
	      cat(sprintf(
	"Abstract Index vector (class 'abIndex') of length %.0f, kind \"%s\"\n",
			  length(object), knd))
	      if(knd == "rleDiff") {
### FIXME: show something like this is equivalent to  c(2:10, 13:34, ...)
		  cat(" and slot \"rleD\":\n")
		  show(object@rleD)
	      } else {
		  cat(" and \"x\" slot\n")
		  show(object@x)
	      }
	      invisible(object)
	  })


##' Constructor of "abIndex" version of  n:m
##' @param from
##' @param to
##'
##' @return an "abIndex" object semantically equivalent to  from:to
abIseq1 <- function(from = 1, to = 1) {
    stopifnot(length(from) == 1L, length(to) == 1L)
    to <- to - from
    new("abIndex", kind="rleDiff", rleD =
	new("rleDiff", first = as.integer(from), rle =
	    .rle(lengths = abs(to),# <- double : maybe > .Machine$integer.max
		 values = as.integer(sign(to)))))
}

## an "abIndex" version of seq(), i.e. seq.default():
abIseq <- function(from = 1, to = 1, by = ((to - from)/(length.out - 1)),
		   length.out = NULL, along.with = NULL)
{
    if((One <- nargs() == 1L) && !missing(from)) {
	lf <- length(from)
	return(if(mode(from) == "numeric" && lf == 1L) abIseq1(1L, from) else
	       if(lf) abIseq1(1L, lf) else new("abIndex"))
    }
    if(!missing(along.with)) {
	length.out <- length(along.with)
	if(One) return(if(length.out) abIseq1(1L, length.out) else new("abIndex"))
    }
    else if(!missing(length.out))
	length.out <- ceiling(length.out)
    if(is.null(length.out))
	if(missing(by))
	    abIseq1(from,to)
	else { # dealing with 'by'
	    del <- to - from
	    if(del == 0 && to == 0) return(as(to, "abIndex"))
	    n <- del/by
	    if(!(length(n) && is.finite(n))) {
		if(length(by) && by == 0 && length(del) && del == 0)
		    return(as(from, "abIndex"))
		stop("invalid (to - from)/by in seq(.)")
	    }
	    if(n < 0L)
		stop("wrong sign in 'by' argument")
	    if(n > .Machine$integer.max)
		stop("'by' argument is much too small")

	    dd <- abs(del)/max(abs(to), abs(from))
	    if (dd < 100*.Machine$double.eps) return(from)
	    n <- as.integer(n + 1e-7)
	    x <- from + abIseq1(0L,n) * by
	    ## correct for overshot because of fuzz -- FIXME: need pmin() for "abIndex":
	    if(by > 0) pmin(x, to) else pmax(x, to)
	}
    else if(!is.finite(length.out) || length.out < 0L)
	stop("length must be non-negative number")
    else if(length.out == 0L)
	new("abIndex")
    else if (One) abIseq1(1L, length.out)
    else if(missing(by)) {
	# if(from == to || length.out < 2) by <- 1
	if(missing(to))
	    to <- from + length.out - 1L
	if(missing(from))
	    from <- to - length.out + 1L
	if(length.out > 2L)
	    if(from == to)
		rep2abI(from, length.out) ## rep.int(from, length.out)
	    else c(as(from,"abIndex"),
                   from + abIseq1(1L, length.out - 2L) * by, to)
	else as(c(from, to)[seq_len(length.out)],"abIndex")
    }
    else if(missing(to))
	from + abIseq1(0L, length.out - 1L) * by
    else if(missing(from))
	to - abIseq1(length.out - 1L, 0L) * by
    else stop("too many arguments")
}

##'  rep.int(x, times)    " as abIndex "
##' @param x   numeric vector
##' @param times  integer (valued) scalar: the number of repetitions
##'
##' @return an "abIndex" vector
rep2abI <- function(x, times) {
    r <- new("abIndex")
    if((n <- length(x)) == 0)
        return(r)
    if(n == 1) { # clear case for compression
        r@kind <- "rleDiff"
        rD <- new("rleDiff")
        rD@first <- x[1L]
        rD@rle <- .rle(lengths = times - 1L, values = 0L)
        r@rleD <- rD
    } else { ## n >= 2 .. check if compression is worth it:
        ## .. say if compression of  x itself is worth {FIXME? optimal cutoff}
        rr <- rleMaybe(.diff(x))
	if(is.null(rr)) {
	    r@kind <- if(is.integer(x)) "int32" else "double"
	    r@x <- rep.int(x, times)
	} else {
	    r@kind <- "rleDiff"
	    rD <- new("rleDiff")
	    rD@first <- x[1L]
	    Dx <- x[1L] - x[length(x)]
	    N <- (length(rr$lengths) + 1L)*times
	    rD@rle <- .rle(lengths = rep.int(c(rr$lengths, 1L), times)[-N],
			   values =  rep.int(c(rr$values,  Dx), times)[-N])
	    r@rleD <- rD
	}
    }
    r
}

combine_rleD <- function(rleList, m = length(rleList))
{
    ## Combine list of "rleDiff"s into a new one -- for c(..)
    ## auxiliary (and main working horse) for  c.abIndex()

### TODO: really should do this in C

    i1 <- unlist(lapply(rleList, slot, "first"))
    rles <- lapply(rleList, slot, "rle")
    ## the list of vectors of 'lengths' and 'values' :
    lens <- lapply(rles, `[[`, "lengths")
    vals <- lapply(rles, `[[`, "values")
    ## the 'ends' are needed for the "jump sizes" in between:
    ends2 <- function(x) # related to ends.rleD() above
        x@first + c(0, with(x@rle, sum(lengths*values)))
    ends <- unlist(lapply(rleList, ends2))[-c(1, 2*m)]
    ii <- 2L*seq_len(m - 1)
    d.ends <- ends[ii] - ends[ii-1L]

    ## llen1 <- unlist(lapply(lens, length)) + 1L
    ## n <- sum(llen1)
    n <- m + sum(lengths(lens, use.names=FALSE))
    ## comb(): intersperse x2[[j]] between lis[[j] & lis[[j+1]] :
    comb <- function(lis, x2)
        unlist(mapply(c, lis, x2, SIMPLIFY=FALSE, USE.NAMES=FALSE))
    n.len <- comb(lens, 1L)[-n]
    n.val <- comb(vals, c(d.ends,NA))[-n]
    new("rleDiff", first = i1[1],
	rle = .rle(lengths = n.len, values = n.val))
} ## {combine_rleD}

## For now -- S4 method on c(), i.e., setMethod("c", ...)
## seems "difficult", and this works "magically"
## when the first argument is an abIndex :
c.abIndex <- function(...)
{
    m <- length(list(...))
    if(m <= 1)
	return(if(m == 0) new("abIndex") else as(..1, "abIndex"))
    ## else:  have length m >= 2
    labi <- lapply(list(...), as, Class = "abIndex")
    knd <- unlist(lapply(labi, slot, "kind"))
    ## Convention: Result kind should be the 'kind' of the first
    neq.k <- knd != (k1 <- knd[1])
    if(any(neq.k)) {
	if(all(not.rD <- knd != "rleDiff")) { ## either "double" or "int32" .. using 'x'
	    k1 <- "double"
	    ## and it will just work to c(.) the 'x' slots
	}
	else {
	    warning("c(<abIndex>,..) of different kinds, coercing all to 'rleDiff'")
	    labi[not.rD] <- lapply(labi[not.rD],
				   function(av) vec2abI(av@x, force=TRUE))
	    k1 <- "rleDiff"
        }
    }
    switch(k1,
	   "rleDiff" = {
	       new("abIndex", kind="rleDiff",
		   rleD = combine_rleD(lapply(labi, slot, "rleD"), m))
	   },
	   "double" =, "int32" = {
	       new("abIndex", kind = k1,
		   x = do.call(c, lapply(labi, slot, "x")))
	   })
}

setMethod("length", "abIndex", function(x)
	  if(identical(x@kind, "rleDiff"))
	  sum(x@rleD@rle$lengths)+ 1L else length(x@x))

abI2num <- function(from) {
    switch(from@kind,
	   "rleDiff" = {
	       x <- from@rleD
	       ## as  inverse.rle():
	       cumsum(c(x@first, rep.int(x@rle$values, x@rle$lengths)))
	   },
	   "int32" =, "double" = from@x)
}
setAs("abIndex", "numeric", abI2num)
setAs("abIndex", "vector",  abI2num)
setAs("abIndex", "integer", function(from) as.integer(abI2num(from)))
## for S3 lovers and back-compatibility:
setMethod(as.integer, "abIndex", function(x) as.integer(abI2num(x)))
setMethod(as.numeric, "abIndex", function(x) abI2num(x))
setMethod(as.vector,  c(x = "abIndex", mode = "ANY"), function(x) abI2num(x))
setMethod(as.vector,  c(x = "abIndex", mode = "character"),
	  ## this is beautiful -- because of as() !
	  function(x, mode) as(abI2num(x), mode))

## Need   max(<i>), min(<i>),   all(<i> == <j>)   any(<i> == <j>)

## --->  Groups  "Summary"  and "Compare"  (maybe all "Ops")

## For that, we really  need  "[" and/or  "rep"() methods -- TODO --
##
setMethod("[", signature(x = "abIndex", i = "index"),
	  function (x, i, j, ..., drop)
      {
          switch(x@kind,
		 "rleDiff" = {
		     ## FIXME
		     ## intIv() in ./sparseVector.R -- not memory-efficient (??)
		     ## n <- length(x)
		     ## ii <- intIv(i, n) ## ii : 1-based integer indices
		     ## d <- x@rleD
		     ## Now work with the equivalent of
		     ##   cumsum(c(d@first, rep.int(d@rle$values, d@rle$lengths)))

                     stop("<abIndex>[i]  is not yet implemented")
                 },
                 "int32" =, "double" =
                 ## as it's not rle-packed, can remain simple:
                 x@x[i])
      })

##' Endpoints of all linear stretches -- auxiliary for range(.)
##' @param x an "rleDiff" object
##'
##' @return numeric vector of end points of all linear stretches of x.
ends.rleD <- function(x)
{
    rl <- x@rle
    stopifnot(length(lens <- rl$lengths) == length(vals <- rl$values))
    cumsum(c(x@first, lens*vals))
}

##' Collapse or "uniquify" an 'rle' object, i.e.,
##'  1) drop 'lengths' 0 parts
##'  2) *merge* adjacent parts where 'values' are the same
##'
##' @param x an "rle" object
##'
##' @return an "rle" object, a "unique" version of the input 'x'
rleCollapse <- function(x)
{
    ## TODO: faster (and simpler!) in C
    ## TODO(2): move this to 'R base'
    L <- x$lengths
    V <- x$values
    chng <- FALSE
    if((chng <- any(i0 <- L == 0))) { ## drop 0 'lengths' parts
        L <- L[!i0] ; V <- V[!i0]
    }
    ## FIXME: This is not elegant nor efficient:
    while(any(i0 <- diff(V) == 0)) { ## merge adjacent parts with same values
        if(!chng) chng <- TRUE
        ## fix one stretch (and repeat), starting at  ii0  and total length  1+ li0
        ii0 <- which.max(i0)# index of first TRUE
        li0 <- if((l0 <- length(i0)) <= ii0) 1 else which.min(!i0[ii0:l0])
        stopifnot(li0 >= 1)## <- for now
        L[ii0] <- sum(L[ii0+(0:li0)])
        ii <- -(ii0 + seq_len(li0))
        L <- L[ii]
        V <- V[ii]
    }
    if(chng) { x$lengths <- L ; x$values <- V }
    x
} ## {rleCollapse}

setMethod("drop", "abIndex",
	  function(x) {
	      if(x@kind == "rleDiff")
		  x@rleD@rle <- rleCollapse(x@rleD@rle)
	      x
	  })


## Summary: { max, min, range, prod, sum, any, all } :
## have  'summGener1' := those without prod, sum

setMethod("Summary", signature(x = "abIndex", na.rm = "ANY"),
          function(x, ..., na.rm)
      {
          switch(x@kind,
                 "rleDiff" =
             {
                 d <- x@rleD
                 if(.Generic %in% c("range","min","max")) {
                     callGeneric(ends.rleD(d), ..., na.rm=na.rm)
                 } else { ## "sum", "prod" :
                     switch(.Generic,
                            "all" = {
                                ## these often, but *not* always come in pairs
				## en <- ends.rleD(d)
                                ## so maybe it does not really help!

                                stop("all(<abIndex>) is not yet implemented")

                                ## all(c(d@first, d@rle$values), ..., na.rm=na.rm)
                            },
                            "any" = any(c(d@first, d@rle$values), ..., na.rm=na.rm),
                            "sum" = {
                                stop("sum(<abIndex>) is not yet implemented")
                            },
                            "prod"= {
                                stop("prod(<abIndex>) is not yet implemented")
                            })
                 }
             },
                 "int32" =, "double" = callGeneric(x@x, ..., na.rm = na.rm)
                 )
      })

### "Ops" :=  sub-groups   "Arith", "Compare", and "Logic"
##
## For now (*), only "Arith" does make sense
## --> keep "Ops" undefined and define "Arith" :
## ----
## (*) :  TODO: logical <-> abIndex --> "Compare" etc as well
setMethod("Ops", signature(e1 = "abIndex", e2 = "abIndex"),
	  function(e1, e2) { .bail.out.2(.Generic, class(e1), class(e2)) })
setMethod("Ops", signature(e1 = "abIndex", e2 = "ANY"),
	  function(e1, e2) { .bail.out.2(.Generic, class(e1), class(e2)) })
setMethod("Ops", signature(e1 = "ANY", e2 = "abIndex"),
	  function(e1, e2) { .bail.out.2(.Generic, class(e1), class(e2)) })

setMethod("Arith", signature(e1 = "abIndex", e2 = "abIndex"),
	  function(e1, e2)
      {
          l1 <- length(e1)
          l2 <- length(e2)
          mM <- range(l1,l2)

          stop("not yet implemented")
          ## FIXME ------------------

          if(mM[1] != mM[2]) { ## lengths differ
              if(mM[1] %% mM[2] != 0) ## identical warning as in main/arithmetic.c
                  warning("longer object length\n\tis not a multiple of shorter object length")
              if(l1 < l2) {

              } else { ## l1 > l2

              }
          }
          switch(e1@kind,
                 "rleDiff" = {

                 },
                 "int32" =, "double" = {

                 })
      })


## numLike = {numeric, logical}:
setMethod("Arith", signature(e1 = "abIndex", e2 = "numLike"),
	  function(e1, e2)
      {
	  if(!length(e1)) return(e1)
	  if(e1@kind != "rleDiff") {	# no compression
	      e1@x <- callGeneric(e1@x, e2)
	      if(e1@kind != "double" && is.double(e1@x))
		  e1@kind <- "double"
	      return(e1)
	  }
	  if(length(e2) == 1) { ## scalar
	      if(is.na(e2))
		  return(rep2abI(e2, length(e1)))
	      ## else 'e2' is not NA and scalar
	      switch(.Generic,
		     "+" =, "-" = {
			 e1@rleD@first <- callGeneric(e1@rleD@first, e2)
			 e1
		     },
		     "*" = {
			 e1@rleD@first <- e1@rleD@first * e2
                         r <- e1@rleD@rle$values * e2
			 if(is0(e2) && all0(r)) {
			     ## result all 0: collapse
			     e1@rleD@rle$values <- r[1L]
			     e1@rleD@rle$lengths <- sum(e1@rleD@rle$lengths)
			 }
                         else ## normal case
                             e1@rleD@rle$values <- r
			 e1
		     },
		     "/" = {
                         if(is0(e2) ## division by 0
                            && length(unique(sign(ends.rleD(e1@rleD)))) > 1) {
                             ## at least one subsequence contains 0, i.e., changes sign:
			     warning("x / 0 for an <abIndex> x with sign-change\n no longer representable as 'rleDiff'")
                             return(vec2abI(abI2num(e1) / 0))
                         }
                         e1@rleD@first <- e1@rleD@first / e2
                         e1@rleD@rle$values <- e1@rleD@rle$values / e2
			 e1
		     },
		     "^" = {
			 if(e2 == 1) e1 else vec2abI(abI2num(e1) ^ e2)
		     },
		     "%%" = , "%/%" = vec2abI(callGeneric(abI2num(e1), e2)))
	  }
	  else ## length(e2) != 1
	      callGeneric(e1, as(e2, "abIndex"))
      })

setMethod("Arith", signature(e1 = "numLike", e2 = "abIndex"),
	  function(e1, e2)
      {
	  if(!length(e2)) return(e2)
	  if(e2@kind != "rleDiff") {	# no compression
	      e2@x <- callGeneric(e1, e2@x)
	      if(e2@kind != "double" && is.double(e2@x))
		  e2@kind <- "double"
	      return(e2)
	  }
	  if(length(e1) == 1) { ## scalar
	      if(is.na(e1))
		  return(rep2abI(e1, length(e2)))
	      ## else 'e1' is not NA and scalar
	      switch(.Generic,
		     "+" = {
			 e2@rleD@first <- e1 + e2@rleD@first
			 e2
		     },
		     "-" = {
			 e2@rleD@first <- e1 - e2@rleD@first
			 e2@rleD@rle$values <- -e2@rleD@rle$values
			 e2
		     },
		     "*" = {
			 e2@rleD@first <- e1 * e2@rleD@first
			 r <- e1 * e2@rleD@rle$values
			 if(is0(e1) && all0(r)) {
			     ## result all 0: collapse
			     e2@rleD@rle$values <- r[1L]
			     e2@rleD@rle$lengths <- sum(e2@rleD@rle$lengths)
			 }
			 else ## normal case
			     e2@rleD@rle$values <- r
			 e2
		     },
		     "/" = , "^" =,
		     "%%" = , "%/%" = vec2abI(callGeneric(e1, abI2num(e2))))
	  }
	  else ## length(e1) != 1
	      callGeneric(as(e1, "abIndex"), e2)
      })

setMethod("is.na", signature(x = "abIndex"),
	  function(x) {
	      if(x@kind != "rleDiff") is.na(x@x)
	      else {
		  rd <- x@rleD
                  rl <- rd@rle
                  len <- 1+ sum(L <- rl$lengths)
		  if(is.na(rd@first))
                      rep.int(TRUE, len)
                  else { ## interesting case
                      V <- rl$values
                      if(!any(ina <- is.na(V))) rep.int(FALSE, len)
                      else { ## at least one V is NA --> "x" is NA from then on:
                          k <- match(TRUE,ina) # the first one
                          l1 <- 1+ sum(L[seq_len(k-1)])
                          c(rep.int(FALSE, l1), rep.int(TRUE, len - l1))
                      }
                  }
	      }
	  })
## TODO ??   "is.nan"  analogously ??
##
setMethod("is.finite", signature(x = "abIndex"),
	  function(x) {
	      if(x@kind != "rleDiff") is.finite(x@x)
	      else {
		  rd <- x@rleD
                  rl <- rd@rle
                  len <- 1+ sum(L <- rl$lengths)
		  if(!is.finite(rd@first))
                      rep.int(FALSE, len)
                  else { ## interesting case
                      V <- rl$values
                      if(all(iFin <- is.finite(V))) rep.int(TRUE, len)
                      else { ## at least one V is +- Inf --> "x" is Inf/NaN from there
                          k <- match(FALSE,iFin) # the first non-finite one
                          l1 <- 1+ sum(L[seq_len(k-1)])
                          c(rep.int(TRUE, l1), rep.int(FALSE, len - l1))
                      }
                  }
	      }
	  })
setMethod("is.infinite", signature(x = "abIndex"),
	  function(x) {
	      if(x@kind != "rleDiff") is.infinite(x@x)
	      else {
		  rd <- x@rleD
                  rl <- rd@rle
                  len <- 1+ sum(L <- rl$lengths)
		  if(is.infinite(rd@first))
                      rep.int(TRUE, len)
                  else { ## interesting case
                      V <- rl$values
                      if(!any(iInf <- is.infinite(V))) rep.int(FALSE, len)
                      else { ## at least one V is +- Inf --> "x" is Inf/NaN from there
                          k <- match(TRUE,iInf) # the first one
                          l1 <- 1+ sum(L[seq_len(k-1)])
                          ## FIXME? do *not* consider 'NaN' (changing TRUE to FALSE):
                          c(rep.int(FALSE, l1), rep.int(TRUE, len - l1))
                      }
                  }
	      }
	  })


all.equal.abI <- function(target, current, ...)
{
    if(!is(target, "abIndex") || !is(current, "abIndex"))
	return(paste0("target is ", data.class(target), ", current is ",
		      data.class(current)))
    lt <- length(target)
    lc <- length(current)
    if(lt != lc)
	paste0("abIndex", ": lengths (", lt, ", ", lc, ") differ")
    else if(target@kind == current@kind) {
	all.equal.default(target, current, ...)
    } else ## different 'kinds' -- take "easy" exit:
	all.equal(abI2num(target), abI2num(current), ...)
} ## {all.equal.abI}

setMethod("all.equal", c(target = "abIndex", current = "abIndex"),
	  all.equal.abI)
setMethod("all.equal", c(target = "abIndex", current = "numLike"),
	  function(target, current, ...)
	  all.equal.abI(target, as(current, "abIndex"), ...))
setMethod("all.equal", c(target = "numLike", current = "abIndex"),
	  function(target, current, ...)
	  all.equal.abI(as(target, "abIndex"), current, ...))

## Then I want something like  get.ind.sel(.)  [ ./Tsparse.R ] working,
## i.e. possibly   match(i, <abI>, nomatch = 0)

setAs("seqMat", "numeric", function(from)
  {
      do.call(c, lapply(seq_len(ncol(from)), function(j)
                        seq(from=from[1L,j], to = from[2L,j])))
  })

setAs("numeric", "seqMat",
      function(from) as(as(from, "abIndex"), "seqMat"))

setAs("abIndex", "seqMat", function(from)
  {
      n <- length(from)
      d <- from@rleD
      va <- d@rle$values
      le <- d@rle$lengths
      m <- length(le)
      ## Now work the 'ends' are  cumsum(c(d@first, le * va))
      ## we need to care for the "length 1" stretches:
      if(any(nonPair <- le[2* seq_len(m2 <- m %/% 2)] != 1)) {

          m2 + n + va + nonPair # <- "dummy" using "unused"
          ## an "easy" (but not so efficient when 'm' is "large")
          ## way would be to "make these" into pairs, then work for that case...
      }
      ## use ~/R/MM/Pkg-ex/Matrix/abIndex-experi.R for trying things ...

      stop("<abIndex>  -->  <seqMat>  is not yet implemented")
  })

setAs("seqMat", "abIndex", function(from)
  {
      stop("<seqMat>  -->  <abIndex>  is not yet implemented")
  })
