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

test.paste0 <- function() {
    s1 <- paste0("foo", "bar")
    s2 <- paste0(c("A", "B"), collapse = ", ")

    assertThat(s1, equalTo("foobar"))
    assertThat(s2, equalTo("A, B"))
}

test.paste0.sep <- function() {
    assertThat(paste0("a", "b", sep=""), identicalTo("ab"))
}

test.rep_len <- function() {
    assertThat(rep_len("a", 2), identicalTo(c("a", "a")))
}

test.anyNA <- function() {
    assertTrue(anyNA(c(1, NA, 3)))
    assertTrue(anyNA(c(1, NA, 3), recursive = TRUE))
    assertFalse(anyNA(list(a = c(1, NA, 3), b = "a")))
    assertTrue(anyNA(list(a = c(1, NA, 3), b = "a"), recursive = TRUE))
    assertFalse(anyNA(as.POSIXlt(Sys.time())))
}

test.lengths <- function() {
    assertThat(lengths(seq(10)), identicalTo(rep(1L, times = 10)))
    x <- list(a = c(1, 2), b = "foobar")
    assertThat(names(lengths(x)), identicalTo(c("a", "b")))
    assertThat(lengths(x), equalTo(c(2,1)))
    assertTrue(is.null(names(lengths(x, use.names = FALSE))))
}

