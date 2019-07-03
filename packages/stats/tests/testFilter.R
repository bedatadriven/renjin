library(hamcrest)

test.filter <- function() {
    x <- 1:100
    ts <- filter(x, rep(1, 3))

    assertThat(length(ts), equalTo(100))
}