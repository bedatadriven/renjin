
# Test the interaction between the names attribute and slots on S4 objects
# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests


library(hamcrest)

test.warnWhenAssignNamesAndThereIsNoNamesSlot <- function() {
    setClass("B", contains = "numeric")
    xx <- new("B", 1)

    assertThat( names(xx) <- "A", emitsWarning())
    
    # after this should not warn
    names(xx) <- "A"
    assertThat( names(xx) <- "A", not(emitsWarning()))
}

test.validateSlotAssignments <- function() {
    setClass("A", representation(mySlot = "numeric"))
    a <- new("A", mySlot = 1)
    
    ## test the checks on @<- primitive assignment
    assertThat( a@mySlot <- "A", throwsError())
    
    ## test the checks on @<- primitive assignment
    assertThat( a@nonExistantSlot <- 1, throwsError())
}

test.namesAttributesMayNotBeSetOnS4Objects <- function() {
    setClass("C", representation(aSlot = "numeric", names= "character"))
    c <- new("C", aSlot = 1, names = "A")
    
    assertThat( { names(c) <- "B" }, throwsError())
}

test.namesAttributesMayBeSetOnVectorsWithNamesSlot <- function() {
    setClass("D", contains = "numeric", representation(names = "character"))
    d <- new("D", 1, names = "duck")
    
    names(d) <- "A"
    
    assertThat(d@names, identicalTo("A"))
}


test.namesAttributesMayBeSetOnVectorsWithoutNamesSlotButWithWarning <- function() {
    setClass("D", contains = "numeric", representation(anotherSlot = "character"))
    d <- new("D", 1, anotherSlot = "foo")
    
    assertThat( names(d) <- "A", emitsWarning())
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
