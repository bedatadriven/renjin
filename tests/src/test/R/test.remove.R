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

test.remove <- function() {
    x <- 1
    y <- 2
    z <- 3
    remove("x")
    remove("doesnotexist")
    assertFalse(exists("x"))
    assertTrue(exists("y"))
    assertTrue(exists("z"))
}

test.remove.multiple <- function() {
    x <- 1
    y <- 2
    z <- 3
    remove("x", "y")
    assertFalse(exists("x"))
    assertFalse(exists("y"))
    assertTrue(exists("z"))
}

test.remove.no.inherits <- function() {

    xx <- 42

    f <- function() {
        remove("xx")
    }
    f()

    assertTrue(exists("xx"))

}


test.remove.inherits <- function() {

    xx <- 42

    f <- function() {
        remove("xx", inherits = TRUE)
    }
    f()

    assertFalse(exists("xx"))
}