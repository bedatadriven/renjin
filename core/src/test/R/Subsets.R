

testCallReplacement <- function() {
	
	call <- quote(sin(x))
	call[[1]] <- "cos"
	assertThat(typeof(call), equalTo("language"))	
}

testCallSubsetting <- function() {
	call <- quote(sin(x,y,z))
	call <- call[c(1L,2L)]
	assertThat(typeof(call), equalTo("language"))	
	
}

testRemoveFunctionFromFunctionCall <- function() {
	call <- quote(sin(x))
	call[[1]] <- NULL
	assertThat(typeof(call), equalTo("pairlist"))
	assertThat(length(call), equalTo(1))
}