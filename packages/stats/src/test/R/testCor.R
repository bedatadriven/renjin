
library(hamcrest)

test.correlationMatrixNames <- function() {

	m <- matrix(1:12, ncol = 4)
	colnames(m) <- letters[1:4]
	
	x <- stats::cor(m, method = "pearson")
	
	assertThat(rownames(x), identicalTo(c("a", "b", "c", "d")))
	assertThat(colnames(x), identicalTo(c("a", "b", "c", "d")))
}