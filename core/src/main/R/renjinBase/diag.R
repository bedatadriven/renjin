
# Pure R version of diag

diag <- function(x = 1, nrow, ncol)
{
    if (is.matrix(x)) {
        if (nargs() > 1L)
            stop("'nrow' or 'ncol' cannot be specified when 'x' is a matrix")

        if((m <- min(dim(x))) == 0L)
	    return(vector(typeof(x), 0L)) # logical, integer, also list ..

        y <- c(x)[1L + 0L:(m - 1L) * (dim(x)[1L] + 1L)]
        nms <- dimnames(x)
        if (is.list(nms) && !any(sapply(nms, is.null)) &&
            identical((nm <- nms[[1L]][seq_len(m)]), nms[[2L]][seq_len(m)]))
            names(y) <- nm
        return(y)
    }
    if(is.array(x) && length(dim(x)) != 1L)
        stop("'x' is an array, but not 1D.")

    if(missing(x))
	n <- nrow
    else if(length(x) == 1L && nargs() == 1L) {
	n <- as.integer(x)
	x <- 1
    }
    else n <- length(x)
    if(!missing(nrow))
	n <- nrow
    if(missing(ncol))
	ncol <- n
    p <- ncol
    y <- vector(typeof(x), n * p)
    dim(y) <- c(n, p)
    if((m <- min(n, p)) > 0L) y[1L + 0L:(m - 1L) * (n + 1L)] <- x
    y
}