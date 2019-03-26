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


test.exit.handlers.called.on.error <- function() {

    env <- new.env()
    f <- function() stop("F!")
    g <- function() { on.exit( { env$called <- TRUE } ); f() }

    res <- try(g())

    assertTrue(inherits(res, "try-error"))
    assertThat(env$called, identicalTo(TRUE))
}

test.exit.handlers.called.on.error2 <- function() {

    env <- new.env()
    g <- function() { on.exit( { env$called <- TRUE } ); stop("GRRR"); }

    res <- try(g())

    assertTrue(inherits(res, "try-error"))
    assertThat(env$called, identicalTo(TRUE))
}

test.exit.handlers.called.on.return <- function() {

    env <- new.env()
    g <- function() { on.exit( { env$called <- TRUE } ); return("G"); }

    res <- g()

    assertThat(res, identicalTo("G"))
    assertThat(env$called, identicalTo(TRUE))
}

test.exit.handlers.in.promise <- function() {

    env <- new.env()
    f <- function() {
        x <- TRUE
        force( { on.exit({ env$called <- x }); 41 })

        # on.exit() handlers should be attached to f(), not
        # the call to force()

        assertThat(env$called, identicalTo(NULL))

        return(42)
    }

    res <- f()

    # Now that f() has exited, side effects should be visible
    assertThat(res, identicalTo(42))
    assertThat(env$called, identicalTo(TRUE))
}


test.exit.handlers.in.eval <- function() {

    env <- new.env()
    eval(quote(on.exit(env$called <- TRUE)))

    assertThat(env$called, identicalTo(TRUE))
}


test.exit.handlers.in.eval.with.error <- function() {

    env <- new.env()
    res <- try(eval(quote({ on.exit(env$called <- TRUE); stop("E!!"); })))

    assertThat(env$called, identicalTo(TRUE))

}

