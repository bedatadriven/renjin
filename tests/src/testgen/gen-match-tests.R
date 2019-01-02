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

inputs <- list(
  
  NULL,
  
  # logical
  logical(0),
  TRUE,
  FALSE,
  NA,
  c(TRUE, FALSE, FALSE),
  c(TRUE, FALSE, NA),
  c(FALSE, FALSE, TRUE, TRUE, TRUE),
  c(NA),
  c(NA, NA),
    
  # integers
  integer(0),
  1L,
  NA_integer_,
  1:3,
  c(1L, 2L, NA_integer_),
  c(NA_integer_, NA_integer_),

  # doubles
  double(0),
  1.9,
  NA_real_,
  c(1.5, 2.5, 3.5),
  c(1.5, 1.49, 1.51, 2, 2, NA),
  c(1.5, 1.49, 1.51, 2, 2, Inf, -Inf),
  c(1.5, NA, 1.49, 1.51, 2, 2, NA, NaN),
  c(1.5, 1.49, 1.51, 2, 2, NA, NaN, Inf, -Inf),
  c(NA_real_, NA_real_),

  # complex
  #complex(0),
 # 1+3i,
  #NA_complex_,
 # c(1+3.4i, 1.5+0i, 4.5+3i),
 # c(1+2i, 0, NaN, NA),
 # c(1+3i, 0, Inf+2i, Inf, -Inf),
  
  # character
  character(0),
  "foo",
  NA_character_,
  c("foo", "1.5", "1", "32arglebargle", "foo", "1+2i", "1+arglei"),
  c("foo", "1.5", "1", "32arglebargle", "1+2i", "1+arglei", NA),
  c("NaN", "TRUE", "FALSE", "true", "false", "T", "F", "tRuE", "faLse", "Inf")
 
)

fn <- "match"

# Setup generic implementations
test <- test.open("generate-match-tests.R", fn)
writeln(test, "library(hamcrest)")

# define some nonsense generic functions
writeFixture(test, "match.foo <- function(...) 41", fn)
writeFixture(test, "Math.bar <- function(...) 44")

# Try combinations of vectors
for(i in inputs) {
  for(j in inputs) {
    writeTest(test, "match", i, j)
  }
}

close(test)