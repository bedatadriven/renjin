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


library(stats)
library(hamcrest)

test.ignore.NULL <- function() {
    x <- matrix(c(2,4,3,1,5,7),nrow=3)
    y <- c(7,4,2)

    assertThat(complete.cases(x, y),             identicalTo(c(TRUE, TRUE, TRUE)))
    assertThat(complete.cases(x, y, NULL),       identicalTo(c(TRUE, TRUE, TRUE)))
    assertThat(complete.cases(x, y, numeric(0)), throwsError())
}

test.do.not.ignore.empty <- function() {
    x <- matrix(c(2,4,3,1,5,7),nrow=3)
    y <- c(7,4,NA)

    assertThat(complete.cases(x, y),             identicalTo(c(TRUE, TRUE, FALSE)))
    assertThat(complete.cases(x, y, numeric(0)), throwsError())
}