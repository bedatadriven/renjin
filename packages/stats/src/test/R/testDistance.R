
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

test.dist.matrix.wrapper <- function() {
  
  triangle <- structure(c(1, 2, 3, 1, 2, 1), class = "dist", Upper = FALSE, Diag = FALSE, Size = 4L)

  m <- as.matrix(triangle)


  assertThat(m, identicalTo(structure(c(0, 1, 2, 3, 1, 0, 1, 2, 2, 1, 0, 1, 3, 2, 1, 0),
                              .Dim = c(4L, 4L), 
                              .Dimnames = list(c("1", "2", "3", "4"), 
                                               c("1", "2", "3", "4")))))

}
  


  
