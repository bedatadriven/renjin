# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(stats)
library(methods)
library(hamcrest)

test.setClassInheritNonConditionalExplicit <- function(){

	## test (non-conditional) explicit inheritance
    setClass("xy", representation(x = "numeric", y = "numeric"))

	setIs("xy", "complex",
    		coerce = function(from) complex(real = from@x, imaginary = from@y),
    		replace = function(from, value) {
    			from@x <- Re(value)
    			from@y <- Im(value)
    			from
    		})

	set.seed(124)
    x1 <- rnorm(10)
    y1 <- rnorm(10)
    cc <- complex(real = x1, imaginary = y1)
    xyc <- new("xy", x = x1, y = y1)
    asxyc <- as(xyc, "complex")

	assertFalse(
		isS4(asxyc)
		)

	assertTrue(
		isS4(xyc)
		)

    assertThat(
    	cc,
    	equalTo(as(xyc, "complex"))
   	)

    as(xyc, "complex") <- cc * 1i
    nxy <- new("xy", x = -y1, y = x1)

	assertTrue(
		isS4(nxy)
		)

    assertThat(
    	attributes(xyc),
    	identicalTo(attributes(nxy))
    	)

    assertThat(
    	nxy@x,
    	identicalTo(xyc@x)
    	)

    assertThat(
    	nxy@y,
    	identicalTo(xyc@y)
    	)

    assertThat(
    	xyc,
    	identicalTo(new("xy", x = -y1, y = x1))
    	)

	setGeneric("size", function(x, y) standardGeneric("size"))

	assertThat(
		selectMethod("size", c("ANY","ANY"), optional = TRUE),
		is.null
		)

	assertTrue(
		is(size, "standardGeneric")
		)

	assertTrue(
		is.null(selectMethod("size", c("ANY","ANY"), optional = TRUE))
		)

	setMethod("size", signature(x = "vector", y = "vector"), function(x, y) length(x) + length(y))


#	m <- getMethod("size", "vector")
#   cat(c("attributes(m) = ", deparse(attributes(m)), "\n"))

    assertThat(
    	size(xyc, xyc),
    	identicalTo(length(x1) + length(x1))
    	)

}