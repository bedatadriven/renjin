

library(hamcrest)


test.matrix.to.vector <- function() {

    m <- matrix(1:4, nrow = 1)
    dimnames(m) <- list("x", letters[1:4])

    v <- drop(m)

    assertThat(dim(v), is.null)
    assertThat(dimnames(v), is.null)
    assertThat(names(v), identicalTo(letters[1:4]))
}

test.matrix.to.vector2 <- function() {

    m <- matrix(1:4, nrow = 1)
    dimnames(m) <- list("x", NULL)

    v <- drop(m)

    assertThat(dim(v), is.null)
    assertThat(dimnames(v), is.null)
    assertThat(names(v), is.null)
}



test.matrix.to.matrix <- function() {

    m <- matrix(1:12, nrow = 3)
    dimnames(m) <- list(NULL, letters[1:4])

    v <- drop(m)

    str(attributes(v))

    assertThat(dim(v), identicalTo(c(3L, 4L)))
    assertThat(dimnames(v), identicalTo(list(NULL, letters[1:4])))
}

test.array.to.matrix <- function() {

    a <- 1:12
    dim(a) <- c(1, 3, 4)
    dimnames(a) <- list("x", c("y1", "y2", "y3"), c("z1", "z2", "z3", "z4"))

    m <- drop(a)

    str(attributes(m))

}