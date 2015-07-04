

library(hamcrest)

test.negativeComplex <- function() {
	z <- 1+1i
	assertThat( -z, equalTo( complex(real = -1, imaginary = -1)))
}
