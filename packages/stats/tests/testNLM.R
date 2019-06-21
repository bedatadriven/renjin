
library(hamcrest)

test.nlm <- function() {
		
	f <- function(x) sum((x-1:length(x))^2)
	x <- nlm(f, c(10,10))

	assertThat(x$estimate, closeTo(c(1, 2), 0.00001))
	assertThat(x$code, equalTo(c(1)))
	assertThat(x$minimum, closeTo(c(4.303458e-26), 0.0001e-26))
	assertThat(x$gradient, closeTo(c( 2.757794e-13, -3.099743e-13), 0.00001e-13))
	
}