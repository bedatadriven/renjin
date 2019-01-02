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

test.match.call <- function() {

    f <- function(a, zz) { match.call() }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}

test.match.call.nested <- function() {
    g <- function(a) a
    f <- function(a, zz) { g(match.call()) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}


test.match.call.lazy <- function() {
    g <- function(a) a
    f <- function(a, zz, yy = match.call()) { g(yy) }
    x <- f(1, 2)
    assertThat(x, identicalTo(quote(f(a = 1, zz = 2))))
}

test.match.extra <- function() {

    f <- function(x, ...) match.call(expand.dots = FALSE)$...

    extra <- f(1, a=3+4, b=z)

    assertThat(extra$a, identicalTo(quote(3 + 4)))
    assertThat(extra$b, identicalTo(quote(z)))
}

test.match.extra.then.dollar <- function() {

    f <- function(x, ...) { pl <- match.call(expand.dots = FALSE)$...; pl$b }

    b <- f(1, a=3+4, b=z)

    assertThat(b, identicalTo(quote(z)))
}

test.replace.call.by.name <- function() {

    call <- quote(f(a = 1, b = 2))

    call[["b"]] <- 92

    assertThat(call, identicalTo(quote(f(a=1,b=92))))
}

test.replace.call.by.index <- function() {

    call <- quote(f(a = 1, b = 2))

    call[[2]] <- 94
    call[3] <- 42

    assertThat(attributes(call), identicalTo(NULL))
    assertThat(call, identicalTo(quote(f(a=94,b=42))))
}