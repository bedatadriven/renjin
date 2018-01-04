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


# These test currently fail due to incorrect formals of .local()
# methods package needs to

setClass("city", contains = "environment")
city <- new("city", new.env(hash = TRUE, parent = emptyenv()))
setReplaceMethod("[[", c(x="city", i="character", j="missing", value="ANY"), function(x,i,value) { assign(i, value, x@.xData); x })
setReplaceMethod("[[", c(x="city", i="character", j="missing", value="NULL"), function(x,i,value) { assign(i, value, x@.xData); x })
setReplaceMethod("[[", c(x="city", i="missing", j="character", value="ANY"), function(x,j,value) { assign(j, value, x@.xData); x })
setReplaceMethod("[[", c(x="city", i="missing", j="character", value="NULL"), function(x,j,value) { assign(j, value, x@.xData); x })
ignore.test.s4.local.01 = function() { assertThat( {city[["Name"]]<-"The Hague"; city@.xData$Name} , identicalTo( "The Hague" )) }
ignore.test.s4.local.02 = function() { assertThat( {city[["Name",]]<-NULL; city@.xData$Name} , identicalTo( NULL )) }
ignore.test.s4.local.03 = function() { assertThat( {city[[,"Country"]]<-"The Netherlands"; city@.xData$Country} , identicalTo( "The Netherlands" )) }
ignore.test.s4.local.04 = function() { assertThat( {assign("Country","Germany",city@.xData); city[[,"Country"]]<-NULL; city@.xData$Country} , identicalTo( NULL )) }


setClass( 'City', contains = 'environment')
city2 <- new("City", new.env(hash = TRUE, parent = emptyenv()))
setReplaceMethod("[[", c(x="City", i="ANY", j="missing", value="ANY"), function(x,i,value) { assign(i, value, x@.xData); x })
setReplaceMethod("[[", c(x="City", i="character", j="missing", value="NULL"), function(x,i,value) { assign(i, value, x@.xData); x })
ignore.test.s4.local.05 = function() {
    assertThat( {                                          city2[["Name"]]<-"Amsterdam"; city2@.xData$Name } , identicalTo( "Amsterdam" ))
}
ignore.test.s4.local.06 = function() {
    assertThat( { assign("Name","Munich",city2@.xData);    city2[["Name", ]] <- "Amsterdam"; city2@.xData$Name } , identicalTo( "Amsterdam" ))
}
ignore.test.s4.local.07 = function() {
    assertThat( { assign("Name","Rotterdam",city2@.xData); city2[["Name", ]] <- NULL; city2@.xData$Name } , identicalTo( NULL ))
}
ignore.test.s4.local.08 = function() {
    assertThat( { assign("Name","Rotterdam",city2@.xData); city2[["Name"]] <- NULL; city2@.xData$Name } , identicalTo( NULL ))
}
