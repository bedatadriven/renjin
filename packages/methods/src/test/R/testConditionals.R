# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

test.setClassSetGeneric <- function() {

	setClass("maybe")
	setClass("A", representation(x = "numeric"))
	setIs("A", "maybe",
          test = function(object)length(object@x) >= 1 && object@x[[1]] > 0,
          coerce = function(from)from,
          replace = function(from, value)
          stop("meaningless to replace the \"maybe\" part of an object"))
    aa <- new("A", x=1)
    setGeneric("ff", function(x)"default ff")

	assertTrue(
		is(ff, "standardGeneric")
		)

	assertThat(
		body(getMethod("ff","ANY")),
		identicalTo("default ff")
		)

	## test that the setGeneric() call created the generic & default
	ffMaybe <- function(x) "ff maybe method"
	setMethod("ff", "maybe", ffMaybe)
	aa2 <- new("A", x = -1) # condition not TRUE

	assertThat(
		ff(aa),
		identicalTo("default ff")
		)

	assertThat(
		ff(aa2),
		identicalTo("default ff")	# failed in R 2.11.0
		)

	## a method to test the condition
	setMethod("ff", "A",
    	  function(x) {
    	      if(is(x, "maybe"))
    		  ffMaybe(x)
    	      else
    		  callNextMethod()
    	  })

	assertThat(
		ff(aa),
		identicalTo("ff maybe method")
		)

	assertThat(
		ff(aa2),
		identicalTo("default ff")
		)

}
