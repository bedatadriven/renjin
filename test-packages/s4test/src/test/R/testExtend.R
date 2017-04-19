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
library("org.renjin.test:s4test")


# NSBS is exported by s4test,
# but extends a class NativeNSBS that is NOT exported
setClass("RleNSBS", contains="NSBS", representation(subscript="Rle"))

test.extend.primitive.0 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature="Gene", function(x, i, j, ...) x@quality[i])
    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[5:6], identicalTo( c(20,3.2) ))
    assertThat(a[2:3], identicalTo( c(89.5, 67)) )
}

test.extend.primitive.1 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature="Gene", function(x, i, j, ...) 50.5)
    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[5:6], identicalTo( c(50.5) ))
    assertThat(a[2:3], identicalTo( c(50.5)) )
}

test.extend.primitive.2 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature="Gene", function(x, i, j, ...) 87.4)
    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[[1]], identicalTo( c(87.4) ))
}

test.extend.primitive.3 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature="Promoter", function(x, i, j, ...) 65.78)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[[1]], identicalTo( c(65.78) ))
}

test.extend.primitive.4 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature="Promoter", function(x, i, j, ...) 65.78)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[[stop()]], identicalTo( c(65.78) ))
}

test.extend.primitive.5 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature=c("Promoter", "character"), function(x, i, j, ...) 99.99)
    setMethod("[[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 65.78)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[["hello"]], identicalTo( c(99.99) ))
    assertThat(a[[1]], identicalTo( c(65.78) ))

}

test.extend.primitive.6 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature=c("Promoter", "character"), function(x, i, j, ...) 299.99)
    setMethod("[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 165.78)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a["hello"], identicalTo( c(299.99) ))
    assertThat(a[1], identicalTo( c(165.78) ))

}

test.extend.primitive.7 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature=c("Promoter", "double"), function(x, i, j, ...) 36.18)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[1.0], identicalTo( c(36.18) ))

}

test.extend.primitive.8 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature=c("Promoter", "integer"), function(x, i, j, ...) 83.18)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[1L], identicalTo( c(83.18) ))

}

test.extend.primitive.9 = function() {

    setClass("Promoter2", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature=c("Promoter2", "numeric"), function(x, i, j, ...) 57.98)
    b <- new("Promoter2", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(b[1L], identicalTo( c(57.98) ))

}

test.extend.primitive.10 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature=c("Promoter", "numeric"), function(x, i, j, ...) 69.98)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )

    assertThat(a[1.0], identicalTo( c(69.98) ))

}

test.extend.primitive.11 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setClass("KOZAK", representation("character"))
    setMethod("[", signature=c("Promoter", "KOZAK"), function(x, i, j, ...) 70.71)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )
    b <- new("KOZAK", "ACCATG")

    assertThat(a[b], identicalTo( c(70.71) ))

}

test.extend.primitive.12 = function() {

    setClass("Promoter", representation(name="character", sequence="character", quality="numeric"))
    setClass("KOZAK", representation("character"))
    setMethod("[", signature=c("Promoter", "KOZAK", "Promoter"), function(x, i, j, ...) 56.56)
    a <- new("Promoter", name="IRES", sequence="ATGAAAC", quality=c(80,79.5,100,100,98,0.8,10) )
    b <- new("KOZAK", "ACCATG")

    assertThat(a[b,a], identicalTo( c(56.56) ))

}
