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
## the grep functions

source("src/testgen/gen.R")


## Read test cases from regex-posix-unittest package
## Source: https://hackage.haskell.org/package/regex-posix-unittest

posix <- read.csv("src/testgen/regex-posix-unittest-1.1.csv", stringsAsFactors = FALSE)

patterns <- c(
  "",
  ".",
  ".+",
  "a.+",
  "a+",
  "+",
  "*",
  ""
)

strings <- c(
  "",
  "aaa",
  "123",
  "a"
)

# Setup generic implementations
test <- test.open("gen-regex-tests.R", "grepl")
writeln(test, "library(hamcrest)")

for(i in 1:nrow(posix)) {
  writeTest(test, "grepl", pattern = posix$pattern[i], x = posix$string[i])
}


close(test)

