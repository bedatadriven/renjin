
library(hamcrest)

test.assign.env <- function() {
    e <- new.env()
    e$a <- 41
    e$"b" <- 42
    e$"==" <- 43

    assertThat(e[["a"]], identicalTo(41))
    assertThat(e[["b"]], identicalTo(42))
    assertThat(e[["=="]], identicalTo(43))
}

test.s3 <- function() {

    `$<-.foo` <- function(x, name, value) {
        stopifnot(is.character(name))
        x[name] <- 44
        x
    }

    x <- structure(3L, class="foo")
    x$a <- 45
    assertThat(x, identicalTo(structure(c(3, 44), class = "foo", .Names = c("", "a"))))
}