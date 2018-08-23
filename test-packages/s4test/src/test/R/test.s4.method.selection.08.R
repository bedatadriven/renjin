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
a <- new("AA")
x <- 2L

setMethod("[", signature(x="AA"), function(x,i,j,...) ifelse(i>j,10,20) )

test.arg.eval.01 = function() {
    assertThat(a[x+1,x+2], identicalTo(20))
}

test.arg.eval.02 = function() {
    assertThat(a[x+2L, x+1L], identicalTo(10))
}