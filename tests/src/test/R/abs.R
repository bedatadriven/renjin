
library(hamcrest)

test.abs <- function() {

    assertThat( abs(-1L), identicalTo(1L) )
    assertThat( abs(1L), identicalTo(1L) )
    assertThat( abs(-1), identicalTo(1) )
    assertThat( abs(TRUE), identicalTo(1L) )
    assertThat( abs(FALSE), identicalTo(0L) )
    assertThat( abs(c(1, -0.5)), identicalTo(c(1, 0.5)) )
    assertThat( abs(list(-1)), throwsError() )
    assertThat( abs("1"), throwsError() )
    assertThat( abs(NA), identicalTo(NA_integer_) )
    assertThat( abs(c(NA, -1)), identicalTo(c(NA, 1)) )
    assertThat( abs(NA + 1L), identicalTo(NA_integer_) )

    # copied from micro-tests:
    assertThat( abs(0/0), identicalTo(NaN) )
    assertThat( abs(NA + 1), identicalTo(NA_real_) )
    assertThat( abs(c(0/0,1i)), identicalTo(c(NaN, 1)) )
    assertThat( abs((0 + 0i)/0), identicalTo(NaN) )

}