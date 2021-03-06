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

source("src/testgen/gen.R")


inputs <- c(1/24, 1/10, 0, 1, -1, 2, 1.5, 10, 314, 1e6, Inf, -Inf, NA, NaN)

# Setup generic implementations
test <- test.open("gen-pow-tests.R", "pow")
writeln(test, "library(hamcrest)")


# test all combinations

for(x in inputs) {
  for(y in inputs) {
    writeTest(test, "^", x, y, TEST.NAME = "pow", tol = 1e-6)
  }
}
close(test)
