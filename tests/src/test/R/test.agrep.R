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

test.agrep <- function() {
	assertThat( agrep("lasy", "1 lazy 2"), equalTo(1) )
	#assertThat( agrep("lasy", c(" 1 lazy 2", "1 lasy 2"), max = list(sub = 0)), equalTo(2) )
	assertThat( agrep("laysy", c("1 lazy", "1", "1 LAZY"), max = 2, value = TRUE), equalTo("1 lazy") )
	assertThat( agrep("laysy", c("1 lazy", "1", "1 LAZY"), max = 2, ignore.case = TRUE), equalTo(c(1,3)) )
}