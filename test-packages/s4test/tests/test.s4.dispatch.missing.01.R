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


setClass("A", representation(a="numeric"))
a <- new("A", a = 10)
setMethod("[[", c(x="A",  i="missing", j="missing"), function(x, ...)       "A#missing#missing" )
setMethod("[[", c(x="A", i="ANY", j="ANY"      ), function(x, i, j, ...) "A#ANY#ANY")
setMethod("[[", c(x="A",  i="A"  ,j="missing"  ), function(x, i, j, ...)    "A#A#missing" )
setMethod("[[", c(x="A",  i="A", j="ANY"        ), function(x, i, j, ...) "A#A#ANY")
setMethod("[[", c(x="A",  i="missing", j="A"      ), function(x, j, ...)    "A#missing#A" )
setMethod("[[", c(x="A", i="ANY"   , j="A"        ), function(x, i, j, ...) "A#ANY#A" )
setMethod("[[", c(x="A", i="A",       j="A"      ), function(x, i, j, ...) 1.8 )
test.s4.missing.01 = function() { assertThat( a[[]] , identicalTo( "A#missing#missing" )) }
test.s4.missing.02 = function() { assertThat( a[[a,a]] , identicalTo( 1.8 )) }
test.s4.missing.03 = function() { assertThat( a[[a,]] , identicalTo( "A#A#missing" )) }
test.s4.missing.04 = function() { assertThat( a[[,a]] , identicalTo( "A#missing#A" )) }
test.s4.missing.05 = function() { assertThat( a[[i=a,j=]] , identicalTo( "A#A#missing" )) }
test.s4.missing.06 = function() { assertThat( a[[j=a,i=]] , identicalTo( "A#A#missing" )) }
test.s4.missing.07 = function() { assertThat( a[[a,1]] , identicalTo( "A#A#ANY" )) }
test.s4.missing.07b = function() { assertThat( a[[j=a,i=1]] , identicalTo( "A#A#ANY" )) }
test.s4.missing.08 = function() { assertThat( a[[1,a]] , identicalTo( "A#ANY#A" )) }
test.s4.missing.08b = function() { assertThat( a[[j=1,i=a]] , identicalTo( "A#ANY#A" )) }

setClass("B", representation(b = "numeric"))
b <- new("B", b = 10)
setMethod("[[", c(x = "B", i = "missing", j = "ANY"    ), function(x, j,    ...) 1.47)
test.s4.missing.09 = function() { assertThat( b[[,1]] , identicalTo( 1.47 )) }
test.s4.missing.10 = function() { assertThat( b[[]] , identicalTo( 1.47 ))}
test.s4.missing.11 = function() {
    setMethod("[[", c(x = "B", i = "missing", j = "missing"), function(x,       ...) 1.46);
    assertThat( b[[]] , identicalTo( 1.46 ))
}

setClass("C", representation(c = "numeric"))
c <- new("C", c = 10)
setMethod("[[<-", c(x = "C", i = "C", j = "C", value = "C"    ), function(x, i, j,..., value) { x@c <- 1.001; x} )
setMethod("[[<-", c(x = "C", i = "C", j = "C", value = "NULL"    ), function(x, i, j,..., value) { x@c <- 1.002; x} )
setMethod("[[<-", c(x = "C", i = "C", j = "missing", value = "C"    ), function(x, i, j,..., value) { x@c <- 1.003; x} )
setMethod("[[<-", c(x = "C", i = "C", j = "missing", value = "NULL"    ), function(x, i, j,..., value) { x@c <- 1.004; x} )
test.s4.missing.12 = function() { assertThat( {c[[c,c]]<-c; c@c} , identicalTo( 1.001 )) }
test.s4.missing.13 = function() { assertThat( {c[[c,c]]<-NULL; c@c} , identicalTo( 1.002 )) }
test.s4.missing.14 = function() { assertThat( {c[[c,]]<-c; c@c} , identicalTo( 1.003 )) }
test.s4.missing.15 = function() { assertThat( {c[[c,]]<-NULL; c@c} , identicalTo( 1.004 )) }

setClass("D", representation(dval = "numeric"))
d <- new("D", dval = 10)
setMethod("[[<-", c(x = "D", i = "D", j = "missing", value = "D"    ), function(x, i,..., value) { x@dval <- 1.001; x} )
setMethod("[[<-", c(x = "D", i = "D", j = "missing", value = "NULL"    ), function(x, i,..., value) { x@dval <- 1.002; x} )
test.s4.missing.16 = function() { assertThat( {d[[d,]]<-d; d@dval} , identicalTo( 1.001 )) }
test.s4.missing.18 = function() { assertThat( {d[[d]]<-d; d@dval} , identicalTo( 1.001 )) }
test.s4.missing.17 = function() { assertThat( {d[[d,]]<-NULL; d@dval} , identicalTo( 1.002 )) }
test.s4.missing.18 = function() { assertThat( {d[[j=d,i=]]<-NULL; d@dval} , identicalTo( 1.002 )) }
test.s4.missing.20 = function() { assertThat( {d[[d]]<-NULL; d@dval} , identicalTo( 1.002 )) }

setClass("G", representation(gval = "numeric"))
g <- new("G", gval = 10)
setMethod("[[<-", c(x = "G", i = "numeric", j = "missing", value = "character"    ), function(x, i,..., value) { x@gval <- 1.001; x} )
setMethod("[[<-", c(x = "G", i = "numeric", j = "missing", value = "NULL"    ), function(x, i,..., value) { x@gval <- 1.002; x} )
test.s4.missing.21 = function() { assertThat( {g[[1,]]<-"test"; g@gval} , identicalTo( 1.001 )) }
test.s4.missing.22 = function() { assertThat( {g[[1,]]<-NULL; g@gval} , identicalTo( 1.002 )) }
test.s4.missing.23 = function() { assertThat( {g[[1]]<-"test"; g@gval} , identicalTo( 1.001 )) }
test.s4.missing.24 = function() { assertThat( {g[[1]]<-NULL; g@gval} , identicalTo( 1.002 )) }
test.s4.missing.25 = function() { assertThat( {g[[1,]]<-NULL; g@gval} , identicalTo( 1.002 )) }
test.s4.missing.26 = function() { assertThat( {g[[j=1,i=]]<-NULL; g@gval} , identicalTo( 1.002 )) }

setClass("H", representation(hval = "numeric"))
h <- new("H", hval = 10)
setMethod("[", c(x = "H", i = "numeric", j = "missing", drop = "character"    ), function(x, i,..., drop) 1.001 )
setMethod("[", c(x = "H", i = "numeric", j = "missing", drop = "NULL"    ), function(x, i,..., drop) 1.002 )
test.s4.missing.27 = function() { assertThat( h[1,,NULL] , identicalTo( 1.002 )) }
test.s4.missing.28 = function() { assertThat( h[1,,NULL] , identicalTo( 1.002 )) }
test.s4.missing.29 = function() { assertThat( h[j=1,,NULL] , identicalTo( 1.002 )) }
test.s4.missing.30 = function() { assertThat( h[j=1,i=,NULL] , identicalTo( 1.002 )) }
test.s4.missing.31 = function() { assertThat( h[1,,drop="test"] , identicalTo( 1.001 ) ) }
test.s4.missing.32 = function() { assertThat( h[1,,drop="test"] , identicalTo( 1.001 ) ) }
