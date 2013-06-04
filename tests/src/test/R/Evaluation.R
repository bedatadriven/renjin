
library(hamcrest)

# general tests of correctness of evaluation

test.VectorsDoNotMaskFunctions <- function() {
	c <- 1
	assertThat( c(1,2,3), equalTo(1:3) )	
}

test.MissingArgPropogatesToSubsequentCalls <- function() {
	
	f <- function(x) missing(x)
	g <- function(x) f(x)
	
	assertTrue(g())	
}

test.MissingArgWithDefaultsDoNotPropogatesToSubsequentCalls <- function() {
	f <- function(x) missing(x)
	g <- function(x=1) f(x)
	
	wasEvaled <- 0
	g(x = (wasEvaled <- 1) )
	
	assertFalse(wasEvaled)
}

test.AttachList <- function() {
    attach(list(a=1,b=2,z=42))
    assertThat(z, equalTo(42))
}

test.DoCallFunctionLookup <- function() {

    list <- 1:10
    do.call("list", list(1,2,3))

}