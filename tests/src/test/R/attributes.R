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

test.AccessAttributeDataframe <- function() {
  a <- data.frame(a=c(1,2),b=c(2,1))
  row.names(a) = c('A','B')

	assertThat( attr(a, "names"), equalTo(c("a","b")) )
	assertThat( attr(a, "row.names"), equalTo(c("A","B")) )
	assertThat( attr(a, "class"), equalTo("data.frame") )

}

test.AccessAttributeList <- function() {
	a <- list(a=c(0), b = list())
	assertThat( attr(a, "names"), equalTo(c("a","b")) )
}

ignor.test.AccessAttributeMatrix <- function() {
	a <- matrix(1:9,nrow=3)
  row.names(a) <- c("A","B","C")
  colnames(a) <- c("d","e","f")

	assertThat( attr(a, "dim"), equalTo(c(3,3)) )
	assertThat( attr(a, "dimnames")[[1]], equalTo(c("A","B","C")) )
	assertThat( attr(a, "dimnames")[[2]], equalTo("d","e","f") )
}

ignor.test.AccessAttributeRegexpOutput <- function() {
	a <- c("abc")
  b <- regexpr('(b)', a, perl = TRUE)

	assertThat( attr(b, "capture.start"), equalTo(2L) )
	assertThat( attr(b, "capture.names"), equalTo("") )
	assertThat( attr(b, "capture.length"), equalTo(1L) )
	assertThat( attr(b, "match.length"), equalTo(1L) )
	assertThat( attr(b, "useBytes"), equalTo(TRUE) )
}

ignor.test.AccessAttributeAsDistOutput <- function() {
	a <- matrix(1:9,nrow=3)
  row.names(a) <- c("A","B","C")
  colnames(a) <- c("d","e","f")
  d <- as.dist(a)

	assertThat( attr(d, "Labels"), equalTo(c("A","B","C")) )
	assertThat( attr(d, "Size"), equalTo(3L) )
	assertThat( attr(d, "call"), equalTo("as.dist.default(m = a)") )
	assertThat( attr(d, "class"), equalTo("dist") )
	assertThat( attr(d, "Diag"), equalTo(FALSE) )
	assertThat( attr(d, "Upper"), equalTo(FALSE) )
}