# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

test.setClassNames1 <- function() {
    setClass("B", contains = "numeric")
    xx <- new("B", 1)

    assertTrue(
        is(tryCatch(names(xx) <- "A" , warning = function(e)e), "warning")
        )

    # after this should not warn
    names(xx) <- "A"
    assertTrue(
        is(tryCatch(names(xx) <- "B" , warning = function(e)e), "character")
        )
}

test.setClassNames2 <- function() {
    setClass("A", representation(xx = "numeric"))
    a <- new("A", xx = 1)

    assertTrue(
        is(tryCatch(names(a) <- "A" , error = function(e)e), "error")
        )

    ## test the checks on @<- primitive assignment
    assertTrue(
        is(tryCatch(a@xx <- "A" , error = function(e)e), "error")
        )

    ## test the checks on @<- primitive assignment
    assertTrue(
        is(tryCatch(a@yy <- 1 , error = function(e)e), "error")
        )
}

test.setClassNamesRepresentation <- function() {
    setClass("C", representation(xx = "numeric", names= "character"))
    c <- new("C", xx = 1, names = "A")
    c@names <- "B"

    assertTrue(
        is(tryCatch(names(c) <- "A" , error = function(e)e), "error")
        )
}


test.setClassNamesContains <- function() {
    setClass("D", contains = "numeric", representation(names = "character"))
    d <- new("D", 1)
    names(d) <- "A"

    assertThat(
        d@names,
        identicalTo("A")
        )
}

test.setClassDotDataSlot <- function() {
    setClass("B", contains = "numeric", representation(haha = "numeric"))
    b = new("B", 1:10, haha = 2:5)
    x = c(1.1, 1.2, 1.3, 1.4, 1.5)
    c = new("B", x, haha = 5:2)
    d = b * c
    e = x * 1:10

    assertThat(
        b@.Data,
        identicalTo(1:10)
        )

    assertThat(
        b@haha,
        identicalTo(2:5)
        )

    assertThat(
        sum(b),
        identicalTo(55L)
        )

    assertTrue(
        is(b@.Data, class(1:10))
        )

    assertTrue(
        is(c@.Data, class(x))
        )

    assertTrue(
        is(d@.Data, class(e))
        )

    assertThat(
        d@.Data,
        identicalTo(e)
        )

    assertThat(
        d@haha,
        identicalTo(b@haha)
        )

    assertTrue(
        is(b, "B")
        )

    assertTrue(
        is(c, "B")
        )

    assertTrue(
        is(d, "B")
        )


}
