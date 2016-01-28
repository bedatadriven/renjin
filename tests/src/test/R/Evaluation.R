
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

	assertThat(wasEvaled, equalTo(0))
}

test.AttachList <- function() {
    attach(list(a=1,b=2,z=42))
    assertThat(z, equalTo(42))
}

test.DoCallFunctionLookup <- function() {

    list <- 1:10
    do.call("list", list(1,2,3))

}

test.EmptyFirstArgumentInTextSubstr <- function() {
  out <- try(substr(c("abcd"), c(), 1));
  assertThat( class(out), equalTo("try-error"));
}

test.EmptySecondArgumentInTextSubstr <- function() {
  out <- try( substr(c("abcd"), 1, c()) );
assertThat( class( out ), equalTo("try-error"));
}

test.ZeroFirstArgumentInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 0, 1), equalTo("a"));
}

test.ZeroSecondArgumentInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 1, 0), equalTo(""));
}

test.ZeroBothArgumentsInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 0, 0), equalTo(""));
}

test.EmptyInputStringInTextSubstr <- function() {
  out <- substr(c(), 1, 1);
  assertThat( out, identicalTo(character(0)));
}