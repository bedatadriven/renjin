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

setGeneric("stay", function(who, what, where) standardGeneric("stay"))
setMethod("stay", signature("character", "missing", "missing"), function(who, what=NULL, where=NULL) paste(who, "stay", "here"))
setMethod("stay", signature("character", "ANY", "ANY"), function(who, what=NULL, where=NULL) paste(who, "ANY", "ANY"))
setMethod("stay", signature("character", "missing", "character"), function(who, what=NULL, where=NULL) paste(who, "stay", where))
setMethod("stay", signature("character", "ANY", "character"), function(who, what=NULL, where=NULL) paste(who, "ANY", where))
setMethod("stay", signature("character", "character", "missing"), function(who, what=NULL, where=NULL) paste(who, what, "here"))
setMethod("stay", signature("character", "character", "ANY"), function(who, what=NULL, where=NULL) paste(who, what, "ANY"))
setMethod("stay", signature("character", "character", "character"), function(who, what=NULL, where=NULL) paste(who, what, where))

test.missing.01 = function() { assertThat(  stay("I") , identicalTo(  "I stay here" )) }
test.missing.02 = function() { assertThat(  stay("I","go","home") , identicalTo(  "I go home" )) }
test.missing.03 = function() { assertThat(  stay("I","home") , identicalTo(  "I home here" )) }
test.missing.04 = function() { assertThat(  stay("I",where="home") , identicalTo(  "I stay home" )) }
test.missing.05 = function() { assertThat(  stay("You",what="come") , identicalTo(  "You come here" )) }
test.missing.06 = function() { assertThat(  stay("You","come") , identicalTo( "You come here"   )) }
test.missing.07 = function() { assertThat(  stay("She","left") , identicalTo( "She left here" )) }

