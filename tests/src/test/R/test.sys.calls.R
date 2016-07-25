
library(hamcrest)


## Functions used in test cases

foo.default <- function(n) {
    # Return a vector of function names
    calls <- sys.calls()
    sapply(calls, function(call) as.character(call[[1]]))
}

foo <- function(n) {
    UseMethod("foo")
}

as.double.bar <- function(n) {
    foo.default()
}

g <- function(q) {
    foo(q)
}


## Test cases

test.simple <- function() {
    calls <- foo.default(0)
    assertThat(calls, identicalTo(c("test.simple", "foo.default")))
}

test.s3 <- function() {
    calls <- foo(0)
    assertThat(calls, identicalTo(c("test.s3", "foo", "foo.default")))
}

test.nested3 <- function() {
    calls <- g(0)
    assertThat(calls, identicalTo(c("test.nested3", "g", "foo", "foo.default")))
}
