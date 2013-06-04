library(hamcrest)

test.CallReplacement <- function() {
	
	call <- quote(sin(x))
	call[[1]] <- "cos"
	assertThat(typeof(call), equalTo("language"))	
}

test.CallSubsetting <- function() {
	call <- quote(sin(x,y,z))
	call <- call[c(1L,2L)]
	assertThat(typeof(call), equalTo("language"))	
	
}

test.RemoveFunctionFromFunctionCall <- function() {
	call <- quote(sin(x))
	call[[1]] <- NULL
	assertThat(typeof(call), equalTo("pairlist"))
	assertThat(length(call), equalTo(1))
}

test.DataFrameDollar <- function() {

    df <- data.frame(x=1:3)
    df$y <- 4:6

    assertThat(df$y, equalTo(c(4,5,6)))
}

