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

test.rowsum.default <- function() {
    x <- rep(1, 9)
    group <- c(10, 10, 1, 1, 1, 3, 4, 5, 3)
    assertThat(
        rowsum(x, group),
        identicalTo(matrix(c(3, 2, 1, 1, 2), ncol = 1, dimnames = list(c("1", "3", "4", "5", "10"), NULL)))
    )

    assertThat(
        rowsum(matrix(c(1, 0.5, 0.1), ncol = 3, nrow = 3), group = c(1, 1, 2)),
        identicalTo(matrix(c(1.5, 0.1, 1.5, 0.1, 1.5, 0.1), ncol = 3, dimnames = list(c("1", "2"), NULL)))
    )

    # ensure that integers are retained:
    assertThat(
        rowsum(1L, group = 1),
        identicalTo(matrix(1L, ncol = 1, dimnames = list("1", NULL)))
    )
}

test.rowsum.data.frame <- function() {
    df <- data.frame(
        char = letters[1:5],
        num1 = c(1, 1, 1, 1, 1),
        num2 = seq(0.1, 0.5, 0.1),
        logi = logical(5),
        stringsAsFactors = FALSE)

    group <- c(1, 1, 1, 2, 2)

    # throw error on non-numeric columns:
    assertThat(rowsum(df, group), throwsError())

    assertThat(
        rowsum(df[, 2, drop = FALSE], group),
        identicalTo(matrix(c(3, 2), ncol = 1, dimnames = list(c("1", "2"), NULL)))
        )
    # setting the tolerance in the next test is required to get it to pass:
    assertThat(
        rowsum(df[, 2:3], group),
        identicalTo(matrix(c(3, 2, 0.6, 0.9), ncol = 2, dimnames = list(c("1", "2"), NULL)), tol = 1e-9)
        )
}
