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


setClass("A", representation(a="numeric"))
a <- new("A", a = 10)
setMethod("[[", c(x="A", j="missing", i="missing"), function(x, ...)       1.4 )
setMethod("[[", c(x="A", j="ANY",     i="ANY"    ), function(x, i, j, ...) 1.43)
setMethod("[[", c(x="A", j="missing", i="A"      ), function(x, i, ...)    1.5 )
setMethod("[[", c(x="A", j="ANY",     i="A"      ), function(x, i, j, ...) 1.55)
setMethod("[[", c(x="A", j="A",       i="missing"), function(x, j, ...)    1.6 )
setMethod("[[", c(x="A", j="A",       i="ANY"    ), function(x, i, j, ...) 1.7 )
setMethod("[[", c(x="A", i="A",       j="A"      ), function(x, i, j, ...) 1.8 )

test.s4.missing.01 = function() {
    assertThat( a[[]] , identicalTo( 1.4 ))
}

test.s4.missing.02 = function() {
    assertThat( a[[a,a]] , identicalTo( 1.8 ))
}

test.s4.missing.03 = function() {
    assertThat( a[[a,]] , identicalTo( 1.5 ))
}

test.s4.missing.04 = function() {
    assertThat( a[[,a]] , identicalTo( 1.6 ))
}

test.s4.missing.05 = function() {
    assertThat( a[[i=a,j=]] , identicalTo( 1.5 ))
}

# GNU R will return 1.5, and doesn't consider provided argument names
test.s4.missing.06 = function() {
    assertThat( a[[j=a,i=]] , identicalTo( 1.6 ))
}


setClass("B", representation(b = "numeric"))
b <- new("B", b = 10)
setMethod("[[", c(x = "B", i = "missing", j = "ANY"    ), function(x, j,    ...) 1.47)

test.s4.missing.07 = function() {
    assertThat( b[[,1]] , identicalTo( 1.47 ))
}

test.s4.missing.08 = function() {
    assertThat( b[[]] , identicalTo( 1.47 ))
}

test.s4.missing.09 = function() {
    setMethod("[[", c(x = "B", i = "missing", j = "missing"), function(x,       ...) 1.46)
    assertThat( b[[]] , identicalTo( 1.46 ))
}
