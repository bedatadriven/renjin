# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

failing.test.setClassNextWithDot <- function() {
    setClass("A")
    setClass("B", contains = c("array", "A"))
    a = array(1:12, c(2,3,4))
    bb = new("B", a)
    a2 = array(8:1, rep(2,3))

	assertThat(
		initialize(bb, a2),
		identicalTo(new("B",a2))
		)

    withDots <- function(x, ...) names(list(...))
    setGeneric("withDots")
    setClass("C", representation(x="numeric", y="character"))
    setMethod("withDots", "C", function(x, ...)
              callNextMethod()
              )

	assertThat(
		withDots(1, a=1, b=2),
		identicalTo(withDots(new("C"), a=1, b=2))
		)


}
