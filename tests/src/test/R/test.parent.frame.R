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


## Functions used in test cases

foo.default <- function(n) {
    parent.frame(n)
}

foo <- function(n) {
    UseMethod("foo")
}

g <- function() {
    foo.default(2)
}

as.character.baz <- function(x) {
    parent.frame()
}

## Test cases

test.simple <- function() {
    zz <- 99
    pf <- foo.default(1)
    
    assertThat(pf$zz, identicalTo(99))
}

test.s3 <- function() {
    qq <- 42
    pf <- foo(1)
    
    assertThat(pf$qq, identicalTo(42))
}

test.s3.from.eval <- function() {
    qz <- 93
    pf <- eval(quote(foo(1)))

    assertThat(pf$qz, identicalTo(93))
}


test.three <- function() {
    qz <- 43
    pf <- g()
    assertThat(pf$qz, identicalTo(43))
}


# Test call from top level
pf <- as.character(structure(1, class = "baz"))
assertThat(pf, identicalTo(.GlobalEnv))