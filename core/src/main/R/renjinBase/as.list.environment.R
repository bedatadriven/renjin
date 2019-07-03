


as.list.environment <- function(x, all.names=FALSE, sorted=FALSE, ...) {
    names <- ls(envir = x, all.names = all.names, sorted = sorted)
    list <- lapply(names, function(n) x[[n]])
    names(list) <- names
    list
}