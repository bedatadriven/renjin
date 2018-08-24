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

debugSource("src/testgen/gen.R")

Sys.setlocale("LC_TIME", "C")

fn <- 'format.POSIXlt'

test <- test.open("gen-time-format-tests.R", fn)
writeln(test, "library(hamcrest)")
writeFixture(test, "unix <- function(t) as.POSIXlt(t, origin='1970-01-01', tz='UTC')")

dateSeq <- function(from, length, by = "day") as.character(seq(from=as.Date(from), length.out = length, by = by))

writeDateTests <- function(formats, dates) {
  for(format in formats) {
    for(date in dates) {
      writeTest(test, 'format', literal(sprintf("as.Date('%s')", date)), format)
    }
  }
}

# Day of week
writeDateTests(c("%a", "%A", "%w"), dateSeq("2018-01-01", 10))

# Months
writeDateTests(c("%b", "%B"), dateSeq("2018-01-01", 14, "month"))

# Day
writeDateTests(c("%Y-%M-%d"), dateSeq("2016-02-15", 20))

# 2-digit Year
writeDateTests(c("%y-%M-%d"), c("1855-01-01", "1901-04-15", "1967-01-01", "1968-01-01", "1999-01-01", "2001-01-01", "2050-04-03"))


# Week numbers
writeDateTests(c("%Y %U", "%Y %W", "%j"), dateSeq("2014-01-01", 365*4)[c(TRUE, FALSE, FALSE, FALSE)])
  
## Now times....

for(format in c("%H:%M:%S", "%I:%M:%S %p")) {
  for(time in seq(from=1534942369, by = 354, length.out = 300)) {
    writeTest(test, 'format', literal(sprintf("unix(%d)", time)), format)
  }
}

close(test)
