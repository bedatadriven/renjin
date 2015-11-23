####  Utilities  for  Sparse Model Matrices

## The "first" version {no longer used}:
fac2sparse <- function(from, to = c("d","i","l","n","z"), drop.unused.levels = FALSE)
{
    ## factor(-like) --> sparseMatrix {also works for integer, character}
    fact <- if (drop.unused.levels) factor(from) else as.factor(from)
    levs <- levels(fact)
    n <- length(fact)
    to <- match.arg(to)
    ## MM: using new() and then assigning slots has efficiency "advantage"
    ##     of *not* validity checking
    res <- new(paste0(to, "gCMatrix"))
    res@i <- as.integer(fact) - 1L # 0-based
    res@p <- 0:n
    res@Dim <- c(length(levs), n)
    res@Dimnames <- list(levs, NULL)
    if(to != "n")
	res@x <- rep.int(switch(to,
				"d" = 1., "i" = 1L, "l" = TRUE, "z" = 1+0i),
			 n)
    res
}

## This version can deal with NA's [maybe slightly less efficient (how much?)] :
fac2sparse <- function(from, to = c("d","i","l","n","z"),
		       drop.unused.levels = TRUE, giveCsparse = TRUE)
{
    ## factor(-like) --> sparseMatrix {also works for integer, character}
    fact <- if (drop.unused.levels) factor(from) else as.factor(from)
    levs <- levels(fact)
    n <- length(fact)
    to <- match.arg(to)
    i <- as.integer(fact) - 1L ## 0-based indices
    df <- data.frame(i = i, j = if(n) 0:(n-1L) else integer())[!is.na(i),]
    if(to != "n")
	df$x <- rep.int(switch(to,
			       "d" = 1., "i" = 1L, "l" = TRUE, "z" = 1+0i),
			nrow(df))
    T <- do.call("new", c(list(Class = paste0(to, "gTMatrix"),
			       Dim = c(length(levs), n),
			       Dimnames = list(levs, names(fact))), df))
    if(giveCsparse) .Call(Tsparse_to_Csparse, T, FALSE) else T
}

setAs("factor", "sparseMatrix", function(from) fac2sparse(from, to = "d"))

##' fac2Sparse() := fac2sparse w/ contrasts
##'
##' @param from factor of which we want the "contrasted" (indicator)
##'   design matrix
##' @param to character string specifying the response type
##' @param drop.unused.level logical indicating if non-present factor
##'   levels should be dropped, via  factor(from)
##' @param factorPatt12 logical vector fp[] of length 2
##'   fp[1] : give contrasted t(X);  fp[2] : give "dummy" t(X) [=fac2sparse()]
##' @param contrasts.arg character string or NULL or (coercable to)
##'		sparseMatrix, specifying the contrast
##'
##' @return a list of length two, each with the corresponding t(model matrix),
##'	when the corresponding factorPatt12 is true.
fac2Sparse <- function(from, to = c("d","i","l","n","z"),
		       drop.unused.levels = TRUE, giveCsparse = TRUE,
		       factorPatt12, contrasts.arg = NULL)
{
    stopifnot(is.logical(factorPatt12), length(factorPatt12) == 2)
    if(any(factorPatt12))
	m <- fac2sparse(from, to=to, drop.unused.levels=drop.unused.levels,
			giveCsparse=giveCsparse)
    ##
    ## code '2' : keep dummy, i.e. no contrasts :
    ans <- list(NULL, if(factorPatt12[2]) m)
    ##
    if(factorPatt12[1]) {
	## *do* use contrasts.arg
	if(is.null(contrasts.arg))
	    contrasts.arg <- getOption("contrasts")[if(is.ordered(from))
						    "ordered" else "unordered"]
	ans[[1]] <-
	    crossprod(if(is.character(contrasts.arg)) {
		stopifnot(is.function(FUN <- get(contrasts.arg)))
		## calling  contr.*() with correct level names directly :
		FUN(rownames(m), sparse = TRUE)
	    } else as(contrasts.arg, "sparseMatrix"), m)
    }
    ans
}

