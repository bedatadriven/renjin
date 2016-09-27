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

test.match.call <- function() {

    f <- function(a, zz) { match.call() }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}

test.match.call.nested <- function() {
    g <- function(a) a
    f <- function(a, zz) { g(match.call()) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}


test.match.call.lazy <- function() {
    g <- function(a) a
    f <- function(a, zz, yy = match.call()) { g(yy) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}
