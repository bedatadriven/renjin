
library(hamcrest)
library("org.renjin.test:native")

m <- matrix(as.double(1:12), nrow = 3)
copy <- .Call("call_copyMatrix", m)

assertThat(copy, identicalTo(m))
