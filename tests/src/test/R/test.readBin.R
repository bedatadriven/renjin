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

setwd("/home/alex/dev/renjin/tests/src/test/R")

test.read <- function() {
    zz <- file("test.readBin.data", "rb")
    on.exit(close(zz))

    assertThat(readBin(zz, integer(), 4), equalTo(1:4))
    assertThat(readBin(zz, integer(), 6), equalTo(5:10))
    assertThat(readBin(zz, numeric(), 1, endian = "swap"), closeTo(3.141593, 0.0001))
    assertThat(readBin(zz, numeric(), size = 4), closeTo(3.141593, 0.0001))
    assertThat(readBin(zz, numeric(), size = 4, endian = "swap"), identicalTo( 9.869604, tol = 0.0001))
    assertThat(readBin(zz, complex(), 1), identicalTo(3.141593+3i, tol = 0.0001))
    assertThat(readBin(zz, character(), 1), identicalTo("A test of a connection"))
}

test.read.raw <- function() {

    ints <- as.raw(c(0x01, 0x00, 0x00, 0x00,  # integer vector
                     0x02, 0x00, 0x00, 0x00,
                     0x03, 0x00, 0x00, 0x00,
                     0x04, 0x00, 0x00, 0x00,
                     0x05, 0x00, 0x00, 0x00,
                     0x06, 0x00, 0x00, 0x00,
                     0x07, 0x00, 0x00, 0x00,
                     0x08, 0x00, 0x00, 0x00,
                     0x09, 0x00, 0x00, 0x00,
                     0x0a, 0x00, 0x00, 0x00))

    assertThat(readBin(ints, integer(), 10), equalTo(1:10))


    d <- as.raw(c(0x40, 0x09,  0x21, 0xfb, 0x54, 0x44, 0x2d, 0x18))

    assertThat(readBin(d, numeric(), 1, endian = "swap"), closeTo(3.141593, 0.0001))
}

test.unterminated.string <- function() {

    bytes <- as.raw(c(65, 66, 67))
    assertThat(readBin(bytes, character(0)), equalTo("ABC"))
    assertThat(readBin(bytes, character(0), n = 2), equalTo(c("ABC", "")))


    bytes <- as.raw(c(65, 66, 67, 0, 68, 69, 70))
    assertThat(readBin(bytes, character(0), n = 2), equalTo(c("ABC", "DEF")))

}