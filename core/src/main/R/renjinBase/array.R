

array <-
function(data = NA, dim = length(data), dimnames = NULL)
{
    data <- as.vector(data)
    dim <- as.integer(dim)
    vl <- prod(dim)
    if(length(data) != vl) {
        if(vl > .Machine$integer.max)
            stop("'dim' specifies too large an array")
        data <- rep(data, length.out=vl)
    }
    if(length(dim))
	dim(data) <- dim
    if(is.list(dimnames) && length(dimnames))
	dimnames(data) <- dimnames
    data
}