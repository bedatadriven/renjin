
library(hamcrest)

test.textConnection <- function() {
	con <- textConnection("foobar")
	lines <- readLines(con)
	assertThat(lines, equalTo("foobar"))
}

test.textConnectionManyLines <- function() {
	text <- c("a", "b", "c", "d")
	con <- textConnection(text)
	lines <- readLines(con)
	assertThat(lines, equalTo(text))
}