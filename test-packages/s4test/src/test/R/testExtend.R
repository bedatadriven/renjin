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

test.extend.primitive = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature="Gene", function(x, i, j, ...) x@quality[i])

    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[5:6], identicalTo( c(20,3.2) ))
    assertThat(a[2:3], identicalTo( c(89.5, 67)) )
}

test.extend.primitive2 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[", signature="Gene", function(x, i, j, ...) 50.5)

    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[5:6], identicalTo( c(50.5) ))
    assertThat(a[2:3], identicalTo( c(50.5)) )
}

test.extend.primitive3 = function() {

    setClass("Gene", representation(name="character", sequence="character", quality="numeric"))
    setMethod("[[", signature="Gene", function(x, i, j, ...) 87.4)

    a <- new("Gene", name="GeneA", sequence="ATGAAA", quality=c(99,89.5,67,86.5,20,3.2) )

    assertThat(a[[1]], identicalTo( c(87.4) ))
}