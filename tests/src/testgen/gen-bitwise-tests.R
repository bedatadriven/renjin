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


## Generates test cases for combining functions

source("src/testgen/gen.R")


inputs <- list(
  NA_integer_,
  0L,
  1L,
  -1L,
  31L,
  255L,
  -1L,
  -255L,
  as.integer(2^30),
  as.integer(2^30) + 1024,
  -as.integer(2^30 - 1024),
  
  # some non-integers to test conversion
  0,
  3.0,
  3.14,
  "",
  "3",
  "3.14"
)

unary <- c("bitwNot")
binary <- c("bitwAnd", "bitwOr", "bitwXor")
shift <- c("bitwShiftL", "bitwShiftR")

for(fn in c(unary, binary, shift)) {
  
  test <- test.open("generate-bitwise-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  # define some nonsense generic functions
  #writeFixture(test, "%s.foo <- function(x) 41", fn)
  #writeFixture(test, "Math.bar <- function(x) 44")
  
  if(fn %in% unary) {
    for(input in inputs) {
      writeTest(test, fn, input)
    }
  } else if(fn %in% binary) {
    for(x in inputs) {
      for(y in inputs) {
        writeTest(test, fn, x, y)
      }
    }
  } else if(fn %in% shift) {
    for(x in inputs) {
      for(y in -2:33) {
        writeTest(test, fn, x, y)
      }
    }
  }
  
  close(test)
}
