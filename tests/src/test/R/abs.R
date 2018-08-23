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

test.abs <- function() {

    assertThat( abs(-1L), identicalTo(1L) )
    assertThat( abs(1L), identicalTo(1L) )
    assertThat( abs(-1), identicalTo(1) )
    assertThat( abs(TRUE), identicalTo(1L) )
    assertThat( abs(FALSE), identicalTo(0L) )
    assertThat( abs(c(1, -0.5)), identicalTo(c(1, 0.5)) )
    assertThat( abs(list(-1)), throwsError() )
    assertThat( abs("1"), throwsError() )
    assertThat( abs(NA), identicalTo(NA_integer_) )
    assertThat( abs(c(NA, -1)), identicalTo(c(NA, 1)) )
    assertThat( abs(NA + 1L), identicalTo(NA_integer_) )

    # copied from micro-tests:
    assertThat( abs(0/0), identicalTo(NaN) )
    assertThat( abs(NA + 1), identicalTo(NA_real_) )
    assertThat( abs(c(0/0,1i)), identicalTo(c(NaN, 1)) )
    assertThat( abs((0 + 0i)/0), identicalTo(NaN) )

}