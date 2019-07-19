library(hamcrest)

test.tilde <- function() {
    x <- do.call("~", list(as.name('foo')))

    assertThat(deparse(x), identicalTo("~foo"))
}

test.match.call <- function() {

    f <- function(...) match.call()

    x <- do.call("f", list(quote(x), quote(y)))

    assertThat(x, identicalTo(quote(f(x, y))))
}
