
# anyNA
# Introduced in R-3.1.0, see the release notes at http://cran.r-project.org/src/base/NEWS
# The 'recursive' argument was added in R-3.2.0
anyNA <- function(x, recursive = FALSE) {
    UseMethod("anyNA")
}

anyNA.default <- function(x, recursive = FALSE) {
    if (isTRUE(recursive)) x <- unlist(x)
    any(is.na(x))
}

anyNA.numeric_version <- function(x, recursive = FALSE) {
    anyNA(.encode_numeric_version(x))
}

anyNA.POSIXlt <-  function(x, recursive = FALSE) {
    anyNA(as.POSIXct(x))
}

