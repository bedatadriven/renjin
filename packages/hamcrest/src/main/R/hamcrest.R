

assertTrue <- function(value) {
	if(!value) {
		stop("\nExpected: !", deparse(substitute(value)))
	}
}

assertFalse <- function(value) {
	if(value) {
		stop("\nExpected: !", deparse(substitute(value)))
	}
}

assertThat <- function(actual, matcher) {
	if(!matcher(actual)) {
		stop("\nExpected: ", deparse(substitute(matcher)), "\nGot: ", deparse(actual))
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

instanceOf <- function(expected) {
    function(actual) {
        inherits(actual, expected)
    }
}
