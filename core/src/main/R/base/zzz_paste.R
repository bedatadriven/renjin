
# paste0
# Introduced in R-2.15.0, see the release notes at http://cran.r-project.org/src/base/NEWS.2
paste0 <- function(..., collapse = NULL) {
    .Internal(paste(list(...), sep = "", collapse = collapse))
}

