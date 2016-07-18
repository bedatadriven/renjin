
# --------------------------------------
# ASSERTION FUNCTION
# --------------------------------------

assertThat <- function(actual, matcher) {
	
	call <- match.call()

	if(!matcher(actual)) {
		stop(sprintf("\nassertThat(%s, %s) failed\nGot: %s", 
				deparse(call$actual), deparse(call$matcher), deparse(actual)))
	}
}


assertTrue <- function(value) {

	call <- match.call()

	if(!identical(value, TRUE)) {
		stop(sprintf("\nassertTrue(%s) failed\nGot: %s", 
				deparse(call$value), deparse(value)))
	}	
}


assertFalse <- function(value) {

	call <- match.call()

	if(!identical(value, FALSE)) {
		stop(sprintf("\nassertFalse(%s) failed\nGot: %s", 
				deparse(call$value), deparse(value)))
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

identicalTo <- function(expected, tol) {
	function(actual) {
	    # When comparing floating point values, round the results 
	    # to a fixed number of singificant digits before comparing, if 
	    # the signif argument is provided
	    if(!missing(tol) && is.double(expected) && is.double(actual)) {
	        rel.diff <- abs(expected - actual) / abs(expected)
	        
	        prettyMuchEqual <- all( (rel.diff < tol) | !is.finite(rel.diff) )
	        
	        prettyMuchEqual && identical(attributes(expected), attributes(actual))
	    
	    } else { 
            identical(expected, actual)
	    }
	}
}

deparsesTo <- function(expected) {
    function(actual) {
        identical(paste(deparse(actual), collapse=""), expected)
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

throwsError <- function() {
	function(actual) {
		result <- tryCatch( force(actual), error = function(e) e )
		return(inherits(result, "error")) 
	}
}

emitsWarning <- function() {
	function(actual) {
		result <- tryCatch( force(actual), warning = function(e) e )
		return(inherits(result, "warning")) 
	}
}

not <- function(matcher) {
	function(actual) {
		return(!matcher(actual))
	}
}