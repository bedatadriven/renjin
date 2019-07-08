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

test.rep.no.args <- function() {
    assertThat(rep(c("x", "y")), identicalTo(c("x", "y")))
    assertThat(rep(c(a=1, b=2)), identicalTo(c(a=1, b=2)))
    assertThat(rep(structure(1, foo="bar")), identicalTo(1))
}

test.rep.times <- function() {
    assertThat(rep(c("x", "y"), times=2), identicalTo(c("x", "y", "x", "y")))
    assertThat(rep(c(a=1,b=2), times=2), identicalTo(c(a=1,b=2,a=1,b=2)))
}

test.rep.each <- function() {
    assertThat(rep(c("x", "y"), each=2), identicalTo(c("x", "x", "y", "y")))
}

test.rep.with.times.and.each <- function() {
    assertThat(rep("x", times=3, each=1), identicalTo(c("x", "x", "x")))
    assertThat(rep(c("x", "y"), times=3, each=2), identicalTo(c("x", "x", "y", "y", "x", "x", "y", "y", "x", "x", "y", "y")))
}


test.rep.with.extra.args <- function() {
    assertThat(rep("x", 3, foo = 9, BAA = "BAA"), identicalTo(c("x", "x", "x")))
}

test.rep.forwarded.args <- function() {
    f <- function(...) rep(42L, ...)

    assertThat(f(times=3), identicalTo(c(42L, 42L, 42L)))
}

test.rep.missing.arg <- function() {
    f <- function(x, times, each) rep(x, times, each)

    assertThat(f(1L, 3L), identicalTo(c(1L, 1L, 1L)))
}

test.rep.empty.arg <- function() {
    assertThat(rep(c(1,2),,3), identicalTo(c(1,2,1)))
}