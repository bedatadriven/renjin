# adapted from https://github.com/wch/r-source/tree/trunk/src/library/methods/tests

library(hamcrest)

test.data.assign <- function() {
	
	value <- new("MethodDefinition")
	value@.Data <- function(x) length(x)
	
	assertThat(typeof(value), identicalTo("closure"))
	assertThat(attr(value, 'class')[1], identicalTo("MethodDefinition"))
}

test.data.assign.attribs.preserved <- function() {
	
	data <- c(x='vector')
	attr(data, 'foo') <- 'bar'	
	
	object <- new("signature")
	object@.Data <- data
	
	assertThat(names(object), identicalTo("x"))
	assertThat(attr(object, 'foo'), identicalTo("bar"))

}

test.data.matrix <- function() {

	setClass(
		"Interval",
		representation( type = "character" ),
		prototype(matrix( 0, 0, 2 )),
		contains = "matrix")

	i <- new("Interval")
	i@.Data <- matrix(1:4, nrow=2)

	assertThat(i@.Data, identicalTo(matrix(1:4, nrow=2)))
}

