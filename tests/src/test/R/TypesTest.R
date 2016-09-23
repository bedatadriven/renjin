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

test.FunctionCallIsNotPairList <- function() {
	assertFalse( is.pairlist( quote(1+1) ) )
}

test.AsCharacter <- function() {
	assertThat( as.character(1), equalTo(1) )
	assertThat( as.character("foobar"), equalTo("foobar"))
	assertThat( as.character(1L), equalTo("1"))
	assertThat( as.character(1.3333333333333333333333333333333333),
			equalTo("1.33333333333333"))
	assertThat( as.character(TRUE), equalTo("TRUE") )
}

test.AsCharacterWithNA <- function() {
	assertThat(  as.character(NA), identicalTo( NA_character_ ))
}

test.AsCharacterFromList <- function() {
	assertThat( as.character(list(3, 'a', TRUE)), identicalTo(c("3", "a", "TRUE")) )
	assertThat( as.character(list(c(1,3), 'a', TRUE)), equalTo( c("c(1, 3)", "a", "TRUE") ))
}

test.AsCharacterFromSymbol <- function() {
	assertThat( as.character(quote(x)), equalTo( "x" ) )
}

test.AsCharacterFromNull <- function() {
	x <- NULL
	g <- function(b) b
	f <- function(a) g(as.character(a))
	assertThat(f(x), identicalTo(character(0)))
}
