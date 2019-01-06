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

test.example <- function() {

    x1 <- cbind(1, 1:10)
    x2 <- cbind(x1, 2:11)

    assertThat(kappa(x1), closeTo(15.71, 0.01))
    assertThat(kappa(x1, exact = TRUE), closeTo(13.68, 0.01))

    hilbert <- function(n) { i <- 1:n; 1 / outer(i - 1, i, "+") }
    h9 <- hilbert(9)
    sv9 <- svd(h9)$d
    assertThat(kappa(h9), closeTo(728288806518, 1e3))
    assertThat(kappa(h9, exact = TRUE), closeTo(493154472194, 1e3))
}
