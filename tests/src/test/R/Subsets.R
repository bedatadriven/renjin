#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

test.CallReplacement <- function() {
	
	call <- quote(sin(x))
	call[[1]] <- "cos"
	assertThat(typeof(call), equalTo("language"))	
}

test.CallSubsetting <- function() {
	call <- quote(sin(x,y,z))
	call <- call[c(1L,2L)]
	assertThat(typeof(call), equalTo("language"))	
	
}

test.RemoveFunctionFromFunctionCall <- function() {
	call <- quote(sin(x))
	call[[1]] <- NULL
	assertThat(typeof(call), equalTo("pairlist"))
	assertThat(length(call), equalTo(1))
}

test.DataFrameDollar <- function() {

    df <- data.frame(x=1:3)
    df$y <- 4:6

    assertThat(df$y, equalTo(c(4,5,6)))
}


test.RemoveLastElementInPairList <- function() {
    x <- pairlist(a=1,b=1)
    x$b <- NULL

    assertThat(length(x), equalTo(1))
}
