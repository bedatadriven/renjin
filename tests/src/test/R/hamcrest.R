

assertTrue <- function(value) {
	if(!value) {
		stop(paste("\nExpected: !", deparse(substitute(value))))
	}
}

assertFalse <- function(value) {
	if(value) {
		stop(paste("\nExpected: !", deparse(substitute(value))))
	}
}

assertThat <- function(actual, matcher) {
	if(!matcher(actual)) {
		stop(paste("\nExpected:", deparse(substitute(matcher)), "\ngot: ", deparse(actual) ))
	}
}

closeTo <- function(expected, delta) {
	function(actual) {
		length(expected) == length(actual) &&
				all(abs(expected-actual)<delta)	
	}
}

identicalTo <- function(expected) {
	function(actual) {
		identical(expected, actual)
	}
}

equalTo <- function(expected) {
	function(actual) {
		length(actual) == length(expected) &&
				actual == expected
	}
}
