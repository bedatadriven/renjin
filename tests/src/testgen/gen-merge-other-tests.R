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
  data.frame(id=5:2, i4=5:2, x=15:12),
  data.frame(id=1:5, i1=c(1:4, NA_integer_)),
  data.frame(id=5:2, i4=5:2),

  # empty data.frames
  data.frame(id=integer(0), i1=integer(0), x=integer(0)),
  data.frame(id=integer(0), i1=integer(0))
)

ARGS <- list(
  byxy.all      = list(by.x="i1", by.y = "i4", all = TRUE),
  byxy.inc      = list(by.x="i1", by.y = "i4", incomparables = c(NA, 3)),
  byxy.inc.all  = list(by.x="i1", by.y = "i4", incomparables = c(NA, 3), all = TRUE),
  allx          = list(all.x = TRUE),
  ally          = list(all.y = TRUE),
  all           = list(all = TRUE),
  all.srt       = list(all = TRUE, sort = FALSE),
  by.suf        = list(by = "id", suffixes = c(".1st",".2nd")),
  by.suf.all    = list(by = "id", suffixes = c(".1st",".2nd"), all = TRUE)
)

for(fn in funs) {

  # Setup generic implementations
  test <- test.open("generate-merge-other-tests.R", fn)
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
