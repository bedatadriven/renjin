
# --------------------------------------
# ASSERTION FUNCTION
# --------------------------------------

assertThat <- function(actual, matcher) {
	if(!matcher(actual)) {
		stop("\nExpected: ", deparse(substitute(matcher)), "\nGot: ", deparse(actual))
	}
}

# --------------------------------------
# MATCHER FUNCTIONS
# --------------------------------------

closeTo <- function(expected, delta) {
    stopifnot(is.numeric(expected) & is.numeric(delta) & length(delta) == 1L)
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

isTrue <- function() {
    function(actual) {
        identical(TRUE, actual)
    }
}

isFalse <- function() {
    function(actual) {
        identical(FALSE, actual)
    }
}

# --------------------------------------
# ABBREVIATIONS
# --------------------------------------

assertTrue <- function(value) {
    assertThat(value, isTrue())
}

assertFalse <- function(value) {
    assertThat(value, isFalse())
}
