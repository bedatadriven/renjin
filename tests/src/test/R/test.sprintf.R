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

test.format <- function() {
    assertThat(sprintf("%i", 1L), identicalTo("1"))
    assertThat(sprintf("%f", 1L), identicalTo("1.000000"))
    assertThat(sprintf("%E", 1L), identicalTo("1.000000E+00"))
    assertThat(sprintf("%e", 1L), identicalTo("1.000000e+00"))
    assertThat(sprintf("%g", 1L), identicalTo("1"))
    assertThat(sprintf("%G", 1L), identicalTo("1"))

    assertThat(sprintf("%g", 102400000L), identicalTo("1.024e+08"))
    assertThat(sprintf("%G", 102400000L), identicalTo("1.024E+08"))


    assertThat(sprintf('%.f', 3 ), identicalTo("3"))
    assertThat(sprintf('%0.f', 3 ), identicalTo("3"))
    assertThat(sprintf('%0.f', 3.1 ), identicalTo("3"))
    assertThat(sprintf('%0.f', 3.5 ), identicalTo("4"))
    assertThat(sprintf('%0.f', 3.75 ), identicalTo("4"))

    assertThat(sprintf("0x%x", 64), identicalTo("0x40"))
}

test.float.conversion <- function() {
    assertThat(sprintf("Sven is %i feet tall", 7.0), identicalTo("Sven is 7 feet tall"));
    assertThat(sprintf("Sven is %i feet tall", 7.5), throwsError());
}

test.na.handling <- function() {
    assertThat(sprintf("%d", NA), equalTo("NA"))
    assertThat(sprintf("%f", NA), equalTo("NA"))
    assertThat(sprintf("%g", NA), equalTo("NA"))
    assertThat(sprintf("%s", NA), equalTo("NA"))
    assertThat(sprintf("%f", NA_character_), equalTo("NA"))
}

test.character.conversion <- function() {
    assertThat(sprintf("Level %s", factor(c("a","a","b"))), identicalTo(c("Level a", "Level a", "Level b")))
}

test.recycling <- function() {
    assertThat(sprintf(c("a%dx%d", "b%dy%d", "c%dz%d"), 1:6, 9:12), identicalTo(
         c("a1x9", "b2y10", "c3z11", "a4x12", "b5y9", "c6z10")))
}

test.extra.escapes <- function() {
    assertThat(sprintf("^z\\d{3}|^%s$", "lb1"), identicalTo("^z\\d{3}|^lb1$"))
}