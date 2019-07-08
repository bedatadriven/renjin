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


test.method_selection.04 = function() {
    setClass("D", representation(d = "character"))
    setClass("C", representation(c = "character"), contains = "D")
    setClass("B", representation(b = "character"), contains = "C")
    setClass("A", representation(a = "character"), contains = "B")
    d = new("D", d = "d")
    c = new("C", d = "cd", c = "c")
    b = new("B", d = "bd", c = "bc", b = "b")
    a = new("A", d = "ad", c = "ac", b = "ab", a = "a")

    setMethod("[", signature("B","C","C"), function(x, i, j, ...) 3.5)
    setMethod("[", signature("B","D","B"), function(x, i, j, ...) 5.5)
    setMethod("[", signature("C","C","A"), function(x, i, j, ...) 7.5)
    setMethod("[", signature("C","A","C"), function(x, i, j, ...) 9.5)
    setMethod("[", signature("A","C","C"), function(x, i, j, ...) 11.5)
    setMethod("[", signature("A","B","D"), function(x, i, j, ...) 13.5)

    assertThat(a[a,a], identicalTo( c(13.5) ))
}