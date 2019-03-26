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

g.default <- function(x,...) list(n=nargs(), call=sys.call())
g.foo <- function(x,i,j) NextMethod()
g <- function(x,i,j) UseMethod('g')
x<-1
class(x) <- 'foo'

assertThat(g(x,1)$n, identicalTo(2L))
assertThat(g(x,1)$call, deparsesTo("g.default(x, 1)"))

assertThat(g(x,1,2)$n, identicalTo(3L))
assertThat(g(x,1,2)$call, deparsesTo("g.default(x, 1, 2)"))
