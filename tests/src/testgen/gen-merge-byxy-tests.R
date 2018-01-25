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

funs <- c('merge')

inputs <- list(
  NULL,

  # integer data.frames
  data.frame(id=1:5, i1=c(1:4, NA_integer_), x=1:5),
  data.frame(id=1:4, i2=6:9, x=6:9),
  data.frame(id=1:5, i1=c(1:4, NA_integer_)),
  data.frame(id=1:4, i2=6:9),

  # character data.frames
  data.frame(id=LETTERS[1:5], i1=c(LETTERS[1:4], NA_character_), x=letters[1:5]),
  data.frame(id=LETTERS[1:4], i2=LETTERS[6:9], x=letters[6:9]),
  data.frame(id=LETTERS[1:5], i1=c(LETTERS[1:4], NA_character_)),
  data.frame(id=LETTERS[1:4], i2=LETTERS[6:9]),

  # factor data.frames
  data.frame(id=as.factor(LETTERS[1:5]), i1=as.factor(c(LETTERS[1:4], NA_character_)), x=as.factor(letters[1:5])),
  data.frame(id=as.factor(LETTERS[1:4]), i2=as.factor(LETTERS[6:9]), x=as.factor(letters[6:9])),
  data.frame(id=as.factor(LETTERS[1:5]), i1=as.factor(c(LETTERS[1:4], NA_character_))),
  data.frame(id=as.factor(LETTERS[1:4]), i2=as.factor(LETTERS[6:9])),

  # numeric data.frames
  data.frame(id=c(1.1, 1.2, 1.3, 1.4, 1.5), i1=c(1.1, 1.2, 1.3, 1.4, NA), x=c(1.1, 1.2, 1.3, 1.4, 1.5)),
  data.frame(id=c(1.1, 1.2, 1.3, 1.4), i2=c(1.6, 1.7, 1.8, 1.9), x=c(1.6, 1.7, 1.8, 1.9)),
  data.frame(id=c(1.1, 1.2, 1.3, 1.4, 1.5), i1=c(1.1, 1.2, 1.3, 1.4, NA)),
  data.frame(id=c(1.1, 1.2, 1.3, 1.4), i2=c(1.6, 1.7, 1.8, 1.9)),

  # empty data.frames
  data.frame(id=integer(0), i1=integer(0), x=integer(0)),
  data.frame(id=character(0), i2=character(0), x=character(0)),
  data.frame(id=numeric(0), i3=numeric(0), x=numeric(0)),
  data.frame(id=factor(0), i4=factor(0), x=factor(0)),
  data.frame(id=integer(0), i1=integer(0)),
  data.frame(id=character(0), i2=character(0)),
  data.frame(id=numeric(0), i3=numeric(0)),
  data.frame(id=factor(0), i4=factor(0))
)

ARGS <- list(
  empty         = list(NULL),
  by            = list(by='id'),
  bxy           = list(by.x="i1", by.y = "i4")
)

for(fn in funs) {

  # Setup generic implementations
  test <- test.open("generate-merge-byxy-tests.R", fn)
  writeln(test, "library(hamcrest)")

  # define some nonsense generic functions
  writeFixture(test, "%s.foo <- function(x) 41", fn)
  writeFixture(test, "Math.bar <- function(x) 44")

  # First try with no arguments
  writeTest(test, fn)

  # Now with two arguments
  for(i in inputs) {
    for(j in inputs) {
      for(k in names(ARGS)) {
        writeTest(test, fn, i, j, ARGS = ARGS[[k]])
      }
    }
  }

  close(test)
}
