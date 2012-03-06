cl <- oldClass(x)
## delete class: Version 3 idiom
## to avoid any special methods for [[<-
## This forces a copy, but we are going to need one anyway
## and NAMED=1 prevents any further copying.
class(x) <- NULL
nrows <- .row_names_info(x, 2L)
if(!is.null(value)) {
	N <- NROW(value)
	if(N > nrows)
		stop(gettextf("replacement has %d rows, data has %d", N, nrows),
				domain = NA)
	if (N < nrows)
		if (N > 0L && (nrows %% N == 0L) && length(dim(value)) <= 1L)
			value <- rep(value, length.out = nrows)
		else
			stop(gettextf("replacement has %d rows, data has %d", N, nrows),
					domain = NA)
	if(is.atomic(value)) names(value) <- NULL
}
x[[name]] <- value
class(x) <- cl
return(x)
