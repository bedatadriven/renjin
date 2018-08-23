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

test.change.value.possible <- function() {
    env <- new.env(parent = emptyenv())
    env$x <- 41
    env$y <- 42
    lockEnvironment(env)

    env$x <- 91
    env$y <- 92

    assertThat(env$x, equalTo(91))
    assertThat(env$y, equalTo(92))

    assertThat({ env$q <- 44 }, throwsError())
    assertThat({ rm(x, envir = env) }, throwsError())
}


test.bindings <- function() {
    env <- new.env(parent = emptyenv())
    env$x <- 41
    env$y <- 42
    lockEnvironment(env, TRUE)

    assertThat( { env$x <- 91 }, throwsError())
    assertThat( { env$q <- 44 }, throwsError())
    assertThat( { rm(x, envir = env) }, throwsError())
}
