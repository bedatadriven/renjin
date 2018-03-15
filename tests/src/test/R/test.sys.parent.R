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
library(methods)

## Define some helper functions...

f <- function(n) { fn <- 'f'; sys.parent(n); }
g <- function(n) { fn <- 'g'; f(n); }
h <- function(n) { fn <- 'h'; g(n); }


## When called from the top-level context, sys.parent always returns 0
n1 <- sys.parent()
n2 <- sys.parent(-1);
n3 <- sys.parent(-2);
n4 <- sys.parent(1);
n5 <- sys.parent(2);
n6 <- sys.parent(300);

test.simple.1 = function() { assertThat(n1, identicalTo(0L)) }
test.simple.2 = function() { assertThat(n2, identicalTo(0L)) }
test.simple.3 = function() { assertThat(n3, identicalTo(0L)) }
test.simple.4 = function() { assertThat(n4, identicalTo(0L)) }
test.simple.5 = function() { assertThat(n5, identicalTo(0L)) }
test.simple.6 = function() { assertThat(n6, identicalTo(0L)) }

# Calling h() creates the following call stack:
# 0: TOP
# 1: h()
# 2: g()
# 3: f()
# 4: sys.parent()

# Should return the number of the parent frame relative to f()
h0 <- h(0);
h1 <- h(1);
h2 <- h(2);
h3 <- h(3);
h4 <- h(4);

test.ralative.0 = function() { assertThat(h0, identicalTo(3L))  }# f
test.ralative.1 = function() { assertThat(h1, identicalTo(2L))  }# g
test.ralative.2 = function() { assertThat(h2, identicalTo(1L))  }# h
test.ralative.3 = function() { assertThat(h3, identicalTo(0L))  }# TOP
test.ralative.4 = function() { assertThat(h4, identicalTo(0L))  }# TOP

# S3
a <- structure(pi, class = "foo")
as.integer.foo <- function(x, n) { fn <- "as.integer.foo"; sys.parent(n) }
as.double.foo <- function(x, n) { fn <- "as.double.foo"; as.integer(x, n) }
s3d <- function(x, n) { fn <- "s3d"; as.double(x, n) }
s3c <- function(x, n) { fn <- "s3c"; s3d(x, n) }
s3b <- function(x, n) { fn <- "s3b"; s3c(x, n) }

s3n.1 <- s3b(a, -1)
test.S3n_1 = function() { assertThat( s3n.1, identicalTo(7L)) } #5

s3n.2 <- s3b(a, -2)
test.S3n_2 = function() { assertThat( s3n.2, identicalTo(7L)) } #5

s3n.3 <- s3b(a, -3)
test.S3n_3 = function() { assertThat( s3n.3, identicalTo(7L)) } #5

s3n0 <- s3b(a, 0)
test.S3n0  = function() { assertThat( s3n0 , identicalTo(7L)) } #5

s3n1 <- s3b(a, 1)
test.S3n1  = function() { assertThat( s3n1 , identicalTo(5L)) } #2

s3n2 <- s3b(a, 2)
test.S3n2  = function() { assertThat( s3n2 , identicalTo(3L)) } #1

s3n3 <- s3b(a, 3)
test.S3n3  = function() { assertThat( s3n3 , identicalTo(2L)) } #0

s3n4 <- s3b(a, 4)
test.S3n4  = function() { assertThat( s3n4 , identicalTo(1L)) } #0

s3n5 <- s3b(a, 5)
test.S3n5  = function() { assertThat( s3n5 , identicalTo(0L)) } #0





# S4
.A = setClass("A", slots=list(value="numeric"))
x = .A(value = 10)
setGeneric("add", function(x, n) { standardGeneric("add") })
setMethod("add", signature(x = "A", n = "numeric"), function(x, n) { fn <- "add"; sys.parent(n) })
s4d <- function(x, n) { fn <- "s4d"; add(x, n) }
s4c <- function(x, n) { fn <- "s4c"; s4d(x, n) }
s4b <- function(x, n) { fn <- "s4b"; s4c(x, n) }
s4a.1 <- s4b(x, -1)
s4a.2 <- s4b(x, -2)
s4a.3 <- s4b(x, -3)
s4a0 <- s4b(x, 0)
s4a1 <- s4b(x, 1)
s4a2 <- s4b(x, 2)
s4a3 <- s4b(x, 3)
s4a4 <- s4b(x, 4)
s4a5 <- s4b(x, 5)

test.S4n.1 = function() { assertThat(s4a.1, identicalTo(5L)) }
test.S4n.2 = function() { assertThat(s4a.1, identicalTo(5L)) }
test.S4n.3 = function() { assertThat(s4a.1, identicalTo(5L)) }
test.S4n0  = function() { assertThat(s4a0 , identicalTo(5L)) }
test.S4n1  = function() { assertThat(s4a1 , identicalTo(3L)) }
test.S4n2  = function() { assertThat(s4a2 , identicalTo(2L)) }
test.S4n3  = function() { assertThat(s4a3 , identicalTo(1L)) }
test.S4n4  = function() { assertThat(s4a4 , identicalTo(0L)) }
test.S4n5  = function() { assertThat(s4a5 , identicalTo(0L)) }
