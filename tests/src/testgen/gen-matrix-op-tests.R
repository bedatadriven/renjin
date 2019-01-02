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


## Generates test cases for combining functions

source("src/testgen/gen.R")

fns <- c('crossprod', 'tcrossprod', 'matprod')

inputs <- list(
  c(1L, 2L, 3L),
  c(p=1L, q=2L, r=3L),

  # 2-d matrices
  matrix(1:12, nrow = 3),
  matrix(1:12, nrow = 3, dimnames = list(letters[1:3], letters[4:7])),
  matrix(1:12, nrow = 3, dimnames = list(x = letters[1:3], y = letters[4:7])),

  # 2-d matrices with NAs
  matrix(c(1, NA, NA, 3), nrow = 2),
  matrix(c(1, NA, NA, 3), nrow = 2, dimnames = list(c("r1", "r2"), c("c1", "c2"))),
  matrix(c(1, NA, NA, 3), nrow = 2, dimnames = list(x = c("r1", "r2"), y = c("c1", "c2"))),

  # zero-length 2-d matrices
  matrix(1:3, nrow = 3),
  matrix(1:3, nrow = 3, dimnames = list(letters[1:3], character(0))),
  matrix(1:3, nrow = 3, dimnames = list(x = letters[1:3], y = character(0)))
)


for(fn in fns) {

  # Setup generic implementations
  test <- test.open("gen-matrix-ops.R", fn)
  writeln(test, "library(hamcrest)")
  writeFixture(test, "matprod <- function(x, y) x %*% y")
  
  for(input in inputs) {
    
    if(fn != "matprod") {
      writeTest(test, fn, input)
    }
    for(x in inputs) {
      for(y in inputs) {
        writeTest(test, fn, x, y)
      }
    }
  }


  close(test)
}
