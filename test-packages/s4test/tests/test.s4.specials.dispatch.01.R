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


fg <- setRefClass("foo", c("bar", "flag"))
f1 <- fg(flag = "testing")

test.dispatch.specials.01 = function() {
    # special '$'
    assertThat(f1$flag, identicalTo("testing"))
}

test.dispatch.specials.02 = function() {
    # special '$<-' and '$'
    f1$bar <- 3
    assertThat(f1$bar, identicalTo(3))
}