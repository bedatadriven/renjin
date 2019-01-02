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


test.no.breaks <- function() {

    assertThat(.bincode(1:3, double(0)), identicalTo(c(NA_integer_, NA_integer_, NA_integer_)))
    assertThat(.bincode(1:3, double(0), right = FALSE), identicalTo(c(NA_integer_, NA_integer_, NA_integer_)))
    assertThat(.bincode(1:3, double(0), include.lowest = FALSE), identicalTo(c(NA_integer_, NA_integer_, NA_integer_)))

}

test.single.break <- function() {

    assertThat(.bincode(1:3, 2), identicalTo(c(NA_integer_, NA_integer_, NA_integer_)))
    assertThat(.bincode(1:3, 2, right=FALSE), identicalTo(c(NA_integer_, NA_integer_, NA_integer_)))

    assertThat(.bincode(1:3, 2, include.lowest=TRUE, right=TRUE), identicalTo(c(NA, 1L, NA)))
    assertThat(.bincode(1:3, 2, include.lowest=TRUE, right=FALSE), identicalTo(c(NA, 1L, NA)))

}

test.right.breaks <- function() {

    assertThat(.bincode(1:5, c(2,4)), identicalTo(c(NA, NA, 1L, 1L, NA)))
    assertThat(.bincode(1:5, c(2,4), include.lowest=TRUE), identicalTo(c(NA, 1L, 1L, 1L, NA)))

    assertThat(.bincode(1:5, c(1,4)), identicalTo(c(NA, 1L, 1L, 1L, NA)))
    assertThat(.bincode(1:5, c(1,4), include.lowest=TRUE), identicalTo(c(1L, 1L, 1L, 1L, NA)))

    assertThat(.bincode(1:5, c(1,3,5), include.lowest=TRUE), identicalTo(c(1L, 1L, 1L, 2L, 2L)))
}


test.left.breaks <- function() {

    assertThat(.bincode(1:5, c(2,4), right=FALSE), identicalTo(c(NA, 1L, 1L, NA, NA)))
    assertThat(.bincode(1:5, c(2,4), right=FALSE, include.lowest=TRUE), identicalTo(c(NA, 1L, 1L, 1L, NA)))

    assertThat(.bincode(1:5, c(1,4), right=FALSE), identicalTo(c(1L, 1L, 1L, NA, NA)))
    assertThat(.bincode(1:5, c(1,4), right=FALSE, include.lowest=TRUE), identicalTo(c(1L, 1L, 1L, 1L, NA)))

    assertThat(.bincode(1:5, c(1,3,5), right=FALSE, include.lowest=TRUE), identicalTo(c(1L, 1L, 2L, 2L, 2L)))
    assertThat(.bincode(1:5, c(1,3,5), right=FALSE, include.lowest=FALSE), identicalTo(c(1L, 1L, 2L, 2L, NA)))

}