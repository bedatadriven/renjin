
library(hamcrest)

test.na.index <- function() {
    x <- c("object", "data")
    i <- c(1L, NA_integer_)
    x[i] <- "fixed"

    assertThat(x, identicalTo(c("fixed", "data")))
}
