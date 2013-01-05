library(hamcrest)

test.dist <- function() {
	print(dist(1:10))
}

test.dist.matrix <- function() {
	m <- as.matrix(dist(1:10))
	assertThat(dim(m), equalTo(c(10,10)))
}

test.ns <- function() {
	print(ls(envir=.BaseNamespaceEnv$.__S3MethodsTable__.))
}