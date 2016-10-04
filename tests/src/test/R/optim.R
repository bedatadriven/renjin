library(stats)
library(hamcrest)

test.optim <- function() {
  set.seed(1)
  fr <- function(x) {
    x1 <- x[1];
    x2 <- x[2];
    100 * (x2 - x1 * x1)^2 + (1 - x1)^2
  }
    grr <- function(x) {
    x1 <- x[1];
    x2 <- x[2];
    c(-400 * x1 * (x2 - x1 * x1) - 2 * (1 - x1), 200 * (x2 - x1 * x1))
  }
  expected <- structure(list(par = c(0x1.fffff67ee8016p-1, 0x1.ffffee46a067fp-1),
                             value = 0x1.fe9cee9b5b8ep-43,
                             counts = structure(c(47L, 47L), .Names = c('function', 'gradient')),
                             convergence = 0L,
                             message = 'CONVERGENCE: REL_REDUCTION_OF_F <= FACTR*EPSMCH'),
                        .Names = c('par', 'value', 'counts', 'convergence', 'message'))

  assertThat(optim(c(-1.2,1), fr, grr, method = "L-BFGS-B"), identicalTo(expected))
}
