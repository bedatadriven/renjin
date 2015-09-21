
library(hamcrest)

test.mget <- function() {
    
    env <- new.env()
    env$a <- "FOO"
    env$b <- "BAR"
    env$c <- "ZOO"
    
    result <- mget(c("a", "b", "z"), env, ifnotfound=42)
    
    assertThat(result, identicalTo(list(a="FOO", b="BAR", z=42)))
}