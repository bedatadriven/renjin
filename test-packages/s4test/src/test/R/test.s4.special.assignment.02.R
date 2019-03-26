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

.A = setClass("A", slots = list(x = "numeric"))
.B = setClass("B", slots = list(x = "numeric"))
.C = setClass("C", slots = list(x = "numeric"))
setClassUnion("ABC", c("A","B","C"))

.X = setClass("X", slots = list(value = "ABC"))

A <- .A(x = 1)
B <- .B(x = 2)
C <- .C(x = 3)

X <- .X(value = A)
X@value <- B
X@value <- C

test.assign.to.union <- function() { assertTrue(X@value@x == 3) }