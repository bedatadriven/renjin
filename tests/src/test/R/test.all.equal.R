
library(hamcrest)

test.all.equal.check_attributes <- function() {

    x <- c(a = 1, b = 2, c = 3)
    y <- c(a = 1, b = 2, d = 3)

    assertThat(all.equal(x, y), identicalTo("Names: 1 string mismatch"))
    assertThat(all.equal(x, y, check.attributes = FALSE), identicalTo(TRUE))

    y <- c(a = 1, b = 2, c = 4)

    assertThat(all.equal(x, y), identicalTo("Mean relative difference: 0.333"))
}
