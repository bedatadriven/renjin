#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

library(methods)
library(hamcrest)


test.ellipses <- function() {

    f <- function(...) missing(...)

    assertThat(f(1), identicalTo(FALSE))
    assertThat(f(),  identicalTo(TRUE))

}

test.missing.vararg1 <- function() {

    f <- function(i, j) missing(i)
    g <- function(...) f(..1, ..2)

    assertThat(g(), identicalTo(TRUE))
}


test.missing.arg.to.builtin <- function() {

    f <- function(x) length(x)

    assertThat(f(1:10), identicalTo(10L))
    assertThat(f(), throwsError())
}

test.missing.as.value <- function() {

    # The missing argument is a symbol and can
    # itself be manipulated in R code as any
    # other quoted symbol

    # Function to generate the missing value symbol
    m <- function() formals(function(x) x)[[1]]

    assertThat(typeof(m()), identicalTo("symbol"))
    assertThat(missing(m()), throwsError())

    symbols <- c(m(), m(), quote(x))

    assertThat(length(symbols), identicalTo(3L))
    assertThat(deparse(symbols), identicalTo("list(, , x)"))
}


test.missing.as.value.assigned.to.symbol <- function() {

    # while the missing argument symbol *can* be manipulated
    # symbolically, once you assign it to a symbol, things get
    # weirder.

    # TESTED with GNU R 3.4.0
    #             GNU R 3.0.0


    g <- function(a) a
    m <- formals(g)[[1]]  # = missing argument

    f <- function(x) missing(x)
    h <- function(x) typeof(x)

    assertThat(f(m), identicalTo(TRUE))
    assertThat(h(m), throwsError())
    assertThat(m, throwsError())
    assertThat(typeof(m), throwsError())
    assertThat(missing(m), identicalTo(TRUE))
}

test.missing.invalid.symbol <- function() {

    assertThat(missing(symbol.does.not.exist), throwsError())

}

test.missing.with.default.1 <- function() {

   f <- function(x = 2) missing(x)

   assertThat(f(), identicalTo(TRUE))
   assertThat(f(1), identicalTo(FALSE))
   assertThat(f(2), identicalTo(FALSE))
}

test.missing.with.default.2 <- function() {

   f <- function(x) missing(x)
   g <- function(y = 42) f(y)

   assertThat(f(), identicalTo(TRUE))
   assertThat(g(), identicalTo(FALSE))
}

test.missing.with.default.3 <- function() {

   f <- function(x = 42) x
   g <- function(y) f(y)

   assertThat(f(), identicalTo(42))
   assertThat(g(), throwsError())
}

test.missing.with.reassigned <- function() {

   f <- function(x) {
        x <- 42
        missing(x)
   }
   assertThat(f(), identicalTo(FALSE))
   assertThat(f(1), identicalTo(FALSE))
}

test.missing.with.cycles <- function() {
    
    f <- function(x = y, y = x) c(missing(x), missing(y))

    assertThat(f(),      identicalTo(c(TRUE, TRUE)))
    assertThat(f(x = 1), identicalTo(c(FALSE, TRUE)))
    assertThat(f(y = 1), identicalTo(c(TRUE,  FALSE)))
    assertThat(f(3, 4),  identicalTo(c(FALSE, FALSE)))

}


test.subset.s3.missing <- function() {


   `[.foo` <- function(x, i, j, drop = FALSE) { 142 }

   x <- structure(99, class = 'foo')
   g <- function(x,i,j) x[i,j]

   assertThat(g(x), identicalTo(142))
   assertThat(g(x,,), identicalTo(142))

}

test.subset.s3.missing.args <- function() {

    `[.bar` <- function(x, i, j, drop = TRUE) c(missing(i), missing(j), missing(drop))
    x <- 1:5
    class(x) <- 'bar'

    assertThat(x[1,2], identicalTo(c(FALSE, FALSE, TRUE)))
    assertThat(x[,2], identicalTo(c(TRUE, FALSE, TRUE)))
}

test.subset.missing.builtin <- function() {

    x <- matrix(1:12, nrow=3)
    f <- function(x, i, j) x[i,j]
    assertThat(f(x, 1), identicalTo(c(1L, 4L, 7L, 10L)))
}

test.subset.missing.builtin.2 <- function() {

    x <- matrix(1:12, nrow=3)
    f <- function(x, i, j) x[i,j]
    g <- function(x, i, j) f(x, i, j)
    assertThat(g(x, 1), identicalTo(c(1L, 4L, 7L, 10L)))
}

test.subset.missing.builtin.recursive <- function() {
    x <- matrix(1:12, nrow=3)
    f <- function(x, i = j, j = i) x[i,j]
    g <- function(x, i, j) f(x, i, j)
    assertThat(length(g(x)), identicalTo(12L))
}

test.subset.missing.with.default <- function() {

    x <- matrix(1:12, nrow=3)
    f <- function(i=3, j=3) x[i, j]

    assertThat(f(), identicalTo(9L))
}


test.subset.missing.with.varargs <- function() {

    x <- matrix(1:12, nrow=3)
    f <- function(x, ...) x[..1, ..2]

    assertThat(f(x), identicalTo(x))
    assertThat(f(x, 1, 2), identicalTo(4L))
}


test.subset.missing.with.varargs.2 <- function() {

    f <- function(x, i, j) missing(i)
    g <- function(x, ...) f(x, ..1, ..2)
    h <- function(x, ...) g(x, ...)
    r <- function(...) h(..1, ..2, ...3)
    s <- function(x, i, j) h(x, i, j)

    x <- 1L
    assertThat(h(x, , 3), identicalTo(TRUE))
    assertThat(r(), identicalTo(TRUE))
    assertThat(s(x), identicalTo(TRUE))
    assertThat(s(x, 1, 1), identicalTo(FALSE))
    assertThat(s(x, , 1), identicalTo(TRUE))
}


test.combine.missing <- function() {

    assertThat(c(1,,3), throwsError())
}

test.ommited.arg <- function() {

    f <- function(x, y) missing(x)
    g <- function(x = 99, y = 33) x

    assertThat(f( , 3), identicalTo(TRUE))
    assertThat(g( , 4), identicalTo(99))
}

test.S4.default.afterEllipses = function() {

    setClass("A", representation(a = "numeric")); a = new("A", a = 0)
    setClass("C", contains = "A"); c = new("C", a = 3)
    setMethod("[", signature(x = "A", i = "ANY", j = "ANY", drop = "ANY"), function(x, i, j, ..., drop = TRUE) length(drop))

    assertThat(c[ , drop = 1:5] , identicalTo( 5L ))
    assertThat(a[ , drop = 1:5] , identicalTo( 5L ))
    assertThat(c[ , ] , identicalTo( 1L ))
    assertThat(a[ , ] , identicalTo( 1L ))
}

test.S4.default.beforeEllipses = function() {

    setClass("B", representation(b = "numeric")); b = new("B", b = 0)
    setMethod("[", signature(x = "B", i = "ANY", j = "ANY", drop = "ANY"), function(x, i, j = 1:5, ..., drop) length(j))

    assertThat(b[ , 1:3 ] , identicalTo( 3L ) )
#    This behavior is not replicated in Renjin, and 'j' is treated similar to 'drop' and returns 5 instead of error!
#    assertThat(b[ , ] , throwsError() )
}
