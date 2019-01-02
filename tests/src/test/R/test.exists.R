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

test.mode <- function() {

    f <- function() 42
    i <- 99L
    d <- 3.145
    b <- TRUE
    chr <- "Hello world"
    raw <- raw(length = 10)
    cpx <- 1+2i
    n <- as.name("x")
    cl <- quote({ launchMissiles(); drinkTea(); })
    p <- parse(text = "1; 2")

    assertTrue(exists("f", mode = "function"))
    assertTrue(exists("i", mode = "integer"))
    assertTrue(exists("b", mode = "logical"))
    assertTrue(exists("d", mode = "double"))
    assertFalse(exists("d", mode = "single"))
    assertTrue(exists("chr", mode = "character"))
    assertTrue(exists("cpx", mode = "complex"))

    assertTrue(exists("n", mode = "name"))
    assertTrue(exists("n", mode = "symbol"))

    assertTrue(exists("raw", mode = "raw"))

    assertTrue(exists("cl", mode = "language"))
    assertFalse(exists("cl", mode = "call"))

    assertTrue(exists("p", mode = "expression"))

    assertTrue(exists("d", mode = "numeric"))
    assertTrue(exists("i", mode = "numeric"))
    assertFalse(exists("b", mode = "numeric"))
    assertFalse(exists("cpx", mode = "numeric"))

    assertTrue(exists("c", mode = "builtin", inherits=TRUE))

    assertFalse(exists("cpx", mode = "moonbeam"))

    assertFalse(exists("i", mode = "NA"))

}

test.exist.promise <- function() {

    f <- function(x) {
        assertTrue(exists("x", mode = "numeric"))
    }

    f(1+1)
}

