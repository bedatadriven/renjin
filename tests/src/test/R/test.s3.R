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


test.new.first.argument <- function() {

    f.baz <- function(x, y) c(x, y)
    f <- function(x, y) {  UseMethod("f", structure(99, class='baz')) }

    assertThat(f(41,42), identicalTo(c(41,42)))

}

test.ops.method.value <- function() {

    Ops.foo <- function(e1,e2) .Method
    foo <- structure(1, class="foo")

    assertThat(foo == foo, identicalTo(c("Ops.foo", "Ops.foo")))
    assertThat(foo == 1, identicalTo(c("Ops.foo", "")))
    assertThat(1 == foo, identicalTo(c("", "Ops.foo")))
}

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

test.usemethod.uses.first.matched.arg <- function() {

    f.default <- function(x, y) 42
    f.foo <- function(x, y) 44

    foo <- structure(33, class='foo')
    f <- function(x, y) {  UseMethod("f") }

    assertThat(f(2,foo), identicalTo(42))
    assertThat(f(y=2,x=foo), identicalTo(44))
}

test.object.is.evaluated.once <- function() {

    f.default <- function(x) x+1
    f <- function(x) {  UseMethod("f") }

    count <- 0
    g <- function() {
        cat("G!!!\n")
        count <<- count + 1
        count
    }

    assertThat(f(g()), identicalTo(2))
    assertThat(count, identicalTo(1))
}

test.class.value <- function() {


    f.default <- function(x, y) as.list(environment(), all.names=T)
    f.foo <- function(x, y) as.list(environment(), all.names=T)
    f <- function(x, y) {  UseMethod("f") }

    foo <- structure(33, class=c('foo', 'bar', 'baz'))

    e1 <- f(foo)
    assertThat(e1$.Generic, identicalTo("f"))
    assertThat(e1$.Class, identicalTo(c("foo", "bar", "baz")))
    assertThat(e1$.Method, identicalTo("f.foo"))
    assertThat(e1$.Group, identicalTo(""))

    e2 <- f(41L)
    assertThat(e2$.Generic, identicalTo("f"))
    assertThat(e2$.Class, identicalTo(NULL))
    assertThat(e2$.Method, identicalTo("f.default"))
    assertThat(e2$.Group, identicalTo(""))
}

test.method.value <- function() {
    `<.foo` <- function(x, y) as.list(environment(), all.names=T)
    `<.bar` <- function(x, y) as.list(environment(), all.names=T)

    foo <- structure(33, class=c('foo', 'bar', 'baz'))
    bar <- structure(99, class='bar')
    baz <- structure(34, class=c('baz', 'foo'))

    e1 <- (foo < baz)
    assertThat(e1$.Generic, identicalTo("<"))
    assertThat(e1$.Group, identicalTo(""))
    assertThat(e1$.Class, identicalTo(c('foo', 'bar', 'baz')))
    assertThat(e1$.Method, identicalTo(c('<.foo', '<.foo')))

    e2 <- (foo < 3)
    assertThat(e2$.Generic, identicalTo("<"))
    assertThat(e2$.Group, identicalTo(""))
    assertThat(e2$.Class, identicalTo(c('foo', 'bar', 'baz')))
    assertThat(e2$.Method, identicalTo(c('<.foo', '')))


    # Incompatible methods
    e3 <- (foo < bar)

    assertThat(e3, identicalTo(TRUE))
}

test.s3.reorder.args <- function() {

     f.default <- function(y, x) c(x, y)
     f <- function(x, y) UseMethod("f")

     assertThat(f(1,2), identicalTo(c(2,1)))
     assertThat(f(x=1,y=2), identicalTo(c(1,2)))
     assertThat(f(y=91,92), identicalTo(c(92,91)))
     assertThat(f(91,x=92), identicalTo(c(92,91)))
}

test.s3.updated.arguments.have.no.effect <- function() {

     f.default <- function(x, y) c(x, y)
     f <- function(x, y) {
        y <- 92
        UseMethod("f")
     }

     assertThat(f(41, 42), identicalTo(c(41,42)))
}

test.s3.ellipses.preserved.in.call <- function() {
     f.default <- function(x, y) sys.call()
     f <- function(...) UseMethod("f")
     g <- function(...) f(...)

     assertThat(f(1,2), identicalTo(quote(f.default(1,2))))
     assertThat(g(1,2), identicalTo(quote(f.default(...))))
}
