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

    assetThat(            ########### PASSED! ###########
        d@names,
        identicalTo("A")
        )
}

test.setClassDotDataSlot <- function() {
    setClass("B", contains="numeric",representation(haha="numeric"))
    b = new("B", 1:10, haha=2:5)

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
        identicalTo(55)
        )

}
