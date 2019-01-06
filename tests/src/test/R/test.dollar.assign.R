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

test.assign.env <- function() {
    e <- new.env()
    e$a <- 41
    e$"b" <- 42
    e$"==" <- 43

    assertThat(e[["a"]], identicalTo(41))
    assertThat(e[["b"]], identicalTo(42))
    assertThat(e[["=="]], identicalTo(43))
}

test.s3 <- function() {

    `$<-.foo` <- function(x, name, value) {
        stopifnot(is.character(name))
        x[name] <- 44
        x
    }

    x <- structure(3L, class="foo")
    x$a <- 45
    assertThat(x, identicalTo(structure(c(3, 44), class = "foo", .Names = c("", "a"))))
}