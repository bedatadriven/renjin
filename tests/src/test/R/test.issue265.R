library(hamcrest)

test.int.overflow <- function() {
    assertThat(as.integer(2^30), identicalTo(1073741824L))
    assertThat(as.integer(2^31), identicalTo(NA_integer_))
    assertThat(as.integer(2^40), identicalTo(NA_integer_))
    assertThat(as.integer(-2^30), identicalTo(-1073741824L))
    assertThat(as.integer(-2^30-1000000000), identicalTo(-2073741824L))
    assertThat(as.integer(2147483647), identicalTo(2147483647L))
    assertThat(as.integer(-2147483647), identicalTo(-2147483647L))
    assertThat(as.integer(2147483648), identicalTo(NA_integer_))
    assertThat(as.integer(-2147483648), identicalTo(NA_integer_))
}