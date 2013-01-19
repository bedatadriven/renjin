library(hamcrest)

test.Bquote <- function() {
	model <-  bquote(~0 + .(quote(births)))
	
	assertThat(model, equalTo( ~0 + births ) )
}

test.Namespaces <- function() {
	assertThat(typeof(stats::runif), equalTo("closure"))
}

test.BaseNamespaceEnv <- function() {
	baseNamespace <- environment(ls)
	
	assertThat(typeof(baseNamespace$.BaseNamespaceEnv), equalTo("environment"))
	assertThat(typeof(baseenv()$.BaseNamespaceEnv), equalTo("environment"))
}

test.Xtfrm <- function() {
	assertThat(xtfrm(1:10), identicalTo(1:10))
#	assertThat(xtfrm(c("a","b")), identicalTo(c(1L,2L)))
}

test.VapplyWithMatrixResult <- function() {
	res <- vapply(list(c(1L,2L,NA_integer_),c(1L,2L,NA_integer_)), is.na, c(NA,NA,NA))
	print(dim(res))
	assertThat( res, identicalTo( matrix(c(FALSE,FALSE,TRUE,FALSE,FALSE,TRUE), nrow=3)) )
}
