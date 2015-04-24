
library(hamcrest)

test.match.call <- function() {

    f <- function(a, zz) { match.call() }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}

test.match.call.nested <- function() {
    g <- function(a) a
    f <- function(a, zz) { g(match.call()) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}


test.match.call.lazy <- function() {
    g <- function(a) a
    f <- function(a, zz, yy = match.call()) { g(yy) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}
