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

test.perl <- function() {
    url <- "http://www.google.com"
    m <- regexec("^([[:alpha:]+.-]+):", url, perl = TRUE)

    assertThat(m, identicalTo(
        list(
            structure(c(1L, 1L),
                match.length = c(5L, 4L)))))

}

test.not.perl <- function() {
    url <- "http://www.google.com"
    m <- regexec("^([[:alpha:]+.-]+):", url, perl = FALSE)

    assertThat(m, identicalTo(
        list(
            structure(c(1L, 1Liconvlist),
                match.length = c(5L, 4L)))))

}

test.capture.vectors <- function() {
    url <- c(a = "http://www.google.com", b = "bing.com")
    m <- regexec("^([[:alpha:]+.-]+):", url)

    assertThat(m, identicalTo(
        list(
            structure(c(1L, 1L), match.length = c(5L, 4L)),
            structure(-1L, match.length = -1L))
        ))
}

test.capture.matrix <- function() {

    strings <- c("a-Z-9", "a-q-14", "b-A-", "foo")
    m <- regexec("([a-z]+)-([A-Z]+)-([0-9]+)?", strings)

    assertThat(m, identicalTo(
        list(
            structure(c(1L, 1L, 3L, 5L), match.length = c(5L, 1L, 1L, 1L)),
            structure(-1L, match.length = -1L),
            structure(c(1L, 1L, 3L, 0L), match.length = c(4L, 1L, 1L, 0L)),
            structure(-1L, match.length = -1L))
        ))

}