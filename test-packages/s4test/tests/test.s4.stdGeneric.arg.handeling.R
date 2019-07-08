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

library(hamcrest)
library(methods)

x <- matrix(1:4,2)
setGeneric("nargz", function(x, nrow, ncol) standardGeneric("nargz"))
setMethod("nargz", "ANY", function(x=1, nrow, ncol) {
  if (is.matrix(x)) {
    if (nargs() > 1L) return(paste("input: matrix with '", nargs(), "' args", sep="") )
    else return("input: matrix with '1' arg")
  } else {
    if (nargs() > 1L) return(paste("input: non-matrix with '", nargs(), "' args", sep="") )
    else return("input: non-matrix with '1' arg")
  }
})

test.arg.handeling.1 = function() { assertThat(nargz(x), identicalTo("input: matrix with '1' arg")) }
test.arg.handeling.2 = function() { assertThat(nargz(x,1), identicalTo("input: matrix with '2' args")) }
test.arg.handeling.3 = function() { assertThat(nargz(x,1,1), identicalTo("input: matrix with '3' args")) }
test.arg.handeling.4 = function() { assertThat(nargz(1), identicalTo("input: non-matrix with '1' arg")) }
test.arg.handeling.5 = function() { assertThat(nargz(1,1), identicalTo("input: non-matrix with '2' args")) }
test.arg.handeling.6 = function() { assertThat(nargz(1,1,1), identicalTo("input: non-matrix with '3' args")) }
