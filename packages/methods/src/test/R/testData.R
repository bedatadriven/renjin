# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

test.data.assign <- function() {
	
	value <- new("MethodDefinition")
	value@.Data <- function(x) length(x)
	
	assertThat(typeof(value), identicalTo("closure"))
	assertThat(attr(value, 'class'), identicalTo("MethodDefinition"))
}

test.data.assign.attribs.preserved <- function() {
	
	data <- c(x='vector')
	attr(data, 'foo') <- 'bar'	
	
	object <- new("signature")
	object@.Data <- data
	
	assertThat(names(object), identicalTo("x"))
	assertThat(attr(object, 'foo'), identicalTo("bar"))
	
	
}


