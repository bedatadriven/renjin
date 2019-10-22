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
library(stats)

Ops.bar <- function(e1, e2) {
    .Method
}

Ops.foo <- function(e1, e2) {
    NextMethod(.Generic)
}

Ops.baz <- function(e1, e2) {
    .Method
}



test.ordered.factor <- function() {
    run <- structure(c(4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 4L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L, 10L,
                    10L, 10L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 11L, 5L, 5L, 5L, 5L, 5L, 5L, 5L, 5L, 5L, 5L, 5L,
                    5L, 5L, 5L, 5L, 5L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 7L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L, 9L,
                    9L, 9L, 9L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 8L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L, 6L,
                    6L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 3L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 2L,
                    2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L, 2L),
                    class = c("ordered", "factor"),
                    .Label = c("10", "11", "9", "1", "4", "8", "5", "7", "6", "2", "3"))



    assertThat(which(run == 1), identicalTo(1:16))


}



test.ops.method1 <- function() {

    # Single level of dispatch

    x <- 1
    class(x) <- c("baz")

    assertThat( x == 1, identicalTo( c("Ops.baz", "") ))
    assertThat( 1 == x, identicalTo( c("", "Ops.baz") ))
    assertThat( x == x, identicalTo( c("Ops.baz", "Ops.baz") ))
}

test.ops.method2 <- function() {

    # Double dispatch, first to Ops.foo, then via NextMethod() to Ops.bar

    x <- 1
    class(x) <- c("foo", "bar")

    y <- 2
    class(y) <- "foo"

    assertThat( x == 1, identicalTo( c("Ops.bar", "") ))
    assertThat( y == 1, identicalTo( FALSE ) )
    assertThat( x == y, identicalTo( c("Ops.bar", "Ops.bar") ))
    assertThat( 1 == x, identicalTo( c("", "Ops.bar") ))

}



test.many <- function() {

    x <-  ts(1:10, frequency = 4, start = c(1959, 2))
    y <- x - x

    assertThat(y, identicalTo(structure(c(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
                    .Tsp = c(1959.25, 1961.5, 4), class = 'ts')))

}


test.barbaz <- function() {

    bar.called <- FALSE
    baz.called <- FALSE

    foo     <- function(x) { cat("foo\n"); UseMethod('foo') }
    foo.bar <- function(x) { cat("foo.bar\n"); bar.called <<- TRUE; NextMethod(.Generic) }
    foo.baz <- function(y) { cat("foo.baz\n"); baz.called <<- TRUE; NextMethod(.Generic) }
    foo.default <- function(z) { cat("foo.default\n"); 42 }

    barbaz <- structure(1, class=c("bar", "baz"))

    assertThat(foo(barbaz), identicalTo(42))
    assertTrue(bar.called)
    assertTrue(baz.called)
}

test.objectarg.has.no.effect <- function() {

    foo     <- function(x) { cat("foo\n"); UseMethod('foo') }
    foo.bar <- function(x) { cat("foo.bar\n"); NextMethod(.Generic, object = structure(44, class="baz")) }
    foo.baz <- function(y) { cat("foo.baz\n"); y }
    foo.default <- function(x) { cat("foo.default\n"); x }

    bar <- structure(49, class=c("bar"))

    assertThat(foo(bar), identicalTo(bar))
}

test.objectarg.is.not.evaled <- function() {

    foo     <- function(x) { cat("foo\n"); UseMethod('foo') }
    foo.bar <- function(x) { cat("foo.bar\n"); NextMethod(.Generic, object = stop("FOO!!!!!")) }
    foo.baz <- function(y) { cat("foo.baz\n"); y }
    foo.default <- function(x) { cat("foo.default\n"); x }

    bar <- structure(49, class=c("bar"))

    assertThat(foo(bar), identicalTo(bar))
}


test.extra.args <- function() {

    foo     <- function(x) { cat("foo\n"); UseMethod('foo') }
    foo.bar <- function(x) { cat("foo.bar\n"); NextMethod(.Generic, x, zz = 92) }
    foo.default <- function(x, zz = 91) { cat("foo.default\n"); zz }

    bar <- structure(49, class=c("bar"))

    assertThat(foo(bar), identicalTo(92))
}