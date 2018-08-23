#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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


library(hamcrest)

# general tests of correctness of evaluation

test.VectorsDoNotMaskFunctions <- function() {
	c <- 1
	assertThat( c(1,2,3), equalTo(1:3) )	
}

test.MissingArgPropogatesToSubsequentCalls <- function() {
	
	f <- function(x) missing(x)
	g <- function(x) f(x)
	
	assertTrue(g())	
}

test.MissingArgWithDefaultsDoNotPropogatesToSubsequentCalls <- function() {
	f <- function(x) missing(x)
	g <- function(x=1) f(x)
	
	wasEvaled <- 0
	g(x = (wasEvaled <- 1) )

	assertThat(wasEvaled, equalTo(0))
}

test.AttachList <- function() {
    attach(list(a=1,b=2,z=42))
    assertThat(z, equalTo(42))
}

test.DoCallFunctionLookup <- function() {

    list <- 1:10
    do.call("list", list(1,2,3))

}

test.EmptyFirstArgumentInTextSubstr <- function() {
  out <- try(substr(c("abcd"), c(), 1));
  assertThat( class(out), equalTo("try-error"));
}

test.EmptySecondArgumentInTextSubstr <- function() {
  out <- try( substr(c("abcd"), 1, c()) );
assertThat( class( out ), equalTo("try-error"));
}

test.ZeroFirstArgumentInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 0, 1), equalTo("a"));
}

test.ZeroSecondArgumentInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 1, 0), equalTo(""));
}

test.ZeroBothArgumentsInTextSubstr <- function() {
  assertThat( substr(c("abcd"), 0, 0), equalTo(""));
}

test.EmptyInputStringInTextSubstr <- function() {
  out <- substr(c(), 1, 1);
  assertThat( out, identicalTo(character(0)));
}

test.PromisesAreForcedByDollar <- function() {

    .data <- 1:12

    capture.promise <- function(data) {
        environment()
    }

    x <- list(env = capture.promise(.data))

    f <- function(z) {
        z[1]
    }

    f(x$env$data)
}

test.PromisesAreForcedBySubset <- function() {

    .data <- 1:12

    capture.promise <- function(data) {
        environment()
    }

    x <- list(env = capture.promise(.data))

    f <- function(z) {
        z[1]
    }

    f(x$env[['data']])
}

test.PromisesAreForcedByGet <- function() {

    .data <- 1:12

    capture.promise <- function(data) {
        environment()
    }

    x <- list(env = capture.promise(.data))

    f <- function(z) {
        z[1]
    }

    f(get(envir = x$env, 'data'))
}

test.RecallInPromise <- function() {

    g <- function(x, y) {
        x + y
    }

    f <- function(n) {
        cat(sprintf("n = %d\n", n))
        if(n < 0) {
            99
        } else {
            Recall(n - 1)
        }
    }

    print(f(3))

}