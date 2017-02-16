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

signalerCompleted <- FALSE
handlerCalled1 <- FALSE
handlerCalled2 <- FALSE
restartInvoked <- FALSE
controlFlowResumed <- FALSE

do_signal <- function() {
  
  condition <- structure(
    list(message = "foo"),
    class = c("condition_a", "condition_b", "condition"))
  
  x <- withRestarts({
    signalCondition(condition);
    cat("Control flow resumed.\n");
    controlFlowResumed <<- TRUE
  },
  restart1 = function() { 41 },
  restart2 = function() { restartInvoked <<- TRUE; 42 }
  )

  # execution should continue
  signalerCompleted <<- TRUE

  assertThat(x, identicalTo(42))
}

withCallingHandlers(
  do_signal(),
  condition_a = function(e) { handlerCalled1 <<- TRUE; invokeRestart("restart2"); stop("Should not reach here.") },
  condition_b = function(e) { handlerCalled2 <<- TRUE }
)


assertTrue(signalerCompleted)
assertTrue(restartInvoked)
assertTrue(handlerCalled1)
assertFalse(handlerCalled2)
assertFalse(controlFlowResumed)