
library(hamcrest)

test.colNamesPreserved <- function() {
  m <- matrix(1:10/5, ncol=2)
  colnames(m) <- c("a", "b")
  mf <- format(m, digits=2)

  assertThat(colnames(mf), identicalTo(c("a", "b")))
}