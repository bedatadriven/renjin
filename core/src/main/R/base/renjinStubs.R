
# lengths
# Introduced in R-3.2.0, see the release notes at http://cran.r-project.org/src/base/NEWS
# Fixed in R-3.2.1 to work (trivially) on atomic vectors.
lengths <- function(x, use.names = TRUE) {
    if (!isTRUE(use.names)) x <- unname(x)
    sapply(x, length)
}