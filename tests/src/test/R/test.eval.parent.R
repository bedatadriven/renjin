
library(hamcrest)


foo.default <- function(sexp) {
    eval.parent(sexp)
}

foo <- function(sexp) {
    UseMethod("foo")
}

test.s3 <- function() {
    qq <- 42
    x <- foo(quote(qq))
    assertThat(x, identicalTo(42))
}