## "Sparse  model.matrix()"
##      model.matrix(object, data = environment(object),
##                   contrasts.arg = NULL, xlev = NULL, ...)
##
## Originally: Cut'n'paste from model.matrix() ... just replacing small part at end:
sparse.model.matrix <-
    function(object, data = environment(object), contrasts.arg = NULL,
	     xlev = NULL, transpose = FALSE,
	     drop.unused.levels = FALSE, row.names=TRUE, verbose=FALSE, ...)
{
    t <- if(missing(data)) terms(object) else terms(object, data=data)
    if (is.null(attr(data, "terms")))
	data <- model.frame(object, data, xlev=xlev)
    else {
	reorder <- match(sapply(attr(t,"variables"),deparse,
				width.cutoff=500)[-1L],
			 names(data))
	if (anyNA(reorder))
	    stop("model frame and formula mismatch in model.matrix()")
	if(!isSeq(reorder, ncol(data), Ostart=FALSE))
	    data <- data[,reorder, drop=FALSE]
    }
    int <- attr(t, "response")
    if(length(data)) {      # otherwise no rhs terms, so skip all this
	contr.funs <- as.character(getOption("contrasts"))
	namD <- names(data)
	## turn any character columns into factors
	for(i in namD)
	    if(is.character(data[[i]])) {
		data[[i]] <- factor(data[[i]])
		warning(gettextf("variable '%s' converted to a factor", i),
			domain = NA)
	    }
	isF <- sapply(data, function(x) is.factor(x) || is.logical(x) )
	isF[int] <- FALSE
	isOF <- sapply(data, is.ordered)
	for(nn in namD[isF])            # drop response
	    if(is.null(attr(data[[nn]], "contrasts")))
		contrasts(data[[nn]]) <- contr.funs[1 + isOF[nn]]
	## it might be safer to have numerical contrasts:
	##	  get(contr.funs[1 + isOF[nn]])(nlevels(data[[nn]]))
	if (!is.null(contrasts.arg) && is.list(contrasts.arg)) {
	    if (is.null(namC <- names(contrasts.arg)))
		stop("invalid 'contrasts.arg' argument")
	    for (nn in namC) {
		if (is.na(ni <- match(nn, namD)))
		    warning(gettextf("variable '%s' is absent, its contrast will be ignored", nn),
			    domain = NA)
		else {
		    ca <- contrasts.arg[[nn]]
## FIXME: work for *sparse* ca
		    if(is.matrix(ca)) contrasts(data[[ni]], ncol(ca)) <- ca
		    else contrasts(data[[ni]]) <- contrasts.arg[[nn]]
		}
	    }
	}
    } else {               # internal model.matrix needs some variable
	isF <-  FALSE
	data <- cbind(data, x = 0)
    }
    ## <Sparse> src/library/stats/R/models.R has
    ##    ans <- .Internal(model.matrix(t, data))
    if(verbose) {
	cat("model.spmatrix(t, data, ..)  with t =\n"); str(t,give.attr=FALSE) }
    ans <- model.spmatrix(t, data, transpose=transpose,
    ##     ==============
			  drop.unused.levels=drop.unused.levels,
			  row.names=row.names, verbose=verbose)
    ## </Sparse>
    attr(ans, "contrasts") <-
	lapply(data[isF], function(x) attr(x, "contrasts"))
    ans
} ## {sparse.model.matrix}


