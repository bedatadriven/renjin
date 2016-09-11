
library(methods)
library(stats)

# From the Matrix package


ind4toeplitz <- function(n) {
  A <- matrix(raw(), n, n)
  abs(as.vector(col(A) - row(A))) + 1L
}

.toeplitz.spV <-  function(x, symmetric=TRUE, giveCsparse=TRUE) {
  ## semantically "identical" to stats::toeplitz
  n <- length(x)
  r <- spV2M(x[ind4toeplitz(n)], n,n, symmetric=symmetric, check=FALSE)
  if (giveCsparse) as(r, "CsparseMatrix") else r
}

setClass("sparseVector",
         representation(length = "numeric", i = "numeric", "VIRTUAL"),
         ##                     "longindex"    "longindex"
         ## note that "numeric" contains "integer" (if I like it or not..)
         prototype = prototype(length = 0),
         validity = function(object) {
           n <- object@length
           if(anyNA(i <- object@i))	 "'i' slot has NAs"
           else if(any(!is.finite(i))) "'i' slot is not all finite"
           else if(any(i < 1))	 "'i' must be >= 1"
           else if(n == 0 && length(i))"'i' must be empty when the object length is zero"
           else if(any(i > n)) sprintf("'i' must be in 1:%d", n)
           else if(is.unsorted(i, strictly=TRUE))
             "'i' must be sorted strictly increasingly"
           else TRUE
         })

setMethod("toeplitz", "sparseVector", .toeplitz.spV)