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

setClass("A1", representation(x="numeric", y="character"))
setClass("A2", contains = "A1")
setClass("A3", contains = "A2")
setClass("A4", contains = "A3")

setClass("B1", representation(x="numeric", y="character"))
setClass("B2", contains = "B1")
setClass("B3", contains = "B2")
setClass("B4", contains = "B3")

setGroupGeneric("AX", function(e1, e2) NULL)
setMethod("AX", signature(e1 = "A1", e2 = "A1"), function(e1, e2) callGeneric(e1@x, e2@x))

setGroupGeneric("BY", function(e1, e2) NULL)
setMethod("BY", signature(e1 = "B1", e2 = "B1"), function(e1, e2) callGeneric(e1@y, e2@y))
setMethod("BY", signature(e1 = "B1", e2 = "B2"), function(e1, e2) callGeneric(e1@y, paste(e2@y, "*", sep = "")) )
setMethod("BY", signature(e1 = "B2", e2 = "B1"), function(e1, e2) callGeneric(paste(e1@y, "*", sep = ""), e2@y) )

setGeneric("add", group = c("AX", "BY"), function(e1, e2) standardGeneric("add"))
setMethod("add", signature(e1 = "numeric", e2 = "numeric"), function(e1, e2) e1 + e2)
setMethod("add", signature(e1 = "character", e2 = "character"), function(e1, e2) paste(e1, e2, sep = ":"))

a1 <- new("A1", x=11, y = "<A1>")
a4 <- new("A4", x=44, y = "<A4>")
b1 <- new("B1", x=22, y = "<B1>")
b4 <- new("B4", x=33, y = "<B4>")

test.group.01 = function() { assertThat(add(a1,a1) , identicalTo(22L)) }
test.group.02 = function() { assertThat(add(a1,a4) , identicalTo(55L)) }
test.group.03 = function() { assertThat(add(b1,b1) , identicalTo("<B1>:<B1>")) }
test.group.04 = function() { assertThat(add(b1,b4) , identicalTo("<B1>:<B4>*")) }
test.group.05 = function() { assertThat(add(b4,b4) , identicalTo("<B4>*:<B4>")) } # and warning that: Note: method with signature ‘B2#B1’ chosen for function ‘add’,
                                                                                  #                    target signature ‘B3#B3’.
                                                                                  #                    "B1#B2" would also be valid


# test valueClass option
setGroupGeneric("AXlimited", function(e1, e2) NULL)
setMethod("AXlimited", signature(e1 = "A1", e2 = "A1"), function(e1, e2) callGeneric(e1@x, e2@x))
setGeneric("devide", valueClass = "character", group = "AXlimited", function(e1, e2) standardGeneric("devide") )
setMethod("devide", signature(e1 = "numeric", e2 = "numeric"), function(e1, e2) if((x <- e1 / e2) > 1) "text" else x )

test.group.06 = function() { assertThat(devide(a1, a4), throwsError()) }
test.group.07 = function() { assertThat(devide(a4, a1), identical("text")) }





