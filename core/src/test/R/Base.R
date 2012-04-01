
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