##' Produce the t(Z); Z = "design matrix" of (X : Y), where
##'             --- t(Z) : aka rowwise -version : "r"
##'
##' @title sparse model matrix for 2-way interaction
##' @param X and Y either are numeric matrices {maybe 1-column}
##' @param Y       or "as(<factor>, sparseM)"
##' @param do.names logical
##' @param forceSparse logical
##' @return
##' @author Martin Maechler
sparse2int <- function(X, Y, do.names = TRUE, forceSparse = FALSE, verbose = FALSE)
{
    if(do.names) {
	dnx <- dimnames(X)
	dny <- dimnames(Y)
    }
    dimnames(Y) <- dimnames(X) <- list(NULL,NULL)
    nx <- nrow(X)
    ny <- nrow(Y)
    r <-
	if((nX <- is.numeric(X)) | (nY <- is.numeric(Y))) {
	    if(nX) {
		if (nY || nx > 1) { # both numeric, or X >=2 "columns"
		    F <- if(forceSparse) function(m) .Call(dense_to_Csparse, m) else identity
		    F((if(ny == 1) X else X[rep.int(seq_len(nx),  ny)	, ]) *
		      (if(nx == 1) Y else Y[rep	   (seq_len(ny),each=nx), ]))
		}
		else { ## numeric X (1 "column"),  sparseMatrix Y
		    r <- Y
		    dp <- Y@p[-1] - Y@p[-(Y@Dim[2]+1L)]
		    ## stopifnot(all(dp %in% 0:1)) # just for now
		    ## if(nx == 1)
		    ## FIXME: similar trick would be applicable for nx > 2
		    r@x <- X[dp == 1L] * Y@x
		    r
		}
	    }
	    else { ## sparseMatrix X, numeric Y
		if(ny == 1) {
		    ## FIXME: similar trick would be applicable for ny > 2
		    r <- X
		    dp <- X@p[-1] - X@p[-(X@Dim[2]+1L)]
		    ## stopifnot(all(dp %in% 0:1)) # just for now - drop! - FIXME
		    r@x <- Y[dp == 1L] * X@x
		    r
		}
		else { ## ny > 1 -- *larger* matrix
		    X[rep.int(seq_len(nx),  ny)   , ] *
		    (if(nx == 1) Y else Y[rep(seq_len(ny),each=nx), ])
		}
	    }
	}
	else { ## X & Y are both sparseMatrix
	    (if(ny == 1) X else X[rep.int(seq_len(nx), ny)     , ]) *
	    (if(nx == 1) Y else Y[rep    (seq_len(ny),each=nx) , ])
	}
    if(verbose) cat(sprintf(" sp..2int(%s[%d],%s[%d]) ",
			    if(nX)"<N>" else "<sparse>", nx,
			    if(nY)"<N>" else "<sparse>", ny))

    if(do.names) {
	## FIXME: This names business needs a good solution..
	##        but maybe "up in the caller"
	if(!is.null(dim(r)) &&
	   !is.null(nX <- dnx[[1]]) &&
	   !is.null(nY <- dny[[1]]))
	    rownames(r) <- outer(nX, nY, paste, sep = ":")
    }
    r
}

##' Sparse Model Matrix for a (high order) interaction term  A:B:x:C
##'
##' @param rList list(.) of (transposed) single-factor model matrices,
##'	belonging to, say, factors  a, b, c,...
##' @param do.names
##' @param forceSparse
##' @param verbose
##' @return the model matrix corresponding to a:b:...
sparseInt.r <- function(rList, do.names = TRUE, forceSparse = FALSE, verbose=FALSE)
{
    nl <- length(rList)
    if(forceSparse)
	F <- function(m) if(is.matrix(m)) .Call(dense_to_Csparse, m) else m
    if(verbose)
	cat("sparseInt.r(<list>[1:",nl,"], f.Sp=",forceSparse,"): is.mat()= (",
	    paste(symnum(vapply(rList, is.matrix, NA)), collapse=""),
	    ")\n", sep="")
    if(nl == 1) {
	if(forceSparse) F(rList[[1]]) else rList[[1]]
    } else {
	## 'recursion' free:
	r <- rList[[1]]
	for(j in 2:nl)
	    r <- sparse2int(r, rList[[j]],
			    do.names=do.names, verbose=verbose)
	if(forceSparse) F(r) else r
    }
}


