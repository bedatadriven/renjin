#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#

# --------------------------------------
# ASSERTION FUNCTION
# --------------------------------------
deparse0 <- function(expr) {
  paste(deparse(expr), collapse = "")
}

assertThat <- function(actual, matcher) {
	
	call <- match.call()

    matches <- tryCatch( matcher(actual), error = function(e) {

        stop(sprintf("\nassertThat(%s, %s) failed\nError: %s",
    				deparse0(call$actual), deparse0(call$matcher), deparse0(e$message)))

    })

	if(!matches) {
		stop(sprintf("\nassertThat(%s, %s) failed\nGot: %s", 
				deparse0(call$actual), deparse0(call$matcher), deparse0(actual)))
	}
}


assertTrue <- function(value) {

	call <- match.call()

	if(!identical(value, TRUE)) {
		stop(sprintf("\nassertTrue(%s) failed\nGot: %s", 
				deparse0(call$value), deparse0(value)))
	}	
}


assertFalse <- function(value) {

	call <- match.call()

	if(!identical(value, FALSE)) {
		stop(sprintf("\nassertFalse(%s) failed\nGot: %s", 
				deparse0(call$value), deparse0(value)))
	}	
}



# --------------------------------------
# MATCHER FUNCTIONS
# --------------------------------------
compareReal <- function(actual, expected, tol) {
  rel.diff <- abs(expected - actual) / abs(expected)
  finite <- is.finite(rel.diff) & expected != 0
  finiteValuesCloseEnough <- all(rel.diff[finite] < tol)
  nonFiniteValuesIdentical <- identical(expected[!finite], actual[!finite])
  
  return( finiteValuesCloseEnough && nonFiniteValuesIdentical )
}

identical.attributes <- function(actual, expected, tol = NULL) {
    # Should have the same set of names,
    # though not necessarily in the same order
    if(length(setdiff(names(expected), names(actual))) > 0) {
        return(FALSE)
    }
    
    # Otherwise verify that the values are identical
    for(a in names(expected)) {
        if(!identical.rec(actual[[a]], expected[[a]], tol)) {
            return(FALSE)
        }
    }
    return(TRUE)
}

identical.rec <- function(actual, expected, tol = NULL) {
    if (length(actual) != length(expected))
      return(FALSE)
    if (typeof(actual) != typeof(expected))
      return(FALSE)
    if (!identical.attributes(attributes(actual), attributes(expected), tol)) {
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
      compareReal(unclass(actual), unclass(expected), tol)
    } else if (!is.null(tol) && is.complex(actual)) {
      compareReal(unclass(Re(actual)), unclass(Re(expected)), tol) &&
        compareReal(unclass(Im(actual)), unclass(Im(expected)), tol)
    } else {
      return(identical(actual, expected))
    }
}

equal.rec <- function(actual, expected) {
    if (is.list(actual)) {
      for (i in seq_along(actual)) {
        isSame <- equal.rec(actual[[i]], expected[[i]])
        if (!isSame){
          return(FALSE)
        }
      }
    } else {
        return(length(actual) == length(expected) && all(actual == expected))
    }
}

closeTo <- function(expected, delta) {
    stopifnot(is.numeric(expected) & is.numeric(delta) & length(delta) == 1L)
	function(actual) {
		length(expected) == length(actual) &&
				all(abs(expected-actual)<delta)	
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
		if (is.list(actual))
 	        equal.rec(actual, expected)
 		else
 		    length(actual) == length(expected) && all(actual == expected)
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
