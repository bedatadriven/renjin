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

library(methods)

setClass("Country", representation(name="character",temp="character"))

setClass( 'City', contains = 'environment')

setMethod('[[', signature(x="City", i="missing", j="missing"), function(x,i,j,...,drop) 300)
setMethod('Arith', c("City", "ANY"), function(e1, e2) 350)
setMethod('/', c("City", "character"), function(e1, e2) 351)

setMethod('[', signature(x="City", i="numeric", j="missing"), function(x,i,j,...,drop) 400)

setReplaceMethod('[[', c(x="City", i="ANY", j="missing", value="ANY"),
  function(x,i,j,value) {
    assign( i, value, x@.xData )
    return( x )
  }
)

setGeneric(name = "setCountryTemp",
           def = function(object,value){
             standardGeneric("setCountryTemp")
           }
)

setMethod(f = "setCountryTemp",
          definition = function(object,value){
            object@temp <- value
            return(object)
          }
)

setGeneric(name = "setCountryName",
           def = function(object,value){
             standardGeneric("setCountryName")
           }
)

setMethod(f = "setCountryName", signature(object="Country", value="character"),
          definition = function(object,value){
            object@name <- value
            return(object)
          }
)

