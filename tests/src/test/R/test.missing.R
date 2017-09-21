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


library(hamcrest)


test.ellipses <- function() {

    f <- function(...) missing(...)

    assertThat(f(1), identicalTo(FALSE))
    assertThat(f(),  identicalTo(TRUE))

}

test.missing.arg.to.builtin <- function() {

    f <- function(x) length(x)

    assertThat(f(1:10), identicalTo(10L))
    assertThat(f(), throwsError())
}

test.missing.valid.argument <- function() {

    # The missing argument is a symbol and can
    # itself be manipulated in R code as any
    # other quoted symbol

    g <- function(a) a
    m <- formals(g)[[1]]  # = missing argument

    f <- function(x) typeof(x)

    assertThat(f(m), identicalTo("symbol"))
}


test.missing.with.default.2 <- function() {

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

test.ommited.arg <- function() {

    f <- function(x, y) missing(x)
    g <- function(x = 99, y = 33) x

    assertThat(f( , 3), identicalTo(TRUE))
    assertThat(g( , 4), identicalTo(99))
}

