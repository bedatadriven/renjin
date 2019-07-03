
library(hamcrest)

test.roots <- function() {
	f <- function (x,a) x - a
	xmin <- uniroot(f, c(0, 1), tol = 0.0001, a = 1/3)
	
	assertThat(xmin$root, closeTo(0.333333, 0.00001))
}
