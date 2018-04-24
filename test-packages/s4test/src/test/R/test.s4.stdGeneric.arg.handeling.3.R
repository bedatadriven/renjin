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

library(hamcrest)
library(methods)

setGeneric("add", function(x=FALSE, y, z=TRUE) standardGeneric("add"), signature = "y")
setMethod("add", signature(y="missing"  ), function(x, y, z="zArg") c(x, 100, z) )
setMethod("add", signature(y="numeric"  ), function(x=FALSE, y, z=TRUE) c(x, y, z) )
setMethod("add", signature(y="character"), function(x=TRUE, y, z=FALSE) c(x, y, z) )
setMethod("add", signature(y="logical"), function(x, y, z) c(x, y, z) )

test.signature.01 = function() { assertThat(add(x=0, y=), identicalTo( c("0", "100", "zArg") ))   }
test.signature.02 = function() { assertThat(add(x=0,y=10 ), identicalTo( c(0, 10, 0) ))   }
test.signature.03 = function() { assertThat(add(x=0,y="a"), identicalTo( c("0", "a", "TRUE") ))   }
test.signature.04 = function() { assertThat(add(x=0,y=TRUE), identicalTo( c(0, 1, 1) ))   }
test.signature.05 = function() { assertThat(add(), identicalTo( c("FALSE", "100", "zArg") ))   }
test.signature.06 = function() { assertThat(add(y=10 ), identicalTo( c(1, 10, 0) ))   }
test.signature.07 = function() { assertThat(add(y="a"), identicalTo( c("FALSE", "a", "TRUE") ))   }
test.signature.08 = function() { assertThat(add(y=TRUE), identicalTo( c(FALSE, TRUE, TRUE) ))   }
test.signature.09 = function() { assertThat(add(z="hah"), identicalTo( c("FALSE", "100", "hah") ))   }
test.signature.10 = function() { assertThat(add(y=10, z=200 ), identicalTo( c(1, 10, 200) ))   }
test.signature.11 = function() { assertThat(add(y="a", z=32), identicalTo( c("FALSE", "a", "32") ))   }
test.signature.12 = function() { assertThat(add(y=TRUE, z=32), identicalTo( c(0, 1, 32) ))   }
test.signature.13 = function() { assertThat(add("hah"), identicalTo( c("hah", "100", "zArg") ))   }
test.signature.14 = function() { assertThat(add(10, 200 ), identicalTo( c(10, 200, 0) ))   }
test.signature.15 = function() { assertThat(add("a", 32), identicalTo( c("a", "32", "FALSE") ))   }
test.signature.16 = function() { assertThat(add(TRUE, 32), identicalTo( c(1, 32, 0) ))   }




setGeneric("add2", function(x=FALSE, y, z=TRUE, ..., h) standardGeneric("add2"), signature = c("y", "h") )
setMethod("add2", signature(y="missing", h="missing"  ), function(x, y, z="zArg", h) c(x, 101, z, 201) )
setMethod("add2", signature(y="numeric", h="missing"  ), function(x, y, z="zArg", h) c(x, y, z, 202) )
setMethod("add2", signature(y="character", h="missing"  ), function(x, y, z="zArg", h) c(x, y, z, 203) )
setMethod("add2", signature(y="logical", h="missing"  ), function(x, y, z="zArg", h) c(x, y, z, 204) )
setMethod("add2", signature(y="missing", h="numeric"  ), function(x, y, z="zArg", h) c(x, 102, z, h) )
setMethod("add2", signature(y="missing", h="character"  ), function(x, y, z="zArg", h) c(x, 103, z, h) )
setMethod("add2", signature(y="missing", h="logical"  ), function(x, y, z="zArg", h) c(x, 104, z, h) )
setMethod("add2", signature(y="numeric", h="numeric"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="character", h="character"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="logical", h="logical"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="numeric", h="character"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="character", h="logical"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="logical", h="numeric"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="numeric", h="logical"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="character", h="numeric"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="logical", h="character"  ), function(x, y, z="zArg", h) c(x, y, z, h) )
setMethod("add2", signature(y="numeric"  ), function(x=FALSE, y, z=TRUE) c(x, y, z) )
setMethod("add2", signature(y="character"), function(x=FALSE, y, z=TRUE) c(x, y, z) )
setMethod("add2", signature(y="logical"), function(x, y, z) c(x, y, z) )

