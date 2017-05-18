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

setClass("AA", representation(a="numeric"))
setClass("BB", representation(b="character"))
setMethod("[", signature("AA","ANY"), function(x,i,j,...) 1.5)
setMethod("[", signature("AA","AA"), function(x,i,j,...) 2.5)
setMethod("[", signature("BB","ANY"), function(x,i,j,...) 3.5)
setMethod("[", signature("BB","BB"), function(x,i,j,...) 4.5)

f.count <- 0
f <- function() { f.count <<- f.count+1; 1}
a <- new("AA")
b <- new("BB")

test.arg.eval.01 = function() {
    assertThat(a[f()], identicalTo(1.5))
}

test.arg.eval.02 = function() {
    assertThat(f.count, identicalTo(1))
}

test.arg.eval.03 = function() {
    assertThat(a[f()], identicalTo(1.5))
}

test.arg.eval.04 = function() {
    assertThat(f.count, identicalTo(2))
}

test.arg.eval.05 = function() {
    assertThat(b[f()], identicalTo(3.5))
}

test.arg.eval.06 = function() {
    assertThat(f.count, identicalTo(3))
}

test.arg.eval.07 = function() {
    assertThat(b[f()], identicalTo(3.5))
}

test.arg.eval.08 = function() {
    assertThat(f.count, identicalTo(4))
}