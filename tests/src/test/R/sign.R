
library(hamcrest)

test.sign <- function() {

    assertThat( sign(pi), identicalTo(1) )
    assertThat( sign(0), identicalTo(0) )
    assertThat( sign(-2:3), identicalTo(c(-1,-1,0,1,1,1)) )
    assertThat( sign(1L), identicalTo(1) )
    assertThat( sign(NA), identicalTo(NA_real_) )
    assertThat( sign(1+1i), throwsError() )

}
