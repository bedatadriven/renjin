
library(hamcrest)

test.AccessAttributeRegexpOutput <- function() {
	a <- c("abc")
  b <- regexpr('(b)', a, perl = TRUE)

	assertThat( attr(b, "capture.start"), equalTo(2L) )
	assertThat( attr(b, "capture.names"), equalTo("") )
	assertThat( attr(b, "capture.length"), equalTo(1L) )
	assertThat( attr(b, "match.length"), equalTo(1L) )
	assertThat( attr(b, "useBytes"), equalTo(TRUE) )
}

test.AccessAttributeAsDistOutput <- function() {
	a <- matrix(1:9,nrow=3)
  row.names(a) <- c("A","B","C")
  colnames(a) <- c("d","e","f")
  d <- as.dist(a)

	assertThat( attr(d, "Labels"), equalTo(c("A","B","C")) )
	assertThat( attr(d, "Size"), equalTo(3L) )
	assertThat( attr(d, "call"), equalTo("as.dist.default(m = a)") )
	assertThat( attr(d, "class"), equalTo("dist") )
	assertThat( attr(d, "Diag"), equalTo(FALSE) )
	assertThat( attr(d, "Upper"), equalTo(FALSE) )
}
