# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

failing.test.setClassInherit <- function(){

    setClass("A", representation("numeric"))
    a <- new("A")

    setMethod("Logic", c("A", "A"), function(e1, e2) FALSE)
    b <- a & a                           # inherit &,A,A-method

    assertThat(
        b,
        identicalTo("FALSE")
        )

    setMethod("Logic", c("A", "A"), function(e1, e2) TRUE)
    c <- a & a

    assertThat(
        c,
        identicalTo("TRUE")
        )

}

### Find inherited group methods:
failing.test.setClassFindInherit <- function(){

    if(require(Matrix)) {
        sm <- selectMethod("-", c("dgCMatrix", "numeric"))# direct match with "Arith"
        s2 <- selectMethod("-", c("dtCMatrix", "numeric"))# ambiguity match with "Arith"

        assertThat(
            sm@generic,
            identicalTo("Arith")
            )

        assertThat(
            sm@s2@generic,
            identicalTo("Arith")
            )
    }

    ## some tests of callGeneric().  It's reccommended for use with group generics
    setGeneric("f1", signature=c("a"),
               function(..., a) standardGeneric("f1"))
    setMethod("f1", c(a="ANY"), function(..., a) list(a=a, ...))
    setMethod("f1", c(a="missing"), function(..., a) callGeneric(a=1, ...))
    f2 <- function(b,c,d, a) {
        if (missing(a))
            f1(b=b, c=c, d=d)
        else
            f1(a=a, b=b, c=c, d=d)
    }

    ## use callGeneric both directly (f1) and indirectly (f2)
    ## Latter failed pre rev. 66408; Bug ID 15937

    assertThat(
        c(1,2,3,4),
        identicalTo(as.vector(unlist(f1(2,3,4))))
        )

    assertThat(
        c(1,2,3,4),
        identicalTo(as.vector(unlist(f2(2,3,4))))
        )

    ## test callGeneric() with no arguments.  This is rarely used
    ## because nearly all applications use the groups Ops, etc.
    ## whose members are primitives => must supply args to callGeneric

    Hide <- setClass("Hide", slots = c(data = "vector"), contains = "vector")

    unhide <- function(obj)
        obj@data

    setGeneric("%p%", function(e1, e2) e1 + e2, group = "Ops2")
    setGeneric("%gt%", function(e1, e2) e1 > e2, group = "Ops2")

    setGroupGeneric("Ops2", function(e1,e2)NULL, knownMembers = c("%p%","%gt%"))

    setMethod("Ops2", c("Hide", "Hide"),
              function(e1, e2) {
                  e1 <- unhide(e1)
                  e2 <- unhide(e2)
                  callGeneric()
              })

    setMethod("Ops2", c("Hide", "vector"),
              function(e1, e2) {
                  e1 <- unhide(e1)
                  callGeneric()
              })

    setMethod("Ops2", c("vector", "Hide"),
              function(e1, e2) {
                  e2 <- unhide(e2)
                  callGeneric()
              })

    h1 <- Hide(data = 1:10)
    h2 <- Hide(data = (1:10)*.5+ 0.5)

    assertTrue(
        all.equal(h1%p%h2, h1@data + h2@data)
        )

    assertTrue(
        all.equal(h1 %gt% h2, h1@data > h2@data)
        )

}
