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


## Generates test cases for
## the unary functions in the S3 Math Group

source("src/testgen/gen.R")

unary <- c('acos', 'acosh','asin','asinh','atan','atanh',
'cos', 'cosh', 'cospi', 'sin','sinh',
'sinpi','tan','tanh', 'tanpi', 'abs', 'ceiling', 'exp', 'expm1', 'floor',
'gamma', 'lgamma', 'log', 'log', 'log1p', 'log10', 'round', 'sign', 'signif', 'sqrt', 'trunc', 
'trigamma')

inputs <- signif(c(0.01, 0.1, 1, 1.5, 2, 2.5, 4,10, 100, pi/4, pi/2, pi, 2*pi), digits = 6)


for(fn in unary) {

  # Setup generic implementations
  test <- test.open("gen-math-unary-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  
  if(fn %in% c("round", "sign", "signif", "trunc", "floor", "ceiling")) {
    tol <- NULL
  } else {
    tol <- 0.0001
  }
  
  # define some nonsense generic functions
  writeFixture(test, "%s.foo <- function(...) 41", fn)
  writeFixture(test, "Math.bar <- function(...) 44")
  
  # Check that numerical values are correct
  for(input in c(-inputs, inputs)) {
    writeTest(test, fn, input, tol = tol)
  }
  
  # Check NULL values
  writeTest(test, fn, NULL)
  
  # Check vectorization
  writeTest(test, fn, inputs[1:4], tol = tol)
  
  # Check empty inputs
  writeTest(test, fn, integer(0))
  writeTest(test, fn, double(0))
  
  # Check NA handling
  writeTest(test, fn, NaN)
  writeTest(test, fn, NA_real_)
  
  # Check Infinite Values...
  writeTest(test, fn, Inf, tol = tol)
  writeTest(test, fn, -Inf, tol = tol)
  
  # Check handling of integer inputs
  writeTest(test, fn, c(1L, 4L), tol = tol)
  
  # Verify generic dispatch
  writeTest(test, fn, structure(1, class = 'foo'))
  writeTest(test, fn, structure(1, class = 'bar'))
  writeTest(test, fn, structure(list("a"), class = 'foo'))
  writeTest(test, fn, structure(list("b"), class = 'bar'))
  
  writeTest(test, fn, c(a = 1, b = 2, c = 3), tol = tol)
  writeTest(test, fn, c(x = 1, 2), tol = tol)
  writeTest(test, fn, matrix(1:12, nrow=3), tol = tol)
  writeTest(test, fn, structure(0, rando.attr=4L), tol = tol)
  writeTest(test, fn, structure(0, class='zinga'), tol = tol)
  
  # Generic dispatch with round, digits 
  if(fn %in% c("round", "signif")) {
    writeTest(test, fn, structure(1, class = 'foo'), 2)
    writeTest(test, fn, structure(1, class = 'bar'), 2)
  }
  
  # TODO: argument name matching
  # TODO: complex numbers
  
  close(test)
}

run.test <- function() {
    for(f in ls(envir = .GlobalEnv)) {
      if(grepl(f, pattern="^test\\.")) {
        print(f)
        do.call(f, list())
      } 
    }
}

