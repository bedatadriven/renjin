

test.to.upper <- function() {
    stopifnot(identical(toupper("foo"), "FOO"))
}

test.to.lower <- function() {
    stopifnot(identical(tolower("FOO"), "foo"))
}

