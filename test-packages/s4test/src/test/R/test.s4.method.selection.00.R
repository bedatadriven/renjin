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

setClass("DD", representation(d = "character"))
setClass("CC", representation(c = "character"), contains = "DD")
setClass("BB", representation(b = "character"), contains = "CC")
setClass("AA", representation(a = "character"), contains = "BB")
d = new("DD", d = "d")
c = new("CC", d = "cd", c = "c")
b = new("BB", d = "bd", c = "bc", b = "b")
a = new("AA", d = "ad", c = "ac", b = "ab", a = "a")

test.method_selection.01 = function() {

    setMethod("[", signature("DD"), function(x, i, j, ...) 3.5)

    assertThat(c[1], identicalTo( c(3.5) ))

}

ignore.test.method_selection.00 = function() {

    setMethod("[", signature("B","C","C"), function(x, i, j, ...) 3.5)
    setMethod("[", signature("B","D","B"), function(x, i, j, ...) 5.5)

    assertThat(a[a,a], identicalTo( c(3.5) ))
    assertThat(b[b,b], identicalTo( c(3.5) ))
    assertThat(a[c,c], identicalTo( c(3.5) ))
    assertThat(a[a,c], identicalTo( c(3.5) ))
    assertThat(a[a,b], identicalTo( c(5.5) ))
    assertThat(a[b,b], identicalTo( c(5.5) ))

}