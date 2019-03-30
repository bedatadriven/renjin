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

test.primitive.to.s3.not.evaled <- function() {
    `[.foo` <- function(x, i, j, drop = FALSE) { 142 }
    g <- function(x,i,j) x[i,j]

    x <- structure(99, class = 'foo')

    # Despite the fact that we are calling a primitive,
    # the arguments, besides the first one, are lazily dispatched.
    assertThat(g(x, i = stop("i"), j = stop("j")), identicalTo(142))
}


test.primitive.to.s3.first.promised <- function() {
    `[.foo` <- function(x, i, j, drop = FALSE) substitute(x)
    g <- function(x,i,j) x[i,j]

    x <- structure(99, class = 'foo')

    # Despite the fact that we are calling a primitive,
    # the arguments, besides the first one, are lazily dispatched.
    assertThat(g(x), identicalTo(quote(x)))
}


test.usemethod.then.match.call <- function() {

    foo.default <- function(object) {
        call <- match.call()
        call$object$b <- 92
        call$object
    }
    foo <- function(object) UseMethod("foo")

    object <- foo(list(a=1,b=2))

    assertThat(object$b, equalTo(92))
}


test.usemethod.then.sys.call.dollar <- function() {

    foo.default <- function(object) {
        call <- sys.call()
        call$object$b <- 92
        call$object
    }
    foo <- function(object) UseMethod("foo")

    object <- foo(object=list(a=1,b=2))

    assertThat(object$b, equalTo(92))

}


test.usemethod.then.match.call <- function() {

    foo.default <- function(object) {
        call <- match.call()
        call$object$b <- 92
        call$object
    }
    foo <- function(object) UseMethod("foo")

    object <- foo(list(a=1,b=2))

    assertThat(object$b, equalTo(92))
}


test.usemethod.then.sys.call.dollar <- function() {

    foo.default <- function(object) {
        call <- sys.call()
        call$object$b <- 92
        call$object
    }
    foo <- function(object) UseMethod("foo")

    object <- foo(object=list(a=1,b=2))

    assertThat(object$b, equalTo(92))

}

test.usemethod.uses.first.arg <- function() {

    f.default <- function(x, y) 42
    f.foo <- function(x, y) 44

    foo <- structure(33, class='foo')
    f <- function(x, y) {  UseMethod("f") }

    assertThat(f(2,foo), identicalTo(42))
    assertThat(f(y=2,x=foo), identicalTo(44))
}