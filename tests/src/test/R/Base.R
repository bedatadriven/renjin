
testBquote <- function() {
	model <-  bquote(~0 + .(quote(births)))
	
	assertThat(model, identicalTo( ~0 + births ) )
}

testNamespaces <- function() {
	assertThat(typeof(stats::runif), equalTo("closure"))
}

testBaseNamespaceEnv <- function() {
	baseNamespace <- environment(ls)
	
	assertThat(typeof(baseNamespace$.BaseNamespaceEnv), equalTo("environment"))
	assertThat(typeof(baseenv()$.BaseNamespaceEnv), equalTo("environment"))
}

testXtfrm <- function() {
	assertThat(xtfrm(1:10), identicalTo(1:10))
#	assertThat(xtfrm(c("a","b")), identicalTo(c(1L,2L)))
}

testVapplyWithMatrixResult <- function() {
	res <- vapply(list(c(1L,2L,NA_integer_),c(1L,2L,NA_integer_)), is.na, c(NA,NA,NA))
	print(dim(res))
	assertThat( res, identicalTo( matrix(c(FALSE,FALSE,TRUE,FALSE,FALSE,TRUE), nrow=3)) )
}

testFFT <- function() {
	assertThat( fft(1:4), equalTo(c(10+0i, -2+2i, -2+0i, -2-2i)))
	assertThat( Re(fft(1:5)), equalTo(c(15, -2.5, -2.5, -2.5, -2.5)))
	assertThat( fft(fft(1:4),inverse=TRUE)/4, equalTo(c(1+0i, 2+0i, 3+0i, 4+0i)))
	
}

testDensity <- function() {
	# just make sure it completes without error
	stats::density(rnorm(100))
}