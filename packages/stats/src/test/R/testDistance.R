
library(hamcrest)

test.euclidean <- function() {

  x <- 1:10
  d <- dist(x)

  print(d)

  assertThat(as.vector(d),
    equalTo(c(1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,1,2,3,4,5,6,1,2,3,4,5,1,2,3,4,1,2,3,1,2,1)))

}


test.dist <- function() {
	print(dist(1:10))
}

test.dist.matrix <- function() {
	m <- as.matrix(dist(1:10))
	assertThat(dim(m), equalTo(c(10,10)))
}
