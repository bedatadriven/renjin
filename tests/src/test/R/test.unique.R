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

test.unique <- function() {

    assertThat( unique(c("a", "b", "a")), identicalTo(c("a", "b")) )
    assertThat( unique(c("a", "b", "a"), fromLast = TRUE), identicalTo(c("b", "a")) )
    assertThat( unique(c("b", "a", "a")), identicalTo(c("b", "a")) )
    assertThat( unique(c(NA, 1, NA)), identicalTo(c(NA, 1)) )
    assertThat( unique(c(1, NA, NA)), identicalTo(c(1, NA)) )
    assertThat( unique(c(a=1, b=2, c=1)), identicalTo(c(1,2)) )

    # TODO: fix issue https://github.com/bedatadriven/renjin/issues/207
    #assertThat( unique(c(1, NA, NA), incomparables=NA), identicalTo(c(1, NA, NA)) )

    df <- data.frame(
        x = c("a", "b", "c", "b"),
        y = c("x", "y", "z", "y"),
        stringsAsFactors = FALSE
    )

    assertThat( unique(df), identicalTo(df[c(1,2,3),]) )
    assertThat( unique(df, fromLast = TRUE), identicalTo(df[c(1,3,4),]) )

}