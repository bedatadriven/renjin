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

setClass("A", contains = "numeric")
setMethod("Ops",signature(e1="A",e2="A"), function(e1,e2) 42)

a <- new("A", 99)

assertThat(a+a, identicalTo(42))
assertThat(+a, identicalTo(a))
assertThat(-a, identicalTo(new("A", -99)))

setClass("B", contains = "logical")
setMethod("Ops", signature(e1 = "B", e2 = "B"), function(e1, e2) 43)
setMethod("Ops", signature(e1 = "B"), function(e1) 44)


b <- new("B", TRUE)

assertThat(b & b, identicalTo(43))
assertThat(b | b, identicalTo(43))
assertThat(!b, identicalTo(new("B", FALSE)))


