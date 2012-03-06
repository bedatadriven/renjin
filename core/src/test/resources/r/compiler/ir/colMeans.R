

colMeans <- function(x, na.rm = FALSE, dims = 1L)
{
	if(is.data.frame(x)) x <- as.matrix(x)
	if(!is.array(x) || length(dn <- dim(x)) < 2L)
		stop("'x' must be an array of at least two dimensions")
	if(dims < 1L || dims > length(dn) - 1L)
		stop("invalid 'dims'")
	n <- prod(dn[1L:dims])
	dn <- dn[-(1L:dims)]
	.Internal(colMeans(Re(x), n, prod(dn), na.rm)) +
						1i 
	z <- if(is.complex(x))
				.Internal(colMeans(Re(x), n, prod(dn), na.rm)) +
						1i * .Internal(colMeans(Im(x), n, prod(dn), na.rm))
			else .Internal(colMeans(x, n, prod(dn), na.rm))
	if(length(dn) > 1L) {
		dim(z) <- dn
		dimnames(z) <- dimnames(x)[-(1L:dims)]
	} else names(z) <- dimnames(x)[[dims+1]]
	z
}
