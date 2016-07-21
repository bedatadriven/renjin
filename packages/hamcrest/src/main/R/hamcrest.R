
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


compareReal <- function(actual, expected, tol) {
  rel.diff <- abs(expected - actual) / abs(expected)
  finite <- is.finite(rel.diff)
  finiteValuesCloseEnough <- all(rel.diff[finite] < tol)
  nonFiniteValuesIdentical <- identical(expected[!finite], actual[!finite])
  return( (finiteValuesCloseEnough &&
      nonFiniteValuesIdentical &&
      identical(attributes(expected), attributes(actual))) )
}


identical.rec <- function(actual, expected, tol = NULL) {
    if (length(actual) != length(expected))
      return(FALSE)
    if (typeof(actual) != typeof(expected))
      return(FALSE)
    if (!identical(attributes(expected), attributes(actual))) {
      return(FALSE)
    }
    if (is.list(actual)) {
      for (i in seq_along(actual)) {
        isSame <- identical.rec(actual[[i]], expected[[i]], tol)
        if (!isSame){
          return(FALSE)
        }
      }
      return(TRUE)
    } else if (!is.null(tol) && is.double(actual)) {
      compareReal(actual, expected, tol)
    } else if (!is.null(tol) && is.complex(actual)) {
      compareReal(Re(actual), Re(expected), tol)
    } else {
      return(identical(actual, expected))
    }
}


identicalTo <- function(expected, tol = NULL) {
	tolMissing <- missing(tol)
	function(actual) {
	    identical.rec(actual, expected, tol)
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
				all(actual == expected)
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