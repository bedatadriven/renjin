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

test.quote <- function() {
    assertThat(encodeString(c("a", "b"), quote="'"), identicalTo(c("'a'", "'b'")))
    assertThat(encodeString(c("a", "b"), quote=""), identicalTo(c("a", "b")))
}

test.nas <- function() {
    assertThat(encodeString(c("a", NA), quote="'", na.encode=TRUE), identicalTo(c("'a'", "NA")))
    assertThat(encodeString(c("a", NA), quote="'", na.encode=FALSE), identicalTo(c("'a'", NA)))
}

test.attributes <- function() {
    x <- c("a", "b")
    names(x) <- c("A", "B")
    class(x) <- "foo"
    attr(x, 'rando') <- 'baz'

    y <- encodeString(x)

    assertThat(names(y), identicalTo(c("A", "B")))
    assertThat(class(y), identicalTo("character"))
    assertThat(attr(y, 'rando'), identicalTo('baz'))
}
