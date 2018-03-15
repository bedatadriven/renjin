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

test.capture <- function() {
    url <- "http://www.google.com"
    m <- regexpr("^([[:alpha:]+.-]+):", url, perl = TRUE)

    assertThat(attr(m, 'match.length'), identicalTo(5L))
    assertThat(attr(m, 'capture.start'), identicalTo(structure(1L, .Dim = c(1L, 1L), .Dimnames = list(NULL, ""))))
    assertThat(attr(m, 'capture.length'), identicalTo(structure(4L, .Dim = c(1L, 1L), .Dimnames = list(NULL, ""))))

}


test.capture.vectors <- function() {
    url <- c(a = "http://www.google.com", b = "bing.com")
    m <- regexpr("^([[:alpha:]+.-]+):", url, perl = TRUE)

    assertThat(attr(m, 'match.length'), identicalTo(c(5L, -1L)))
    assertThat(attr(m, 'capture.start'), identicalTo(structure(c(1L, -1L), .Dim = c(2L, 1L), .Dimnames = list(NULL, ""))))
    assertThat(attr(m, 'capture.length'), identicalTo(structure(c(4L, -1L), .Dim = c(2L, 1L), .Dimnames = list(NULL, ""))))
    assertThat(attr(m, 'capture.names'), identicalTo(""))
}

test.capture.matrix <- function() {

    strings <- c("a-Z-9", "a-q-14", "b-A-", "foo")
    m <- regexpr("([a-z]+)-([A-Z]+)-([0-9]+)?", strings, perl = TRUE)

    assertThat(m, equalTo(c(1L, -1L, 1L, -1L)))
    assertThat(attr(m, 'match.length'), identicalTo(c(5L, -1L,  4L, -1L)))
    assertThat(attr(m, 'capture.start'), identicalTo(
        structure(c(1L, -1L, 1L, -1L,
                    3L, -1L, 3L, -1L,
                    5L, -1L, 0L,  -1L), .Dim = c(4L, 3L),
                                        .Dimnames = list(NULL, c("", "", "")))))
    assertThat(attr(m, 'capture.length'), identicalTo(
        structure(c(1L, -1L, 1L, -1L,
                    1L, -1L, 1L, -1L,
                    1L, -1L, 0L, -1L),  .Dim = c(4L, 3L),
                                        .Dimnames = list(NULL, c("", "", "")))))
    assertThat(attr(m, 'capture.names'), identicalTo(c("", "", "")))
}