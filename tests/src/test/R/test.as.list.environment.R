
library(hamcrest)

test.all.names <- function() {

    env <- new.env(TRUE, globalenv(), 29L)
    env$a <- 1
    env$.a <- 2

    x <- as.list.environment(env, FALSE)
    y <- as.list.environment(env, TRUE)

    assertThat(x, identicalTo(list(a = 1)))
    assertThat(y, identicalTo(list(a = 1, .a = 2)))
}

test.hidden.first <- function() {

    env <- new.env(TRUE, globalenv(), 29L)
    env$.a <- 2
    env$a <- 1

    x <- as.list.environment(env, FALSE)
    y <- as.list.environment(env, TRUE)

    assertThat(x, identicalTo(list(a = 1)))
    assertThat(y, identicalTo(list(a = 1, .a = 2)))
}
