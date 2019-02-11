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

a <- structure(pi, class = "foo")

testSys <- function (x, ...) {
    UseMethod("testSys", x)
}

callSys <- function (x, ...) {
    UseMethod("callSys", x)
}

testSys.foo <- function(x, n) {
  fn <- "testSys.foo";
  list(
    sys.parent(n),
    sys.call(n),
    sys.nframe()
  )
}

callSys.foo <- function(x, n) { fn <- "callSys.foo"; testSys(x, n) }
s3d <- function(x, n) { fn <- "s3d"; callSys(x, n) }
s3c <- function(x, n) { fn <- "s3c"; s3d(x, n) }
s3b <- function(x, n) { fn <- "s3b"; s3c(x, n) }

res1 <- s3b(a, 1)
res2 <- s3b(a, 2)
res3 <- s3b(a, 3)
res4 <- s3b(a, 4)
res5 <- s3b(a, 5)
res6 <- s3b(a, 6)
res7 <- s3b(a, 7)

test.res1 = function() {
  assertThat(res1[[1]], identicalTo(5L))
  assertThat(deparse(res1[[2]]), identicalTo("s3b(a, 1)"))
  assertThat(res1[[3]], identicalTo(7L))
}

test.res2 = function() {
  assertThat(res2[[1]], identicalTo(3L))
  assertThat(deparse(res2[[2]]), identicalTo("s3c(x, n)"))
  assertThat(res2[[3]], identicalTo(7L))
}

test.res3 = function() {
  assertThat(res3[[1]], identicalTo(2L))
  assertThat(deparse(res3[[2]]), identicalTo("s3d(x, n)"))
  assertThat(res3[[3]], identicalTo(7L))
}

test.res4 = function() {
  assertThat(res4[[1]], identicalTo(1L))
  assertThat(deparse(res4[[2]]), identicalTo("callSys(x, n)"))
  assertThat(res4[[3]], identicalTo(7L))
}

test.res5 = function() {
  assertThat(res5[[1]], identicalTo(0L))
  assertThat(deparse(res5[[2]]), identicalTo("callSys.foo(x, n)"))
  assertThat(res5[[3]], identicalTo(7L))
}

test.res6 = function() {
  assertThat(res6[[1]], identicalTo(0L))
  assertThat(deparse(res6[[2]]), identicalTo("testSys(x, n)"))
  assertThat(res6[[3]], identicalTo(7L))
}

test.res7 = function() {
  assertThat(res7[[1]], identicalTo(0L))
  assertThat(deparse(res7[[2]]), identicalTo("testSys.foo(x, n)"))
  assertThat(res7[[3]], identicalTo(7L))
}
