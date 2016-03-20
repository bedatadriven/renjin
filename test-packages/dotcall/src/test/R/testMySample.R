
library(hamcrest)
library(org.renjin.test.dotcall)

test.mySample <- function() {
    assertThat(length(mySample(10L)), identicalTo(10L))
}