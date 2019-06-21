library(hamcrest)

test.reshape <- function() {

    # based on an example in ?reshape in GNU R:
    df <- data.frame(id = rep(1:4, rep(2,4)),
                     visit = rep(c("Before","After"), 4),
                     x = seq(4),
                     y = seq(4) + 1)

    res <- reshape(df, timevar = "visit", idvar = "id", direction = "wide", v.names = "x")

    assertThat(dim(res), identicalTo(c(4L, 4L)))
    # TODO: this test is not comprehensive enough as the output doesn't fully match the result in GNU R
}