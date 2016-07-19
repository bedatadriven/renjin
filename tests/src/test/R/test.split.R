
library(hamcrest)

test.split <- function() {

    df <- data.frame(
        x = seq(9),
        y = rep(1:3, each = 3)
    )

    assertThat( split(df$x, df$y), identicalTo(list("1"=1:3, "2"=4:6, "3"=7:9)) )
    # TODO: fix issue https://github.com/bedatadriven/renjin/issues/208
    #assertThat(
    #    split(c(1,2), factor(c("a", "b"), levels = letters[1:3])),
    #    identicalTo(list(a=1, b=2, c=numeric()))
    #)
    assertThat(
        split(c(1,2), factor(c("a", "b"), levels = letters[1:3]), drop = TRUE),
        identicalTo(list(a=1, b=2))
    )

}