## not used currently
is.model.frame <- function(x)
{
  ## Purpose: check if x is a "valid" model.frame
  ## ------------------------------------------------------------
  ## Author: Martin Maechler, Date: 3 Jul 2009
    is.data.frame(x) &&
    !is.null(tms <- attr(x, "terms")) &&
    inherits(tms, "terms") && ## is.terms() would be better
    inherits(tms, "formula") &&
    is.matrix(attr(tms, "factors")) &&
    is.language(vv <- attr(tms, "variables")) &&
    vv[[1]] == as.symbol("list") &&
    all(vapply(as.list(vv[-1]), as.character, "") %in% colnames(x))
    ## all((vars <- sapply(as.list(vv[-1]), as.character)) %in% colnames(x))
    ## and we could go on testing vars
}


##' Create a sparse model matrix from a model frame.
##' -- This version uses  'rBind' and returns  X' i.e. t(X) :
##'
##' @title Sparse Model Matrix from Model Frame
##' @param trms a "terms" object
##' @param mf a data frame, typically resulting from  model.frame()
##' @param transpose logical indicating if  X' = t(X) {is faster!}
##'	or X should be returned
##' @param drop.unused.levels logical indicating if unused factor
##'	levels should be dropped
##' @param row.names
##' @return sparse matrix (class "dgCMatrix")
##' @author Martin Maechler
model.spmatrix <- function(trms, mf, transpose=FALSE,
			   drop.unused.levels = FALSE, row.names=TRUE, verbose=FALSE)
{
    ## Author: Martin Maechler, Date:  7 Jul 2009

    ## mf is a model frame or a "simple" data.frame [after reorder !]
    stopifnot(is.data.frame(mf))
    n <- nrow(mf)
    if(row.names)
	rnames <- row.names(mf)
    ## mf:  make into list, dropping all attributes (but the names)
    fnames <- names(mf <- unclass(mf))
    attributes(mf) <- list(names = fnames)

    if(length(factorPattern <- attr(trms, "factors"))) {
	d <- dim(factorPattern)
	nVar <- d[1]
	nTrm <- d[2]
	n.fP <- dimnames(factorPattern)
	fnames <- n.fP[[1]] # == names of variables {incl. "F(var)"} in the model
	Names  <- n.fP[[2]] # == colnames == names of terms:  "a", "b:c", ...
    } else { ## degenerate, e.g.  'Y ~ 1'
	nVar <- nTrm <- 0L
	fnames <- Names <- character(0)
    }
    ## all the "variables in the model" are also in "mf", including "sin(x)";
    ## actually, ..../src/main/model.c even assumes
    stopifnot((m <- length(mf)) >= nVar)
    if(verbose)
	cat(sprintf("model.spm..(): (n=%d, nVar=%d (m=%d), nTrm=%d)\n",
		    n, nVar,m, nTrm))
    if(m > nVar) mf <- mf[seq_len(nVar)]
    stopifnot(fnames == names(mf))
    noVar <- nVar == 0
    ##>> this seems wrong; we use  1:nVar for indexing mf[] below ..
    ##>> if(noVar) nVar <- 1L # (as in ~/R/D/r-devel/R/src/main/model.c)
    ## Note: "character" variables have been changed to factor in the caller;
    ##     hence: both factor and *logical*  should be dealt as factor :
    is.f <- if(noVar) logical(0) else vapply(mf, function(.)
					     is.factor(.) | is.logical(.), NA)
    indF <- which(is.f)
    if(verbose) { cat(" --> indF =\n"); print(indF) }
    hasInt <- attr(trms, "intercept") == 1
    ## the degree of interaction:
    ## intOrder <- attr(trms, "order")
    ##
    if(!hasInt && length(indF)) {
	## change the '1' of the first factor into a '2' :
	if(any(i1 <- factorPattern[indF, ] == 1))
	    ## replace at the first '1' location:
	    factorPattern[indF,][which.max(i1)] <- 2L
	else {}
	## nothing to do
    }
    ## Convert "factors" to "Rowwise- sparseMatrix ("dummy"-matrix) -----------
    ## Result: a list of sparse model matrices for the "factor"s :
    f.matr <- structure(vector("list", length = length(indF)),
			names = fnames[indF])
    i.f <- 0
    ## ---- For each variable in the model -------------------
    for(i in seq_len(nVar)) {
	nam <- fnames[i]
	f <- mf[[i]]
	if(is.f[i]) {
	    fp <- factorPattern[i,] ## == factorPattern[nam,]
	    contr <- attr(f, "contrasts")
	    f.matr[[(i.f <- i.f + 1)]] <- # a list of 2
		lapply(fac2Sparse(f, to = "d",
				  drop.unused.levels=drop.unused.levels,
				  factorPatt12 = 1:2 %in% fp,
				  contrasts.arg = contr),
		       function(s) {
			   if(is.null(s)) return(s)
			   ## else
			   rownames(s) <-
			       paste0(nam, if(is.null(rownames(s)))
				      ## for some contr.*(), have lost rownames; hmm..
				      seq_len(nrow(s)) else rownames(s))
			   s
		       })
	} else { ## continuous variable --> "matrix" - for all of them
	    if(any(iA <- (cl <- class(f)) == "AsIs")) # drop "AsIs" class
		class(f) <- if(length(cl) > 1L) cl[!iA]
	    nr <- if(is.matrix(f)) nrow(f <- t(f)) else (dim(f) <- c(1L, length(f)))[1]
	    if(is.null(rownames(f)))
		rownames(f) <- if(nr == 1) nam else paste0(nam, seq_len(nr))
	    mf[[i]] <- f
	}
    }
    if(verbose) {
	cat(" ---> f.matr list :\n")
	str(f.matr, max = as.integer(verbose))
	fNms <- format(dQuote(Names))
	dim.string <- gsub('5', as.character(floor(1+log10(n))),
			   " -- concatenating (r, rj): dim = (%5d,%5d) | (%5d,%5d)\n")
    }

    ## FIXME: do all this in C --

    getR <- function(N)			# using 'nm'
	if(!is.null(r <- f.matr[[N]])) r[[factorPattern[N, nm]]] else mf[[N]]
    vNms <- "(Intercept)"[hasInt]
    counts <- integer(nTrm)
    r <-
	if(hasInt) ## column of 1's - as sparse
	    new("dgCMatrix", i = 0:(n-1L), p = c(0L, n),
		Dim = c(n, 1L), x = rep.int(1, n))
	else new("dgCMatrix", Dim = c(n, 0L))
    if(transpose) r <- t(r)
    iTrm <- seq_len(nTrm)
    for(j in iTrm) { ## j-th term
	nm <- Names[j]
	if(verbose) cat(sprintf("term[%2d] %s .. ", j, fNms[j]))
	nmSplits <- strsplit(nm, ":", fixed=TRUE)[[1]]
	## NOTA BENE: This can be very slow when many terms are involved
	## FIXME ??? why does it use *much* memory in those cases ??
	rj <- sparseInt.r(lapply(nmSplits, getR), do.names=TRUE,
			  forceSparse = TRUE, verbose=verbose)# or just (verbose >= 2))
	if(verbose) cat(sprintf(dim.string, nrow(r), ncol(r), nrow(rj),ncol(rj)))
	## fast version of cbind2() / rbind2(), w/o checks, dimnames, etc
	r <- if(transpose) .Call(Csparse_vertcat, r, rj)
		else	   .Call(Csparse_horzcat, r, t(rj))
	## if(verbose) cat(" [Ok]\n")
	vNms <- c(vNms, dimnames(rj)[[1]])
	counts[j] <- nrow(rj)
    }
    rns <- if(row.names) rnames
    dimnames(r) <- if(transpose) list(vNms, rns) else list(rns, vNms)
    attr(r, "assign") <- c(if(hasInt) 0L, rep(iTrm, counts))
    r
} ## model.spmatrix()
