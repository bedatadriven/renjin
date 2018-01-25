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

setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
setClass("KOZAK", representation(sequence="character"))
a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )
p <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )
k <- new("KOZAK", sequence="ACCATG")

test.extend.primitive.0 = function() {

    setMethod("[", signature=c("Promoter", "character"), function(x, i, j, ...) 299.99)
    setMethod("[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 165.78)

    assertThat(p["hello"], identicalTo( c(299.99) ))
    assertThat(p[1], identicalTo( c(165.78) ))

}

test.extend.primitive.1 = function() {

    setMethod("[", signature=c("Promoter", "integer"), function(x, i, j, ...) 83.18)

    assertThat(p[1L], identicalTo( c(83.18) ))

}

test.extend.primitive.2 = function() {

    setMethod("[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 57.98)

    assertThat(p[1.0], identicalTo( c(57.98) ))

}

test.extend.primitive.3 = function() {

    setMethod("[", signature=c("Promoter", "KOZAK"), function(x, i, j, ...) 70.71)

    assertThat(p[k], identicalTo( c(70.71) ))

}

test.extend.primitive.4 = function() {

    setMethod("[", signature=c("Promoter", "KOZAK", "Promoter"), function(x, i, j, ...) 56.56)

    assertThat(p[k,p], identicalTo( c(56.56) ))

}