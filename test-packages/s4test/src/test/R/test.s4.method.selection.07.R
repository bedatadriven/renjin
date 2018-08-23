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

setClass("AA", representation(a="numeric"))
setClass("BB", representation(b="character"))
setMethod("[", signature("AA","ANY"), function(x,i,j,...) 1.5)
setMethod("[", signature("AA","AA"), function(x,i,j,...) 2.5)
setMethod("[", signature("BB","ANY"), function(x,i,j,...) 3.5)
setMethod("[", signature("BB","BB"), function(x,i,j,...) 4.5)

f.count <- 0
f <- function() { f.count <<- f.count+1; cat("FFFFFFFFFFFFFFF\n"); 1}
a <- new("AA")
b <- new("BB")

assertThat(a[f()], identicalTo(1.5))
assertThat(f.count, identicalTo(1))

assertThat(a[f()], identicalTo(1.5))
assertThat(f.count, identicalTo(2))

assertThat(b[f()], identicalTo(3.5))
assertThat(f.count, identicalTo(3))

assertThat(b[f()], identicalTo(3.5))
assertThat(f.count, identicalTo(4))

# f() should not be invoked it is the third argument
# and the maximum signature length is 2.

assertThat(b[b,f()], identicalTo(4.5))
assertThat(f.count, identicalTo(4))
