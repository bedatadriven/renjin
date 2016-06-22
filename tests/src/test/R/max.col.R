
library(hamcrest)

test.max.col <- function() {
    # from the examples in ?max.col:
    mm <- structure(c(1, 3, 2, 1, 2, 3, 1, 4, 0, 2, 2, 3, 0, 4, 7, 2, 5,
          3, 2, 2, 4, 1, 4, 5, 1, 5, 4, 0, 1, 1, 0, 3, 7, 0, 1, 5), .Dim = c(3L,
          12L), .Dimnames = list(c("x", "y", "z"), NULL))

    # random selection of ties:
    res <- max.col(mm)
    assertTrue(res[1] %in% c(4L, 6L, 7L))
    assertTrue(res[2] %in% c(6L, 9L))
    assertTrue(res[3] %in% c(5L, 11L))

    assertThat(max.col(mm, "first"), identicalTo(c(4L, 6L, 5L)))
    assertThat(max.col(mm, "last"), identicalTo(c(7L, 9L, 11L)))
}