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


setClass("D", representation(d = "character"))
setClass("C", representation(c = "character"), contains = "D")
setClass("B", representation(b = "character"), contains = "C")
setClass("A", representation(a = "character"), contains = "B")
d = new("D", d = "d")
c = new("C", d = "cd", c = "c")
b = new("B", d = "bd", c = "bc", b = "b")
a = new("A", d = "ad", c = "ac", b = "ab", a = "a")

setMethod("[", signature("B","C","C"), function(x, i, j, ...) "B#C#C")
setMethod("[", signature("B","D","B"), function(x, i, j, ...) "B#D#B")
setMethod("[", signature("C","C","A"), function(x, i, j, ...) "C#C#A")
setMethod("[", signature("C","A","C"), function(x, i, j, ...) "C#A#C")
setMethod("[", signature("A","C","C"), function(x, i, j, ...) "A#C#C")
setMethod("[", signature("A","D","B"), function(x, i, j, ...) "A#D#B")

oneOf <- function(...) {
    expected <- c(...)
    function(actual) {
        actual %in% expected
    }
}


test.method_selection.05a = function() {
    assertThat(a[a,a], oneOf("A#C#C", "A#D#B"))
}

test.method_selection.05b = function() {
    assertThat(b[b,b], oneOf("B#C#C", "B#D#B"))
}

test.method_selection.05c = function() {
    assertThat(b[b,a], oneOf("C#C#A", "B#C#C", "B#D#B"))
}

test.method_selection.05d = function() {
    assertThat(a[a,b], oneOf("A#C#C", "A#D#B", "C#A#C"))
}

test.method_selection.05e = function() {
    assertThat(a[b,b], oneOf("A#C#C", "A#D#B"))
}