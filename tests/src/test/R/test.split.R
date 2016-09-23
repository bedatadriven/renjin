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

test.split <- function() {

    df <- data.frame(
        x = seq(9),
        y = rep(1:3, each = 3)
    )

    assertThat( split(df$x, df$y), identicalTo(list("1"=1:3, "2"=4:6, "3"=7:9)) )
    # TODO: fix issue https://github.com/bedatadriven/renjin/issues/208
    #assertThat(
    #    split(c(1,2), factor(c("a", "b"), levels = letters[1:3])),
    #    identicalTo(list(a=1, b=2, c=numeric()))
    #)
    assertThat(
        split(c(1,2), factor(c("a", "b"), levels = letters[1:3]), drop = TRUE),
        identicalTo(list(a=1, b=2))
    )

}
