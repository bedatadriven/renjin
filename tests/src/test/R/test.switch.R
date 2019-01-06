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


test.arg.name <- function() {

    # Attempting to use argument names to alter argument matching order
    # should produce the error:
    #    supplied argument name 'a' does not match 'EXPR'

    assertThat(switch(a = 1, b = 2, c = 3, EXPR = "a"), throwsError())
}

test.by.name <- function() {
    assertThat(switch(EXPR = "b",  a = 41, b = 42), identicalTo(42))
    assertThat(switch(EXPR = "QQ", a = 41, b = 42), identicalTo(NULL))
    assertThat(switch(EXPR = "a",  a = 41, b = 42), identicalTo(41))
    assertThat(switch(EXPR = "A",  a = 41, b = 42), identicalTo(NULL))
    assertThat(switch(EXPR = "bb", a = 41, b = 42), identicalTo(NULL))

    assertThat(switch(EXPR = "b",  a =, A = 41, b = 42, 43), identicalTo(42))
    assertThat(switch(EXPR = "QQ", a =, A = 41, b = 42, 43), identicalTo(43))
    assertThat(switch(EXPR = "a",  a =, A = 41, b = 42, 43), identicalTo(41))
    assertThat(switch(EXPR = "A",  a =, A = 41, b = 42, 43), identicalTo(41))
    assertThat(switch(EXPR = "bb", a =, A = 41, b = 42, 43), identicalTo(43))
}

test.NA.by.name <- function() {
    assertThat(switch(NA_character_, `NA` = 91, 92), identicalTo(91))
}

test.by.position <- function() {

    assertThat(switch(EXPR = -1, 91, 92, 93, 94), identicalTo(NULL))
    assertThat(switch(EXPR =  0, 91, 92, 93, 94), identicalTo(NULL))
    assertThat(switch(EXPR =  1, 91, 92, 93, 94), identicalTo(91))
    assertThat(switch(EXPR =  2, 91, 92, 93, 94), identicalTo(92))
    assertThat(switch(EXPR =  3, 91, 92, 93, 94), identicalTo(93))
    assertThat(switch(EXPR =  4, 91, 92, 93, 94), identicalTo(94))
    assertThat(switch(EXPR =  5, 91, 92, 93, 94), identicalTo(NULL))
    assertThat(switch(EXPR = NA, 91, 92, 93, 94), identicalTo(NULL))
}

test.by.factor <- function() {
     ff <- gl(3,1, labels=LETTERS[3:1])

     ## so one might expect " is C" here, but switch ignore the
     ## factor labels

     assertThat(switch(ff[1], A = "I am A", B="Bb..", C=" is C"), identicalTo("I am A"))
}

test.forwarded.args <- function() {

    f <- function(e, ...) switch(e, ..., 999)

    assertThat(f("a", a = 41, b = 42, c = 43), identicalTo(41))
    assertThat(f("Z", a = 41, b = 42, c = 43), identicalTo(999))

}

test.no.partial.matching <- function() {
    assertThat(switch("a", aa=41), identicalTo(NULL))
}

test.eval <- function() {

    # only matching arguments are evaluated
    assertThat(switch("z", a=stop("a"), b=stop("b"), z=99+2, stop("default")), identicalTo(101))
}

test.expr.abbrv <- function() {
    assertThat(switch(EXPR="a", a=1, b=2), identicalTo(1))
    assertThat(switch(EXP ="a", a=1, b=2), identicalTo(1))
    assertThat(switch(EX  ="a", a=1, b=2), identicalTo(1))
    assertThat(switch(E   ="a", a=1, b=2), identicalTo(1))

}