

list.dirs <- function(path = ".", full.names = TRUE, recursive = TRUE)
{

    files <- list.files(path = path, all.files = TRUE, recursive = recursive, include.dirs = TRUE)
    d <- sapply(files, function(d) file.info(d)$isdir)
    files[d]
}

readRenviron <- function(path)
    stop("Not implemented")
