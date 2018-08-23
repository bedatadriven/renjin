#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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


## Functions used in test cases

foo.default <- function(n) {
    # Return a vector of function names
    calls <- sys.calls()
    sapply(calls, function(call) as.character(call[[1]]))
}

foo <- function(n) {
    UseMethod("foo")
}

as.double.bar <- function(n) {
    foo.default()
}

g <- function(q) {
    foo(q)
}


## Test cases

test.simple <- function() {
    calls <- foo.default(0)
    assertThat(calls, identicalTo(c("test.simple", "foo.default")))
}

test.s3 <- function() {
    calls <- foo(0)
    assertThat(calls, identicalTo(c("test.s3", "foo", "foo.default")))
}

test.nested3 <- function() {
    calls <- g(0)
    assertThat(calls, identicalTo(c("test.nested3", "g", "foo", "foo.default")))
}
