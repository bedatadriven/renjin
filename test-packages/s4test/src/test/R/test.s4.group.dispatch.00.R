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


setClass("Gene", representation(name="character", size="numeric"))
setMethod("Arith", c("Gene","Gene"), function(e1, e2) callGeneric(e1@size, e2@size))
setMethod("^", c("Gene","Gene"), function(e1, e2) 3.5)
a <- new("Gene", name="Gene1", size = 5)
b <- new("Gene", name="Gene2", size = 6)

ignore.test.dispatch.group.00 = function() {
    assertThat(a+b, identicalTo(11))
    assertThat(a-b, identicalTo(-1))
    assertThat(a*b, identicalTo(30))
}

ignore.test.dispatch.group.01 = function(){
    assertThat(a^b, identicalTo(3.5))
}
