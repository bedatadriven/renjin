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
## comparison functions
o_LC_COLLATE <- Sys.getlocale("LC_COLLATE")
Sys.setlocale("LC_COLLATE", "C")
source("src/testgen/gen.R")

ops <- c(equals = "==",
         ne = "!=", 
         lt = "<",
         gt = ">",
         le = "<=",
         ge = ">=")


inputs <- list(
  NULL,
  
  # logical
  logical(0),
  c(TRUE, TRUE, FALSE, FALSE, TRUE),
  c(a=TRUE, FALSE),
  c(TRUE, FALSE, NA),
  
  # integer
  integer(0),
  structure(integer(0), .Names = character(0)),
  c(1L, 2L, 3L),
  c(1L, NA, 4L, NA, 999L),
  c(1L, 2L, 1073741824L, 1073741824L), # overflow
  
  # double
  double(0),
  signif(1:5 * pi),
  signif(1:5 * -pi),
  c(a = 1L, b = 2L),  # names
  c(a = 1.5, b = 2.5), 
  c(1.5, 1.51, 0, 1.49, -30),
  c(1.5, 1.51, 0, 1.49, -30, NA),
  c(1.5, 1.51, 0, 1.49, -30, NaN),
  c(1.5, 1.51, 0, 1.49, -30, Inf),
  c(1.5, 1.51, 0, 1.49, -30, -Inf),
  
  
  # character
  character(0),
  c('4.1', 'blahh', '99.9', '-413', NA),
  c("TRUE", "FALSE", "T", "F", "true", "false", "t", "f"),
  
  # complex
  complex(0),
  
  
  # lists
  list(1, 2, 3),
  list(1, 2, NULL),
  list(1L, 2L, 3L),
  list(1L, 2L, NULL),
  list(1, 2, list(3, 4)),
  list(3, "a", list("b", z = list(TRUE, "c"))),
  
  # matrices
  matrix(1:12, nrow = 3), 
  matrix(1:12, nrow = 3, dimnames = list(x=letters[1:3], y=letters[4:7])),  
  structure(1:3, rando.attrib=941L),
  
  #arrays
  array(1:3, dim = 3L, dimnames = list(c('a', 'b', 'c'))),
  array(1:3, dim = 3L, dimnames = list(z = c('a', 'b', 'c'))),
  
  # S3 dispatch?
  structure(list('foo'), class='foo'),
  structure(list('bar'), class='foo'),
  
  # Symbols
  as.name('xyz'),
  
  # Function Calls
  call('sin', 3.14)
)


for(op in names(ops)) {
  
  # Setup generic implementations
  test <- test.open("gen-comparison-tests.R", op)
  writeln(test, "library(hamcrest)")
  
  # define some nonsense generic functions
  writeFixture(test, "`%s.foo` <- function(...) 41", ops[op])
  writeFixture(test, "Ops.bar <- function(...) 46")
  
  # test all combinations
  
  for(x in inputs) {
    for(y in inputs) {
      writeTest(test, ops[op], x, y, TEST.NAME =  op);
    }
  }
  
  # Remove symbols we added to the global environment
  rm(list = c(sprintf("%s.foo", ops[op]), "Ops.bar"))
  
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

Sys.setlocale("LC_COLLATE", o_LC_COLLATE)
