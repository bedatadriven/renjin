# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

test.data.assign <- function() {
	
	value <- new("MethodDefinition")
	value@.Data <- function(x) length(x)
	
	assertThat(typeof(value), identicalTo("closure"))            ########### PASSED! ###########
	assertThat(attr(value, 'class'), identicalTo("MethodDefinition"))            ########### PASSED! ###########
}

test.data.assign.attribs.preserved <- function() {
	
	data <- c(x='vector')
	attr(data, 'foo') <- 'bar'	
	
	object <- new("signature")
	object@.Data <- data
	
	assertThat(names(object), identicalTo("x"))            ########### PASSED! ###########
	assertThat(attr(object, 'foo'), identicalTo("bar"))            ########### PASSED! ###########
	
	
}


