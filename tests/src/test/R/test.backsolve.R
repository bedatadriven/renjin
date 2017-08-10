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


test.example <- function() {

    r <- rbind(c(1,2,3),
                c(0,1,1),
                c(0,0,2))

    x <- c(8,4,2)

    y <- backsolve(r, x)  # -1 3 1

    assertThat(y, identicalTo(c(-1, 3, 1)))

    assertThat(r %*% y, identicalTo(matrix(nrow=3, data=c(8, 4, 2))))
    assertThat(backsolve(r, x, transpose = TRUE), identicalTo(c(8, -12, -5)))

}