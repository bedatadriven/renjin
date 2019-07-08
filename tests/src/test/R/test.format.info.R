
library(hamcrest)

assertThat(format.info(123), identicalTo(c(3L, 0L, 0L)))
assertThat(format.info(pi), identicalTo(c(8L, 6L, 0L)))
assertThat(format.info(1e8), identicalTo(c(5L, 0L, 1L)))
assertThat(format.info(1e222), identicalTo(c(6L, 0L, 2L)))
