library(hamcrest)


test.FFT <- function() {
	assertThat( fft(1:4), equalTo(c(10+0i, -2+2i, -2+0i, -2-2i)))
	assertThat( Re(fft(1:5)), equalTo(c(15, -2.5, -2.5, -2.5, -2.5)))
	assertThat( fft(fft(1:4),inverse=TRUE)/4, equalTo(c(1+0i, 2+0i, 3+0i, 4+0i)))
	
}
