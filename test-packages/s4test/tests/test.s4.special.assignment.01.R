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

library(hamcrest)
library(methods)

.A = setClass("A", contains = "list",
  representation(name = "character", x = "numeric", y = "numeric"),
  prototype(name = "Zero", x = 0, y = 0))

a <- .A()

test.assignment.01 = function() { assertThat(isS4(a), identicalTo(TRUE)) }
test.assignment.02 = function() { assertThat(a@name, identicalTo("Zero")) }

b <- .A()
b[[1]] <- "something"

test.assignment.03 = function() { assertThat(isS4(b), identicalTo(TRUE)) }
test.assignment.04 = function() { assertThat(b@name, identicalTo("Zero")) }
test.assignment.05 = function() { assertThat(b[[1]], identicalTo("something")) }

c <- .A()
c@name <- "One"
c@x <- 1
c@y <- 1

test.assignment.06 = function() { assertThat(isS4(c), identicalTo(TRUE)) }
test.assignment.07 = function() { assertThat(c@name, identicalTo("One")) }
test.assignment.08 = function() { assertThat(c@x, identicalTo(1)) }
test.assignment.09 = function() { assertThat(c@y, identicalTo(1)) }

d <- .A()
d[[1]] <- "something"
d@name <- "One"
d@x <- 1
d@y <- 1

test.assignment.10 = function() { assertThat(isS4(d), identicalTo(TRUE)) }
test.assignment.11 = function() { assertThat(d@name, identicalTo("One")) }
test.assignment.12 = function() { assertThat(d[[1]], identicalTo("something")) }
test.assignment.13 = function() { assertThat(d@x, identicalTo(1)) }
test.assignment.14 = function() { assertThat(d@y, identicalTo(1)) }

  x1 = try( setGeneric("+", function(x,y) standardGeneric("+")) )
  x2 = try( setGeneric("isS4", function(x,y) standardGeneric("isS4")) )
  x3 = try( setGeneric("pchisq", function(x,y) standardGeneric("pchisq")) )
  x4 = try( setGeneric("nchar", function(x,y) standardGeneric("nchar")) )

test.generic.for.builtin = function() {
  assertThat(is(x1, "try-error"), identicalTo(TRUE) )
  assertThat(is(x2, "try-error"), identicalTo(TRUE) )
  assertThat(is(x3, "try-error"), identicalTo(FALSE) )
  assertThat(is(x4, "try-error"), identicalTo(FALSE) )
}