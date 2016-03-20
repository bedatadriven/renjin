

library(hamcrest)
library(org.renjin.test.dotcall)

test.globals <- function() {
    assertThat(global.count(), identicalTo(1L))
    assertThat(global.count(), identicalTo(2L))
    assertThat(global.count(), identicalTo(3L))
    assertThat(global.count(), identicalTo(4L))
            
}