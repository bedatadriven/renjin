
# general tests of correctness of evaluation

testVectorsDoNotMaskFunctions <- function() {
	c <- 1
	assertThat( c(1,2,3), equalTo(1:3) )	
}

testMissingArgPropogatesToSubsequentCalls <- function() {
	
	f <- function(x) missing(x)
	g <- function(x) f(x)
	
	assertTrue(g())	
}

testMissingArgWithDefaultsDoNotPropogatesToSubsequentCalls <- function() {
	f <- function(x) missing(x)
	g <- function(x=1) f(x)
	
	wasEvaled <- 0
	g(x = (wasEvaled <- 1) )
	
	assertFalse(wasEvaled)
}

