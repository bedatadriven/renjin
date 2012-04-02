
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
	assertThat(xtfrm(c("a","b")), identicalTo(c(1L,2L))))
}

testVapplyWithMatrixResult <- function() {
	res <- vapply(list(c(1L,2L,NA_integer_),c(1L,2L,NA_integer_)), is.na, c(NA,NA,NA))
	assertThat( res, identicalTo( matrix(c(FALSE,FALSE,TRUE,FALSE,FALSE,TRUE), nrow=3)) )
}