test.signature.ellipses.01 = function() { assertThat(add2(x=0,y=   ), identicalTo( c("0", "101", "zArg", "201") ))   }
test.signature.ellipses.02 = function() { assertThat(add2(x=0,y=10 ), identicalTo( c("0", "10", "zArg", "202") ))   }
test.signature.ellipses.03 = function() { assertThat(add2(x=0,y="a"), identicalTo( c("0", "a", "zArg", "203") ))   }
test.signature.ellipses.04 = function() { assertThat(add2(x=0,y=TRUE), identicalTo( c("0", "TRUE", "zArg", "204") ))   }
test.signature.ellipses.05 = function() { assertThat(add2(), identicalTo( c("FALSE", "101", "zArg", "201") ))   }
test.signature.ellipses.06 = function() { assertThat(add2(y=10 ), identicalTo( c("FALSE", "10", "zArg", "202") ))   }
test.signature.ellipses.07 = function() { assertThat(add2(y="a"), identicalTo( c("FALSE", "a", "zArg", "203") ))   }
test.signature.ellipses.08 = function() { assertThat(add2(y=TRUE), identicalTo( c("FALSE", "TRUE", "zArg", "204") ))   }
test.signature.ellipses.09 = function() { assertThat(add2(z="hah"), identicalTo( c("FALSE", "101", "hah", "201") ))   }
test.signature.ellipses.11 = function() { assertThat(add2(y=10, z=200 ), identicalTo( c(0, 10, 200, 202) ))   }
test.signature.ellipses.12 = function() { assertThat(add2(y="a", z=32), identicalTo( c("FALSE", "a", "32", "203") ))   }
test.signature.ellipses.13 = function() { assertThat(add2(y=TRUE, z=32), identicalTo( c(0, 1, 32, 204) ))   }
test.signature.ellipses.14 = function() { assertThat(add2("hah"), identicalTo( c("hah", "101", "zArg", "201") ))   }
test.signature.ellipses.15 = function() { assertThat(add2(10, 200 ), identicalTo( c("10", "200", "zArg", "202") ))   }
test.signature.ellipses.16 = function() { assertThat(add2("a", 32), identicalTo( c("a", "32", "zArg", "202") ))   }
test.signature.ellipses.17 = function() { assertThat(add2(TRUE, 32), identicalTo( c("TRUE", "32", "zArg", "202") ))   }
test.signature.ellipses.18 = function() { assertThat(add2(x=0,h=   ), identicalTo( c("0", "101", "zArg", "201") ))   }
test.signature.ellipses.19 = function() { assertThat(add2(x=0,h=10 ), identicalTo( c("0", "102", "zArg", "10") ))   }
test.signature.ellipses.20 = function() { assertThat(add2(x=0,h="a"), identicalTo( c("0", "103", "zArg", "a") ))   }
test.signature.ellipses.21 = function() { assertThat(add2(x=0,h=TRUE), identicalTo( c("0", "104", "zArg", "TRUE") ))   }
test.signature.ellipses.22 = function() { assertThat(add2(), identicalTo( c("FALSE", "101", "zArg", "201") ))   }
test.signature.ellipses.23 = function() { assertThat(add2(h=10 ), identicalTo( c("FALSE", "102", "zArg", "10") ))   }
test.signature.ellipses.24 = function() { assertThat(add2(h="a"), identicalTo( c("FALSE", "103", "zArg", "a") ))   }
test.signature.ellipses.25 = function() { assertThat(add2(h=TRUE), identicalTo( c("FALSE", "104", "zArg", "TRUE") ))   }
test.signature.ellipses.26 = function() { assertThat(add2(z="hah"), identicalTo( c("FALSE", "101", "hah", "201") ))   }
test.signature.ellipses.27 = function() { assertThat(add2(h=10, z=200 ), identicalTo( c(0, 102, 200, 10) ))   }
test.signature.ellipses.28 = function() { assertThat(add2(h="a", z=32), identicalTo( c("FALSE", "103", "32", "a") ))   }
test.signature.ellipses.29 = function() { assertThat(add2(h=TRUE, z=32), identicalTo( c(0, 104, 32, 1) ))   }
test.signature.ellipses.30 = function() { assertThat(add2("hah"), identicalTo( c("hah", "101", "zArg", "201") ))   }
test.signature.ellipses.31 = function() { assertThat(add2(10, 200 ), identicalTo( c("10", "200", "zArg", "202") ))   }
test.signature.ellipses.32 = function() { assertThat(add2("a", 32), identicalTo( c("a", "32", "zArg", "202") ))   }
test.signature.ellipses.33 = function() { assertThat(add2(TRUE, 32), identicalTo( c("TRUE", "32", "zArg", "202") ))   }
