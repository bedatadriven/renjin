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

a <- structure(1L, class = c("A"))
b <- structure(1L, class = c("B"))
ab <- structure(1L, class = c("A", "B"))
abc <- structure(1L, class = c("A", "B", "C"))
c <- structure(1L, class = c("C"))

assertThat(inherits(a, "A"), identicalTo(TRUE))
assertThat(inherits(c, "A"), identicalTo(FALSE))

assertThat(inherits(1L, character(0)), identicalTo(FALSE))

assertThat(inherits(a,  c("A", "B")), identicalTo(TRUE))
assertThat(inherits(ab, c("A", "B")), identicalTo(TRUE))
assertThat(inherits(c,  c("A", "B")), identicalTo(FALSE))

assertThat(inherits(a,  c("A", "B"), which = TRUE), identicalTo(c(1L, 0L)))
assertThat(inherits(ab, c("A", "B"), which = TRUE), identicalTo(c(1L, 2L)))



