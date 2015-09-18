# paste0
# Introduced in R-2.15.0, see the release notes at http://cran.r-project.org/src/base/NEWS.2
paste0 <- function(..., collapse = NULL) {
    paste(..., sep = "", collapse = collapse)
}

# rep_len
# Introduced in R-3.0.0, see the release notes at http://cran.r-project.org/src/base/NEWS
rep_len <- function(x, length.out) {
    rep(x, length.out = length.out)
}

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

# lengths
# Introduced in R-3.2.0, see the release notes at http://cran.r-project.org/src/base/NEWS
# Fixed in R-3.2.1 to work (trivially) on atomic vectors.
lengths <- function(x, use.names = TRUE) {
    if (!isTRUE(use.names)) x <- unname(x)
    sapply(x, length)
}