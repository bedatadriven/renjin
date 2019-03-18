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

fns <- ls(envir = baseenv())
fns.type <- sapply(fns, function(f) typeof(baseenv()[[f]]) == "closure")
closures <- fns[fns.type]

test <- test.open("gen-base-api-tests.R", "api.base")

# Setup generic implementations
writeln(test, "library(hamcrest)")
writeln(test, "formaln <- function(x) names(formals(get(x, envir=baseenv()))) ")

for(fn in closures) {

  writeTest(test, "formaln", fn)  
}

close(test)
