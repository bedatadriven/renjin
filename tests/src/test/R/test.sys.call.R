
library(hamcrest)


## Functions used in test cases

foo.default <- function(n) {
    sexp <- sys.call(n)
    deparse(sexp)
}

foo <- function(n) {
    UseMethod("foo")
}

g <- function() {
    foo.default(2)
}



## Test cases

test.simple <- function() {
    call <- foo.default(0)
    assertThat(call, identicalTo("foo.default(0)"))
}

test.simple1 <- function() {
    call <- foo.default(-1)
    assertThat(call, identicalTo("test.simple1()"))
}

test.s3 <- function() {
    call <- foo(0)
    assertThat(call, identicalTo("foo.default(0)"))
}

test.s3.parent <- function() {
    call <- foo(-1L)
    assertThat(call, identicalTo("foo(-1L)"))
}

