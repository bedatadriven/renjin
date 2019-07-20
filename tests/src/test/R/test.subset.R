
library(hamcrest)


test.na.indexes.matrix <- function() {

    x <- matrix(1:6, nrow = 2)
    dimnames(x) <- list(c("a", "b"), c("x", "y", "z"))

    i <- c(1L, NA_integer_)

    assertThat(x[i, ], identicalTo(
        structure(c(1L, NA, 3L, NA, 5L, NA), .Dim = 2:3, .Dimnames = list(c("a", NA), c("x", "y", "z")))))
}

test.list.blank.string.subscript <- function() {

    x <- list(41, b = 42, c = 43)

    assertThat(x[c('', 'b')], identicalTo(structure(list("NA" = NULL, b = 42), .Names = c(NA, "b"))))

}

test.vector.blank.string.subscript <- function() {

    x <- c(41, b = 42, c = 43)

    assertThat(x[c('', 'b')], identicalTo(structure(c(NA, 42), .Names = c(NA, "b"))))

}