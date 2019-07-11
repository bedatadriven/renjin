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
setGeneric("foo", function(x, i, j, ..., value) standardGeneric("foo"))
setMethod("foo", signature(x="numeric",i="ANY",j="missing",value="ANY"),   function(x,i,value) {

                                                                             return( value )
                                                                           })
#setMethod("foo", signature(x="numeric",i="ANY",j="missing",value="logical"),   function(x,i,value) {
#
#                                                                             return( value )
#                                                                           })

#function (x, i, j, ..., value)
#{
#    .local <- function (x, i, value)
#    {
#        assign(i, value, x@.xData)
#        return(x)
#    }
#    .local(x, i, ..., value)
#}

assertThat(foo(x=100,i=1,value="1000"), identicalTo("1000"))

#x = function(x) x
#f = function(x) x
#g = f
#formals(g) <- NULL
#assertThat(formals(g), identicalTo(NULL))
#assertThat(formals(f), identicalTo(formals(x)))
#
