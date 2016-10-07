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


## Generates test cases for
## the cumxxx functions in the math group

source("src/testgen/gen.R")

unary <- c('cumsum', 'cumprod', 'cummax', 'cummin', 
           'sum', 'prod', 'min', 'max', 'range', 'any', 'all')


inputs <- list(
  
  NULL,
  
  # Logical Vectors
  logical(0),
  c(TRUE, TRUE, FALSE, FALSE, TRUE),
  c(TRUE, TRUE, TRUE),
  c(TRUE, TRUE, NA),
  c(FALSE, FALSE, FALSE, FALSE),
  c(FALSE, FALSE, FALSE, FALSE, NA),
  c(NA, NA, NA),
  c(a=TRUE, b=FALSE),
  
  # Integer vectors
  integer(0),
  structure(integer(0), .Names = character(0)),
  c(NA_integer_, NA_integer_, NA_integer_),
  c(1L, 2L, 3L),
  c(1L, NA, 4L, NA, 999L),
  c(1L, 2L, 1073741824L, 1073741824L), # overflow
  c(a = 1L, b = 2L),  # names
  
  # Double vectors
  double(0),
  structure(double(0), .Names = character(0)),
  c(NA_real_, NA_real_),
  signif(1:5 * pi),
  signif(1:5 * -pi),
  c(1.5, 2.5), 
  c(1.5, NA),
  c(1.5, NaN),
  c(Inf, -1.5),
  c(-Inf, -1.5),
  c(Inf, 1.5),
  c(Inf, -1.5),
  c(a = 1.5, b = 2.5), 
  
  # Character vectors
  character(0),
  c(NA_character_, NA_character_),
  structure(character(0), .Names = character(0)),
  c("a", "b"),
  c("4.1", "blahh", "99.9", "-413", NA),
  c(a = "4.1", b = "99.9"),
  c("TRUE", "TRUE", "FALSE"),
  c("true", "false", "true"),
  c("true", "false", "true"),
  
  # Complex Numbers
  complex(0),
  c(NA_complex_, NA_complex_, NA_complex_),
  structure(complex(0), .Named = character(0)),
  c(1+3i, 4+6i, 1-3i),
  
  # Matrices
  matrix(1:12, nrow = 3),
  structure(1:3, rando.attrib=941L),

  array(1:3, dim = 3L, dimnames = list(c("a", "b", "c")))

 # TODO: GNU R does not seem to handle this correctly...  as.raw(c(0, 255, 31))
)

inputsWithAttributes <- list(
  
  
)


for(fn in unary) {

  # Setup generic implementations
  test <- test.open("gen-summary-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  # min/max/range behavior is dependant on the 
  # current locale.
  if(fn %in% c("min", "max", "range")) {
    writeFixture(test, "Sys.setlocale('LC_COLLATE', 'C')")
  }
  
  # define some nonsense generic functions
  writeln(test, "%s.foo <- function(...) 41", fn)
  writeln(test, "Math.bar <- function(...) 44")
  writeln(test, "Summary.bar <- function(...) 45")
  
  # define the generic functions in the current environment 
  # as well so we produce the right values for comparison
  assign(sprintf("%s.foo",fn), function(...) 41)
  assign(sprintf("Math.bar",fn), function(...) 44)
  assign(sprintf("Summary.bar",fn), function(...) 45)
  
  tol <- 0.0001
  
  # Check that numerical values are correct
  for(input in inputs) {
    writeTest(test, fn, input, tol = tol)
  }
  
  # Check combinations for min, max, range
  # But skip those with attributes to reduce explosion
  short.list <- inputs[ sapply(inputs, function(i) is.null(attributes(i))) ]
  if(fn %in% c("min", "max", "range")) {
    for(na.rm in c(TRUE, FALSE)) {
        for(i in short.list) {
            for(j in short.list) {
                writeTest(test, fn, i, j, na.rm = TRUE)
            }
        }
    }
  }

  # Check S3 dispatch
  writeTest(test, fn, structure("foo", class='foo'))
  writeTest(test, fn, structure(list(1L, "bar"), class='bar'))
  
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

