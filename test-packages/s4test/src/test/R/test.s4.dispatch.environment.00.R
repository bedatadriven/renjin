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

library(hamcrest)
library(methods)


setClass("B", contains="environment")
b <- new("B", emptyenv())
b[["a"]] <- 100
setMethod("[[", c(x="B", i="missing", j="missing"   ), function(x, ...)       2.4  )
setMethod("[[", c(x="B", i="missing", j="ANY"       ), function(x, i, ...)    2.5  )
setMethod("[[", c(x="B", j="missing", i="ANY"       ), function(x, i, ...)    2.58 )
setMethod("[[", c(x="B", j="numeric", i="character" ), function(x, i, j, ...) 2.43 )
setMethod("[[", c(x="B", i="numeric", j="character" ), function(x, i, j, ...) 2.55 )
test.missing.env.01 = function() { assertThat(b[[]] , identicalTo(2.4)) }
test.missing.env.02 = function() { assertThat(b[["a"]] , identicalTo(2.58)) }
test.missing.env.03 = function() { assertThat(b[[1]] , identicalTo(2.58)) }
test.missing.env.04 = function() { assertThat(b[[,1]] , identicalTo(2.5)) }
test.missing.env.05 = function() { assertThat(b[[1.0,"c"]] , identicalTo(2.55)) }
test.missing.env.06 = function() { assertThat(b[["c",1.0]] , identicalTo(2.43)) }

setClass("C", representation(a = "numeric"))
c <- new("C", a = 100)
setMethod("[[", c(x="C", i="missing", j="missing"   ), function(x, ...)       12.4  )
setMethod("[[", c(x="C", i="missing", j="ANY"       ), function(x, i, ...)    12.5  )
setMethod("[[", c(x="C", j="missing", i="ANY"       ), function(x, i, ...)    12.58 )
setMethod("[[", c(x="C", j="numeric", i="character" ), function(x, i, j, ...) 12.43 )
setMethod("[[", c(x="C", i="numeric", j="character" ), function(x, i, j, ...) 12.55 )
test.missing.std.01 = function() { assertThat(c[[]] , identicalTo(12.4)) }
test.missing.std.02 = function() { assertThat(c[["a"]] , identicalTo(12.58)) }
test.missing.std.03 = function() { assertThat(c[[1]] , identicalTo(12.58)) }
test.missing.std.04 = function() { assertThat(c[[,1]] , identicalTo(12.5)) }
test.missing.std.05 = function() { assertThat(c[[1.0,"c"]] , identicalTo(12.55)) }
test.missing.std.06 = function() { assertThat(c[["c",1.0]] , identicalTo(12.43)) }
