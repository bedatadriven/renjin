library(hamcrest)

test.FunctionCallIsNotPairList <- function() {
	assertFalse( is.pairlist( quote(1+1) ) )
}

test.AsCharacter <- function() {
	assertThat( as.character(1), equalTo(1) )
	assertThat( as.character("foobar"), equalTo("foobar"))
	assertThat( as.character(1L), equalTo("1"))
	assertThat( as.character(1.3333333333333333333333333333333333),
			equalTo("1.33333333333333"))
	assertThat( as.character(TRUE), equalTo("TRUE") )
}

test.AsCharacterWithNA <- function() {
	assertThat(  as.character(NA), identicalTo( NA_character_ ))
}

test.AsCharacterFromList <- function() {
	assertThat( as.character(list(3, 'a', TRUE)), identicalTo(c("3", "a", "TRUE")) )
	assertThat( as.character(list(c(1,3), 'a', TRUE)), equalTo( c("c(1, 3)", "a", "TRUE") ))
}

test.AsCharacterFromSymbol <- function() {
	assertThat( as.character(quote(x)), equalTo( "x" ) )
}

test.AsCharacterFromNull <- function() {
	x <- NULL
	g <- function(b) b
	f <- function(a) g(as.character(a))
	assertThat(f(x), identicalTo(character(0)))
}
