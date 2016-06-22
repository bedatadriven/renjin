# Pure R replacement for the original max.col function which calls to the C implementation R_max_col:
max.col <- function(m, ties.method = c("random", "first", "last")) {

  ties.method <- match.arg(ties.method)

  unname(apply(as.matrix(m), 1, function(row) {
    maxVal <- max(row)

    # max.col doesn't have the na.rm argument:
    if (is.na(maxVal)) return(NA_integer_)

    i <- which(row == maxVal)
    # no ties:
    if (length(i) == 1L) return(i)
    # ties:
    k <- switch(ties.method,
           "random" = sample(i, 1),
           "first" = i[1],
           "last" = i[length(i)])
  }))
}