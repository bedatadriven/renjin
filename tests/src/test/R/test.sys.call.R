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


## Functions used in test cases

foo.default <- function(n) {
    sexp <- sys.call(n)
    deparse(sexp)
}

foo <- function(n) {
    UseMethod("foo")
}

g <- function() {
    foo.default(2)
}



## Test cases

test.simple <- function() {
    call <- foo.default(0)
    assertThat(call, identicalTo("foo.default(0)"))
}

test.simple1 <- function() {
    call <- foo.default(-1)
    assertThat(call, identicalTo("test.simple1()"))
}

test.s3 <- function() {
    call <- foo(0)
    assertThat(call, identicalTo("foo.default(0)"))
}

test.s3.parent <- function() {
    call <- foo(-1L)
    assertThat(call, identicalTo("foo(-1L)"))
}

