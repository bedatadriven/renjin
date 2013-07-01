
library(hamcrest)

test.data.assign <- function() {
	
	value <- new("MethodDefinition")
	value@.Data <- function(x) length(x)
	
	assertThat(typeof(value), equalTo("closure"))
	assertThat(attr(value, 'class'), equalTo("MethodDefinition"))
}

test.data.assign.attribs.preserved <- function() {
	
	data <- c(x='vector')
	attr(data, 'foo') <- 'bar'	
	
	object <- new("signature")
	object@.Data <- data
	
	assertThat(names(object), equalTo("x"))
	assertThat(attr(object, 'foo'), equalTo("bar"))
	
	
}


