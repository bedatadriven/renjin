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
library(methods)

# S3
a <- structure(pi, class = "foo")
as.integer.foo <- function(x) { fn <- "as.integer.foo"; sys.parents() }
as.double.foo <- function(x) { fn <- "as.double.foo"; as.integer(x) }
s3d <- function(x) { fn <- "s3d"; as.double(x) }
s3c <- function(x) { fn <- "s3c"; s3d(x) }
s3b <- function(x) { fn <- "s3b"; s3c(x) }

.s3b <- s3b(a) #
.s3c <- s3c(a) #
.s3d <- s3d(a) #
.as.double <- as.double(a) #
.as.integer <- as.integer(a) #
.as.double.foo <- as.double.foo (a) #
.as.integer.foo <- as.integer.foo(a) #c(0)

test.S3_1 = function() { assertThat( .s3b, identicalTo(c(0L, 1L, 2L, 3L, 3L, 5L, 5L))) }
test.S3_2 = function() { assertThat( .s3c, identicalTo(c(0L, 1L, 2L, 2L, 4L, 4L))) }
test.S3_3 = function() { assertThat( .s3d, identicalTo(c(0L, 1L, 1L, 3L, 3L))) }
test.S3_4  = function() { assertThat( .as.double , identicalTo(c(0L, 0L, 2L, 2L))) }
test.S3_5  = function() { assertThat( .as.integer , identicalTo(c(0L, 0L))) }
test.S3_6  = function() { assertThat( .as.double.foo , identicalTo(c(0L, 1L, 1L))) }
test.S3_7  = function() { assertThat( .as.integer.foo , identicalTo(0L)) }


# S4
.A = setClass("A", slots=list(value="numeric"))
x = .A(value = 10)
setGeneric("add", function(x) { standardGeneric("add") })
setMethod("add", signature(x = "A"), function(x) { fn <- "add"; sys.parents() })
s4d <- function(x) { fn <- "s4d"; add(x) }
s4c <- function(x) { fn <- "s4c"; s4d(x) }
s4b <- function(x) { fn <- "s4b"; s4c(x) }

.s4b <- s4b(x)
.s4c <- s4c(x)
.s4d <- s4d(x)
.add <- add(x)

test.S4n.1 = function() { assertThat(.s4b, identicalTo(c(0L, 1L, 2L, 3L, 3L))) }
test.S4n.2 = function() { assertThat(.s4c, identicalTo(c(0L, 1L, 2L, 2L))) }
test.S4n.3 = function() { assertThat(.s4d, identicalTo(c(0L, 1L, 1L))) }
test.S4n0  = function() { assertThat(.add , identicalTo(c(0L, 0L))) }
