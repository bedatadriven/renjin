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

test.sign <- function() {

    assertThat( sign(pi), identicalTo(1) )
    assertThat( sign(0), identicalTo(0) )
    assertThat( sign(-2:3), identicalTo(c(-1,-1,0,1,1,1)) )
    assertThat( sign(1L), identicalTo(1) )
    assertThat( sign(NA), identicalTo(NA_real_) )
    assertThat( sign(1+1i), throwsError() )

}
