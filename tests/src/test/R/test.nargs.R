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

f = function(x,y) {
  paste(">", ">", ">", nargs())
}

assertThat(f(), identicalTo("> > > 0"))
assertThat(f(1), identicalTo("> > > 1"))
assertThat(f(1,1), identicalTo("> > > 2"))


g <- function(i,j=3,...) nargs()

assertThat(g(), identicalTo(0L))
assertThat(g(i=1), identicalTo(1L))
assertThat(g(x=9,y=10,z=11), identicalTo(3L))
