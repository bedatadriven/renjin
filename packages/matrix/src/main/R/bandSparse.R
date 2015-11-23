bandSparse <- function(n, m = n, k, diagonals,
		       symmetric = FALSE, giveCsparse = TRUE)
{
    ## Purpose: Compute a band-matrix by speciyfying its (sub-)diagonal(s)
    ## ----------------------------------------------------------------------
    ## Arguments: (n,m) : Matrix dimension
    ##		      k : integer vector of "diagonal numbers",  with identical
    ##                    meaning as in  band(*, k)
    ##          diagonals: (optional!) list of (sub/super)diagonals
    ##         symmetric: if TRUE, specify only upper or lower triangle;
    ## ----------------------------------------------------------------------
    ## Author: Martin Maechler, Date: 20 Feb 2009, 22:42

    if(use.x <- !missing(diagonals)) # when specified, must be matrix or list
        diag.isMat <- is.matrix(diagonals)
    len.k <- length(k)
    stopifnot(!use.x || is.list(diagonals) || diag.isMat,
	      k == as.integer(k), n == as.integer(n), m == as.integer(m))
    k <- as.integer(k)
    n <- as.integer(n)
    m <- as.integer(m)
    stopifnot(n >= 0, m >= 0, -n+1 <= k, k <= m - 1)
    if(use.x) {
        if(diag.isMat) {
            if(ncol(diagonals) != len.k)
		stop(gettextf("'diagonals' matrix must have %d columns (= length(k) )",
			      len.k), domain=NA)
            getD <- function(j) diagonals[,j]

        } else { ## is.list(diagonals):
            if(length(diagonals) != len.k)
		stop(gettextf("'diagonals' must have the same length (%d) as 'k'",
			      len.k), domain=NA)
            getD <- function(j) diagonals[[j]]
        }
    }
    if(symmetric && any(k < 0) && any(k > 0))
	stop("for symmetric band matrix, only specify upper or lower triangle\n hence, all k must have the same sign")
    dims <- c(n,m)
    k.lengths <- ## This is a bit "ugly"; I got the cases "by inspection"
	if(n >= m) {
	    ifelse(k >= m-n,  m - pmax(0,k), n+k)
	} else { ## n < m
	    ifelse(k >= -n+1, n + pmin(0,k), m-k)
	}
    i <- j <- integer(sum(k.lengths))
    if(use.x)
	x <- if(len.k > 0) # carefully getting correct type/mode
	    rep.int(getD(1)[1], length(i))
    off.i <- 0L
    for(s in seq_len(len.k)) {
	kk <- k[s] ## *is* integer
	l.kk <- k.lengths[s] ## == length of (sub-)diagonal kk
	ii1 <- seq_len(l.kk)
	ind <- ii1 + off.i
	if(kk >= 0) {
	    i[ind] <- ii1
	    j[ind] <- ii1 + kk
	} else { ## k < 0
	    i[ind] <- ii1 - kk
	    j[ind] <- ii1
	}
	if(use.x) {
	    xx <- getD(s)
	    if(length(xx) < l.kk)
		warning(gettextf("the %d-th (sub)-diagonal (k = %d) is too short; filling with NA's",
				 s, kk), domain=NA)
	    x[ind] <- xx[ii1]
	}
	off.i <- off.i + l.kk
    }
    if(symmetric) { ## we should have smarter sparseMatrix()
	UpLo <- if(min(k) >= 0) "U" else "L"
	T <- if(use.x) {
	    if(is.integer(x)) x <- as.double(x)
	    cc <- paste0(.M.kind(x), "sTMatrix")
	    new(cc, i= i-1L, j= j-1L, x = x, Dim= dims, uplo=UpLo)
	}
	else
	    new("nsTMatrix", i= i-1L, j= j-1L, Dim= dims, uplo=UpLo)
	if(giveCsparse) as(T, "CsparseMatrix") else T
    }
    else { ## general, not symmetric
	if(use.x)
	    sparseMatrix(i=i, j=j, x=x, dims=dims, giveCsparse=giveCsparse)
	else
	    sparseMatrix(i=i, j=j,	dims=dims, giveCsparse=giveCsparse)
    }
}
