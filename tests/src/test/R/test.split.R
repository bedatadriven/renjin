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

test.split <- function() {

    df <- data.frame(
        x = seq(9),
        y = rep(1:3, each = 3)
    )

    assertThat( split(df$x, df$y), identicalTo(list("1"=1:3, "2"=4:6, "3"=7:9)) )

    assertThat(
        split(c(1,2), factor(c("a", "b"), levels = letters[1:3])),
        identicalTo(list(a=1, b=2, c=numeric()))
    )
    assertThat(
        split(c(1,2), factor(c("a", "b"), levels = letters[1:3]), drop = TRUE),
        identicalTo(list(a=1, b=2))
    )

    # 'f' longer than 'x':
    assertThat(
        split(1:2, 1:3),
        identicalTo(list("1" = 1L, "2" = 2L, "3" = integer(0)))
    )

    # 'x' longer than 'f' (should also throw a warning):
    assertThat(
        split(1:3, 1:2),
        identicalTo(list("1" = c(1L, 3L), "2" = 2L))
    )

    # NA in factor:
    assertThat(
        split(seq(5), c(1, 1, NA, 2, 2)),
        identicalTo(list("1" = c(1L, 2L), "2" = c(4L, 5L)))
    )
}
