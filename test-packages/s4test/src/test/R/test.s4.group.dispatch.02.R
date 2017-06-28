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

setClass("A",representation(a="numeric"))
setClass("B",contains="A")
setClass("C",contains="B")
setMethod("+",signature(e1="A",e2="A"),function(e1,e2) 101)
setMethod("Arith",signature(e1="B",e2="B"), function(e1,e2) 102)

c=new("C",a=10)
a=new("A",a=12)

test.group.dispatch2.1 = function() { assertThat(c+c, identicalTo(102) ) }
test.group.dispatch2.2 = function() { assertThat(a+a, identicalTo(101) ) }


setClass("AA",representation(a="numeric"))
setClass("BB",contains="AA")
setMethod("+",signature(e1="AA",e2="AA"),function(e1,e2) 101)
setMethod("Arith",signature(e1="AA",e2="AA"), function(e1,e2) 102)
aa=new("AA",a=12)
test.group.dispatch2.3 = function() { assertThat(aa+aa, identicalTo(101) ) }