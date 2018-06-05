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

test.constants <- function() {

    assertThat(bquote(1L), identicalTo(1L))
    assertThat(bquote(1L+x), identicalTo(quote(1L+x)))
}

test.variables <- function() {

    x <- quote(y)
    z <- 43

    assertThat(bquote(x), identicalTo(quote(x)))
    assertThat(bquote(x + .(x)), identicalTo(quote(x + y)))
    assertThat(bquote(.(z)*3), identicalTo(quote(43 * 3)))

}


test.function <- function() {

    f <- eval(bquote(function(..., path = ".") { list(...) }))

    assertThat(f(1,2,3), identicalTo(list(1, 2, 3)))
}

test.missing.arg <- function() {
    ff <- quote(function(..., path = ".") { list(...) })
    fff <- ff[[2]]
    assertThat(as.character(fff[[1]]), identicalTo(""))
    assertFalse(fff[[1]] == as.name("."))
}