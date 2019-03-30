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

test.simple <- function() {

    signalerCompleted <- FALSE
    handlerCalled1 <- FALSE
    handlerCalled2 <- FALSE


    do_signal <- function() {

      condition <- structure(
        list(message = "foo"),
        class = c("condition_a", "condition_b", "condition"))

      signalCondition(condition)

      # execution should continue
      signalerCompleted <<- TRUE
    }

    withCallingHandlers(
      do_signal(),
      condition_a = function(e) { handlerCalled1 <<- TRUE },
      condition_b = function(e) { handlerCalled2 <<- TRUE }
    )


    assertTrue(signalerCompleted)
    assertTrue(handlerCalled1)
    assertTrue(handlerCalled2)

}

# TODO(alex) : reimplement conditions as arguments to stop
ignore.test.rethrow <- function() {

    # From testthat

    skip <- function(message) {
      cond <- structure(list(message = message), class = c("skip", "condition"))
      stop(cond)
    }

    code <- quote(skip("foobar"))

    handled <- FALSE

    handle_skip <- function(e) {
        handled <<- TRUE
        signalCondition(e)
    }

    tryCatch(
        withCallingHandlers({
          eval(code)
          },
          skip = handle_skip
        ),
    # skip silently terminate code
    skip =  function(e) {}
    )
}

test.errors <- function() {

    errorHandlerCalled <- FALSE
    flowResumed <- FALSE

    ev <- try(withCallingHandlers({
            stop('foo')
            flowResumed <<- TRUE
        }, error = function(e) { errorHandlerCalled <<- TRUE }), silent=TRUE)

    assertThat(class(ev), identicalTo("try-error"))
    assertTrue(errorHandlerCalled)
}
