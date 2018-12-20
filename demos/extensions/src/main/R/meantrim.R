meantrim <- function(x) {
    x <- x[x != max(x)]
    x <- x[x != min(x)]
    return(mean(x))
}
