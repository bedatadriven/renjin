
library(hamcrest)

test.unique <- function() {

    assertThat( unique(c("a", "b", "a")), identicalTo(c("a", "b")) )
    assertThat( unique(c("a", "b", "a"), fromLast = TRUE), identicalTo(c("b", "a")) )
    assertThat( unique(c("b", "a", "a")), identicalTo(c("b", "a")) )
    assertThat( unique(c(NA, 1, NA)), identicalTo(c(NA, 1)) )
    assertThat( unique(c(1, NA, NA)), identicalTo(c(1, NA)) )
    assertThat( unique(c(a=1, b=2, c=1)), identicalTo(c(1,2)) )

    # TODO: fix issue https://github.com/bedatadriven/renjin/issues/207
    #assertThat( unique(c(1, NA, NA), incomparables=NA), identicalTo(c(1, NA, NA)) )

    df <- data.frame(
        x = c("a", "b", "c", "b"),
        y = c("x", "y", "z", "y"),
        stringsAsFactors = FALSE
    )

    assertThat( unique(df), identicalTo(df[c(1,2,3),]) )
    assertThat( unique(df, fromLast = TRUE), identicalTo(df[c(1,3,4),]) )

}