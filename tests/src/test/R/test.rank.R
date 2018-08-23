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

test.RankNames <- function(){
    assertThat(rank(c(a=3, b=3, c=1)), identicalTo(c(a=2.5, b=2.5, c=1.0)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='min'), identicalTo(c(a=2L, b=2L, c=1L)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='max'), identicalTo(c(a=3L, b=3L, c=1L)))
    assertThat(rank(c(a=3, b=3, c=1), ties.method='average'), identicalTo(c(a=2.5, b=2.5, c=1.0)))
}
