
library(hamcrest)

test.AccessAttributeRegexpResult <- function() {
	a <- c("abc")
  b <- regexpr('(b)', a, perl = TRUE)

	assertThat( attr(b, "capture.start"), equalTo(2L) )
	assertThat( attr(b, "capture.names"), equalTo("") )
	assertThat( attr(b, "capture.length"), equalTo(1L) )
	assertThat( attr(b, "match.length"), equalTo(1L) )
	assertThat( attr(b, "useBytes"), equalTo(TRUE) )
}