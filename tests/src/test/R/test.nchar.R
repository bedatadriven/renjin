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

test.nchar <- function() {
    # example taken from ?nchar:
    x <- c("asfef", "qwerty", "yuiop[", "b", "stuff.blah.yech")

    assertThat(nchar(x), identicalTo(c(5L, 6L, 6L, 1L, 15L)))

    x[3] <- NA

    assertThat(nchar(x), identicalTo(c(5L, 6L, NA_integer_, 1L, 15L)))
    assertThat(nchar(x, keepNA = TRUE), identicalTo(c(5L, 6L, NA_integer_, 1L, 15L)))
    assertThat(nchar(x, keepNA = FALSE), identicalTo(c(5L, 6L, 2L, 1L, 15L)))

    assertThat(nchar(c(1, 2, 3)), identicalTo(c(1L, 1L, 1L)))

    assertThat(nchar(factor(letters[1:3])), throwsError())
}
