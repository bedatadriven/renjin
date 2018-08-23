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
library("org.renjin.test:s4test")

setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )
p <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

test.extend.primitive.0 = function() {

    setMethod("[", signature="Gene", function(x, i, j, ...) x@quality[i])

    assertThat(a[5:6], identicalTo( c(20,3.2) ))
    assertThat(a[2:3], identicalTo( c(89.5, 67)) )

}

test.extend.primitive.1 = function() {

    setMethod("[", signature="Gene", function(x, i, j, ...) 50.5)

    assertThat(a[5:6], identicalTo( c(50.5) ))
    assertThat(a[2:3], identicalTo( c(50.5)) )

}


test.extend.primitive.2 = function() {

    setMethod("[", signature=c("Gene", "numeric"), function(x, i, j, ...) 36.18)

    assertThat(a[1.0], identicalTo( c(36.18) ))

}

test.extend.primitive.3 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature="Gene", function(x, i, j, ...) 87.4)

    assertThat(a[[1]], identicalTo( c(87.4) ))

}

test.extend.primitive.4 = function() {

    setMethod("[[", signature="Promoter", function(x, i, j, ...) 65.78)

    assertThat(p[[1]], identicalTo( c(65.78) ))

}

test.extend.primitive.5 = function() {

    setMethod("[[", signature="Promoter", function(x, i, j, ...) 65.78)

    assertThat(p[[p,p,stop()]], identicalTo( c(65.78) ))
    assertThat(p[[p,stop()]], throwsError() )
    assertThat(p[[stop()]], throwsError() )

}

test.extend.primitive.6 = function() {

    setMethod("[[", signature=c("Promoter", "character"), function(x, i, j, ...) 99.99)
    setMethod("[[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 65.78)

    assertThat(p[["hello"]], identicalTo( c(99.99) ))
    assertThat(p[[1]], identicalTo( c(65.78) ))

}

