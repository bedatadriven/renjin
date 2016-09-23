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


## Generates test cases for combining functions

source("src/testgen/gen.R")

inputs <- list(
  integer(0),
  1:15,
  
  character(0),
  letters[1:4],
  c(x = "a", y = "b"),
  
  list(),
  list(a = 1, b = 2),
  
  matrix(1:12, nrow=3)
  
)

applyfns <- c("lapply", "sapply")

for(applyfn in applyfns) {
  
  # Setup generic implementations
  test <- test.open("generate-apply-tests.R", applyfn)
  writeln(test, "library(hamcrest)")
  
  # define some functions which we can use
  fns <- c("f1", "f2", "f3")
  writeFixture(test, "f1 <- function(x) x")
  writeFixture(test, "f2 <- function(x) x*2")
  writeFixture(test, "f3 <- function(x) NULL")
  
  for(input in inputs) {
    for(fun in fns) {
      writeTest(test, applyfn, input, fun)
    }
  }
  
  close(test)
}
