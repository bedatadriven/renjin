
startsWith <- function(x, prefix)
{
    if(!is.character(x) || !is.character(prefix)) {
        stop("non-character object(s)")
    }

    if( length(x) == 0 || length(prefix) == 0) {
        return(logical(0))
    }

    substr(x, 1, nchar(prefix)) == prefix
}

endsWith <- function(x, suffix)
{
    if(!is.character(x) || !is.character(suffix)) {
        stop("non-character object(s)")
    }

    if( length(x) == 0 || length(suffix) == 0) {
        return(logical(0))
    }

    substr(x, nchar(x)-nchar(suffix)+1, nchar(x)) == suffix
}

strrep <-
function(x, times)
{
    if(!is.character(x)) x <- as.character(x)
    times <- as.integer(times)

    mapply(x, times, SIMPLIFY = TRUE, USE.NAMES = FALSE, FUN = function(x, times) {
        if(times < 0) {
            error("invalid 'times' value")
        }
        paste(rep.int(x, times), collapse = "")
    })
}
