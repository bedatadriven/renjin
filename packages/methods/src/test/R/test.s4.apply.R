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


setClass("AA",representation(value="integer"))
setMethod("length", signature("AA"), function(x) length(x@value))
a = new("AA", value = 1:10)

test.s4.apply.01 = function() {
    assertThat(lapply(list(a,a,a), length) , identicalTo(list(10L, 10L, 10L)))
}


setClass("A",representation(value="integer"))
setClass("Container", representation(values="list"))
a = new("A", value = 1:10)
b = new("A", value = 1:100)
c = new("Container", values = list(a, b))
setMethod("length", signature("A"), function(x) length(x@value))
setMethod("as.list", signature("Container"), function(x) x@values)
as.list.Container <- function(x) x@values

test.s4.apply.02 = function() {
    assertThat(sapply(c, length) , identicalTo(c(10L, 100L)))
}
