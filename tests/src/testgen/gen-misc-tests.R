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




## Generates test cases for
## functions with higher arity

source("src/testgen/gen.R")

cases <- list(
  
  pretty = list(
      list(x = 1:15),
      list(x = 1:15, h = 2),
      list(x = 1:15, n = 3),
      list(x = 1:15 * 2),
      list(x = 1:20),
      list(x = 1:20, n = 2),
      list(x = 1:20, n = 10),
      list(x = pi),
      list(x = 1.234e100),
      list(x = 1001.1001),
      list(1001.1001, shrink = 0.2))

)


for(fn in names(cases)) {

  # Setup generic implementations
  test <- test.open("gen-misc-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  tol <- 1e-6
  
  # Check that numerical values are correct
  for(input in cases[[fn]]) {
    writeTest(test, fn, ARGS = input, tol = tol)
  }
  
  close(test)
}

