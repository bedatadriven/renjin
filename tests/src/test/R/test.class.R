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

test.implicit.classes <- function() {
    assertThat(class(42),                   identicalTo("numeric"))
    assertThat(class(42L),                  identicalTo("integer"))
    assertThat(class('foo'),                identicalTo('character'))
    assertThat(class(quote(x)),             identicalTo('name'))
    assertThat(class(.GlobalEnv),           identicalTo('environment'))
    assertThat(class(quote(1+1)),           identicalTo('call'))
    assertThat(class(NULL),                 identicalTo("NULL"))
    assertThat(class(matrix(1:12, nrow=3)), identicalTo("matrix"))
    assertThat(class(array(1:12)),          identicalTo("array"))
    assertThat(class(pairlist(a=1,b=2)),    identicalTo("pairlist"))
}

test.coerce.strings <- function() {

    y <- 43L
    class(y) <- 3

    assertThat(class(y), identicalTo("3"))

}

test.set.implicit <- function() {

    x <- 42
    class(x) <- "numeric"

    assertThat(class(x), identicalTo("numeric"))
    assertThat(length(attributes(x)), identicalTo(0L))
}


test.set.implicit.multiple <- function() {

    x <- 42
    class(x) <- c("numeric", "foo")

    assertThat(class(x), identicalTo(c("numeric", "foo")))
    assertThat(attr(x, 'class'), identicalTo(c("numeric", "foo")))
}


test.set.implicit.env <- function() {

    x <- .GlobalEnv
    class(x) <- "environment"

    assertThat(class(x), identicalTo("environment"))
    assertThat(length(attributes(x)), identicalTo(0L))

}

test.set.implicit.via.attr <- function() {

    x <- 42
    attr(x, 'class') <- "numeric"

    assertThat(class(x), identicalTo("numeric"))
    assertThat(attributes(x)$class, identicalTo("numeric"))
}

test.set.matrix <- function() {

    x <- matrix(1:12, nrow=3)
    y <- array(1:12)
    z <- 99

    class(x) <- 'matrix'
    assertThat(class(x), identicalTo('matrix'))
    assertThat(attr(x, 'class'), identicalTo(NULL))

    assertThat( { class(y) <- 'matrix' }, throwsError())
    assertThat( { class(z) <- 'matrix' }, throwsError())
}

test.set.array <- function() {

    x <- matrix(1:12, nrow=3)
    y <- array(1:12)
    z <- 99

    class(x) <- 'array'
    assertThat(class(x), identicalTo('matrix'))
    assertThat(attr(x, 'class'), identicalTo(NULL))

    class(y) <- 'array'
    assertThat(class(y), identicalTo('array'))
    assertThat(attr(y, 'class'), identicalTo(NULL))

    assertThat( { class(z) <- 'array' }, throwsError())
}

test.set.matrix.multiple <- function() {

    x <- 42L
    class(x) <- c("matrix", "foo")

    assertThat(class(x), identicalTo(c("matrix", "foo")))

    class(x) <- c("matrix", "matrix")
    assertThat(class(x), identicalTo(c("matrix", "matrix")))

    attr(x, 'class') <- 'matrix'
    assertThat(attr(x, 'class'), identicalTo('matrix'))

}

test.set.NA <- function() {

    x <- 42L
    class(x) <- NA

    assertThat(class(x), identicalTo(NA_character_))
}

test.set.class.with.attributes <- function() {

    x <- 42L
    y <- 'foo'
    class(y) <- 'bar'
    attr(y, 'rando') <- 99

    class(x) <- y

    assertThat(x, identicalTo(structure(42L, class = structure("foo", class = "bar", rando = 99))))
}


test.set.numeric.class.with.attributes <- function() {

    x <- 42L
    y <- 33
    class(y) <- 'bar'
    attr(y, 'rando') <- 99

    class(x) <- y

    assertThat(x, identicalTo(structure(42L, class = structure("33", class = "bar", rando = 99))))

}

test.set.class.null <- function() {

    x <- structure(1L, class='foo')

    assertThat(class(x), identicalTo('foo'))

    class(x) <- NULL

    assertThat(class(x), identicalTo('integer'))

}

test.set.empty.character <- function() {

    x <- structure(1L, class='foo')

    assertThat(class(x), identicalTo('foo'))

    class(x) <- character(0)

    assertThat(class(x), identicalTo('integer'))
}