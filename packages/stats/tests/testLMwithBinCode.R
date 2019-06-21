
library(hamcrest)

test.kMeans <- function() {
  x <- rnorm(n=80)
  y <- 7+3.2*x
  lm.out <- lm(y~x)

  # make sure print.summary.lm works without error (Issue #28)
  # the offending call is to cut -> bincode which is tested
  # more thoroughly elsewhere
  print(summary(lm.out))

}