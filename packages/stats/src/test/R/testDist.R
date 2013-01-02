library(hamcrest)

test.dist <- function() {
	print(dist(1:10))
}

test.dist.matrix <- function() {
	m <- as.matrix(dist(1:10))
	assertThat(dim(m), equalTo(c(10,10)))
}