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

test.sink <- function() {

    f1 <- tempfile()
    f2 <- tempfile()
    sink(f1)
    cat("A\n")
    cat("B\n")
    sink(f2)
    cat("C\n")
    cat("D\n")
    sink()
    cat("E\n")
    sink()

    assertThat(readLines(f1), identicalTo(c("A", "B", "E")))
    assertThat(readLines(f2), identicalTo(c("C", "D")))
}


test.sink.number <- function() {

    f1 <- tempfile()
    f2 <- tempfile()

    assertThat(sink.number(), identicalTo(0L))
    sink(f1)
    sink(f2)
    assertThat(sink.number(), identicalTo(2L))
    sink()
    assertThat(sink.number(), identicalTo(1L))
    sink()
    assertThat(sink.number(), identicalTo(0L))
}

