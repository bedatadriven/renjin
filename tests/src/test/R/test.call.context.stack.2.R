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

add1 <- function (x, ...) {
   UseMethod("add1", x)
}
add2 <- function (x, ...) {
   UseMethod("add2", x)
}

add2.foo <- function(x, n) {
  fn <- "add2.foo";
  env <- sys.frame(n)
  list(
    sys.parent(n),
    ls(env),
    sys.call(n),
    sys.nframe()
  )
}

add1.foo <- function(x, n) { fn <- "add.foo"; add2(x, n) }
s3d <- function(x, n) { fn <- "s3d"; add1(x, n) }
s3c <- function(x, n) { fn <- "s3c"; s3d(x, n) }
s3b <- function(x, n) { fn <- "s3b"; s3c(x, n) }

res1 <- s3b(a, 1)
res2 <- s3b(a, 2)
res3 <- s3b(a, 3)
res4 <- s3b(a, 4)
res5 <- s3b(a, 5)
res6 <- s3b(a, 6)
res7 <- s3b(a, 7)
