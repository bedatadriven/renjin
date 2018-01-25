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


setClass("Gene", representation(name="character", size="numeric"))
setMethod("Arith", c("Gene","Gene"), function(e1, e2) callGeneric(e1@size, e2@size))
setMethod("^", c("Gene","Gene"), function(e1, e2) 3.5)
a <- new("Gene", name="Gene1", size = 5)
b <- new("Gene", name="Gene2", size = 6)

test.dispatch.group.00 = function() {
    assertThat(a+b, identicalTo(11))
    assertThat(a-b, identicalTo(-1))
    assertThat(a*b, identicalTo(30))
}

test.dispatch.group.01 = function(){
    assertThat(a^b, identicalTo(3.5))
}

test.dispatch.group.02 = function() {
    setClass("A012346789012346789012346789012346789012346789012346789012346789012346789012346789012346789", representation(name="character", size="numeric"))
    setMethod("Arith", c("A012346789012346789012346789012346789012346789012346789012346789012346789012346789012346789", "A012346789012346789012346789012346789012346789012346789012346789012346789012346789012346789"), function(e1, e2) 35.5)
    w = new("A012346789012346789012346789012346789012346789012346789012346789012346789012346789012346789")

    assertThat(w - w, identicalTo(35.5))
    assertThat(w + w, identicalTo(35.5))
}

setClass("AA", representation(name="character"))
setMethod("Arith", c("AA","AA"), function(e1, e2) 4.01)
setMethod("Compare", c("AA","AA"), function(e1, e2) 4.02)
setMethod("Logic", c("AA","AA"), function(e1, e2) 4.03)
setMethod("Ops", c("AA","AA"), function(e1, e2) 4.04)
setMethod("+", c("AA","AA"), function(e1, e2) 4.05)
setMethod("|", c("AA","AA"), function(e1, e2) 4.06)
setMethod("==", c("AA","AA"), function(e1, e2) 4.07)
m <- new("AA")

test.dispatch.group.04 = function() {
    assertThat( m - m , identicalTo( 4.01 ))
    assertThat( m + m , identicalTo( 4.05 ))
    assertThat( m != m , identicalTo( 4.02 ))
    assertThat( m == m , identicalTo( 4.07 ))
    assertThat( m & m  , identicalTo( 4.03 ))
    assertThat( m | m  , identicalTo( 4.06 ))
}