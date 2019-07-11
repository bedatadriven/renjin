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


setClass("AA", representation(a="numeric"))
setClass("BB", contains="AA")

setMethod("Arith", signature("AA","AA"), function(e1, e2) environment())
setMethod("^", signature("AA","AA"), function(e1, e2) environment())
setMethod("*", signature("BB","BB"), function(e1, e2) environment())
a <- new("AA")
b <- new("BB")

m = a + a
n = a ^ a
o = b + b
p = b ^ b
q = b * b


assertThat(
    ls(all.names=TRUE) %in% c(".Random.seed", "a", "b", ".__C__AA", ".__C__BB", "m", "n", "o",
    "p", "q", ".__T__Arith:base", ".__T__^:base", ".__T__*:base"),
    identicalTo( c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE)  )
    )

test.s4.dispatch.metadata.01b = function() {
    assertThat( ls(m, all.names=TRUE) %in% c(".defined", "e1", "e2", ".Generic", ".Method", ".Methods", ".target") , identicalTo( c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE) ))
}

# Failing in GNU R
#
#test.s4.dispatch.metadata.01c = function() {
#    assertThat( ls(n, all.names=TRUE) , identicalTo( c("e1", "e2") ))
#}

test.s4.dispatch.metadata.01d = function() {
    assertThat( ls(o, all.names=TRUE) %in% c(".defined", "e1", "e2", ".Generic", ".Method", ".Methods", ".target"), identicalTo( c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE) ))
}

test.s4.dispatch.metadata.01e = function() {
    assertThat( ls(p, all.names=TRUE) %in% c(".defined", "e1", "e2", ".Generic", ".Method", ".Methods", ".target") , identicalTo( c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE) ))
}

# Failing in GNU R
#test.s4.dispatch.metadata.01f = function() {
#    assertThat( ls(q, all.names=TRUE) %in% c("e1", "e2") , identicalTo( c(TRUE, TRUE) ))
#}

test.s4.dispatch.metadata.02 = function() {
    # typeof
    assertThat( typeof(m$e1) , identicalTo( "S4"  ))
    assertThat( typeof(m$.Generic) , identicalTo( "character"  ))
    assertThat( typeof(m$.Method) , identicalTo( "closure"  ))
#    assertThat( typeof(m$.Methods) , identicalTo( "builtin"  ))    # Renjin returns "symbol"
    assertThat( typeof(m$.target) , identicalTo( "character"  ))
    assertThat( typeof(a) , identicalTo( "S4"  ))
    assertThat( typeof(`.__C__AA`) , identicalTo( "S4"  ))
    assertThat( typeof(m) , identicalTo( "environment"  ))
    assertThat( typeof(`.__T__Arith:base`) , identicalTo( "environment"  ))
    assertThat( typeof(m$.defined) , identicalTo( "character"  ))
    assertThat( typeof(attr(m$.defined, "class") ) , identicalTo( "character"  ))
    assertThat( typeof(attr(m$.defined, "names") ) , identicalTo( "character"  ))
    assertThat( typeof(attr(m$.defined, "package") ) , identicalTo( "character"  ))
    assertThat( typeof(attr(attr(m$.defined, "class"), "package") ) , identicalTo( "character"  ))
    assertThat( typeof(attr(m$.Method, "defined")) , identicalTo( "character"  ))
    assertThat( typeof(attr(attr(m$.Method, "defined"), "class")), identicalTo( "character"))
    assertThat( typeof(attr(attr(m$.Method, "defined"), "names")) , identicalTo( "character" ))
    assertThat( typeof(attr(attr(m$.Method, "defined"), "class")) , identicalTo( "character"  ))
}

test.s4.dispatch.metadata.03 = function() {
    # attributes
    assertThat( attr(m$.defined, "names") , identicalTo( c("e1", "e2")  ))
    assertThat( attr(m$.defined, "package") , identicalTo( c(".GlobalEnv", ".GlobalEnv")  ))
    assertThat( attr(attr(m$.defined, "class"), "package") , identicalTo( "methods"  ))
    assertThat( attr(m$.defined, "class")[1] , identicalTo( "signature"  ))
    assertThat( attr(attr(m$.Method, "defined"), "names") , identicalTo( c("e1", "e2") ))
    assertThat( attr(attr(m$.Method, "defined"), "package") , identicalTo( c(".GlobalEnv", ".GlobalEnv") ))
    assertThat( attr(attr(m$.Method, "defined"), "class")[1], identicalTo( "signature"))
}


test.extend.primitive.5 = function() {
    setClass("Foo", contains="numeric")
    x <- new("Foo", .Data = 42)
    assertThat(typeof(x), identicalTo("double"))
    assertThat(is.double(x), identicalTo(TRUE))
    assertThat(x[1], identicalTo(42))
}