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


setClass("AA", representation(a="numeric"))
setClass("BB", representation(b="character"))

setMethod("[", signature("AA","ANY"), function(x,i,j,...) environment())
setMethod("[", signature("AA","AA"), function(x,i,j,...) environment())
setMethod("[", signature("BB","ANY"), function(x,i,j,...) environment())
setMethod("[", signature("BB","BB"), function(x,i,j,...) environment())

a <- new("AA")
b <- new("BB")

test.s4.dispatch.metadata.04a = function() {
    v <- a[b]
    assertThat( as.character(v$.target) , identicalTo( c("AA", "BB") ))
    assertThat( as.character(v$.defined) , identicalTo( c("AA" , "ANY") ))
    assertThat( attr(v$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(v$.defined, "package"), identicalTo( c(".GlobalEnv", "methods") ))
}

test.s4.dispatch.metadata.04b = function() {
    w <- a[1]
    assertThat( attr(w$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(w$.defined, "package"), identicalTo( c(".GlobalEnv", "methods") ))
}

test.s4.dispatch.metadata.04c = function() {
    x <- a["X"]
    assertThat( attr(x$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(x$.defined, "package") , identicalTo( c(".GlobalEnv", "methods") ))
}

test.s4.dispatch.metadata.04d = function() {
    f <- function() TRUE
    y <- b[f()]
    assertThat( attr(y$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(y$.defined, "package") , identicalTo( c(".GlobalEnv", "methods") ))
}

test.s4.dispatch.metadata.04e = function() {
    z <- b[getClass("MethodDefinition")]
    assertThat( attr(z$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(z$.defined, "package") , identicalTo( c(".GlobalEnv", "methods") ))
}

test.s4.dispatch.metadata.04f = function() {
    z2 <- b[factor()]
    assertThat( attr(z2$.target, "package") , identicalTo( c("methods", "methods") ))
    assertThat( attr(z2$.defined, "package") , identicalTo( c(".GlobalEnv", "methods") ))
}

setMethod("[[", signature("AA","ANY","AA"), function(x,i,j,...) environment())
setMethod("[[", signature("ANY","BB","ANY"), function(x,i,j,...) environment())

test.s4.dispatch.metadata.04g = function() {
    g <- a[[b,b]]
    assertThat( attr(g$.target, "package") , identicalTo( c("methods", "methods", "methods") ))
    assertThat( attr(g$.defined, "package") , identicalTo( c("methods", ".GlobalEnv", "methods") ))
}